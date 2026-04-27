package com.tlcsdm.jvmexplorer.protocol;

/**
 * Provides the wrapped object implementation used by the com.tlcsdm.jvmexplorer.protocol package.
 */
public class WrappedObject {

	private final String objectDescription;

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return String.valueOf(objectDescription);
	}


	/**
	 * Creates a new WrappedObject instance.
	 */
	public WrappedObject(String objectDescription) {
		this.objectDescription = objectDescription;
	}

	/**
	 * Creates a new WrappedObject instance.
	 */
	public WrappedObject() {
		this.objectDescription = null;
	}

	/**
	 * Returns the object description value.
	 */
	public String getObjectDescription() {
		return this.objectDescription;
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WrappedObject other = (WrappedObject) o;
		return java.util.Objects.equals(this.objectDescription, other.objectDescription);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(objectDescription);
	}

}
