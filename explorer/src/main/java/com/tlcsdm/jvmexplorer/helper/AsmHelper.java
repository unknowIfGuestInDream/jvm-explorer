package com.tlcsdm.jvmexplorer.helper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/**
 * Provides the asm helper implementation used by the com.tlcsdm.jvmexplorer.helper package.
 */
public class AsmHelper {

	/**
	 * Parses the supplied value.
	 */
	public static ClassNode parse(byte[] classFile) {
		return parse(classFile, 0);
	}

	/**
	 * Parses the supplied value.
	 */
	public static ClassNode parse(byte[] classFile, int parsingOptions) {
		final ClassReader classReader = new ClassReader(classFile);
		final ClassNode classNode = new ClassNode();
		classReader.accept(classNode, parsingOptions);
		return classNode;
	}

	/**
	 * Parses the supplied value.
	 */
	public static byte[] parse(int flags, ClassNode classNode) {
		final ClassWriter classWriter = new ClassWriter(null, flags);
		classNode.accept(classWriter);
		return classWriter.toByteArray();
	}

}
