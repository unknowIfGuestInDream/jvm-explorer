package com.tlcsdm.jvmexplorer.bytecode.compile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Provides the provided jvm file manager implementation used by the com.tlcsdm.jvmexplorer.bytecode.compile package.
 */
public class ProvidedJvmFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

	private static final Logger log = LoggerFactory.getLogger(ProvidedJvmFileManager.class);


	private final JavacBytecodeProvider javacBytecodeProvider;

	private OutputJavaFileObject output;

	/**
	 * Creates a new ProvidedJvmFileManager instance.
	 */
	public ProvidedJvmFileManager(StandardJavaFileManager fileManager, JavacBytecodeProvider javacBytecodeProvider) {
		super(fileManager);
		this.javacBytecodeProvider = javacBytecodeProvider;
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds,
	                                     boolean recurse) throws IOException {
		final Iterable<JavaFileObject> standardJavaFileObjects = fileManager.list(location,
		                                                                          packageName,
		                                                                          kinds,
		                                                                          recurse);
		if (location != StandardLocation.CLASS_PATH || !kinds.contains(JavaFileObject.Kind.CLASS)) {
			return standardJavaFileObjects;
		}
		final List<JavaFileObject> remoteClasses = javacBytecodeProvider.list(packageName, recurse);
		log.debug("Loaded package {} in provided classpath: {}", packageName, remoteClasses);

		final List<JavaFileObject> results = new ArrayList<>();
		for (JavaFileObject javaFileObject : standardJavaFileObjects) {
			results.add(javaFileObject);
		}
		results.addAll(remoteClasses);

		return results;
	}

	/**
	 * Performs the infer binary name operation.
	 */
	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		if (file instanceof ProvidedJavaFileObject) {
			return file.toString();
		}
		return super.inferBinaryName(location, file);
	}

	@Override
	public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className,
	                                           JavaFileObject.Kind kind, FileObject sibling) {
		return this.output = new OutputJavaFileObject(className, kind);
	}

	/**
	 * Returns the output class content value.
	 */
	public byte[] getOutputClassContent() {
		if (output == null) {
			return null;
		}
		return output.getBytes();
	}

}
