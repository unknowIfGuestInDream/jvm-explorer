package com.tlcsdm.jvmexplorer.bytecode;

public interface Assembler {

	byte[] assemble(String text) throws AssemblyException;

}
