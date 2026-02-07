package com.tlcsdm.jvmexplorer.protocol.helper;

public class ClassNameHelper {

	public static String getSimpleName(String name) {
		final int lastIndex = name.lastIndexOf('.');
		if (lastIndex == name.length() - 1) {
			// Somehow the '.' is the last name. Should never happen but let's be safe.
			return "";
		}
		return name.substring(lastIndex + 1);
	}

	public static String getPackageName(String name) {
		final int lastIndex = name.lastIndexOf('.');
		if (lastIndex == -1) {
			return "";
		}
		return name.substring(0, lastIndex);
	}

}
