package com.tlcsdm.jvmexplorer.bytecode;

/**
 * Defines the contract for assembler behavior in the com.tlcsdm.jvmexplorer.bytecode package.
 */
public interface Assembler {

	/**
	 * Assembles textual bytecode into a class file.
	 */
	byte[] assemble(String text) throws AssemblyException;

}
