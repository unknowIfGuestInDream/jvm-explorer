package com.tlcsdm.jvmexplorer.helper;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

/**
 * Provides the accelerator helper implementation used by the com.tlcsdm.jvmexplorer.helper package.
 */
public class AcceleratorHelper {

	/**
	 * Handles the process request.
	 */
	public static void process(Node runOnNode, KeyCodeCombination accelerator, MenuItem delegateTo) {
		// It seems like menu item accelerators don't trigger in the CodeArea. We have to manually wire it together.
		runOnNode.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
			if (delegateTo.disableProperty().get()) {
				return;
			}
			if (accelerator.match(e)) {
				delegateTo.fire();
			}
		});
	}

}
