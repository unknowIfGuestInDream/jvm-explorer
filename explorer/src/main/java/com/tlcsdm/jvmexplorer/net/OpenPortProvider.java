package com.tlcsdm.jvmexplorer.net;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Provides the open port provider implementation used by the com.tlcsdm.jvmexplorer.net package.
 */
public class OpenPortProvider {

	/**
	 * Returns the open port value.
	 */
	public int getOpenPort() throws IOException {
		try (final ServerSocket ss = new ServerSocket(0)) {
			ss.setReuseAddress(true);
			return ss.getLocalPort();
		}
	}

}
