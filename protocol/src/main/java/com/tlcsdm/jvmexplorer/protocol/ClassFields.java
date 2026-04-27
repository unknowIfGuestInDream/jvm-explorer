package com.tlcsdm.jvmexplorer.protocol;

import java.util.Arrays;

/**
 * Provides the class fields implementation used by the com.tlcsdm.jvmexplorer.protocol package.
 */
public class ClassFields {

	private final ClassField[] fields;


	/**
	 * Creates a new ClassFields instance.
	 */
	public ClassFields(ClassField[] fields) {
		this.fields = fields;
	}

	/**
	 * Creates a new ClassFields instance.
	 */
	public ClassFields() {
		this.fields = null;
	}

	/**
	 * Returns the fields value.
	 */
	public ClassField[] getFields() {
		return this.fields;
	}

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return "ClassFields(fields=" + Arrays.toString(fields) + ")";
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassFields other = (ClassFields) o;
		return Arrays.equals(this.fields, other.fields);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(fields);
	}

}
