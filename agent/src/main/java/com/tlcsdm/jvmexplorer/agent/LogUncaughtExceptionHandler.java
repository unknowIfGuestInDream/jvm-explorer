package com.tlcsdm.jvmexplorer.agent;

import com.esotericsoftware.minlog.Log;

public class LogUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		Log.error("Thread threw exception: " + t, e);
	}

}
