package com.tlcsdm.jvmexplorer.agent;

import com.esotericsoftware.minlog.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;

/**
 * Provides the agent file logger implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
public class AgentFileLogger extends Log.Logger {

	private final ExecutorService executorService;

	private final File outputFile;
	private final boolean append;

	private volatile PrintWriter printWriter;
	private volatile boolean closed = false;

	/**
	 * Closes the associated resource.
	 */
	public void close() {
		closed = true;
		final PrintWriter printWriter = this.printWriter;
		if (printWriter != null) {
			printWriter.close();
		}
	}

	/**
	 * Writes the log to the agent log.
	 */
	@Override
	protected void print(String message) {
		if (closed) {
			System.out.println(message);
			return;
		}
		if (executorService == null) {
			printMessage(message);
			return;
		}
		executorService.submit(new Print(message));
	}

	/**
	 * Writes the log message to the agent log.
	 */
	private void printMessage(String message) {
		try {
			getPrintWriter().println(message);
		}
		catch (IOException | SecurityException e) {
			// This should ideally never happen...
			super.print(message);
		}
	}

	/**
	 * Returns the print writer value.
	 */
	private PrintWriter getPrintWriter() throws IOException {
		if (printWriter == null) {
			synchronized (this) {
				if (printWriter == null) {
					outputFile.getParentFile().mkdirs();
					final FileWriter fileWriter = new FileWriter(outputFile, append);
					printWriter = new PrintWriter(fileWriter, true);
				}
			}
		}
		return printWriter;
	}

	/**
	 * Provides the print implementation used by the com.tlcsdm.jvmexplorer.agent package.
	 */
	private class Print implements Runnable {
		private final String message;

		/**
		 * Creates a new Print task.
		 */
		Print(String message) {
			this.message = message;
		}

		/**
		 * Runs the configured task.
		 */
		@Override
		public void run() {
			AgentFileLogger.this.printMessage(message);
		}
	}


	/**
	 * Handles the agent file logger workflow.
	 */
	public AgentFileLogger(ExecutorService executorService, File outputFile, boolean append) {
		this.executorService = executorService;
		this.outputFile = outputFile;
		this.append = append;
	}

}
