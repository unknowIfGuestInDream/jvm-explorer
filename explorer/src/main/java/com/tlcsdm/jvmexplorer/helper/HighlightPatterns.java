package com.tlcsdm.jvmexplorer.helper;

import com.tlcsdm.jvmexplorer.protocol.helper.ClassNameHelper;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides the highlight patterns implementation used by the com.tlcsdm.jvmexplorer.helper package.
 */
class HighlightPatterns {

	private static final String[] KEYWORDS = new String[] { "abstract",
	                                                        "assert",
	                                                        "boolean",
	                                                        "break",
	                                                        "byte",
	                                                        "case",
	                                                        "catch",
	                                                        "char",
	                                                        "class",
	                                                        "const",
	                                                        "continue",
	                                                        "default",
	                                                        "do",
	                                                        "double",
	                                                        "else",
	                                                        "enum",
	                                                        "extends",
	                                                        "false",
	                                                        "final",
	                                                        "finally",
	                                                        "float",
	                                                        "for",
	                                                        "goto",
	                                                        "if",
	                                                        "implements",
	                                                        "import",
	                                                        "instanceof",
	                                                        "int",
	                                                        "interface",
	                                                        "long",
	                                                        "native",
	                                                        "new",
	                                                        "null",
	                                                        "package",
	                                                        "private",
	                                                        "protected",
	                                                        "public",
	                                                        "return",
	                                                        "short",
	                                                        "static",
	                                                        "strictfp",
	                                                        "super",
	                                                        "switch",
	                                                        "synchronized",
	                                                        "this",
	                                                        "throw",
	                                                        "throws",
	                                                        "transient",
	                                                        "true",
	                                                        "try",
	                                                        "void",
	                                                        "volatile",
	                                                        "while", };

	/**
	 * Creates field pattern.
	 */
	static String createFieldPattern(ClassNode classNode) {
		return classNode.fields.stream().map(fn -> {
			final String modifiers = Pattern.quote(Modifier.toString(Modifier.fieldModifiers() & fn.access));
			final String fieldType = getPatternForTypeDeclaration(Type.getType(fn.desc));
			final String fieldName = fn.name;
			final String fieldDefinitionHalf = Stream.of(modifiers, fieldType)
			                                         .filter(s -> !s.isEmpty())
			                                         .collect(Collectors.joining(" "));
			return "(?<=" + fieldDefinitionHalf + " )" + Pattern.quote(fieldName);
		}).collect(Collectors.joining("|"));
	}

	// Field type, method return type
	/**
	 * Returns the pattern for type declaration value.
	 */
	private static String getPatternForTypeDeclaration(Type type) {
		String fieldType = ClassNameHelper.getSimpleName(type.getClassName());
		if (fieldType.contains("$")) {
			// Handle inner classes
			fieldType = fieldType.substring(fieldType.lastIndexOf("$") + 1);
		}
		// We could also handle generics by checking the signature
		return Pattern.quote(fieldType);
	}

	/**
	 * Creates method pattern.
	 */
	static String createMethodPattern(ClassNode classNode) {
		return classNode.methods.stream().map(mn -> {
			final String modifiers = Pattern.quote(Modifier.toString(Modifier.methodModifiers() & mn.access));
			final String returnType = getPatternForTypeDeclaration(Type.getReturnType(mn.desc));
			String methodName = mn.name;
			final List<String> parts = new ArrayList<>();
			parts.add(modifiers);
			if (methodName.equals("<init>")) {
				methodName = ClassNameHelper.getSimpleName(classNode.name.replace('/', '.'));
			}
			else {
				parts.add(returnType);
			}
			final String methodDefinitionHalf = String.join(" ", parts);
			return "(?<=" + methodDefinitionHalf + " )" + Pattern.quote(methodName) + "(?=\\()";
		}).collect(Collectors.joining("|"));
	}

	/**
	 * Performs the of operation.
	 */
	static HighlightHelper.HighlightContext of(Map<String, String> patterns) {
		final Pattern pattern = compilePattern(patterns);
		return new HighlightHelper.HighlightContext(Set.copyOf(patterns.keySet()), pattern);
	}

	/**
	 * Compiles pattern.
	 */
	static Pattern compilePattern(Map<String, String> patternMap) {
		return Pattern.compile(patternMap.entrySet()
		                                 .stream()
		                                 .map(e -> "(?<" + e.getKey() + ">" + e.getValue() + ")")
		                                 .collect(Collectors.joining("|")));
	}

	static Map<String, String> getStaticPatterns() {
		final Map<String, String> patternMap = new LinkedHashMap<>();
		patternMap.put("keyword", "\\b(" + String.join("|", KEYWORDS) + ")\\b");
		patternMap.put("paren", "[()]");
		patternMap.put("brace", "[{}]");
		patternMap.put("bracket", "[\\[\\]]");
		patternMap.put("semicolon", ";");
		patternMap.put("string", "\"([^\"\\\\]|\\\\.)*\"");
		patternMap.put("comment", "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/");
		patternMap.put("annotation", "@[a-zA-Z\\d\\.]+");
		patternMap.put("number", "(?<=[^\\w$])\\d+");
		patternMap.put("label", "L\\d+"); // bytecode label
		patternMap.put("char", "'.+?'");
		patternMap.put("fieldref", "(?<=\\bthis\\.)[\\w]+(?![\\(\\w])");
		return patternMap;
	}

}
