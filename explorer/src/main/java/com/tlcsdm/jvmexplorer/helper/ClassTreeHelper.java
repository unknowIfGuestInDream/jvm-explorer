package com.tlcsdm.jvmexplorer.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlcsdm.jvmexplorer.fx.classes.ClassTreeNode;
import com.tlcsdm.jvmexplorer.fx.classes.FilterableTreeItem;
import com.tlcsdm.jvmexplorer.protocol.ClassLoaderDescriptor;
import com.tlcsdm.jvmexplorer.protocol.LoadedClass;
import javafx.scene.control.TreeItem;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides the class tree helper implementation used by the com.tlcsdm.jvmexplorer.helper package.
 */
public class ClassTreeHelper {

	private static final Logger log = LoggerFactory.getLogger(ClassTreeHelper.class);


	/**
	 * Builds the configured result object.
	 */
	public ClassTreeNode buildClassLoaderTree(List<LoadedClass> loadedClasses) {
		final ClassTreeNode classTreeRoot = ClassTreeNode.root();
		for (LoadedClass loadedClass : loadedClasses) {
			ClassTreeNode classRoot = classTreeRoot;
			if (loadedClass.getClassLoaderDescriptor() != null) {
				classRoot = addClassLoader(loadedClass, classTreeRoot);
			}
			addClass(loadedClass, classRoot);
		}
		return classTreeRoot;
	}

	/**
	 * Handles the add class loader workflow.
	 */
	private ClassTreeNode addClassLoader(LoadedClass loadedClass, ClassTreeNode treeRoot) {
		ClassTreeNode classLoaderTree = treeRoot;
		final List<ClassLoaderDescriptor> classLoaders = Stream.iterate(loadedClass.getClassLoaderDescriptor(),
		                                                                Objects::nonNull,
		                                                                ClassLoaderDescriptor::getParent)
		                                                       .collect(Collectors.toCollection(ArrayList::new));
		Collections.reverse(classLoaders);

		for (ClassLoaderDescriptor classLoaderDescriptor : classLoaders) {
			classLoaderTree = classLoaderTree.addClassLoader(classLoaderDescriptor);
		}

		return classLoaderTree;
	}

	/**
	 * Handles the add class workflow.
	 */
	private void addClass(LoadedClass loadedClass, ClassTreeNode classRoot) {
		final String[] classNameParts = loadedClass.getName().split("\\.");
		ClassTreeNode classTree = classRoot;
		for (int i = 0; i < classNameParts.length - 1; i++) {
			final String classNamePart = classNameParts[i];
			classTree = classTree.addPackage(classNamePart);
		}
		classTree.addClass(loadedClass);
	}

	/**
	 * Builds the configured result object.
	 */
	public ClassTreeNode buildClassTree(List<LoadedClass> loadedClasses) {
		final ClassTreeNode classTreeRoot = ClassTreeNode.root();
		for (LoadedClass loadedClass : loadedClasses) {
			addClass(loadedClass, classTreeRoot);
		}
		return classTreeRoot;
	}

	public List<LoadedClass> getLoadedClassScope(FilterableTreeItem<ClassTreeNode> classesTreeRoot,
	                                             TreeItem<ClassTreeNode> classLoaderNode) {
		if (classLoaderNode == null) {
			return classesTreeRoot.streamSource()
			                      .filter(p -> p.getType() == ClassTreeNode.Type.CLASS)
			                      .map(ClassTreeNode::getLoadedClass)
			                      .collect(Collectors.toList());
		}
		// Include the class tree root to account for bootstrap class loader which doesn't show up here
		return Stream.concat(getNodeClassLoaderTreeItemStream(classLoaderNode), Stream.of(classesTreeRoot))
		             .map(this::getClassesLoadedIn)
		             .flatMap(List::stream)
		             .collect(Collectors.toList());
	}

