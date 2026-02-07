package com.tlcsdm.jvmexplorer.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.tlcsdm.jvmexplorer.protocol.Protocol;

import java.io.IOException;
import java.net.BindException;
import java.util.concurrent.ScheduledExecutorService;

public class ServerLauncher {

	private static final Logger log = LoggerFactory.getLogger(ServerLauncher.class);


	private final OpenPortProvider openPortProvider;

	public JvmExplorerServer launch(ScheduledExecutorService executorService, ClientHandler clientHandler) {
		setupLogging();
		final JvmExplorerServer server = new JvmExplorerServer(Protocol.WRITE_BUFFER_SIZE,
		                                                       Protocol.OBJECT_BUFFER_SIZE) {
			@Override
			protected Connection newConnection() {
				return new JvmClientImpl(executorService);
			}
		};
		registerProtocol(server);
		server.addListener(clientHandler);
		server.start();
		bindPort(server);
		return server;
	}

	private void setupLogging() {
		final int minlogLevel = getLogLevel();
		log.debug("Setting minlog log level to {}", minlogLevel);
		Log.set(minlogLevel);
		Log.setLogger(new MinlogToSlf4j());
	}

	private void registerProtocol(Server server) {
		final Kryo kryo = server.getKryo();
		Protocol.register(kryo);
	}

	private void bindPort(JvmExplorerServer server) {
		while (true) {
			// This finds an open port and passes it into kryonet. It handles a race condition where a port is taken
			// after finding it but before binding the kryonet server to it.
			// An alternative is to pass in 0 directly to kryonet, and use reflection to access the server socket,
			// then grab the port. Ideally reflection can be avoided so this approach isn't used.
			try {
				final int port = openPortProvider.getOpenPort();
				log.debug("Found open port: {}", port);
				server.bind(port);
				server.setPort(port);
				log.debug("Bound port successfully: {}", port);
			}
			catch (BindException e) {
				if (e.getMessage().contains("Address already in use")) {
					log.trace("Port was taken, retrying...");
					// Someone stole our port, let's keep trying
					continue;
				}
				log.error("Failed to bind server to port", e);
			}
			catch (IOException e) {
				log.error("Failed to bind server to port", e);
			}
			break;
		}
	}

	private int getLogLevel() {
		if (log.isTraceEnabled()) {
			return Log.LEVEL_TRACE;
		}
		else if (log.isDebugEnabled()) {
			return Log.LEVEL_DEBUG;
		}
		else if (log.isInfoEnabled()) {
			return Log.LEVEL_INFO;
		}
		else if (log.isWarnEnabled()) {
			return Log.LEVEL_WARN;
		}
		else if (log.isErrorEnabled()) {
			return Log.LEVEL_ERROR;
		}
		else {
			return Log.LEVEL_NONE;
		}
	}


	public ServerLauncher(OpenPortProvider openPortProvider) {
		this.openPortProvider = openPortProvider;
	}

}
