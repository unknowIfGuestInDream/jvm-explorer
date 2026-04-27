package com.tlcsdm.jvmexplorer.bytecode;

/**
 * Provides the assembly exception implementation used by the com.tlcsdm.jvmexplorer.bytecode package.
 */
public class AssemblyException extends RuntimeException {

	/**
	 * Creates a new AssemblyException instance.
	 */
	public AssemblyException(Throwable source) {
		super(source);
	}

}
