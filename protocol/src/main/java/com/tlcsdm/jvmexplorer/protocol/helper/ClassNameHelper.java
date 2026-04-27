package com.tlcsdm.jvmexplorer.protocol.helper;

/**
 * Provides the class name helper implementation used by the com.tlcsdm.jvmexplorer.protocol.helper package.
 */
public class ClassNameHelper {

	/**
	 * Returns the simple name value.
	 */
	public static String getSimpleName(String name) {
		final int lastIndex = name.lastIndexOf('.');
		if (lastIndex == name.length() - 1) {
			// Somehow the '.' is the last name. Should never happen but let's be safe.
			return "";
		}
		return name.substring(lastIndex + 1);
	}

	/**
	 * Returns the package name value.
	 */
	public static String getPackageName(String name) {
		final int lastIndex = name.lastIndexOf('.');
		if (lastIndex == -1) {
			return "";
		}
		return name.substring(0, lastIndex);
	}

}
