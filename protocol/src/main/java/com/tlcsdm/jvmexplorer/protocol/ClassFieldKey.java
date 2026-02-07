package com.tlcsdm.jvmexplorer.protocol;

import com.tlcsdm.jvmexplorer.protocol.helper.ClassNameHelper;

import java.lang.reflect.Modifier;

public class ClassFieldKey {

	private final String className;
	private final String fieldName;
	private final String typeName;
	private final int modifiers;

	@Override
	public String toString() {
		return Modifier.toString(modifiers) + " " + typeName.replace("java.lang.", "") + " " + getSimpleName() + "."
		       + fieldName;
	}

	public String getSimpleName() {
		return ClassNameHelper.getSimpleName(className);
	}


	public ClassFieldKey(String className, String fieldName, String typeName, int modifiers) {
		this.className = className;
		this.fieldName = fieldName;
		this.typeName = typeName;
		this.modifiers = modifiers;
	}

	public ClassFieldKey() {
		this.className = null;
		this.fieldName = null;
		this.typeName = null;
		this.modifiers = 0;
	}

	public String getClassName() {
		return this.className;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public String getTypeName() {
		return this.typeName;
	}

	public int getModifiers() {
		return this.modifiers;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassFieldKey other = (ClassFieldKey) o;
		return java.util.Objects.equals(this.className, other.className) && java.util.Objects.equals(this.fieldName, other.fieldName) && java.util.Objects.equals(this.typeName, other.typeName) && java.util.Objects.equals(this.modifiers, other.modifiers);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(className, fieldName, typeName, modifiers);
	}

}
