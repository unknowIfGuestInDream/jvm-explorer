package com.tlcsdm.jvmexplorer.agent;

/**
 * Provides the single class loader implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
public class SingleClassLoader extends ClassLoader {

	private final String className;
	private final byte[] bytes;

	/**
	 * Creates a new SingleClassLoader instance.
	 */
	public SingleClassLoader(ClassLoader parent, String className, byte[] bytes) {
		super(parent);
		this.className = className;
		this.bytes = bytes;
	}

	/**
	 * Finds class.
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (!this.className.equals(name)) {
			return super.findClass(name);
		}
		final byte[] b = this.bytes;
		final int len = b.length;
		return defineClass(name, b, 0, len);
	}

}