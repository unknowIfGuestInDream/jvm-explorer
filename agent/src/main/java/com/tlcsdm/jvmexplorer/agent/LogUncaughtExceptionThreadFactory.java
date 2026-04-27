package com.tlcsdm.jvmexplorer.agent;

import java.util.concurrent.ThreadFactory;

/**
 * Provides the log uncaught exception thread factory implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
public class LogUncaughtExceptionThreadFactory implements ThreadFactory {

	/**
	 * Creates a new thread.
	 */
	@Override
	public Thread newThread(Runnable r) {
		final Thread newThread = new Thread(r);
		newThread.setUncaughtExceptionHandler(new LogUncaughtExceptionHandler());
		return newThread;
	}

}
