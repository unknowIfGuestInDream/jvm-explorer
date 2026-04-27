package com.tlcsdm.jvmexplorer.agent;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * Provides the class file save transformer implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
public class ClassFileSaveTransformer implements ClassFileTransformer {

	private final String className;

		private byte[] bytes;

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
	                        ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		if (className.replace('/', '.').equals(this.className)) {
			bytes = classfileBuffer;
		}
		return null;
	}


	/**
	 * Creates a new ClassFileSaveTransformer instance.
	 */
	public ClassFileSaveTransformer(String className) {
		this.className = className;
	}

	/**
	 * Returns the class name value.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Returns the bytes value.
	 */
	public byte[] getBytes() {
		return this.bytes;
	}

}
