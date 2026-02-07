package com.tlcsdm.jvmexplorer.agent;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

import java.util.concurrent.ExecutorService;

public class CleanupListener extends Listener {

	private final ExecutorService executorService;
	private final AgentFileLogger agentFileLogger;

	@Override
	public void disconnected(Connection connection) {
		Log.info("Cleaning up");
		executorService.shutdown();
		agentFileLogger.close();
	}


	public CleanupListener(ExecutorService executorService, AgentFileLogger agentFileLogger) {
		this.executorService = executorService;
		this.agentFileLogger = agentFileLogger;
	}

}
