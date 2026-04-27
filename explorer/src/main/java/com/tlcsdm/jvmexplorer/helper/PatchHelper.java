package com.tlcsdm.jvmexplorer.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlcsdm.jvmexplorer.agent.RunningJvm;
import com.tlcsdm.jvmexplorer.net.ClientHandler;
import com.tlcsdm.jvmexplorer.protocol.ClassLoaderDescriptor;
import com.tlcsdm.jvmexplorer.protocol.LoadedClass;
import com.tlcsdm.jvmexplorer.protocol.PatchResult;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.jar.JarFile;

/**
 * Provides the patch helper implementation used by the com.tlcsdm.jvmexplorer.helper package.
 */
public class PatchHelper {

	private static final Logger log = LoggerFactory.getLogger(PatchHelper.class);


	public boolean patch(File jarFile, RunningJvm runningJvm, ClientHandler clientHandler,
	                     ClassLoaderDescriptor classLoaderDescriptor, Consumer<Integer> patchedClasses) {
		log.debug("Attempting to patch {} with {}", runningJvm, jarFile);
		final AtomicInteger patchedClassCount = new AtomicInteger();
		try (final JarFile jar = new JarFile(jarFile)) {
			jar.stream().parallel().filter(j -> j.getName().endsWith(".class")).forEach(classFile -> {
				try (var entryStream = jar.getInputStream(classFile)) {
					final String name = classFile.getName().replace('/', '.').replace(".class", "");
					log.debug("Patching {}", name);
					final byte[] classContents = entryStream.readAllBytes();
					// Note - we may not always want to pass in the class loader. It could be in a child classloader.
					final LoadedClass loadedClass = new LoadedClass(name, classLoaderDescriptor, null);
					final PatchResult result = clientHandler.replaceClass(runningJvm, loadedClass, classContents);
					if (!result.isSuccess()) {
						throw new IllegalStateException(
								"Failed to replace class on jvm: " + name + " because " + result.getMessage());
					}
				}
				catch (IOException e) {
					log.warn("Failed to process {}", classFile.getName());
					throw new UncheckedIOException(e);
				}
				synchronized (patchedClassCount) {
					patchedClasses.accept(patchedClassCount.incrementAndGet());
				}
			});
			return true;
		}
		catch (IOException | UncheckedIOException | IllegalStateException e) {
			log.warn("Failed to patch {} with {}", runningJvm, jarFile, e);
			return false;
		}
	}

}
