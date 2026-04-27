package com.tlcsdm.jvmexplorer.fx.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esotericsoftware.minlog.Log;
import com.tlcsdm.jvmexplorer.JvmExplorer;
import com.tlcsdm.jvmexplorer.agent.AgentException;
import com.tlcsdm.jvmexplorer.agent.AgentPreparer;
import com.tlcsdm.jvmexplorer.agent.RunningJvm;
import com.tlcsdm.jvmexplorer.fx.TreeViewPlaceholderSkin;
import com.tlcsdm.jvmexplorer.helper.AlertHelper;
import com.tlcsdm.jvmexplorer.helper.ClassTreeHelper;
import com.tlcsdm.jvmexplorer.helper.ExportHelper;
import com.tlcsdm.jvmexplorer.helper.FilterHelper;
import com.tlcsdm.jvmexplorer.net.ClientHandler;
import com.tlcsdm.jvmexplorer.protocol.AgentConfiguration;
import com.tlcsdm.jvmexplorer.protocol.ClassContent;
import com.tlcsdm.jvmexplorer.protocol.LoadedClass;
import com.tlcsdm.jvmexplorer.settings.JvmExplorerSettings;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.File;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides the loaded classes controller implementation used by the com.tlcsdm.jvmexplorer.fx.classes package.
 */
public class LoadedClassesController {

	private static final Logger log = LoggerFactory.getLogger(LoadedClassesController.class);


	private static final NumberFormat numberFormat = NumberFormat.getInstance();

	private static final int CLASSES_NOT_LOADING = -1;

	private static final String AGENT_PATH = "agents/agent.jar";
	private static final File AGENT_LOG_FILE = new File(JvmExplorer.APP_DIR, "logs/agent.log");

	static {
		// Try to clean up on initial load. Prevents the file from growing too large.
		AGENT_LOG_FILE.delete();
	}

	private final ClassTreeHelper classTreeHelper = new ClassTreeHelper();
	private final AgentPreparer agentPreparer = new AgentPreparer();
	private final SimpleIntegerProperty loadedClassProgressCount = new SimpleIntegerProperty(CLASSES_NOT_LOADING);
	private final SimpleObjectProperty<ClassContent> currentClass = new SimpleObjectProperty<>();
	private final FilterHelper filterHelper = new FilterHelper();
	private final SimpleBooleanProperty agentLoading = new SimpleBooleanProperty();

	@FXML
	private TreeView<ClassTreeNode> classes;

	@FXML
	private TextField searchClasses;

	@FXML
	private TitledPane classesTitlePane;

	private FilterableTreeItem<ClassTreeNode> classesTreeRoot;
	private ScheduledExecutorService executorService;
	private ClientHandler clientHandler;
	private ExportHelper exportHelper;
	private AlertHelper alertHelper;
	private ObjectProperty<RunningJvm> currentJvm;
	private int serverPort;
	private JvmExplorerSettings settings;

	/**
	 * Returns the current class property.
	 */
	public ObjectProperty<ClassContent> currentClassProperty() {
		return currentClass;
	}

	public void initialize(Stage stage, ScheduledExecutorService executorService, ClientHandler clientHandler,
	                       ObjectProperty<RunningJvm> currentJvm, int serverPort, JvmExplorerSettings settings,
	                       FilterableTreeItem<ClassTreeNode> classesTreeRoot) {
		this.classesTreeRoot = classesTreeRoot;
		this.executorService = executorService;
		this.clientHandler = clientHandler;
		this.exportHelper = new ExportHelper(clientHandler);
		this.alertHelper = new AlertHelper(stage);
		this.currentJvm = currentJvm;
		this.serverPort = serverPort;
		this.settings = settings;
		initialize();
	}

	/**
	 * Initializes the component state and dependencies.
	 */
	private void initialize() {
		setupTreeSearching();
		setupAgentLoader();
		setupClassesCore();
		setupTitlePaneText();
		setupTreePlaceholder();
	}

