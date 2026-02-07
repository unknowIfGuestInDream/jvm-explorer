package com.tlcsdm.jvmexplorer.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esotericsoftware.minlog.Log;

public class MinlogToSlf4j extends Log.Logger {

	private static final Logger log = LoggerFactory.getLogger(MinlogToSlf4j.class);


	@Override
	public void log(int level, String category, String message, Throwable ex) {
		final String line = category != null ? (category + " - " + message) : message;

		switch (level) {
		case Log.LEVEL_ERROR:
			log.error(line, ex);
			break;
		case Log.LEVEL_WARN:
			log.warn(line, ex);
			break;
		case Log.LEVEL_INFO:
			log.info(line, ex);
			break;
		case Log.LEVEL_DEBUG:
			log.debug(line, ex);
			break;
		case Log.LEVEL_TRACE:
			log.trace(line, ex);
			break;
		}
	}

}
