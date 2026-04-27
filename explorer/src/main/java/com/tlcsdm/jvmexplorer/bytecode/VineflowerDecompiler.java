package com.tlcsdm.jvmexplorer.bytecode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.io.File;
import java.util.jar.Manifest;

/**
 * Provides the vineflower decompiler implementation used by the com.tlcsdm.jvmexplorer.bytecode package.
 */
public class VineflowerDecompiler implements Decompiler {

	private static final Logger log = LoggerFactory.getLogger(VineflowerDecompiler.class);


	/**
	 * Performs the process operation.
	 */
	@Override
	public String process(byte[] bytes) {
		final IBytecodeProvider bytecodeProvider = new BytecodeProvider(bytes);
		final ResultSaver resultSaver = new ResultSaver();
		final IFernflowerLogger fernflowerLogger = new FernflowerLogger();
		final Fernflower fernflower = new Fernflower(bytecodeProvider,
		                                             resultSaver,
		                                             IFernflowerPreferences.DEFAULTS,
		                                             fernflowerLogger);

		try {
			fernflower.addSource(new File("fake-file.class"));
			fernflower.decompileContext();
		}
		catch (Exception e) {
			log.warn("Failed to decompile class", e);
		}

		return resultSaver.getContent();
	}

	/**
	 * Provides the bytecode provider implementation used by the com.tlcsdm.jvmexplorer.bytecode package.
	 */
	private static class BytecodeProvider implements IBytecodeProvider {
		private final byte[] bytes;

		/**
		 * Creates a new BytecodeProvider instance.
		 */
		BytecodeProvider(byte[] bytes) {
			this.bytes = bytes;
		}

		/**
		 * Returns the bytecode value.
		 */
		@Override
		public byte[] getBytecode(String externalPath, String internalPath) {
			return bytes;
		}
	}

	/**
	 * Provides the fernflower logger implementation used by the com.tlcsdm.jvmexplorer.bytecode package.
	 */
	private static class FernflowerLogger extends IFernflowerLogger {
		/**
		 * Writes message.
		 */
		@Override
		public void writeMessage(String message, Severity severity) {
			switch (severity) {
			case TRACE:
				log.trace(message);
				break;
			case INFO:
				log.info(message);
				break;
			case WARN:
				log.warn(message);
				break;
			case ERROR:
				log.error(message);
				break;
			}
		}

		/**
		 * Writes message.
		 */
		@Override
		public void writeMessage(String message, Severity severity, Throwable t) {
			switch (severity) {
			case TRACE:
				log.trace(message, t);
				break;
			case INFO:
				log.info(message, t);
				break;
			case WARN:
				log.warn(message, t);
				break;
			case ERROR:
				log.error(message, t);
				break;
			}
		}
	}

	/**
	 * Provides the result saver implementation used by the com.tlcsdm.jvmexplorer.bytecode package.
	 */
	private static class ResultSaver implements IResultSaver {
		private String content;

		/**
		 * Returns the content value.
		 */
		public String getContent() {
			return this.content;
		}

		@Override
		public void saveFolder(String path) {}

		@Override
		public void copyFile(String source, String path, String entryName) {}

		/**
		 * Performs the save class file operation.
		 */
		@Override
		public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
			this.content = content;
		}

		@Override
		public void createArchive(String path, String archiveName, Manifest manifest) {}

		@Override
		public void saveDirEntry(String path, String archiveName, String entryName) {}

		@Override
		public void copyEntry(String source, String path, String archiveName, String entry) {}

		@Override
		public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName,
		                           String content) {}

		@Override
		public void closeArchive(String path, String archiveName) {}
	}

}
