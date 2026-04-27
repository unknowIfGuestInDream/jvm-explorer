package com.tlcsdm.jvmexplorer.protocol;

/**
 * Provides the class loader descriptor implementation used by the com.tlcsdm.jvmexplorer.protocol package.
 */
public class ClassLoaderDescriptor {

	private final String id;

	private final String simpleClassName;
	private final String description;

	private final ClassLoaderDescriptor parent;

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return description;
	}


	/**
	 * Creates a new ClassLoaderDescriptor instance.
	 */
	public ClassLoaderDescriptor(String id, String simpleClassName, String description, ClassLoaderDescriptor parent) {
		this.id = id;
		this.simpleClassName = simpleClassName;
		this.description = description;
		this.parent = parent;
	}

	/**
	 * Creates a new ClassLoaderDescriptor instance.
	 */
	public ClassLoaderDescriptor() {
		this.id = null;
		this.simpleClassName = null;
		this.description = null;
		this.parent = null;
	}

	/**
	 * Returns the id value.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Returns the simple class name value.
	 */
	public String getSimpleClassName() {
		return this.simpleClassName;
	}

	/**
	 * Returns the description value.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Returns the parent value.
	 */
	public ClassLoaderDescriptor getParent() {
		return this.parent;
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassLoaderDescriptor other = (ClassLoaderDescriptor) o;
		return java.util.Objects.equals(this.id, other.id) && java.util.Objects.equals(this.simpleClassName, other.simpleClassName) && java.util.Objects.equals(this.description, other.description) && java.util.Objects.equals(this.parent, other.parent);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(id, simpleClassName, description, parent);
	}

	/**
	 * Builds the configured result object.
	 */
	public static ClassLoaderDescriptorBuilder builder() {
		return new ClassLoaderDescriptorBuilder();
	}

	/**
	 * Provides the class loader descriptor builder implementation used by the com.tlcsdm.jvmexplorer.protocol package.
	 */
	public static class ClassLoaderDescriptorBuilder {
		private String id;
		private String simpleClassName;
		private String description;
		private ClassLoaderDescriptor parent;

		/**
		 * Performs the id operation.
		 */
		public ClassLoaderDescriptorBuilder id(String id) {
			this.id = id;
			return this;
		}

		/**
		 * Performs the simple class name operation.
		 */
		public ClassLoaderDescriptorBuilder simpleClassName(String simpleClassName) {
			this.simpleClassName = simpleClassName;
			return this;
		}

		/**
		 * Performs the description operation.
		 */
		public ClassLoaderDescriptorBuilder description(String description) {
			this.description = description;
			return this;
		}

		/**
		 * Performs the parent operation.
		 */
		public ClassLoaderDescriptorBuilder parent(ClassLoaderDescriptor parent) {
			this.parent = parent;
			return this;
		}

		/**
		 * Builds the configured result object.
		 */
		public ClassLoaderDescriptor build() {
			return new ClassLoaderDescriptor(id, simpleClassName, description, parent);
		}
	}

}
