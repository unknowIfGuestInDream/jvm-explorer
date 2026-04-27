package com.tlcsdm.jvmexplorer.agent;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

import java.util.concurrent.ExecutorService;

/**
 * Provides the cleanup listener implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
public class CleanupListener extends Listener {

	private final ExecutorService executorService;
	private final AgentFileLogger agentFileLogger;

	/**
	 * Handles the disconnected callback.
	 */
	@Override
	public void disconnected(Connection connection) {
		Log.info("Cleaning up");
		executorService.shutdown();
		agentFileLogger.close();
	}


	/**
	 * Creates a new CleanupListener instance.
	 */
	public CleanupListener(ExecutorService executorService, AgentFileLogger agentFileLogger) {
		this.executorService = executorService;
		this.agentFileLogger = agentFileLogger;
	}

}
