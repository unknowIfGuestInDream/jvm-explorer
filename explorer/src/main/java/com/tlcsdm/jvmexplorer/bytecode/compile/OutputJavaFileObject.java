package com.tlcsdm.jvmexplorer.bytecode.compile;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * Provides the output java file object implementation used by the com.tlcsdm.jvmexplorer.bytecode.compile package.
 */
public class OutputJavaFileObject extends SimpleJavaFileObject {

	private final ByteArrayOutputStream os = new ByteArrayOutputStream();

	/**
	 * Creates a new OutputJavaFileObject instance.
	 */
	public OutputJavaFileObject(String name, JavaFileObject.Kind kind) {
		super(URI.create("memory:///" + name.replace('.', '/') + kind.extension), kind);
	}

	/**
	 * Returns the bytes value.
	 */
	public byte[] getBytes() {
		return this.os.toByteArray();
	}

	/**
	 * Opens the output stream.
	 */
	@Override
	public OutputStream openOutputStream() {
		return this.os;
	}

}
