package com.tlcsdm.jvmexplorer.protocol;

import com.esotericsoftware.minlog.Log;
import com.tlcsdm.jvmexplorer.protocol.helper.ClassNameHelper;

import java.lang.reflect.Modifier;

public class LoadedClass implements Comparable<LoadedClass> {

	private final String name;
	private final ClassLoaderDescriptor classLoaderDescriptor;

	// Not used for equality or anything. Simply additional (optional) information.
	private final MetaType metaType;

	public String getSimpleName() {
		return ClassNameHelper.getSimpleName(name);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(LoadedClass o) {
		return name.compareTo(o.name);
	}

	public enum MetaType {
		INNER, INTERFACE, ABSTRACT, ENUM, ANNOTATION, EXCEPTION, ABSTRACT_EXCEPTION, ANONYMOUS;

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


	public LoadedClass(String name, ClassLoaderDescriptor classLoaderDescriptor, MetaType metaType) {
		this.name = name;
		this.classLoaderDescriptor = classLoaderDescriptor;
		this.metaType = metaType;
	}


	public LoadedClass() {
	}

	public String getName() {
		return this.name;
	}

	public ClassLoaderDescriptor getClassLoaderDescriptor() {
		return this.classLoaderDescriptor;
	}

	public MetaType getMetaType() {
		return this.metaType;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LoadedClass other = (LoadedClass) o;
		return java.util.Objects.equals(this.name, other.name) && java.util.Objects.equals(this.classLoaderDescriptor, other.classLoaderDescriptor) && java.util.Objects.equals(this.metaType, other.metaType);
	}


	@Override
	public int hashCode() {
		return java.util.Objects.hash(name, classLoaderDescriptor, metaType);
	}

}
