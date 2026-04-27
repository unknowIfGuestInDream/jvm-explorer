package com.tlcsdm.jvmexplorer.agent;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.tlcsdm.jvmexplorer.protocol.JvmClient;

import java.util.concurrent.ExecutorService;

/**
 * Provides the client listener implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
public class ClientListener extends Listener {

	private final ExecutorService executorService;
	private final String identifier;
	private final JvmClient jvmClient;

	/**
	 * Performs the connected operation.
	 */
	@Override
	public void connected(Connection connection) {
		executorService.submit(new Register(jvmClient, identifier));
	}

	/**
	 * Provides the register implementation used by the com.tlcsdm.jvmexplorer.agent package.
	 */
	private static class Register implements Runnable {
		private final JvmClient jvmClient;
		private final String identifier;

		/**
		 * Creates a new Register task.
		 */
		Register(JvmClient jvmClient, String identifier) {
			this.jvmClient = jvmClient;
			this.identifier = identifier;
		}

		/**
		 * Runs the configured task.
		 */
		@Override
		public void run() {
			jvmClient.register(identifier);
			Log.info("Registered client");
		}
	}


	/**
	 * Performs the client listener operation.
	 */
	public ClientListener(ExecutorService executorService, String identifier, JvmClient jvmClient) {
		this.executorService = executorService;
		this.identifier = identifier;
		this.jvmClient = jvmClient;
	}

}
