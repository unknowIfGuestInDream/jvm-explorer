package com.tlcsdm.jvmexplorer.protocol;

public class ClassFieldPath {

	private final ClassFieldKey[] classFieldKeys;
	private final ClassLoaderDescriptor classLoaderDescriptor;


	public ClassFieldPath(ClassFieldKey[] classFieldKeys, ClassLoaderDescriptor classLoaderDescriptor) {
		this.classFieldKeys = classFieldKeys;
		this.classLoaderDescriptor = classLoaderDescriptor;
	}

	public ClassFieldPath() {
		this.classFieldKeys = null;
		this.classLoaderDescriptor = null;
	}

	public ClassFieldKey[] getClassFieldKeys() {
		return this.classFieldKeys;
	}

	public ClassLoaderDescriptor getClassLoaderDescriptor() {
		return this.classLoaderDescriptor;
	}

	@Override
	public String toString() {
		return "ClassFieldPath(classFieldKeys=" + classFieldKeys + ", classLoaderDescriptor=" + classLoaderDescriptor + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassFieldPath other = (ClassFieldPath) o;
		return java.util.Objects.equals(this.classFieldKeys, other.classFieldKeys) && java.util.Objects.equals(this.classLoaderDescriptor, other.classLoaderDescriptor);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(classFieldKeys, classLoaderDescriptor);
	}

}
