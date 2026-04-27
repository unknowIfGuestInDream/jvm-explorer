package com.tlcsdm.jvmexplorer.fx.jvms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlcsdm.jvmexplorer.JvmExplorer;
import com.tlcsdm.jvmexplorer.agent.AgentException;
import com.tlcsdm.jvmexplorer.agent.AgentPreparer;
import com.tlcsdm.jvmexplorer.agent.RunningJvm;
import com.tlcsdm.jvmexplorer.helper.AlertHelper;
import com.tlcsdm.jvmexplorer.helper.FileHelper;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Provides the running jvm list cell factory implementation used by the com.tlcsdm.jvmexplorer.fx.jvms package.
 */
public class RunningJvmListCellFactory implements Callback<ListView<RunningJvm>, ListCell<RunningJvm>> {

	private static final Logger log = LoggerFactory.getLogger(RunningJvmListCellFactory.class);


	private final AgentPreparer agentPreparer = new AgentPreparer();
	private final FileHelper fileHelper = new FileHelper();

	private final ExecutorService executorService;
	private final AlertHelper alertHelper;
	private final ObjectProperty<RunningJvm> currentJvm;

	/**
	 * Calls the wrapped operation and returns its result.
	 */
	@Override
	public ListCell<RunningJvm> call(ListView<RunningJvm> listView) {
		final ListCell<RunningJvm> listCell = new ListCell<>();
		listCell.graphicProperty().bind(Bindings.createObjectBinding(() -> {
			if (listCell.getItem() == null) {
				return null;
			}
			return new ImageView(new Image(Objects.requireNonNull(getClass().getClassLoader()
			                                                                .getResourceAsStream("icons/jvm.png"))));
		}, listCell.itemProperty()));
		final ContextMenu rowContextMenu = new ContextMenu();
		final MenuItem viewPropertiesItem = new MenuItem(JvmExplorer.getBundle().getString("ctx.viewSystemProperties"));
		viewPropertiesItem.setOnAction(e -> {
			final RunningJvm currentItem = listCell.getItem();
			if (currentItem == null) {
				return;
			}
			displayProperties(currentItem);
		});
		rowContextMenu.getItems().add(viewPropertiesItem);
		final MenuItem launchProcess = createLaunchProcessMenuItem(listView);
		rowContextMenu.getItems().addAll(new SeparatorMenuItem(), launchProcess);
		final ContextMenu emptyContextMenu = new ContextMenu();
		final MenuItem launchProcessEmpty = createLaunchProcessMenuItem(listView);
		emptyContextMenu.getItems().add(launchProcessEmpty);
		listCell.contextMenuProperty()
		        .bind(Bindings.when(listCell.itemProperty().isNotNull())
		                      .then(rowContextMenu)
		                      .otherwise(emptyContextMenu));
		listCell.textProperty()
		        .bind(Bindings.when(listCell.itemProperty().isNotNull())
		                      .then(listCell.itemProperty().asString())
		                      .otherwise(""));
		final Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(listCell.itemProperty().asString());
		listCell.tooltipProperty()
		        .bind(Bindings.when(listCell.itemProperty().isNotNull()).then(tooltip).otherwise((Tooltip) null));
		return listCell;
	}

	/**
	 * Performs the display properties operation.
	 */
	private void displayProperties(RunningJvm jvm) {
		executorService.submit(() -> {
			try {
				final Properties properties = jvm.getSystemProperties();
				Platform.runLater(() -> {
					final ResourceBundle bundle = JvmExplorer.getBundle();
					final List<String> propertiesList = properties.entrySet()
					                                              .stream()
					                                              .map(entry -> entry.getKey() + ": "
					                                                            + entry.getValue())
					                                              .collect(Collectors.toList());
					alertHelper.showExpandableList(bundle.getString("dialog.jvmSystemProperties"),
					                               java.text.MessageFormat.format(bundle.getString("dialog.systemProperties"), jvm),
					                               null,
					                               propertiesList);
				});
			}
			catch (AgentException ex) {
				Platform.runLater(() -> {
					final ResourceBundle bundle = JvmExplorer.getBundle();
					alertHelper.showError(bundle.getString("error.operationFailed"),
					                      bundle.getString("error.failedToLoadSystemProperties"),
					                      ex);
				});
			}
		});
	}

	/**
	 * Creates launch process menu item.
	 */
	private MenuItem createLaunchProcessMenuItem(ListView<RunningJvm> listView) {
		final MenuItem launchProcessMenuItem = new MenuItem(JvmExplorer.getBundle().getString("ctx.launchJar"));
		launchProcessMenuItem.setOnAction(e -> {
			final File selectedFile = fileHelper.openJar(listView.getScene().getWindow(), JvmExplorer.getBundle().getString("ctx.launchJar"));
			if (selectedFile == null) {
				return;
			}
			launchJar(selectedFile);
		});
		return launchProcessMenuItem;
	}

	/**
	 * Launches jar.
	 */
	private void launchJar(File selectedFile) {
		executorService.submit(() -> {
			final String agentPath = agentPreparer.loadAgentOnFileSystem("agents/launch-agent.jar");
			final List<String> launchArgs = new ArrayList<>();
			launchArgs.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
			launchArgs.add("-javaagent:" + agentPath);
			launchArgs.add("-jar");
			launchArgs.add(selectedFile.getAbsolutePath());
			try {
				final Process process = new ProcessBuilder().command(launchArgs).redirectErrorStream(true).start();
				// Create a new thread here, so we can log all output
				new Thread(() -> {
					try (final BufferedReader br =
							     new BufferedReader(new InputStreamReader(process.getInputStream()))) {
						br.lines().forEach(line -> log.debug("Launched JVM: {}", line));
					}
					catch (IOException e) {
						log.warn("Exception while reading output from launched process", e);
					}
				}).start();
			}
			catch (IOException ex) {
				Platform.runLater(() -> {
					final ResourceBundle bundle = JvmExplorer.getBundle();
					alertHelper.showError(bundle.getString("error.jarLaunchFailed"), bundle.getString("error.failedToLaunchJar"), ex);
					this.currentJvm.set(null);
				});
			}
		});
	}


	/**
	 * Creates a new RunningJvmListCellFactory instance.
	 */
	public RunningJvmListCellFactory(ExecutorService executorService, AlertHelper alertHelper, ObjectProperty<RunningJvm> currentJvm) {
		this.executorService = executorService;
		this.alertHelper = alertHelper;
		this.currentJvm = currentJvm;
	}

}
