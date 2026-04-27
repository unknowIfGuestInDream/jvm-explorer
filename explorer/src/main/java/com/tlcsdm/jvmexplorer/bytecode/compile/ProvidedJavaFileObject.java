package com.tlcsdm.jvmexplorer.bytecode.compile;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Supplier;

/**
 * Provides the provided java file object implementation used by the com.tlcsdm.jvmexplorer.bytecode.compile package.
 */
public class ProvidedJavaFileObject extends SimpleJavaFileObject {

	private final Supplier<byte[]> remoteFileSupplier;
	private final String className;

	/**
	 * Creates a new ProvidedJavaFileObject instance.
	 */
	public ProvidedJavaFileObject(String name, JavaFileObject.Kind kind, Supplier<byte[]> remoteFileSupplier) {
		super(URI.create("provided:///" + name.replace('.', '/') + kind.extension), kind);
		this.className = name;
		this.remoteFileSupplier = remoteFileSupplier;
	}

	/**
	 * Opens the input stream.
	 */
	@Override
	public InputStream openInputStream() {
		final byte[] bytes = remoteFileSupplier.get();
		return new ByteArrayInputStream(bytes);
	}

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return className;
	}

}
