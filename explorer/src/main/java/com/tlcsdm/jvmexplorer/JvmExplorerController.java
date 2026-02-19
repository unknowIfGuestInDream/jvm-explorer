package com.tlcsdm.jvmexplorer;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esotericsoftware.kryonet.Connection;
import com.tlcsdm.jvmexplorer.agent.RunningJvm;
import com.tlcsdm.jvmexplorer.fx.classes.ClassTreeNode;
import com.tlcsdm.jvmexplorer.fx.classes.FilterableTreeItem;
import com.tlcsdm.jvmexplorer.fx.classes.LoadedClassesController;
import com.tlcsdm.jvmexplorer.fx.jvms.RunningJvmsController;
import com.tlcsdm.jvmexplorer.fx.openclass.CurrentClassController;
import com.tlcsdm.jvmexplorer.helper.AlertHelper;
import com.tlcsdm.jvmexplorer.helper.ScreenHelper;
import com.tlcsdm.jvmexplorer.net.ClientHandler;
import com.tlcsdm.jvmexplorer.net.JvmExplorerServer;
import com.tlcsdm.jvmexplorer.net.OpenPortProvider;
import com.tlcsdm.jvmexplorer.net.ServerLauncher;
import com.tlcsdm.jvmexplorer.preferences.AppPreferences;
import com.tlcsdm.jvmexplorer.protocol.ClassContent;
import com.tlcsdm.jvmexplorer.protocol.helper.VerboseScheduledExecutorService;
import com.tlcsdm.jvmexplorer.settings.JvmExplorerSettings;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class JvmExplorerController {

	private static final Logger log = LoggerFactory.getLogger(JvmExplorerController.class);

	private static final String LANG_DISPLAY_ENGLISH = "English";
	private static final String LANG_DISPLAY_CHINESE = "\u4E2D\u6587";
	private static final String LANG_DISPLAY_JAPANESE = "\u65E5\u672C\u8A9E";
	private static final String LANG_CODE_ENGLISH = "en";
	private static final String LANG_CODE_CHINESE = "zh";
	private static final String LANG_CODE_JAPANESE = "ja";

	private static final String THEME_DEFAULT = "Default";
	private static final String THEME_PRIMER_LIGHT = "Primer Light";
	private static final String THEME_PRIMER_DARK = "Primer Dark";
	private static final String THEME_NORD_LIGHT = "Nord Light";
	private static final String THEME_NORD_DARK = "Nord Dark";
	private static final String THEME_CUPERTINO_LIGHT = "Cupertino Light";
	private static final String THEME_CUPERTINO_DARK = "Cupertino Dark";
	private static final String THEME_DRACULA = "Dracula";

	private static final long SERVER_SHUTDOWN_TIMEOUT_MS = 5000;

	private final ScheduledExecutorService executorService =
			new VerboseScheduledExecutorService(Executors.newScheduledThreadPool(
			8));
	@FXML
	private RunningJvmsController runningJvmsController;
	@FXML
	private LoadedClassesController loadedClassesController;
	@FXML
	private CurrentClassController currentClassController;
	@FXML
	private SplitPane splitPane;
	@FXML
	private MenuBar menuBar;
	private Stage stage;
	private AlertHelper alertHelper;
	private ResourceBundle bundle;
	private PreferencesFx preferencesFx;
	private boolean isChangingLanguage = false;
	private final ClientHandler clientHandler = ClientHandler.builder()
	                                                         .onConnect(this::onConnect)
	                                                         .onDisconnect(this::onDisconnect)
	                                                         .build();
	private JvmExplorerServer server;

	public void initialize(Stage stage, ResourceBundle bundle) {
		this.stage = stage;
		this.bundle = bundle;
		this.alertHelper = new AlertHelper(stage);
		final OpenPortProvider openPortProvider = new OpenPortProvider();
		final ServerLauncher serverLauncher = new ServerLauncher(openPortProvider);
		server = serverLauncher.launch(executorService, clientHandler);
		stage.setOnHidden(e -> {
			log.debug("Stage hidden, cleaning up resources");
			executorService.shutdown();
			server.stop();
			final Thread updateThread = server.getUpdateThread();
			if (updateThread != null) {
				try {
					updateThread.join(SERVER_SHUTDOWN_TIMEOUT_MS);
				}
				catch (InterruptedException ex) {
					log.warn("Interrupted while waiting for server thread to stop", ex);
					Thread.currentThread().interrupt();
				}
				if (updateThread.isAlive()) {
					log.warn("Server thread did not stop within timeout");
				}
			}
			try {
				server.dispose();
			}
			catch (IOException ex) {
				log.warn("Failed to close server", ex);
			}
		});

		initializeMenuBar();
		initializePreferences();
		setupTitlePaneText();
		wireChildControllers();
	}

	private void initializeMenuBar() {
		Menu fileMenu = new Menu(bundle.getString("menu.file"));

		MenuItem restart = new MenuItem(bundle.getString("menu.file.restart"));
		restart.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		restart.setOnAction(e -> restartApplication());

		MenuItem exit = new MenuItem(bundle.getString("menu.file.exit"));
		exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
		exit.setOnAction(e -> stage.close());

		fileMenu.getItems().addAll(restart, new SeparatorMenuItem(), exit);

		Menu preferencesMenu = new Menu(bundle.getString("menu.preferences"));
		MenuItem preferencesItem = new MenuItem(bundle.getString("menu.preferences"));
		preferencesItem.setOnAction(e -> showPreferences());
		preferencesMenu.getItems().add(preferencesItem);

		Menu helpMenu = new Menu(bundle.getString("menu.help"));
		MenuItem about = new MenuItem(bundle.getString("menu.help.about"));
		about.setOnAction(e -> showAboutDialog());
		helpMenu.getItems().add(about);

		menuBar.getMenus().addAll(fileMenu, preferencesMenu, helpMenu);
	}

	private void initializePreferences() {
		final AppPreferences preferences = AppPreferences.getInstance();
		preferencesFx = createPreferencesFx(preferences);

		preferences.languageProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null && !newVal.equals(oldVal) && !isChangingLanguage) {
				restartApplication();
			}
		});
	}

	private PreferencesFx createPreferencesFx(AppPreferences preferences) {
		ObservableList<String> languageOptions =
				FXCollections.observableArrayList(LANG_DISPLAY_ENGLISH, LANG_DISPLAY_CHINESE, LANG_DISPLAY_JAPANESE);

		ObjectProperty<String> languageSelection = new SimpleObjectProperty<>();
		languageSelection.set(langCodeToDisplayName(preferences.getLanguage()));

		languageSelection.addListener((obs, oldVal, newVal) -> {
			if (newVal != null && !newVal.equals(oldVal) && !isChangingLanguage) {
				String newLangCode = displayNameToLangCode(newVal);
				String currentLangCode = preferences.getLanguage();
				if (!newLangCode.equals(currentLangCode)) {
					isChangingLanguage = true;
					try {
						preferences.setLanguage(newLangCode);
					}
					finally {
						isChangingLanguage = false;
					}
				}
			}
		});

		ObservableList<String> themeOptions = FXCollections.observableArrayList(
				THEME_DEFAULT,
				THEME_PRIMER_LIGHT, THEME_PRIMER_DARK,
				THEME_NORD_LIGHT, THEME_NORD_DARK,
				THEME_CUPERTINO_LIGHT, THEME_CUPERTINO_DARK,
				THEME_DRACULA
		);

		ObjectProperty<String> themeSelection = new SimpleObjectProperty<>();
		themeSelection.set(preferences.getTheme());

		themeSelection.addListener((obs, oldVal, newVal) -> {
			if (newVal != null && !newVal.equals(oldVal)) {
				preferences.setTheme(newVal);
				JvmExplorer.applyTheme(newVal);
			}
		});

		return PreferencesFx.of(AppPreferences.class,
				Category.of(bundle.getString("preferences.category.general"),
						Group.of(
								Setting.of(bundle.getString("preferences.language"),
										languageOptions,
										languageSelection),
								Setting.of(bundle.getString("preferences.theme"),
										themeOptions,
										themeSelection)
						)
				)
		).instantPersistent(false).saveSettings(true).buttonsVisibility(true);
	}

	private static String langCodeToDisplayName(String langCode) {
		return switch (langCode) {
			case LANG_CODE_CHINESE -> LANG_DISPLAY_CHINESE;
			case LANG_CODE_JAPANESE -> LANG_DISPLAY_JAPANESE;
			default -> LANG_DISPLAY_ENGLISH;
		};
	}

	private static String displayNameToLangCode(String displayName) {
		return switch (displayName) {
			case LANG_DISPLAY_CHINESE -> LANG_CODE_CHINESE;
			case LANG_DISPLAY_JAPANESE -> LANG_CODE_JAPANESE;
			default -> LANG_CODE_ENGLISH;
		};
	}

	private void restartApplication() {
		preferencesFx = null;
		stage.close();

		Platform.runLater(() -> {
			try {
				JvmExplorer app = new JvmExplorer();
				Stage newStage = new Stage();
				app.start(newStage);
			}
			catch (Exception e) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Error");
				alert.setContentText(e.getMessage());
				alert.showAndWait();
			}
		});
	}

	private void showPreferences() {
		preferencesFx.show(true);
	}

	private void showAboutDialog() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(bundle.getString("app.about.title"));
		alert.setHeaderText(bundle.getString("app.about.header"));
		alert.setContentText(bundle.getString("app.about.content"));
		alert.initOwner(stage);
		alert.showAndWait();
	}

	private void setupTitlePaneText() {
		stage.titleProperty().bind(Bindings.createStringBinding(() -> {
			final RunningJvm currentJvm = this.runningJvmsController.getCurrentJvm();
			if (currentJvm != null) {
				return "JVM Explorer - " + currentJvm;
			}
			return "JVM Explorer";
		}, this.runningJvmsController.currentJvmProperty()));
	}

	private void wireChildControllers() {
		final ObjectProperty<RunningJvm> currentJvm = this.runningJvmsController.currentJvmProperty();
		final ObjectProperty<ClassContent> currentClass = this.loadedClassesController.currentClassProperty();
		final JvmExplorerSettings jvmExplorerSettings =
				JvmExplorerSettings.load(JvmExplorerSettings.DEFAULT_SETTINGS_FILE);
		jvmExplorerSettings.configureAutoSaving(JvmExplorerSettings.DEFAULT_SETTINGS_FILE);

		initializeStage(jvmExplorerSettings);

		final int serverPort = server.getPort();

		final FilterableTreeItem<ClassTreeNode> classesTreeRoot = new FilterableTreeItem<>();

		this.runningJvmsController.initialize(stage, executorService);
		this.loadedClassesController.initialize(stage,
		                                        executorService,
		                                        clientHandler,
		                                        currentJvm,
		                                        serverPort,
		                                        jvmExplorerSettings,
		                                        classesTreeRoot);
		this.currentClassController.initialize(stage,
		                                       executorService,
		                                       clientHandler,
		                                       currentJvm,
		                                       currentClass,
		                                       classesTreeRoot,
		                                       loadedClassesController::select);
	}

	private void initializeStage(JvmExplorerSettings jvmExplorerSettings) {
		final EventHandler<WindowEvent> onFirstShow = e -> {

			if (Double.isNaN(jvmExplorerSettings.getWidth().get())) {
				jvmExplorerSettings.getWidth().set(this.stage.getWidth());
			}
			if (Double.isNaN(jvmExplorerSettings.getHeight().get())) {
				jvmExplorerSettings.getHeight().set(this.stage.getHeight());
			}

			if (jvmExplorerSettings.getWidth().get() < 200) {
				jvmExplorerSettings.getWidth().set(1200);
			}
			if (jvmExplorerSettings.getHeight().get() < 200) {
				jvmExplorerSettings.getHeight().set(600);
			}

			this.stage.setWidth(jvmExplorerSettings.getWidth().get());
			this.stage.setHeight(jvmExplorerSettings.getHeight().get());

			this.stage.setMaximized(jvmExplorerSettings.getMaximized().get());

			this.stage.maximizedProperty().addListener((obs, old, newv) -> {
				jvmExplorerSettings.getMaximized().set(newv);
			});

			jvmExplorerSettings.getWidth().addListener((obs, old, newv) -> {
				this.stage.setWidth(newv.doubleValue());
			});

			jvmExplorerSettings.getHeight().addListener((obs, old, newv) -> {
				this.stage.setHeight(newv.doubleValue());
			});

			jvmExplorerSettings.getHeight().bind(this.stage.heightProperty());
			jvmExplorerSettings.getWidth().bind(this.stage.widthProperty());

			if (Double.isNaN(jvmExplorerSettings.getX().get())) {
				jvmExplorerSettings.getX().set(this.stage.getX());
			}
			if (Double.isNaN(jvmExplorerSettings.getY().get())) {
				jvmExplorerSettings.getY().set(this.stage.getY());
			}

			final Rectangle base = new Rectangle((int) jvmExplorerSettings.getX().get(),
			                                     (int) jvmExplorerSettings.getY().get(),
			                                     (int) jvmExplorerSettings.getWidth().get(),
			                                     (int) jvmExplorerSettings.getHeight().get());

			if (!ScreenHelper.isOnScreen(base, 0.20)) {
				this.stage.centerOnScreen();
			}
			else {
				this.stage.setX(jvmExplorerSettings.getX().get());
				this.stage.setY(jvmExplorerSettings.getY().get());
			}

			jvmExplorerSettings.getX().addListener((obs, old, newv) -> {
				this.stage.setX(newv.doubleValue());
			});

			jvmExplorerSettings.getY().addListener((obs, old, newv) -> {
				this.stage.setY(newv.doubleValue());
			});

			jvmExplorerSettings.getX().bind(this.stage.xProperty());
			jvmExplorerSettings.getY().bind(this.stage.yProperty());

			final SplitPane.Divider firstDivider = splitPane.getDividers().get(0);
			final SplitPane.Divider secondDivider = splitPane.getDividers().get(1);

			// Let's set it a first time to try and prevent graphical issues
			firstDivider.positionProperty().set(jvmExplorerSettings.getFirstDividerPosition().get());
			secondDivider.positionProperty().set(jvmExplorerSettings.getSecondDividerPosition().get());

			Platform.runLater(() -> {
				// Divider positions not respected if scene size != stage on initial show
				// Therefor we have to run this later after the scene shows the first time
				// https://stackoverflow.com/questions/15041332/javafx-splitpane-divider-position-inconsistent-behaviour
				firstDivider.positionProperty().bindBidirectional(jvmExplorerSettings.getFirstDividerPosition());
				secondDivider.positionProperty().bindBidirectional(jvmExplorerSettings.getSecondDividerPosition());
			});
		};

		this.stage.addEventHandler(WindowEvent.WINDOW_SHOWN, onFirstShow);
		this.stage.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> {
			this.stage.removeEventHandler(WindowEvent.WINDOW_SHOWN, onFirstShow);
		});
	}

	private void onConnect(RunningJvm jvm, Connection connection) {
		log.debug("Connected to {}", jvm);
		if (jvm.equals(runningJvmsController.getCurrentJvm())) {
			loadedClassesController.loadClasses(jvm);
		}
	}

	private void onDisconnect(RunningJvm jvm) {
		Platform.runLater(() -> {
			final RunningJvm selectedJvm = runningJvmsController.getCurrentJvm();
			if (jvm.equals(selectedJvm)) {
				if (!this.stage.isShowing()) {
					return;
				}
				alertHelper.showError(bundle.getString("error.connectionLost"), bundle.getString("error.connectionLostToJvm"));
				runningJvmsController.setCurrentJvm(null);
			}
		});
	}

}
