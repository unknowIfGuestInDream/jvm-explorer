package com.tlcsdm.jvmexplorer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;


public class Startup {

	private static final Logger log = LoggerFactory.getLogger(Startup.class);


	public static void main(String[] args) {
		// Suppress JavaFX 'unnamed module' warning
		java.util.logging.Logger.getLogger("com.sun.javafx.application.PlatformImpl")
				.setLevel(Level.SEVERE);
		final String version = Startup.class.getPackage().getImplementationVersion();
		log.info("Starting application. Application Version: {}. Java Version: {}.",
		         (version == null ? "Development" : version),
		         System.getProperty("java.version"));
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			log.warn("Thread uncaught exception: " + t, e);
		});
		JvmExplorer.launch(JvmExplorer.class, args);
	}

}
