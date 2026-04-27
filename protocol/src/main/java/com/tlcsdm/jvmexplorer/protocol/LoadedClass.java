package com.tlcsdm.jvmexplorer.protocol;

import com.esotericsoftware.minlog.Log;
import com.tlcsdm.jvmexplorer.protocol.helper.ClassNameHelper;

import java.lang.reflect.Modifier;

/**
 * Provides the loaded class implementation used by the com.tlcsdm.jvmexplorer.protocol package.
 */
public class LoadedClass implements Comparable<LoadedClass> {

	private final String name;
	private final ClassLoaderDescriptor classLoaderDescriptor;

	// Not used for equality or anything. Simply additional (optional) information.
	private final MetaType metaType;

	/**
	 * Returns the simple name value.
	 */
	public String getSimpleName() {
		return ClassNameHelper.getSimpleName(name);
	}

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Performs the compare to operation.
	 */
	@Override
	public int compareTo(LoadedClass o) {
		return name.compareTo(o.name);
	}

	/**
	 * Enumerates the supported meta type values used by the com.tlcsdm.jvmexplorer.protocol package.
	 */
	public enum MetaType {
		INNER, INTERFACE, ABSTRACT, ENUM, ANNOTATION, EXCEPTION, ABSTRACT_EXCEPTION, ANONYMOUS;

		/**
		 * Returns the for value.
		 */
		public static MetaType getFor(final Class<?> c) {
			try {
				if (c.isAnonymousClass()) {
					return ANONYMOUS;
				}
				else if (c.isEnum()) {
					return ENUM;
				}
				else if (c.isInterface()) {
					return INTERFACE;
				}
				else if (c.isAnnotation()) {
					return ANNOTATION;
				}
				else if (Exception.class.isAssignableFrom(c)) {
					if (Modifier.isAbstract(c.getModifiers())) {
						return ABSTRACT_EXCEPTION;
					}
					return EXCEPTION;
				}
				else if (Modifier.isAbstract(c.getModifiers())) {
					return ABSTRACT;
				}
				else if (c.getEnclosingClass() != null) {
					return INNER;
				}
			}
			catch (Throwable t) {
				// Likely failed to load a dependent class (such as inner class)
				Log.debug("Failed to get MetaType for " + c + ": " + t.getClass() + " " + t.getMessage());
			}
			return null;
		}
	}


	/**
	 * Loads ed class.
	 */
	public LoadedClass(String name, ClassLoaderDescriptor classLoaderDescriptor, MetaType metaType) {
		this.name = name;
		this.classLoaderDescriptor = classLoaderDescriptor;
		this.metaType = metaType;
	}

	/**
	 * Loads ed class.
	 */
	public LoadedClass() {
		this.name = null;
		this.classLoaderDescriptor = null;
		this.metaType = null;
	}

	/**
	 * Returns the name value.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the class loader descriptor value.
	 */
	public ClassLoaderDescriptor getClassLoaderDescriptor() {
		return this.classLoaderDescriptor;
	}

	/**
	 * Returns the meta type value.
	 */
	public MetaType getMetaType() {
		return this.metaType;
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LoadedClass other = (LoadedClass) o;
		return java.util.Objects.equals(this.name, other.name) && java.util.Objects.equals(this.classLoaderDescriptor, other.classLoaderDescriptor);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(name, classLoaderDescriptor);
	}

}
