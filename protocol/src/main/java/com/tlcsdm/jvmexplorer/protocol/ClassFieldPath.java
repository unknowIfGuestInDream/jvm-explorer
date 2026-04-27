package com.tlcsdm.jvmexplorer.protocol;

import java.util.Arrays;

/**
 * Provides the class field path implementation used by the com.tlcsdm.jvmexplorer.protocol package.
 */
public class ClassFieldPath {

	private final ClassFieldKey[] classFieldKeys;
	private final ClassLoaderDescriptor classLoaderDescriptor;


	/**
	 * Creates a new ClassFieldPath instance.
	 */
	public ClassFieldPath(ClassFieldKey[] classFieldKeys, ClassLoaderDescriptor classLoaderDescriptor) {
		this.classFieldKeys = classFieldKeys;
		this.classLoaderDescriptor = classLoaderDescriptor;
	}

	/**
	 * Creates a new ClassFieldPath instance.
	 */
	public ClassFieldPath() {
		this.classFieldKeys = null;
		this.classLoaderDescriptor = null;
	}

	/**
	 * Returns the class field keys value.
	 */
	public ClassFieldKey[] getClassFieldKeys() {
		return this.classFieldKeys;
	}

	/**
	 * Returns the class loader descriptor value.
	 */
	public ClassLoaderDescriptor getClassLoaderDescriptor() {
		return this.classLoaderDescriptor;
	}

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return "ClassFieldPath(classFieldKeys=" + Arrays.toString(classFieldKeys) + ", classLoaderDescriptor=" + classLoaderDescriptor + ")";
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassFieldPath other = (ClassFieldPath) o;
		return Arrays.equals(this.classFieldKeys, other.classFieldKeys) && java.util.Objects.equals(this.classLoaderDescriptor, other.classLoaderDescriptor);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(Arrays.hashCode(classFieldKeys), classLoaderDescriptor);
	}

}
