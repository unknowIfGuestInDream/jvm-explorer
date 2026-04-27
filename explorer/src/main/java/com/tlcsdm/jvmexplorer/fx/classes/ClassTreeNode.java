package com.tlcsdm.jvmexplorer.fx.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlcsdm.jvmexplorer.protocol.ClassLoaderDescriptor;
import com.tlcsdm.jvmexplorer.protocol.LoadedClass;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides the class tree node implementation used by the com.tlcsdm.jvmexplorer.fx.classes package.
 */
public class ClassTreeNode implements Comparable<ClassTreeNode> {

	private static final Logger log = LoggerFactory.getLogger(ClassTreeNode.class);


	private final Map<String, ClassTreeNode> children = new HashMap<>();
	private final LoadedClass loadedClass;
	private final String packageSegment;
	private final ClassLoaderDescriptor classLoaderDescriptor;

	/**
	 * Handles the root workflow.
	 */
	public static ClassTreeNode root() {
		return new ClassTreeNode(null, null, null);
	}

	/**
	 * Converts this instance to tree item.
	 */
	public FilterableTreeItem<ClassTreeNode> toTreeItem() {
		final FilterableTreeItem<ClassTreeNode> treeItem = new FilterableTreeItem<>(this);
		children.forEach((key, value) -> treeItem.getSourceChildren().add(value.toTreeItem()));
		treeItem.getSourceChildren().sort(Comparator.comparing(TreeItem::getValue));
		return treeItem;
	}

	/**
	 * Handles the add package workflow.
	 */
	public ClassTreeNode addPackage(String name) {
		final String key = getKeyForPackage(name);
		return children.computeIfAbsent(key, k -> ClassTreeNode.ofPackage(name));
	}

	/**
	 * Returns the key for package value.
	 */
	private String getKeyForPackage(String packagePart) {
		return ClassTreeNode.Type.PACKAGE.name() + ":" + packagePart;
	}

	/**
	 * Handles the of package workflow.
	 */
	private static ClassTreeNode ofPackage(String packagePart) {
		return new ClassTreeNode(null, packagePart, null);
	}

	/**
	 * Handles the add class workflow.
	 */
	public ClassTreeNode addClass(LoadedClass loadedClass) {
		final String key = getKeyForClass(loadedClass);
		final ClassTreeNode classNode = ClassTreeNode.ofClass(loadedClass);
		final ClassTreeNode previous = children.put(key, classNode);
		if (previous != null) {
			log.warn("Loaded duplicate class: {}", loadedClass);
		}
		return classNode;
	}

	/**
	 * Returns the key for class value.
	 */
	private String getKeyForClass(LoadedClass loadedClass) {
		return ClassTreeNode.Type.CLASS.name() + ":" + loadedClass.getSimpleName();
	}

	/**
	 * Handles the of class workflow.
	 */
	private static ClassTreeNode ofClass(LoadedClass loadedClass) {
		return new ClassTreeNode(loadedClass, loadedClass.getSimpleName(), null);
	}

	/**
	 * Handles the add class loader workflow.
	 */
	public ClassTreeNode addClassLoader(ClassLoaderDescriptor classLoaderDescriptor) {
		final String key = getKeyForClassLoader(classLoaderDescriptor);
		return children.computeIfAbsent(key, k -> ClassTreeNode.ofClassLoader(classLoaderDescriptor));
	}

	/**
	 * Returns the key for class loader value.
	 */
	private String getKeyForClassLoader(ClassLoaderDescriptor classLoaderDescriptor) {
		return ClassTreeNode.Type.CLASSLOADER.name() + ":" + classLoaderDescriptor.getId();
	}

	/**
	 * Handles the of class loader workflow.
	 */
	private static ClassTreeNode ofClassLoader(ClassLoaderDescriptor classLoaderDescriptor) {
		return new ClassTreeNode(null, classLoaderDescriptor.getSimpleClassName(), classLoaderDescriptor);
	}

	// Mainly for debugging purposes
	/**
	 * Converts this instance to detailed string.
	 */
	public String toDetailedString() {
		return toDetailedString(0);
	}

	private Map<String, ClassTreeNode> getChildren() {
		return this.children;
	}

	/**
	 * Converts this instance to detailed string.
	 */
	private String toDetailedString(int indent) {
		final String nodeString = getType() + "-" + this;
		final String childrenString = getChildren().entrySet()
		                                           .stream()
		                                           .map(e -> e.getKey() + "-" + e.getValue()
		                                                                         .toDetailedString(indent + 1))
		                                           .collect(Collectors.joining(",\n" + "\t".repeat(indent + 1)));
		return nodeString + " [" + childrenString + "]";
	}

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return packageSegment;
	}

	/**
	 * Handles the compare to workflow.
	 */
	@Override
	public int compareTo(ClassTreeNode o) {
		// ClassLoader > Package > Class, then compare displayName
		return Comparator.<ClassTreeNode>comparingInt(node -> node.getType().ordinal())
		                 .thenComparing(ClassTreeNode::getPackageSegment)
		                 .compare(this, o);
	}

	/**
	 * Returns the type value.
	 */
	public Type getType() {
		if (loadedClass != null) {
			return Type.CLASS;
		}
		else if (classLoaderDescriptor != null) {
			return Type.CLASSLOADER;
		}
		else {
			return Type.PACKAGE;
		}
	}

	/**
	 * Enumerates the supported type values used by the com.tlcsdm.jvmexplorer.fx.classes package.
	 */
	public enum Type {
		CLASSLOADER("icons/classloader.png"), PACKAGE("icons/package.png"), CLASS("icons/class.png"),
		;
		private final String imagePath;

		/**
		 * Creates a new Type value.
		 */
		Type(String imagePath) {
			this.imagePath = imagePath;
		}

		private volatile Image image;

		/**
		 * Returns the image value.
		 */
		public Image getImage() {
			if (image == null) {
				synchronized (this) {
					if (image == null) {
						image = new Image(Objects.requireNonNull(getClass().getClassLoader()
						                                                   .getResourceAsStream(imagePath)));
					}
				}
			}
			return image;
		}
	}


	/**
	 * Handles the class tree node workflow.
	 */
	public ClassTreeNode(LoadedClass loadedClass, String packageSegment, ClassLoaderDescriptor classLoaderDescriptor) {
		this.loadedClass = loadedClass;
		this.packageSegment = packageSegment;
		this.classLoaderDescriptor = classLoaderDescriptor;
	}

	/**
	 * Handles the class tree node workflow.
	 */
	public ClassTreeNode() {
		this.loadedClass = null;
		this.packageSegment = null;
		this.classLoaderDescriptor = null;
	}

	/**
	 * Returns the loaded class value.
	 */
	public LoadedClass getLoadedClass() {
		return this.loadedClass;
	}

	/**
	 * Returns the package segment value.
	 */
	public String getPackageSegment() {
		return this.packageSegment;
	}

	/**
	 * Returns the class loader descriptor value.
	 */
	public ClassLoaderDescriptor getClassLoaderDescriptor() {
		return this.classLoaderDescriptor;
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassTreeNode other = (ClassTreeNode) o;
		return java.util.Objects.equals(this.loadedClass, other.loadedClass) && java.util.Objects.equals(this.packageSegment, other.packageSegment) && java.util.Objects.equals(this.classLoaderDescriptor, other.classLoaderDescriptor);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(loadedClass, packageSegment, classLoaderDescriptor);
	}

}
