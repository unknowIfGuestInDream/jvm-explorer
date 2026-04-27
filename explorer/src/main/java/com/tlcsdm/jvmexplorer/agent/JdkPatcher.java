package com.tlcsdm.jvmexplorer.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Provides the jdk patcher implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
class JdkPatcher {

	private static final Logger log = LoggerFactory.getLogger(JdkPatcher.class);

	private static final String INSTRUMENT_32_BIT = "jdk_patch/instrument-32.dll";
	private static final String INSTRUMENT_64_BIT = "jdk_patch/instrument-64.dll";

	/**
	 * Attempts to patch the target jvm to support attaching. This may only work in a few cases.
	 * <p>
	 * Writing DLL files to another JVM's directory may be flagged by security software.
	 * This behavior can be disabled by setting the system property
	 * {@code jvm.explorer.patch.disabled=true}.
	 *
	 * @param runningJvm
	 * 		the jvm to patch
	 * @return true if a patch was applied and a re-attach should be tried, false if nothing was changed
	 */
	static boolean patchJdkForAgent(RunningJvm runningJvm) {
		try {
			if (Boolean.getBoolean("jvm.explorer.patch.disabled")) {
				log.info("JDK patching is disabled via system property 'jvm.explorer.patch.disabled'");
				return false;
			}
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				// Patch only supports windows at the moment
				return false;
			}
			final Properties properties = runningJvm.getSystemProperties();
			final String javaHome = properties.getProperty("java.home");
			final boolean is32Bit = "x86".equals(properties.getProperty("os.arch"));
			final String resourceName = is32Bit ? INSTRUMENT_32_BIT : INSTRUMENT_64_BIT;
			log.warn("Attempting to patch JDK at {} - this may trigger security software. "
			         + "Set -Djvm.explorer.patch.disabled=true to disable this behavior.", javaHome);
			final File instrumentFile = new File(javaHome, "bin" + File.separator + "instrument.dll");
			if (instrumentFile.exists()) {
				log.debug("Resource already exists for {}", javaHome);
				// Already exists, or is patched already
				return false;
			}
			try (final FileOutputStream fileOutputStream = new FileOutputStream(instrumentFile);
			     final InputStream inputStream = Objects.requireNonNull(JdkPatcher.class.getClassLoader()
			                                                                            .getResource(resourceName))
			                                            .openStream()
			) {
				inputStream.transferTo(fileOutputStream);
			}
			log.debug("Patched {}", instrumentFile);
			return true;
		}
		catch (Exception e) {
			log.debug("Failed to patch agent", e);
			return false;
		}
	}

}
