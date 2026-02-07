package com.tlcsdm.jvmexplorer.protocol;

import com.tlcsdm.jvmexplorer.protocol.helper.FieldValueHelper;

public class ClassField {

	private final ClassFieldKey classFieldKey;

	private final Object value;

	@Override
	public String toString() {
		return classFieldKey + " = " + FieldValueHelper.getValueAsString(value).replace("\n", "");
	}


	public ClassField(ClassFieldKey classFieldKey, Object value) {
		this.classFieldKey = classFieldKey;
		this.value = value;
	}

	public ClassField() {
		this.classFieldKey = null;
		this.value = null;
	}

	public ClassFieldKey getClassFieldKey() {
		return this.classFieldKey;
	}

	public Object getValue() {
		return this.value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassField other = (ClassField) o;
		return java.util.Objects.equals(this.classFieldKey, other.classFieldKey) && java.util.Objects.equals(this.value, other.value);
	}

	public ClassField withValue(Object value) {
		return new ClassField(this.classFieldKey, value);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(classFieldKey, value);
	}

}
