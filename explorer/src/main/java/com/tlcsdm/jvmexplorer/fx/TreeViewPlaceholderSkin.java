package com.tlcsdm.jvmexplorer.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.layout.StackPane;

// Note: we are making an assumption that the tree view root is never null, and the tree view is empty if the root
// has no children
/**
 * Provides the tree view placeholder skin implementation used by the com.tlcsdm.jvmexplorer.fx package.
 */
public class TreeViewPlaceholderSkin<T> extends TreeViewSkin<T> {

	private static final Logger log = LoggerFactory.getLogger(TreeViewPlaceholderSkin.class);


	private final SimpleObjectProperty<Node> placeholderProperty;

	private StackPane placeholderRegion;

	/**
	 * Creates a new TreeViewPlaceholderSkin instance.
	 */
	public TreeViewPlaceholderSkin(TreeView<T> control) {
		super(control);
		placeholderProperty = new SimpleObjectProperty<>();
		placeholderProperty.addListener((obs, old, newv) -> {
			if (placeholderRegion != null) {
				placeholderRegion.getChildren().setAll(newv);
			}
		});
		registerChangeListener(Bindings.isEmpty(getSkinnable().getRoot().getChildren()),
		                       e -> updatePlaceholderSupport());
		updatePlaceholderSupport();
	}

	/**
	 * Performs the update placeholder support operation.
	 */
	private void updatePlaceholderSupport() {
		final boolean empty = isTreeEmpty();
		if (empty) {
			if (placeholderRegion == null) {
				placeholderRegion = new StackPane();
				placeholderRegion.getStyleClass().setAll("placeholder");
				getChildren().add(placeholderRegion);
				final Node placeholder = placeholderProperty.get();
				if (placeholder != null) {
					placeholderRegion.getChildren().setAll(placeholder);
				}
			}
		}
		getVirtualFlow().setVisible(!empty);
		if (placeholderRegion != null) {
			placeholderRegion.setVisible(empty);
		}
	}

	/**
	 * Returns whether tree empty is enabled or currently true.
	 */
	private boolean isTreeEmpty() {
		return getSkinnable().getRoot().getChildren().isEmpty();
	}

	/**
	 * Performs the placeholder property operation.
	 */
	public Property<Node> placeholderProperty() {
		return placeholderProperty;
	}

	/**
	 * Performs the layout children operation.
	 */
	@Override
	protected void layoutChildren(double x, double y, double w, double h) {
		super.layoutChildren(x, y, w, h);
		if (placeholderRegion != null && placeholderRegion.isVisible()) {
			placeholderRegion.resizeRelocate(x, y, w, h);
		}
	}

}
