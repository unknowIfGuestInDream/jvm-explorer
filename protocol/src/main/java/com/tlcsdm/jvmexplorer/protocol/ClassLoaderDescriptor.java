package com.tlcsdm.jvmexplorer.protocol;

public class ClassLoaderDescriptor {

	private final String id;

	private final String simpleClassName;
	private final String description;

	private final ClassLoaderDescriptor parent;

	@Override
	public String toString() {
		return description;
	}


	public ClassLoaderDescriptor(String id, String simpleClassName, String description, ClassLoaderDescriptor parent) {
		this.id = id;
		this.simpleClassName = simpleClassName;
		this.description = description;
		this.parent = parent;
	}

	public ClassLoaderDescriptor() {
		this.id = null;
		this.simpleClassName = null;
		this.description = null;
		this.parent = null;
	}

	public String getId() {
		return this.id;
	}

	public String getSimpleClassName() {
		return this.simpleClassName;
	}

	public String getDescription() {
		return this.description;
	}

	public ClassLoaderDescriptor getParent() {
		return this.parent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassLoaderDescriptor other = (ClassLoaderDescriptor) o;
		return java.util.Objects.equals(this.id, other.id) && java.util.Objects.equals(this.simpleClassName, other.simpleClassName) && java.util.Objects.equals(this.description, other.description) && java.util.Objects.equals(this.parent, other.parent);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(id, simpleClassName, description, parent);
	}

	public static ClassLoaderDescriptorBuilder builder() {
		return new ClassLoaderDescriptorBuilder();
	}

	public static class ClassLoaderDescriptorBuilder {
		private String id;
		private String simpleClassName;
		private String description;
		private ClassLoaderDescriptor parent;

		public ClassLoaderDescriptorBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ClassLoaderDescriptorBuilder simpleClassName(String simpleClassName) {
			this.simpleClassName = simpleClassName;
			return this;
		}

		public ClassLoaderDescriptorBuilder description(String description) {
			this.description = description;
			return this;
		}

		public ClassLoaderDescriptorBuilder parent(ClassLoaderDescriptor parent) {
			this.parent = parent;
			return this;
		}

		public ClassLoaderDescriptor build() {
			return new ClassLoaderDescriptor(id, simpleClassName, description, parent);
		}
	}

}
