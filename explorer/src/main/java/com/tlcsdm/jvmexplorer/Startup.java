package com.tlcsdm.jvmexplorer;


public class Startup {

	private static final Logger log = LoggerFactory.getLogger(Startup.class);


	public static void main(String[] args) {
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
