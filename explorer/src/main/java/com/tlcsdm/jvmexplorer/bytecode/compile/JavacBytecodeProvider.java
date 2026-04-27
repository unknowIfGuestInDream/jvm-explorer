package com.tlcsdm.jvmexplorer.bytecode.compile;

import javax.tools.JavaFileObject;
import java.util.List;

/**
 * Defines the contract for javac bytecode provider behavior in the com.tlcsdm.jvmexplorer.bytecode.compile package.
 */
public interface JavacBytecodeProvider {

	/**
	 * Lists Java file objects available to the compiler for a package.
	 */
	List<JavaFileObject> list(String packageName, boolean recurse);

}
