package com.tlcsdm.jvmexplorer.agent;


import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

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


	public ClassFileSaveTransformer(String className) {
		this.className = className;
	}

	public String getClassName() {
		return this.className;
	}

	public byte[] getBytes() {
		return this.bytes;
	}

}
