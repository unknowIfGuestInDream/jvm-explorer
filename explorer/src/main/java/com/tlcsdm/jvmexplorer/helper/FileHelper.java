package com.tlcsdm.jvmexplorer.helper;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

/**
 * Provides the file helper implementation used by the com.tlcsdm.jvmexplorer.helper package.
 */
public class FileHelper {

	private static final String TEST_FILE_PATH = "INTEGRATION_TEST_FILE_PATH";

	// For testing only
	/**
	 * Updates the test file value.
	 */
	public static void setTestFile(Window window, File file) {
		window.getProperties().put(TEST_FILE_PATH, file);
	}

	/**
	 * Opens the jar.
	 */
	public File openJar(Window owner, String title) {
		final File testFile = getTestFile(owner);
		if (testFile != null) {
			return testFile;
		}
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JAR Files", "*.jar"));
		return fileChooser.showOpenDialog(owner);
	}

	// Integration tests can't handle the file chooser. This allows testing stuff that requires file selection.
	/**
	 * Returns the test file value.
	 */
	private File getTestFile(Window owner) {
		if (owner == null) {
			return null;
		}
		final Object filePath = owner.getProperties().get(TEST_FILE_PATH);
		if (filePath != null) {
			return new File(filePath.toString());
		}
		return null;
	}

	/**
	 * Handles the save jar workflow.
	 */
	public File saveJar(Window owner, String title, String initialFileName) {
		final File testFile = getTestFile(owner);
		if (testFile != null) {
			return testFile;
		}
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JAR Files", "*.jar"));
		if (initialFileName != null) {
			fileChooser.setInitialFileName(initialFileName);
		}
		return fileChooser.showSaveDialog(owner);
	}

	/**
	 * Opens the class.
	 */
	public File openClass(Window owner, String title) {
		final File testFile = getTestFile(owner);
		if (testFile != null) {
			return testFile;
		}
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Class Files", "*.class"));
		return fileChooser.showOpenDialog(owner);
	}

	/**
	 * Handles the save class workflow.
	 */
	public File saveClass(Window owner, String title, String initialFileName) {
		final File testFile = getTestFile(owner);
		if (testFile != null) {
			return testFile;
		}
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Class Files", "*.class"));
		if (initialFileName != null) {
			fileChooser.setInitialFileName(initialFileName);
		}
		return fileChooser.showSaveDialog(owner);
	}

}
