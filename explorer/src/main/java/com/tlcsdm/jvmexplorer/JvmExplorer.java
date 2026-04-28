package com.tlcsdm.jvmexplorer;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlcsdm.jvmexplorer.preferences.AppPreferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides the jvm explorer implementation used by the com.tlcsdm.jvmexplorer package.
 */
public class JvmExplorer extends Application {

	private static final Logger log = LoggerFactory.getLogger(JvmExplorer.class);

	public static final String BUNDLE_BASE_NAME = "com.tlcsdm.jvmexplorer.i18n.messages";

	public static final File APP_DIR = new File(System.getProperty("user.home"), "jvm-explorer");

	private static ResourceBundle resourceBundle;

	/**
	 * Returns the bundle value.
	 */
	public static ResourceBundle getBundle() {
		return resourceBundle;
	}

	/**
	 * Starts the configured workflow.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		final AppPreferences preferences = AppPreferences.getInstance();
		final Locale locale = preferences.getLocale();
		Locale.setDefault(locale);

		applyTheme(preferences.getTheme());

		final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
		resourceBundle = bundle;
		final FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main.fxml"), bundle);
		final Parent root = loader.load();
		final JvmExplorerController jvmExplorerController = loader.getController();

		final Scene scene = new Scene(root);
		scene.getStylesheets().addAll("css/style-override.css", "css/java-keywords.css");
		primaryStage.setScene(scene);
		InputStream iconStream = getClass().getClassLoader().getResourceAsStream("icons/jvm.png");
		if (iconStream != null) {
			primaryStage.getIcons().add(new Image(iconStream));
		}

		jvmExplorerController.initialize(primaryStage, bundle);

		primaryStage.show();
		log.debug("Started explorer");
	}

	/**
	 * Applies theme.
	 */
	public static void applyTheme(String themeName) {
		String stylesheet = switch (themeName) {
			case "Default" -> null;
			case "Primer Dark" -> new PrimerDark().getUserAgentStylesheet();
			case "Nord Light" -> new NordLight().getUserAgentStylesheet();
			case "Nord Dark" -> new NordDark().getUserAgentStylesheet();
			case "Cupertino Light" -> new CupertinoLight().getUserAgentStylesheet();
			case "Cupertino Dark" -> new CupertinoDark().getUserAgentStylesheet();
			case "Dracula" -> new Dracula().getUserAgentStylesheet();
			default -> new PrimerLight().getUserAgentStylesheet();
		};
		Application.setUserAgentStylesheet(stylesheet);
	}

}
