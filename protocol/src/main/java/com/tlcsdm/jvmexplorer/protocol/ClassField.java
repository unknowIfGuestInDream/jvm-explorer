package com.tlcsdm.jvmexplorer.protocol;

import com.tlcsdm.jvmexplorer.protocol.helper.FieldValueHelper;

/**
 * Provides the class field implementation used by the com.tlcsdm.jvmexplorer.protocol package.
 */
public class ClassField {

	private final ClassFieldKey classFieldKey;

	private final Object value;

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return classFieldKey + " = " + FieldValueHelper.getValueAsString(value).replace("\n", "");
	}


	/**
	 * Creates a new ClassField instance.
	 */
	public ClassField(ClassFieldKey classFieldKey, Object value) {
		this.classFieldKey = classFieldKey;
		this.value = value;
	}

	/**
	 * Creates a new ClassField instance.
	 */
	public ClassField() {
		this.classFieldKey = null;
		this.value = null;
	}

	/**
	 * Returns the class field key value.
	 */
	public ClassFieldKey getClassFieldKey() {
		return this.classFieldKey;
	}

	/**
	 * Returns the value value.
	 */
	public Object getValue() {
		return this.value;
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassField other = (ClassField) o;
		return java.util.Objects.equals(this.classFieldKey, other.classFieldKey) && java.util.Objects.equals(this.value, other.value);
	}

	/**
	 * Handles the with value workflow.
	 */
	public ClassField withValue(Object value) {
		return new ClassField(this.classFieldKey, value);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(classFieldKey, value);
	}

}
