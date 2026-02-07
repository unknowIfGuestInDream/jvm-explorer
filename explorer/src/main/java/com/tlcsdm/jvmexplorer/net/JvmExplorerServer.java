package com.tlcsdm.jvmexplorer.net;

import com.esotericsoftware.kryonet.Server;

public class JvmExplorerServer extends Server {

	@Setter(AccessLevel.PACKAGE)
		private int port;

	public JvmExplorerServer(int writeBufferSize, int objectBufferSize) {
		super(writeBufferSize, objectBufferSize);
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
