package com.tlcsdm.jvmexplorer.helper;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.List;

/**
 * Provides the alert helper implementation used by the com.tlcsdm.jvmexplorer.helper package.
 */
public class AlertHelper {

	private final Stage ownerStage;

	/**
	 * Performs the show error operation.
	 */
	public void showError(String title, String headerText, Exception ex) {
		final Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(ex.getMessage());
		final Tooltip tooltip = new Tooltip(ex.getMessage());
		Tooltip.install(alert.getDialogPane(), tooltip);
		alert.initOwner(ownerStage);
		alert.showAndWait();
		Tooltip.uninstall(alert.getDialogPane(), tooltip);
	}

	/**
	 * Performs the show error operation.
	 */
	public void showError(String title, String headerText, Exception ex, List<String> list) {
		final Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(ex.getMessage());
		final Tooltip tooltip = new Tooltip(ex.getMessage());
		Tooltip.install(alert.getDialogPane(), tooltip);
		final ListView<String> stringListView = new ListView<>();
		stringListView.getItems().setAll(list);
		stringListView.getItems().sort(Comparator.naturalOrder());
		alert.getDialogPane().setExpandableContent(stringListView);
		alert.initOwner(ownerStage);
		alert.showAndWait();
		Tooltip.uninstall(alert.getDialogPane(), tooltip);
	}

	/**
	 * Performs the show expandable list operation.
	 */
	public void showExpandableList(String title, String headerText, String contentText, List<String> list) {
		final Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);
		final ListView<String> stringListView = new ListView<>();
		stringListView.getItems().setAll(list);
		stringListView.getItems().sort(Comparator.naturalOrder());
		alert.getDialogPane().setExpandableContent(stringListView);
		alert.initOwner(ownerStage);
		alert.showAndWait();
	}

	/**
	 * Performs the show observable info operation.
	 */
	public void showObservableInfo(ObservableValue<String> title, ObservableValue<String> headerText) {
		final Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Export In Progress");
		alert.titleProperty().bind(title);
		alert.headerTextProperty().bind(headerText);
		alert.setContentText(null);
		alert.initOwner(ownerStage);
		alert.showAndWait();
	}

	/**
	 * Performs the show operation.
	 */
	public void show(Alert.AlertType alertType, String titleText, String headerText) {
		final Alert alert = new Alert(alertType);
		alert.setTitle(titleText);
		alert.setHeaderText(headerText);
		alert.setContentText(null);
		alert.initOwner(ownerStage);
		alert.showAndWait();
	}

	/**
	 * Performs the show error operation.
	 */
	public void showError(String titleText, String headerText) {
		if (headerText != null && headerText.length() > 500) {
			headerText = headerText.substring(0, 500) + "...";
		}
		final Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(titleText);
		alert.setHeaderText(headerText);
		alert.setContentText(null);
		alert.initOwner(this.ownerStage);
		alert.showAndWait();
	}


	/**
	 * Creates a new AlertHelper instance.
	 */
	public AlertHelper(Stage ownerStage) {
		this.ownerStage = ownerStage;
	}

}
