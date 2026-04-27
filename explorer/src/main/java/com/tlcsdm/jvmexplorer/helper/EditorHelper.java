package com.tlcsdm.jvmexplorer.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

/**
 * Provides the editor helper implementation used by the com.tlcsdm.jvmexplorer.helper package.
 */
public class EditorHelper {

	private static final Logger log = LoggerFactory.getLogger(EditorHelper.class);


	/**
	 * Returns the object string value.
	 */
	public String getObjectString(String type, Object object) {
		try {
			final Class<?> klass = parseType(type);
			return new Gson().toJson(object, klass);
		}
		catch (Exception e) {
			log.warn("Failed to get value: {}, {}", type, object, e);
			return null;
		}
	}

	/**
	 * Parses type.
	 */
	private static Class<?> parseType(String className) throws ClassNotFoundException {
		switch (className) {
		case "boolean":
			return boolean.class;
		case "byte":
			return byte.class;
		case "short":
			return short.class;
		case "int":
			return int.class;
		case "long":
			return long.class;
		case "float":
			return float.class;
		case "double":
			return double.class;
		case "char":
			return char.class;
		case "void":
			return void.class;
		default:
			return Class.forName(className);
		}
	}

	/**
	 * Handles the edit request.
	 */
	public Object edit(String type, String newValue) {
		try {
			final Class<?> klass = parseType(type);
			return new Gson().fromJson(newValue, klass);
		}
		catch (Exception e) {
			log.warn("Failed to edit value: {}, {}", type, newValue, e);
			return null;
		}
	}

}
