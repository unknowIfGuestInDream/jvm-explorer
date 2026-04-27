package com.tlcsdm.jvmexplorer.bytecode;

/**
 * Defines the contract for bytecode textifier behavior in the com.tlcsdm.jvmexplorer.bytecode package.
 */
public interface BytecodeTextifier {

	/**
	 * Converts class bytes into a displayable text representation.
	 */
	String process(byte[] bytecode);

}
