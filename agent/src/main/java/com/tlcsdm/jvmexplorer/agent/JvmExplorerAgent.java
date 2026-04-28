package com.tlcsdm.jvmexplorer.agent;

import com.esotericsoftware.minlog.Log;
import com.tlcsdm.jvmexplorer.protocol.AgentConfiguration;
import com.tlcsdm.jvmexplorer.protocol.helper.VerboseScheduledExecutorService;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Provides the jvm explorer agent implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
public class JvmExplorerAgent {

	/**
	 * Starts the JVM agent after it is loaded by the JVM instrumentation API.
	 */
	public static void agentmain(String agentArgs, Instrumentation instrumentation) {
		final AgentConfiguration agentConfiguration = AgentConfiguration.parseAgentArgs(agentArgs);
		final ScheduledExecutorService executorService = createExecutorService();
		final AgentFileLogger logger = setupLogger(agentConfiguration.getLogFilePath(),
		                                           agentConfiguration.getLogLevel());
		Log.info("Agent connected. Configuration: " + agentConfiguration);
		try {
			final ClientLauncher clientLauncher = new ClientLauncher();
			clientLauncher.launch(executorService, agentConfiguration, instrumentation, logger);
		}
		catch (Exception e) {
			Log.error("Agent setup failed", e);
			logger.close();
			executorService.shutdown();
		}
	}

	/**
	 * Creates executor service.
	 */
	private static ScheduledExecutorService createExecutorService() {
		final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3,
		                                                                                           new LogUncaughtExceptionThreadFactory());
		return new VerboseScheduledExecutorService(scheduledExecutorService);
	}

	/**
	 * Sets up the file logger used by the attached agent.
	 */
	private static AgentFileLogger setupLogger(String logFilePath, int logLevel) {
		final AgentFileLogger logger = new AgentFileLogger(null, new File(logFilePath), true);
		Log.setLogger(logger);
		Log.set(logLevel);
		Log.info("Initialized logger");
		return logger;
	}

}
