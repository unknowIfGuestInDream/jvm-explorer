package com.tlcsdm.jvmexplorer.fx.openclass;

import com.tlcsdm.jvmexplorer.JvmExplorer;
import com.tlcsdm.jvmexplorer.agent.RunningJvm;
import com.tlcsdm.jvmexplorer.helper.AlertHelper;
import com.tlcsdm.jvmexplorer.helper.ClipboardHelper;
import com.tlcsdm.jvmexplorer.helper.EditorHelper;
import com.tlcsdm.jvmexplorer.helper.FieldTreeHelper;
import com.tlcsdm.jvmexplorer.net.ClientHandler;
import com.tlcsdm.jvmexplorer.protocol.ClassContent;
import com.tlcsdm.jvmexplorer.protocol.ClassField;
import com.tlcsdm.jvmexplorer.protocol.ClassFieldKey;
import com.tlcsdm.jvmexplorer.protocol.ClassFieldPath;
import com.tlcsdm.jvmexplorer.protocol.ClassLoaderDescriptor;
import com.tlcsdm.jvmexplorer.protocol.WrappedObject;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

/**
 * Provides the class field cell factory implementation used by the com.tlcsdm.jvmexplorer.fx.openclass package.
 */
public class ClassFieldCellFactory implements Callback<TreeView<ClassField>, TreeCell<ClassField>> {

	private final FieldTreeHelper fieldTreeHelper = new FieldTreeHelper();

	private final EditorHelper editorHelper;
	private final ExecutorService executorService;
	private final ClientHandler clientHandler;
	private final ObjectProperty<RunningJvm> currentJvm;
	private final AlertHelper alertHelper;
	private final ObjectProperty<ClassContent> currentClass;

	/**
	 * Calls the wrapped operation and returns its result.
	 */
	@Override
	public TreeCell<ClassField> call(TreeView<ClassField> param) {
		final TreeCell<ClassField> cell = new TreeCell<>();
		setupContextMenu(cell);
		setupTextBinding(cell);
		setupImageBinding(cell);
		setupTooltipBinding(cell);
		return cell;
	}

	/**
	 * Sets up the context menu for field actions.
	 */
	private void setupContextMenu(TreeCell<ClassField> cell) {
		final ContextMenu rowContextMenu = new ContextMenu();
		final MenuItem editRow = new MenuItem(JvmExplorer.getBundle().getString("ctx.editValue"));
		editRow.setOnAction(e -> {
			final ClassField classField = cell.getItem();
			if (classField == null) {
				return;
			}
			final String currentValue = editorHelper.getObjectString(classField.getClassFieldKey().getTypeName(),
			                                                         classField.getValue());
			final TextInputDialog dialog = new TextInputDialog(currentValue);
			final ResourceBundle bundle = JvmExplorer.getBundle();
			dialog.setTitle(bundle.getString("dialog.updateField"));
			dialog.setHeaderText(bundle.getString("dialog.enterNewValue"));
			dialog.setContentText(null);
			dialog.initOwner(cell.getScene().getWindow());
			dialog.showAndWait().ifPresent(result -> {
				final RunningJvm selectedJvm = currentJvm.get();
				if (selectedJvm == null) {
					return;
				}
				edit(selectedJvm, cell.getTreeItem(), result);
			});
		});
		final MenuItem copyValue = new MenuItem(JvmExplorer.getBundle().getString("ctx.copyValue"));
		copyValue.setOnAction(e -> {
			final ClassField classField = cell.getItem();
			if (classField == null) {
				return;
			}
			final String stringValue = String.valueOf(classField.getValue());
			ClipboardHelper.copy(stringValue);
		});
		cell.itemProperty().addListener((obs, old, newv) -> {
			rowContextMenu.getItems().clear();
			if (newv != null) {
				if (!(newv.getValue() instanceof WrappedObject)) {
					rowContextMenu.getItems().addAll(editRow);
				}
				rowContextMenu.getItems().addAll(copyValue);
			}
		});
		cell.setContextMenu(rowContextMenu);
	}

	/**
	 * Sets up text property binding.
	 */
	private void setupTextBinding(TreeCell<ClassField> treeCell) {
		treeCell.textProperty()
		        .bind(Bindings.when(treeCell.itemProperty().isNotNull())
		                      .then(treeCell.itemProperty().asString())
		                      .otherwise(""));
	}