	/**
	 * Returns the node class loader tree item stream value.
	 */
	public Stream<TreeItem<ClassTreeNode>> getNodeClassLoaderTreeItemStream(TreeItem<ClassTreeNode> treeItem) {
		return Stream.iterate(treeItem, o -> o != null && o.getValue() != null, TreeItem::getParent)
		             .filter(p -> p.getValue().getType() == ClassTreeNode.Type.CLASSLOADER);
	}

	// classLoaderNode can be the root/null value to indicate bootstrap class loader
	/**
	 * Returns the classes loaded in value.
	 */
	private List<LoadedClass> getClassesLoadedIn(TreeItem<ClassTreeNode> classLoaderNode) {
		final Queue<TreeItem<ClassTreeNode>> frontier = new ArrayDeque<>(getChildren(classLoaderNode));
		final List<LoadedClass> loadedClasses = new ArrayList<>();
		while (!frontier.isEmpty()) {
			final TreeItem<ClassTreeNode> next = frontier.poll();
			final ClassTreeNode nextNode = next.getValue();
			if (nextNode.getType() == ClassTreeNode.Type.CLASS) {
				loadedClasses.add(nextNode.getLoadedClass());
			}
			if (nextNode.getType() == ClassTreeNode.Type.PACKAGE) {
				frontier.addAll(getChildren(next));
			}
		}
		return loadedClasses;
	}

	/**
	 * Returns the children value.
	 */
	private List<TreeItem<ClassTreeNode>> getChildren(TreeItem<ClassTreeNode> treeItem) {
		if (treeItem instanceof FilterableTreeItem) {
			return ((FilterableTreeItem<ClassTreeNode>) treeItem).getSourceChildren();
		}
		log.warn("Trying to get children of regular tree node {}", treeItem);
		return treeItem.getChildren();
	}

	/**
	 * Returns the package name value.
	 */
	public String getPackageName(TreeItem<ClassTreeNode> packageNode) {
		final List<String> packageParts = Stream.iterate(packageNode,
		                                                 o -> o != null && o.getValue() != null
		                                                      && o.getValue().getType() == ClassTreeNode.Type.PACKAGE,
		                                                 TreeItem::getParent)
		                                        .map(item -> item.getValue().getPackageSegment())
		                                        .collect(Collectors.toCollection(ArrayList::new));
		Collections.reverse(packageParts);
		return String.join(".", packageParts);
	}

	// Note this includes all subpackages as well
	public List<LoadedClass> getClassesInPackage(FilterableTreeItem<ClassTreeNode> classesTreeRoot,
	                                             String fullPackageName, ClassLoaderDescriptor packageClassLoader) {
		return classesTreeRoot.streamVisible()
		                      .filter(p -> p.getType() == ClassTreeNode.Type.CLASS)
		                      .map(ClassTreeNode::getLoadedClass)
		                      .filter(c -> c.getName().startsWith(fullPackageName))
		                      .filter(c -> (packageClassLoader == null)
		                                   || packageClassLoader.equals(c.getClassLoaderDescriptor()))
		                      .collect(Collectors.toList());
	}

	/**
	 * Returns the node class loader value.
	 */
	public ClassLoaderDescriptor getNodeClassLoader(TreeItem<ClassTreeNode> treeItem) {
		final ClassTreeNode classTreeNode = getNodeClassLoaderNode(treeItem);
		return classTreeNode != null ? classTreeNode.getClassLoaderDescriptor() : null;
	}

	/**
	 * Returns the node class loader node value.
	 */
	public ClassTreeNode getNodeClassLoaderNode(TreeItem<ClassTreeNode> treeItem) {
		final TreeItem<ClassTreeNode> classTreeNode = getNodeClassLoaderTreeItem(treeItem);
		return classTreeNode != null ? classTreeNode.getValue() : null;
	}

	/**
	 * Returns the node class loader tree item value.
	 */
	public TreeItem<ClassTreeNode> getNodeClassLoaderTreeItem(TreeItem<ClassTreeNode> treeItem) {
		return getNodeClassLoaderTreeItemStream(treeItem).findFirst().orElse(null);
	}

}
