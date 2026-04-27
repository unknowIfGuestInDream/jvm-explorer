package com.tlcsdm.jvmexplorer.protocol;

import com.tlcsdm.jvmexplorer.protocol.helper.ClassNameHelper;

import java.lang.reflect.Modifier;

/**
 * Provides the class field key implementation used by the com.tlcsdm.jvmexplorer.protocol package.
 */
public class ClassFieldKey {

	private final String className;
	private final String fieldName;
	private final String typeName;
	private final int modifiers;

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return Modifier.toString(modifiers) + " " + typeName.replace("java.lang.", "") + " " + getSimpleName() + "."
		       + fieldName;
	}

	/**
	 * Returns the simple name value.
	 */
	public String getSimpleName() {
		return ClassNameHelper.getSimpleName(className);
	}


	/**
	 * Creates a new ClassFieldKey instance.
	 */
	public ClassFieldKey(String className, String fieldName, String typeName, int modifiers) {
		this.className = className;
		this.fieldName = fieldName;
		this.typeName = typeName;
		this.modifiers = modifiers;
	}

	/**
	 * Creates a new ClassFieldKey instance.
	 */
	public ClassFieldKey() {
		this.className = null;
		this.fieldName = null;
		this.typeName = null;
		this.modifiers = 0;
	}

	/**
	 * Returns the class name value.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Returns the field name value.
	 */
	public String getFieldName() {
		return this.fieldName;
	}

	/**
	 * Returns the type name value.
	 */
	public String getTypeName() {
		return this.typeName;
	}

	/**
	 * Returns the modifiers value.
	 */
	public int getModifiers() {
		return this.modifiers;
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassFieldKey other = (ClassFieldKey) o;
		return java.util.Objects.equals(this.className, other.className) && java.util.Objects.equals(this.fieldName, other.fieldName) && java.util.Objects.equals(this.typeName, other.typeName) && java.util.Objects.equals(this.modifiers, other.modifiers);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(className, fieldName, typeName, modifiers);
	}

}
