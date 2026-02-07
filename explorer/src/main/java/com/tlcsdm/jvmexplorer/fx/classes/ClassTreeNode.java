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

public class ClassTreeNode implements Comparable<ClassTreeNode> {

	private static final Logger log = LoggerFactory.getLogger(ClassTreeNode.class);


	@Getter(AccessLevel.PRIVATE)
	private final Map<String, ClassTreeNode> children = new HashMap<>();
	private final LoadedClass loadedClass;
	private final String packageSegment;
	private final ClassLoaderDescriptor classLoaderDescriptor;

	public static ClassTreeNode root() {
		return new ClassTreeNode(null, null, null);
	}

	public FilterableTreeItem<ClassTreeNode> toTreeItem() {
		final FilterableTreeItem<ClassTreeNode> treeItem = new FilterableTreeItem<>(this);
		children.forEach((key, value) -> treeItem.getSourceChildren().add(value.toTreeItem()));
		treeItem.getSourceChildren().sort(Comparator.comparing(TreeItem::getValue));
		return treeItem;
	}

	public ClassTreeNode addPackage(String name) {
		final String key = getKeyForPackage(name);
		return children.computeIfAbsent(key, k -> ClassTreeNode.ofPackage(name));
	}

	private String getKeyForPackage(String packagePart) {
		return ClassTreeNode.Type.PACKAGE.name() + ":" + packagePart;
	}

	private static ClassTreeNode ofPackage(String packagePart) {
		return new ClassTreeNode(null, packagePart, null);
	}

	public ClassTreeNode addClass(LoadedClass loadedClass) {
		final String key = getKeyForClass(loadedClass);
		final ClassTreeNode classNode = ClassTreeNode.ofClass(loadedClass);
		final ClassTreeNode previous = children.put(key, classNode);
		if (previous != null) {
			log.warn("Loaded duplicate class: {}", loadedClass);
		}
		return classNode;
	}

	private String getKeyForClass(LoadedClass loadedClass) {
		return ClassTreeNode.Type.CLASS.name() + ":" + loadedClass.getSimpleName();
	}

	private static ClassTreeNode ofClass(LoadedClass loadedClass) {
		return new ClassTreeNode(loadedClass, loadedClass.getSimpleName(), null);
	}

	public ClassTreeNode addClassLoader(ClassLoaderDescriptor classLoaderDescriptor) {
		final String key = getKeyForClassLoader(classLoaderDescriptor);
		return children.computeIfAbsent(key, k -> ClassTreeNode.ofClassLoader(classLoaderDescriptor));
	}

	private String getKeyForClassLoader(ClassLoaderDescriptor classLoaderDescriptor) {
		return ClassTreeNode.Type.CLASSLOADER.name() + ":" + classLoaderDescriptor.getId();
	}

	private static ClassTreeNode ofClassLoader(ClassLoaderDescriptor classLoaderDescriptor) {
		return new ClassTreeNode(null, classLoaderDescriptor.getSimpleClassName(), classLoaderDescriptor);
	}

	// Mainly for debugging purposes
	public String toDetailedString() {
		return toDetailedString(0);
	}

	private String toDetailedString(int indent) {
		final String nodeString = getType() + "-" + this;
		final String childrenString = getChildren().entrySet()
		                                           .stream()
		                                           .map(e -> e.getKey() + "-" + e.getValue()
		                                                                         .toDetailedString(indent + 1))
		                                           .collect(Collectors.joining(",\n" + "\t".repeat(indent + 1)));
		return nodeString + " [" + childrenString + "]";
	}

	@Override
	public String toString() {
		return packageSegment;
	}

	@Override
	public int compareTo(ClassTreeNode o) {
		// ClassLoader > Package > Class, then compare displayName
		return Comparator.<ClassTreeNode>comparingInt(node -> node.getType().ordinal())
		                 .thenComparing(ClassTreeNode::getPackageSegment)
		                 .compare(this, o);
	}

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

		public enum Type {
		CLASSLOADER("icons/classloader.png"), PACKAGE("icons/package.png"), CLASS("icons/class.png"),
		;
		private final String imagePath;
		private volatile Image image;

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


	public ClassTreeNode(Map<String, ClassTreeNode> children, LoadedClass loadedClass, String packageSegment, ClassLoaderDescriptor classLoaderDescriptor, String imagePath, volatile Image image) {
		this.children = children;
		this.loadedClass = loadedClass;
		this.packageSegment = packageSegment;
		this.classLoaderDescriptor = classLoaderDescriptor;
		this.imagePath = imagePath;
		this.image = image;
	}


	public ClassTreeNode() {
	}

	public Map<String, ClassTreeNode> getChildren() {
		return this.children;
	}

	public LoadedClass getLoadedClass() {
		return this.loadedClass;
	}

	public String getPackageSegment() {
		return this.packageSegment;
	}

	public ClassLoaderDescriptor getClassLoaderDescriptor() {
		return this.classLoaderDescriptor;
	}

	public String getImagePath() {
		return this.imagePath;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassTreeNode other = (ClassTreeNode) o;
		return java.util.Objects.equals(this.children, other.children) && java.util.Objects.equals(this.loadedClass, other.loadedClass) && java.util.Objects.equals(this.packageSegment, other.packageSegment) && java.util.Objects.equals(this.classLoaderDescriptor, other.classLoaderDescriptor) && java.util.Objects.equals(this.imagePath, other.imagePath) && java.util.Objects.equals(this.image, other.image);
	}


	@Override
	public int hashCode() {
		return java.util.Objects.hash(children, loadedClass, packageSegment, classLoaderDescriptor, imagePath, image);
	}

}
