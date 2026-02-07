package com.tlcsdm.jvmexplorer.protocol;

import java.util.Arrays;

public class ClassFields {

	private final ClassField[] fields;


	public ClassFields(ClassField[] fields) {
		this.fields = fields;
	}

	public ClassFields() {
		this.fields = null;
	}

	public ClassField[] getFields() {
		return this.fields;
	}

	@Override
	public String toString() {
		return "ClassFields(fields=" + Arrays.toString(fields) + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassFields other = (ClassFields) o;
		return Arrays.equals(this.fields, other.fields);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(fields);
	}

}
