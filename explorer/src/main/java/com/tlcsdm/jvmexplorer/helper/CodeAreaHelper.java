package com.tlcsdm.jvmexplorer.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides the code area helper implementation used by the com.tlcsdm.jvmexplorer.helper package.
 */
public class CodeAreaHelper {

	private static final Logger log = LoggerFactory.getLogger(CodeAreaHelper.class);


	private final ExecutorService executorService;

	// CodeArea must be in a VBox to replace and insert into VirtualizedScrollPane
	/**
	 * Initializes the component state and dependencies.
	 */
	public void initializeJavaEditor(CodeArea codeArea) {
		codeArea.getStylesheets()
		        .setAll(Objects.requireNonNull(getClass().getClassLoader().getResource("css/code-area.css"))
		                       .toExternalForm());
		// Hack to insert the scroll pane. SceneBuilder wasn't picking it up.
		final VBox parent = (VBox) codeArea.getParent();
		parent.getChildren().remove(codeArea);
		final Node scrollPane = new VirtualizedScrollPane<>(codeArea);
		parent.getChildren().add(scrollPane);
		VBox.setVgrow(scrollPane, Priority.ALWAYS);
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		final Subscription subscription = codeArea.multiPlainChanges()
		                                          .successionEnds(Duration.ofMillis(500))
		                                          .retainLatestUntilLater(executorService)
		                                          .supplyTask(() -> computeHighlighting(codeArea))
		                                          .awaitLatest(codeArea.multiPlainChanges())
		                                          .filterMap(t -> {
			                                          if (t.isSuccess()) {
				                                          return Optional.of(t.get());
			                                          }
			                                          else {
				                                          log.warn("Failed to compute highlighting", t.getFailure());
				                                          return Optional.empty();
			                                          }
		                                          })
		                                          .subscribe(highlighting -> applyHighlighting(codeArea,
		                                                                                       highlighting));

		// Stop highlighting when the window is closed
		final EventHandler<WindowEvent> eventHandler = e -> {
			log.debug("Window closed, unsubscribing highlighting for {}", codeArea);
			subscription.unsubscribe();
		};
		codeArea.sceneProperty().addListener((obs, old, newv) -> {
			if (old != null) {
				old.getWindow().removeEventHandler(WindowEvent.WINDOW_HIDDEN, eventHandler);
			}
			if (newv != null) {
				newv.getWindow().addEventHandler(WindowEvent.WINDOW_HIDDEN, eventHandler);
			}
		});

		// Auto-indent to the whitespace of the previous line
		final Pattern whiteSpace = Pattern.compile("^\\s+");
		codeArea.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER) {
				final int caretPosition = codeArea.getCaretPosition();
				final int currentParagraph = codeArea.getCurrentParagraph();
				if (currentParagraph <= 0) {
					return;
				}
				final Matcher matcher = whiteSpace.matcher(codeArea.getParagraph(currentParagraph - 1)
				                                                   .getSegments()
				                                                   .get(0));
				if (matcher.find()) {
					Platform.runLater(() -> codeArea.insertText(caretPosition, matcher.group()));
				}
			}
		});
	}

	/**
	 * Performs the compute highlighting operation.
	 */
	private Task<HighlightContainer> computeHighlighting(CodeArea codeArea) {
		final String text = codeArea.getText();
		final String propName = HighlightHelper.HighlightContext.class.getName();
		final var highlightContext = (HighlightHelper.HighlightContext) codeArea.getProperties().get(propName);
		final Task<HighlightContainer> task = new Task<>() {
			/**
			 * Calls the wrapped operation and returns its result.
			 */
			@Override
			protected HighlightContainer call() {
				final StyleSpans<Collection<String>> highlighting;
				if (highlightContext != null) {
					highlighting = HighlightHelper.computeHighlighting(text, highlightContext);
				}
				else {
					highlighting = HighlightHelper.computeHighlighting(text);
				}
				return new HighlightContainer(highlighting, text);
			}
		};
		executorService.execute(task);
		return task;
	}

	/**
	 * Applies highlighting.
	 */
	private void applyHighlighting(CodeArea codeArea, HighlightContainer highlighting) {
		if (!codeArea.getText().equals(highlighting.getText())) {
			log.debug("Detected text change; aborting highlight change");
			return;
		}
		codeArea.setStyleSpans(0, highlighting.getHightlighting());
	}

	/**
	 * Performs the trigger highlight update operation.
	 */
	public void triggerHighlightUpdate(CodeArea codeArea) {
		final Task<HighlightContainer> initialTask = computeHighlighting(codeArea);
		initialTask.setOnSucceeded(e -> applyHighlighting(codeArea, initialTask.getValue()));
	}

	// Prevent highlighting the wrong text if the text changes
	/**
	 * Provides the highlight container implementation used by the com.tlcsdm.jvmexplorer.helper package.
	 */
	private static class HighlightContainer {
		private final StyleSpans<Collection<String>> hightlighting;
		private final String text;

		/**
		 * Creates a new HighlightContainer instance.
		 */
		public HighlightContainer(StyleSpans<Collection<String>> hightlighting, String text) {
			this.hightlighting = hightlighting;
			this.text = text;
		}

		/**
		 * Returns the hightlighting value.
		 */
		public StyleSpans<Collection<String>> getHightlighting() {
			return this.hightlighting;
		}

		/**
		 * Returns the text value.
		 */
		public String getText() {
			return this.text;
		}
	}


	/**
	 * Performs the code area helper operation.
	 */
	public CodeAreaHelper(ExecutorService executorService) {
		this.executorService = executorService;
	}

	/**
	 * Performs the code area helper operation.
	 */
	public CodeAreaHelper() {
		this.executorService = null;
	}

	/**
	 * Returns the executor service value.
	 */
	public ExecutorService getExecutorService() {
		return this.executorService;
	}

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return "CodeAreaHelper(executorService=" + executorService + ")";
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CodeAreaHelper other = (CodeAreaHelper) o;
		return java.util.Objects.equals(this.executorService, other.executorService);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(executorService);
	}

}
