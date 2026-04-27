package com.tlcsdm.jvmexplorer.fx.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Provides the filterable tree item implementation used by the com.tlcsdm.jvmexplorer.fx.classes package.
 */
public class FilterableTreeItem<T> extends TreeItem<T> {

	private static final Logger log = LoggerFactory.getLogger(FilterableTreeItem.class);


	private final ObservableList<TreeItem<T>> sourceChildren = FXCollections.observableArrayList();
	private final ObjectProperty<Predicate<T>> predicate = new SimpleObjectProperty<>();

	// Do not convert this to a local variable. This is a field, so it doesn't get garbage collected.
	private final FilteredList<TreeItem<T>> filteredChildren = new FilteredList<>(sourceChildren);

	/**
	 * Creates a new FilterableTreeItem instance.
	 */
	public FilterableTreeItem() {
		this(null);
	}

	/**
	 * Creates a new FilterableTreeItem instance.
	 */
	public FilterableTreeItem(T value) {
		super(value);
		setupFilteredItemBinding();
	}

	/**
	 * Sets up filtered item binding.
	 */
	private void setupFilteredItemBinding() {
		filteredChildren.predicateProperty().bind(Bindings.createObjectBinding(() -> {
			// javac blows up if you inline this to a return
			final Predicate<TreeItem<T>> p = child -> {
				if (child instanceof FilterableTreeItem) {
					((FilterableTreeItem<T>) child).predicateProperty().set(predicate.get());
				}
				if (predicate.get() == null || !child.getChildren().isEmpty()) {
					return true;
				}
				return predicate.get().test(child.getValue());
			};
			return p;
		}, predicate));

		Bindings.bindContent(super.getChildren(), filteredChildren);
	}

	/**
	 * Returns the predicate property.
	 */
	public ObjectProperty<Predicate<T>> predicateProperty() {
		return predicate;
	}

	/**
	 * Handles the stream visible workflow.
	 */
	public Stream<T> streamVisible() {
		return streamVisibleItems().map(TreeItem::getValue);
	}

	/**
	 * Handles the stream visible items workflow.
	 */
	public Stream<FilterableTreeItem<T>> streamVisibleItems() {
		return bfs(FilterableTreeItem::getChildren);
	}

	// Assumes all children are also FilterableTreeItems
	// This does not include the current node
	/**
	 * Handles the bfs workflow.
	 */
	private Stream<FilterableTreeItem<T>> bfs(Function<FilterableTreeItem<T>, List<TreeItem<T>>> childFunction) {
		final Queue<Supplier<Stream<FilterableTreeItem<T>>>> generations = new LinkedList<>();
		generations.add(() -> childFunction.apply(this)
		                                   .stream()
		                                   .filter(FilterableTreeItem.class::isInstance)
		                                   .map(FilterableTreeItem.class::cast));
		return Stream.generate(generations::poll)
		             .takeWhile(Objects::nonNull)
		             .flatMap(Supplier::get)
		             .distinct()
		             .peek(n -> generations.add(() -> childFunction.apply(n)
		                                                           .stream()
		                                                           .filter(FilterableTreeItem.class::isInstance)
		                                                           .map(FilterableTreeItem.class::cast)));
	}

	/**
	 * Handles the stream source workflow.
	 */
	public Stream<T> streamSource() {
		return streamSourceItems().map(TreeItem::getValue);
	}

	/**
	 * Handles the stream source items workflow.
	 */
	public Stream<FilterableTreeItem<T>> streamSourceItems() {
		return bfs(FilterableTreeItem::getSourceChildren);
	}

	/**
	 * Returns the source children value.
	 */
	public ObservableList<TreeItem<T>> getSourceChildren() {
		return sourceChildren;
	}

}