package com.tlcsdm.jvmexplorer.agent;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.tlcsdm.jvmexplorer.protocol.JvmClient;

import java.util.concurrent.ExecutorService;

public class ClientListener extends Listener {

	private final ExecutorService executorService;
	private final String identifier;
	private final JvmClient jvmClient;

	@Override
	public void connected(Connection connection) {
		executorService.submit(new Register(jvmClient, identifier));
	}

		private static class Register implements Runnable {
		private final JvmClient jvmClient;
		private final String identifier;

		Register(JvmClient jvmClient, String identifier) {
			this.jvmClient = jvmClient;
			this.identifier = identifier;
		}

		@Override
		public void run() {
			jvmClient.register(identifier);
			Log.info("Registered client");
		}
	}


	public ClientListener(ExecutorService executorService, String identifier, JvmClient jvmClient) {
		this.executorService = executorService;
		this.identifier = identifier;
		this.jvmClient = jvmClient;
	}

}
