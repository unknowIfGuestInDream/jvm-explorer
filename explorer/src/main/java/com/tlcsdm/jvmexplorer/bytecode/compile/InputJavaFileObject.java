package com.tlcsdm.jvmexplorer.bytecode.compile;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * Provides the input java file object implementation used by the com.tlcsdm.jvmexplorer.bytecode.compile package.
 */
public class InputJavaFileObject extends SimpleJavaFileObject {

	private final CharSequence content;

	/**
	 * Creates a new InputJavaFileObject instance.
	 */
	public InputJavaFileObject(String className, CharSequence content) {
		super(URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension),
		      JavaFileObject.Kind.SOURCE);
		this.content = content;
	}

	/**
	 * Returns the char content value.
	 */
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return this.content;
	}

}
