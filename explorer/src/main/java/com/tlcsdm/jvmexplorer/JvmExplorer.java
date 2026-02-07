package com.tlcsdm.jvmexplorer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class JvmExplorer extends Application {

	private static final Logger log = LoggerFactory.getLogger(JvmExplorer.class);


	public static final File APP_DIR = new File(System.getProperty("user.home"), "jvm-explorer");

	@Override
	public void start(Stage primaryStage) throws Exception {
		final FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main.fxml"));
		final Parent root = loader.load();
		final JvmExplorerController jvmExplorerController = loader.getController();

		final Scene scene = new Scene(root);
		scene.getStylesheets().addAll("css/style-override.css", "css/java-keywords.css");
		primaryStage.setScene(scene);

		jvmExplorerController.initialize(primaryStage);

		primaryStage.show();
		log.debug("Started explorer");
	}

}
