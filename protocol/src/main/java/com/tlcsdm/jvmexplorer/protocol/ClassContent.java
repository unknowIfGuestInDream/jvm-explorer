package com.tlcsdm.jvmexplorer.protocol;

import java.util.Arrays;

/**
 * Provides the class content implementation used by the com.tlcsdm.jvmexplorer.protocol package.
 */
public class ClassContent {

	private final LoadedClass loadedClass;
	private final byte[] classContent;
	private final ClassFields classFields;


	/**
	 * Creates a new ClassContent instance.
	 */
	public ClassContent(LoadedClass loadedClass, byte[] classContent, ClassFields classFields) {
		this.loadedClass = loadedClass;
		this.classContent = classContent;
		this.classFields = classFields;
	}

	/**
	 * Creates a new ClassContent instance.
	 */
	public ClassContent() {
		this.loadedClass = null;
		this.classContent = null;
		this.classFields = null;
	}

	/**
	 * Returns the loaded class value.
	 */
	public LoadedClass getLoadedClass() {
		return this.loadedClass;
	}

	/**
	 * Returns the class content value.
	 */
	public byte[] getClassContent() {
		return this.classContent;
	}

	/**
	 * Returns the class fields value.
	 */
	public ClassFields getClassFields() {
		return this.classFields;
	}

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return "ClassContent(loadedClass=" + loadedClass + ", classContent=" + Arrays.toString(classContent) + ", classFields=" + classFields + ")";
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassContent other = (ClassContent) o;
		return java.util.Objects.equals(this.loadedClass, other.loadedClass) && Arrays.equals(this.classContent, other.classContent) && java.util.Objects.equals(this.classFields, other.classFields);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(loadedClass, Arrays.hashCode(classContent), classFields);
	}

}
