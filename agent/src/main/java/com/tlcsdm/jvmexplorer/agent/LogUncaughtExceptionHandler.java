package com.tlcsdm.jvmexplorer.agent;

import com.esotericsoftware.minlog.Log;

/**
 * Provides the log uncaught exception handler implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
public class LogUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	/**
	 * Performs the uncaught exception operation.
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		Log.error("Thread threw exception: " + t, e);
	}

}
