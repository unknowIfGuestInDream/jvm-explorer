package com.tlcsdm.jvmexplorer.helper;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * Provides the clipboard helper implementation used by the com.tlcsdm.jvmexplorer.helper package.
 */
public class ClipboardHelper {

	/**
	 * Performs the copy operation.
	 */
	public static void copy(String text) {
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();
		content.putString(text);
		clipboard.setContent(content);
	}

}
