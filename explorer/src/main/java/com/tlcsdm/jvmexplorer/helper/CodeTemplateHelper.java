package com.tlcsdm.jvmexplorer.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Objects;

/**
 * Provides the code template helper implementation used by the com.tlcsdm.jvmexplorer.helper package.
 */
public class CodeTemplateHelper {

	private static final Logger log = LoggerFactory.getLogger(CodeTemplateHelper.class);


	/**
	 * Loads modify method.
	 */
	public String loadModifyMethod(String className, String methodDescription, String code) {
		final String template = loadTemplate("modify-method-template.txt");
		return template.replace("<class-name>", className)
		               .replace("<method-desc>", methodDescription)
		               .replace("<code>", code)
		               .strip();
	}

	/**
	 * Loads template.
	 */
	private String loadTemplate(String path) {
		try (var inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
			final byte[] templateBytes = Objects.requireNonNull(inputStream).readAllBytes();
			return new String(templateBytes);
		}
		catch (IOException e) {
			log.error("Failed to load template resource {}", path, e);
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Loads remote callable.
	 */
	public String loadRemoteCallable(String packageName, String className) {
		final String template = loadTemplate("remote-code-template.txt");
		final String templateWithPackage = addPackage(template, packageName);
		final String templateWithClass = addClassName(templateWithPackage, className);
		return templateWithClass.strip();
	}

	/**
	 * Handles the add package workflow.
	 */
	private String addPackage(String template, String packageName) {
		final String packageStatement =
				packageName != null && !packageName.isEmpty() ? ("package " + packageName + ";") : "";
		return replace(template, "package", packageStatement);
	}

	/**
	 * Handles the add class name workflow.
	 */
	private String addClassName(String template, String className) {
		return replace(template, "class-name", className);
	}

	/**
	 * Handles the replace workflow.
	 */
	private String replace(String template, String templateKey, String replacement) {
		return template.replaceFirst("<" + templateKey + ">", replacement);
	}

}