	/**
	 * Sets up tree search filtering.
	 */
	private void setupTreeSearching() {
		searchClasses.textProperty().addListener((obs, old, newv) -> {
			// TreeView is kinda buggy with selection when filtering and selects a ton of random stuff sometimes
			// Haven't figured out specifically why/what it does, but since we do a lot on selecting a new class
			// (load that class + decompile), it gets kinda laggy. Therefore, we remove the class on text change.
			classes.getSelectionModel().clearSelection();
		});
		classesTreeRoot.predicateProperty().bind(Bindings.createObjectBinding(() -> {
			final String text = searchClasses.getText().trim();
			final Predicate<String> predicate = filterHelper.createStringPredicate(text);
			return t -> {
				// We are only searching classes here. A node will stay visible if any of its children are.
				if (t.getType() == ClassTreeNode.Type.CLASS) {
					return predicate.test(t.getLoadedClass().toString());
				}
				return false;
			};
		}, searchClasses.textProperty()));
		classes.setRoot(classesTreeRoot);

		searchClasses.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				final String className = searchClasses.getText();
				log.debug("Pressed enter, attempting to quick-select {}", className);
				classesTreeRoot.streamSourceItems()
				               .filter(i -> i.getValue() != null)
				               .filter(i -> i.getValue().getType() == ClassTreeNode.Type.CLASS)
				               .filter(i -> i.getValue().getLoadedClass().getSimpleName().equals(className)
				                            || i.getValue().getLoadedClass().getName().equals(className))
				               .findFirst()
				               .ifPresent(selected -> {
					               // reset first, or javafx breaks and selects the wrong thing
					               // note we also reset the selected item on text change, but that's unrelated
					               searchClasses.clear();
					               log.debug("Quick-selected {}", selected);
					               select(selected);
				               });
			}
		});

		final Tooltip tooltip = new Tooltip();
		tooltip.setText(JvmExplorer.getBundle().getString("placeholder.searchTooltip"));
		searchClasses.setTooltip(tooltip);
	}

	/**
	 * Sets up agent loading and attachment.
	 */
	private void setupAgentLoader() {
		currentJvm.addListener((obs, old, newv) -> {
			if (newv == null) {
				loadedClassProgressCount.set(CLASSES_NOT_LOADING);
			}
			classesTreeRoot.getSourceChildren().clear();
			if (newv != null) {
				agentLoading.set(true);
				executorService.submit(() -> {
					final String agentArgs = buildAgentArgs(newv);
					try {
						final String localPath = agentPreparer.loadAgentOnFileSystem(AGENT_PATH);
						newv.loadAgent(localPath, agentArgs);
					}
					catch (AgentException | UncheckedIOException e) {
						onAttachFail(newv, e);
					}
					finally {
						Platform.runLater(() -> agentLoading.set(false));
					}
				});
			}
			if (old != null) {
				executorService.submit(() -> clientHandler.close(old));
			}
		});
	}

	/**
	 * Handles the attach fail event.
	 */
	private void onAttachFail(RunningJvm targetJvm, Exception e) {
		final List<String> propertyList = new ArrayList<>();
		try {
			final Properties properties = targetJvm.getSystemProperties();
			final List<String> propertiesToAdd = properties.entrySet()
			                                               .stream()
			                                               .map(entry -> entry.getKey() + ": " + entry.getValue())
			                                               .collect(Collectors.toList());
			propertyList.addAll(propertiesToAdd);
		}
		catch (Exception e2) {
			log.debug("Failed to get properties for JVM {}", targetJvm, e2);
		}
		Platform.runLater(() -> {
			if (propertyList.isEmpty()) {
				final ResourceBundle bundle = JvmExplorer.getBundle();
				alertHelper.showError(bundle.getString("error.agentError"), bundle.getString("error.failedToLoadAgent"), e);
			}
			else {
				final ResourceBundle bundle = JvmExplorer.getBundle();
				alertHelper.showError(bundle.getString("error.agentError"), bundle.getString("error.failedToLoadAgent"), e, propertyList);
			}
			currentJvm.set(null);
		});
	}

	/**
	 * Sets up core class tree functionality.
	 */
	private void setupClassesCore() {
		classes.getSelectionModel()
		       .selectedItemProperty()
		       .addListener((obs, old, newv) -> onSelectedClassChange(old, newv));
		classes.setShowRoot(false);

		classes.setCellFactory(new ClassCellFactory(executorService,
		                                            alertHelper,
		                                            currentJvm,
		                                            clientHandler,
		                                            classesTreeRoot,
		                                            exportHelper,
		                                            this::loadClasses,
		                                            settings));
	}

	/**
	 * Sets up title pane text binding.
	 */
	private void setupTitlePaneText() {
		classesTitlePane.textProperty()
		                .bind(Bindings.createStringBinding(this::getTitlePaneText,
		                                                   currentJvm,
		                                                   classesTreeRoot.getChildren(),
		                                                   classesTreeRoot.getSourceChildren(),
		                                                   searchClasses.textProperty()));
	}

	/**
	 * Sets up the tree placeholder.
	 */
	private void setupTreePlaceholder() {
		final TreeViewPlaceholderSkin<?> treeViewPlaceholderSkin = new TreeViewPlaceholderSkin<>(classes);
		final Label placeholderLabel = new Label();
		placeholderLabel.textProperty()
		                .bind(Bindings.createStringBinding(this::getPlaceholderText,
		                                                   currentJvm,
		                                                   loadedClassProgressCount,
		                                                   classesTreeRoot.getChildren(),
		                                                   classesTreeRoot.getSourceChildren(),
		                                                   searchClasses.textProperty(),
		                                                   agentLoading));
		treeViewPlaceholderSkin.placeholderProperty().setValue(placeholderLabel);
		classes.setSkin(treeViewPlaceholderSkin);
	}

	/**
	 * Handles the select workflow.
	 */
	public void select(TreeItem<ClassTreeNode> itemToSelect) {
		classes.getSelectionModel().select(itemToSelect);
	}

	/**
	 * Builds the configured result object.
	 */
	private String buildAgentArgs(RunningJvm runningJvm) {
		return AgentConfiguration.builder()
		                         .hostName("localhost")
		                         .port(serverPort)
		                         .identifier(runningJvm.toIdentifier())
		                         .logLevel(Log.LEVEL_DEBUG)
		                         .logFilePath(AGENT_LOG_FILE.getAbsolutePath())
		                         .build()
		                         .toAgentArgs();
	}

	/**
	 * Handles the selected class change event.
	 */
	private void onSelectedClassChange(TreeItem<ClassTreeNode> old, TreeItem<ClassTreeNode> newv) {
		if (newv == null) {
			currentClass.setValue(null);
			return;
		}
		if (newv.getValue().getType() != ClassTreeNode.Type.CLASS) {
			return;
		}
		final LoadedClass loadedClass = newv.getValue().getLoadedClass();
		final RunningJvm selectedJvm = currentJvm.get();
		if (selectedJvm == null) {
			return;
		}
		log.debug("Selected class: {}", loadedClass);
		executorService.submit(() -> {
			final ClassContent classContent = clientHandler.getClassContent(selectedJvm, loadedClass);
			log.debug("Received class content for {}", loadedClass);
			if (classContent != null) {
				Platform.runLater(() -> {
					final TreeItem<ClassTreeNode> selected = classes.getSelectionModel().getSelectedItem();
					if (selected != newv) {
						log.debug("Class changed from {}, ignoring load", loadedClass);
						// Class changed already
						return;
					}
					currentClass.set(classContent);
				});
			}
		});
	}

	/**
	 * Loads classes.
	 */
	public void loadClasses(RunningJvm runningJvm) {
		executorService.submit(() -> doLoadClasses(runningJvm));
	}

	/**
	 * Returns the title pane text value.
	 */
	private String getTitlePaneText() {
		final long visibleItems = classesTreeRoot.streamVisible().filter(p -> p.getLoadedClass() != null).count();
		final long sourceItems = classesTreeRoot.streamSource().filter(p -> p.getLoadedClass() != null).count();
		return JvmExplorer.getBundle().getString("ui.loadedClasses") + " (" + getLoadedClassDisplay(visibleItems, sourceItems) + ")";
	}

	/**
	 * Returns the placeholder text value.
	 */
	private String getPlaceholderText() {
		final ResourceBundle bundle = JvmExplorer.getBundle();
		if (currentJvm.get() == null) {
			return bundle.getString("placeholder.noJvmSelected");
		}
		else if (loadedClassProgressCount.get() != CLASSES_NOT_LOADING) {
			if (loadedClassProgressCount.get() == 0) {
				return bundle.getString("placeholder.loadingInitializing");
			}
			return MessageFormat.format(bundle.getString("placeholder.loadingClasses"), loadedClassProgressCount.get());
		}
		else if (agentLoading.get()) {
			return bundle.getString("placeholder.agentAttaching");
		}
		else if (!searchClasses.getText().trim().isEmpty()) {
			return bundle.getString("placeholder.noClassesFound");
		}
		else {
			return bundle.getString("placeholder.noClassesAgentError");
		}
	}

	/**
	 * Handles the do load classes workflow.
	 */
	private void doLoadClasses(RunningJvm runningJvm) {
		Platform.runLater(() -> loadedClassProgressCount.set(0));
		final List<LoadedClass> loadedClasses = clientHandler.getLoadedClasses(runningJvm, loadedClassPercent -> {
			Platform.runLater(() -> this.loadedClassProgressCount.set(loadedClassPercent));
		});
		if (loadedClasses == null) {
			log.warn("Failed to load active classes: {}", runningJvm);
			return;
		}
		log.debug("Received loaded classes for {}", runningJvm);
		final ClassTreeNode classTreeRoot = buildClassTree(loadedClasses);
		Platform.runLater(() -> {
			if (!runningJvm.equals(currentJvm.get())) {
				log.debug("JVM changed from {}, ignoring loaded classes", runningJvm);
				return;
			}
			final FilterableTreeItem<ClassTreeNode> root = classTreeRoot.toTreeItem();
			classesTreeRoot.getSourceChildren().setAll(root.getSourceChildren());
			loadedClassProgressCount.set(CLASSES_NOT_LOADING);
		});
	}

	/**
	 * Returns the loaded class display value.
	 */
	private String getLoadedClassDisplay(long visibleItems, long sourceItems) {
		if (visibleItems == sourceItems) {
			return numberFormat.format(visibleItems);
		}
		return numberFormat.format(visibleItems) + "/" + numberFormat.format(sourceItems);
	}

	/**
	 * Builds the configured result object.
	 */
	private ClassTreeNode buildClassTree(List<LoadedClass> loadedClasses) {
		if (this.settings.getShowClassLoader().get()) {
			return classTreeHelper.buildClassLoaderTree(loadedClasses);
		}
		else {
			return classTreeHelper.buildClassTree(loadedClasses);
		}
	}

}
