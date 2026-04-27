package com.tlcsdm.jvmexplorer.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlcsdm.jvmexplorer.agent.RunningJvm;
import com.tlcsdm.jvmexplorer.net.ClientHandler;
import com.tlcsdm.jvmexplorer.protocol.LoadedClass;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Provides the export helper implementation used by the com.tlcsdm.jvmexplorer.helper package.
 */
public class ExportHelper {

	private static final Logger log = LoggerFactory.getLogger(ExportHelper.class);


	private final ClientHandler clientHandler;

	public boolean export(RunningJvm jvm, List<LoadedClass> loadedClasses, File outputJar,
	                      Consumer<Integer> currentProgress) {
		log.debug("Exporting {} files in {} to {}", loadedClasses.size(), jvm, outputJar);
		try {
			Files.deleteIfExists(outputJar.toPath());
			Files.createFile(outputJar.toPath());
		}
		catch (IOException e) {
			log.warn("Failed to create initial file for export", e);
			return false;
		}
		final AtomicInteger count = new AtomicInteger();
		try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(outputJar.toPath()))) {
			// Note - parallel stream runs in common fork join pool despite these being io bound tasks
			loadedClasses.stream()
			             .parallel()
			             .map(loadedClass -> new Pair<>(loadedClass, clientHandler.getClassBytes(jvm, loadedClass)))
			             .forEach(pair -> {
				             log.debug("Exporting: {}", pair.getKey().getName());
				             synchronized (count) {
					             // Possible race condition - count could be incremented before another thread, but the
					             // other thread could run currentProgress first. Therefore, we synchronize.
					             currentProgress.accept(count.incrementAndGet());
				             }
				             final String name = pair.getKey().getName().replace('.', '/') + ".class";
				             final byte[] content = pair.getValue();
				             write(name, content, jarOutputStream);
			             });
			log.debug("Jar created: {} with {} classes", outputJar, count.get());
			return true;
		}
		catch (IOException | UncheckedIOException e) {
			log.warn("Failed to export", e);
			return false;
		}
	}

	/**
	 * Writes the supplied data.
	 */
	private void write(String name, byte[] content, JarOutputStream jarOutputStream) {
		final ZipEntry zipEntry = new ZipEntry(name);
		try {
			// This synchronization is intentional and it works. Ignore any IDE warning.
			synchronized (jarOutputStream) {
				jarOutputStream.putNextEntry(zipEntry);
				jarOutputStream.write(content);
				jarOutputStream.closeEntry();
			}
		}
		catch (IOException e) {
			log.warn("Failed to write zip file: {}", name, e);
			throw new UncheckedIOException(e);
		}
	}


	/**
	 * Creates a new ExportHelper instance.
	 */
	public ExportHelper(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}

}
