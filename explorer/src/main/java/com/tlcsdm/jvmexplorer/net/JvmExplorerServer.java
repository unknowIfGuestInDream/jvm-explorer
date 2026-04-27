package com.tlcsdm.jvmexplorer.net;

import com.esotericsoftware.kryonet.Server;

/**
 * Provides the jvm explorer server implementation used by the com.tlcsdm.jvmexplorer.net package.
 */
public class JvmExplorerServer extends Server {

	private int port;

	/**
	 * Creates a new JvmExplorerServer instance.
	 */
	public JvmExplorerServer(int writeBufferSize, int objectBufferSize) {
		super(writeBufferSize, objectBufferSize);
	}

	/**
	 * Returns the port value.
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Updates the listening port assigned to this server.
	 */
	void setPort(int port) {
		this.port = port;
	}

}
