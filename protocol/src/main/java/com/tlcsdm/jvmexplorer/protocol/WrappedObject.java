package com.tlcsdm.jvmexplorer.protocol;

public class WrappedObject {

	private final String objectDescription;

	@Override
	public String toString() {
		return String.valueOf(objectDescription);
	}


	public WrappedObject(String objectDescription) {
		this.objectDescription = objectDescription;
	}

	public WrappedObject() {
		this.objectDescription = null;
	}

	public String getObjectDescription() {
		return this.objectDescription;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WrappedObject other = (WrappedObject) o;
		return java.util.Objects.equals(this.objectDescription, other.objectDescription);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(objectDescription);
	}

}