	/**
	 * Sets up image property binding.
	 */
	private void setupImageBinding(TreeCell<ClassField> treeCell) {
		treeCell.graphicProperty().bind(Bindings.createObjectBinding(() -> {
			final ClassField item = treeCell.getItem();
			if (item == null) {
				return null;
			}
			return new ImageView(getFieldType(item).getImage());
		}, treeCell.itemProperty()));
	}

	/**
	 * Sets up tooltip property binding.
	 */
	private void setupTooltipBinding(TreeCell<ClassField> treeCell) {
		final Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(treeCell.itemProperty().asString());
		treeCell.tooltipProperty()
		        .bind(Bindings.when(treeCell.itemProperty().isNotNull()).then(tooltip).otherwise((Tooltip) null));
	}

	/**
	 * Handles the edit request.
	 */
	private void edit(RunningJvm selectedJvm, TreeItem<ClassField> classField, String newValue) {
		final ClassFieldKey[] classFieldKeys = fieldTreeHelper.getClassFieldKeyPath(classField);
		final Object resultObject = editorHelper.edit(classField.getValue().getClassFieldKey().getTypeName(),
		                                              newValue);
		final ClassLoaderDescriptor currentClassLoader =
				currentClass.get().getLoadedClass().getClassLoaderDescriptor();
		final ClassFieldPath classFieldPath = new ClassFieldPath(classFieldKeys, currentClassLoader);
		executorService.submit(() -> {
			if (clientHandler.setField(selectedJvm, classFieldPath, resultObject)) {
				final ClassField updatedClassField = classField.getValue().withValue(resultObject);
				Platform.runLater(() -> classField.setValue(updatedClassField));
			}
			else {
				final ResourceBundle bundle = JvmExplorer.getBundle();
				Platform.runLater(() -> alertHelper.showError(bundle.getString("error.operationFailed"), bundle.getString("error.failedToChangeField")));
			}
		});
	}

	/**
	 * Returns the field type value.
	 */
	private FieldType getFieldType(ClassField classField) {
		if (Modifier.isStatic(classField.getClassFieldKey().getModifiers())) {
			if (Modifier.isFinal(classField.getClassFieldKey().getModifiers())) {
				return FieldType.CONSTANT;
			}
			return FieldType.STATIC;
		}
		else {
			return FieldType.INSTANCE;
		}
	}

	/**
	 * Enumerates the supported field type values used by the com.tlcsdm.jvmexplorer.fx.openclass package.
	 */
	private enum FieldType {
		STATIC("icons/static.png"), INSTANCE("icons/field.png"), CONSTANT("icons/constant.png");
		private final Image image;

		/**
		 * Creates a new FieldType value.
		 */
		FieldType(String imagePath) {
			image = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(imagePath)));
		}

		/**
		 * Returns the image value.
		 */
		public Image getImage() {
			return this.image;
		}
	}


	/**
	 * Handles the class field cell factory workflow.
	 */
	public ClassFieldCellFactory(EditorHelper editorHelper, ExecutorService executorService, ClientHandler clientHandler, ObjectProperty<RunningJvm> currentJvm, AlertHelper alertHelper, ObjectProperty<ClassContent> currentClass) {
		this.editorHelper = editorHelper;
		this.executorService = executorService;
		this.clientHandler = clientHandler;
		this.currentJvm = currentJvm;
		this.alertHelper = alertHelper;
		this.currentClass = currentClass;
	}

	/**
	 * Returns the editor helper value.
	 */
	public EditorHelper getEditorHelper() {
		return this.editorHelper;
	}

	/**
	 * Returns the executor service value.
	 */
	public ExecutorService getExecutorService() {
		return this.executorService;
	}

	/**
	 * Returns the client handler value.
	 */
	public ClientHandler getClientHandler() {
		return this.clientHandler;
	}

	/**
	 * Returns the current jvm value.
	 */
	public ObjectProperty<RunningJvm> getCurrentJvm() {
		return this.currentJvm;
	}

	/**
	 * Returns the alert helper value.
	 */
	public AlertHelper getAlertHelper() {
		return this.alertHelper;
	}

	/**
	 * Returns the current class value.
	 */
	public ObjectProperty<ClassContent> getCurrentClass() {
		return this.currentClass;
	}

}
