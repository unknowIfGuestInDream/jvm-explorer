package com.tlcsdm.jvmexplorer.fx.compile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlcsdm.jvmexplorer.JvmExplorer;
import com.tlcsdm.jvmexplorer.agent.AgentException;
import com.tlcsdm.jvmexplorer.agent.RunningJvm;
import com.tlcsdm.jvmexplorer.bytecode.compile.CompileResult;
import com.tlcsdm.jvmexplorer.bytecode.compile.Compiler;
import com.tlcsdm.jvmexplorer.bytecode.compile.JavacBytecodeProvider;
import com.tlcsdm.jvmexplorer.bytecode.compile.RemoteJavacBytecodeProvider;
import com.tlcsdm.jvmexplorer.helper.AcceleratorHelper;
import com.tlcsdm.jvmexplorer.helper.CodeAreaHelper;
import com.tlcsdm.jvmexplorer.helper.CodeTemplateHelper;
import com.tlcsdm.jvmexplorer.net.ClientHandler;
import com.tlcsdm.jvmexplorer.protocol.ClassLoaderDescriptor;
import com.tlcsdm.jvmexplorer.protocol.ExecutionResult;
import com.tlcsdm.jvmexplorer.protocol.LoadedClass;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.fxmisc.richtext.CodeArea;

import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

/**
 * Provides the remote code executor controller implementation used by the com.tlcsdm.jvmexplorer.fx.compile package.
 */
public class RemoteCodeExecutorController {

	private static final Logger log = LoggerFactory.getLogger(RemoteCodeExecutorController.class);


	private static final String CLASS_NAME = "RemoteTask";

	private final CodeTemplateHelper codeTemplateHelper = new CodeTemplateHelper();

	@FXML
	private CodeArea code;

	@FXML
	private TextArea output;

	@FXML
	private Button runButton;

	private ExecutorService executorService;
	private ClientHandler clientHandler;
	private ClassLoaderDescriptor classLoaderDescriptor;
	private RunningJvm runningJvm;
	private List<LoadedClass> classpath;

	private String mainClassName;

	public void initialize(ExecutorService executorService, ClientHandler clientHandler, RunningJvm runningJvm,
	                       ClassLoaderDescriptor classLoaderDescriptor, String packageName,
	                       List<LoadedClass> classpath) {
		this.executorService = executorService;
		this.clientHandler = clientHandler;
		this.runningJvm = runningJvm;
		this.classLoaderDescriptor = classLoaderDescriptor;
		this.classpath = classpath;
		this.mainClassName = (packageName != null && !packageName.isEmpty() ? (packageName + ".") : "") + CLASS_NAME;

		final CodeAreaHelper codeAreaHelper = new CodeAreaHelper(executorService);

		final String codeTemplate = codeTemplateHelper.loadRemoteCallable(packageName, CLASS_NAME);
		codeAreaHelper.initializeJavaEditor(code);
		code.replaceText(codeTemplate);
		codeAreaHelper.triggerHighlightUpdate(code);

		setupContextMenu();
	}

	/**
	 * Sets up the context menu for remote code execution.
	 */
	private void setupContextMenu() {
		final ContextMenu contextMenu = new ContextMenu();

		final MenuItem run = new MenuItem(JvmExplorer.getBundle().getString("ui.executeCode"));
		run.setOnAction(e -> runButton.fire());

		final KeyCodeCombination shortcut = new KeyCodeCombination(KeyCode.R, KeyCodeCombination.CONTROL_DOWN);
		run.setAccelerator(shortcut);

		AcceleratorHelper.process(code, shortcut, run);

		contextMenu.getItems().add(run);

		code.setContextMenu(contextMenu);
	}

	/**
	 * Handles the execute-code action from the UI.
	 */
	@FXML
	void onExecute() {
		output.setText(JvmExplorer.getBundle().getString("status.compiling"));
		log.debug("Compiling class with {} classes on classpath", classpath.size());
		executorService.submit(() -> {
			final Compiler compiler = new Compiler();
			final JavacBytecodeProvider javacBytecodeProvider = new RemoteJavacBytecodeProvider(clientHandler,
			                                                                                    runningJvm,
			                                                                                    classpath);
			final int targetJavaVersion = Math.min(getJavaVersion(), Runtime.version().feature());
			final CompileResult compileResult = compiler.compile(targetJavaVersion,
			                                                     mainClassName,
			                                                     code.getText(),
			                                                     javacBytecodeProvider);
			log.debug("Compile result: {}", compileResult);
			if (!compileResult.isSuccess()) {
				Platform.runLater(() -> setOutputText(JvmExplorer.getBundle().getString("status.compilationFailed"), compileResult.getStdOut()));
				return;
			}
			Platform.runLater(() -> output.setText(JvmExplorer.getBundle().getString("status.executingCode")));
			log.debug("Executing code in class loader: {}", classLoaderDescriptor);
			final ExecutionResult result = clientHandler.executeCallable(runningJvm,
			                                                             mainClassName,
			                                                             compileResult.getClassContent(),
			                                                             classLoaderDescriptor);
			final ResourceBundle bundle = JvmExplorer.getBundle();
			Platform.runLater(() -> setOutputText((result.isSuccess() ? bundle.getString("status.executionSucceeded") : bundle.getString("status.executionFailed")),
			                                      result.getMessage()));
		});
	}

	/**
	 * Returns the java version value.
	 */
	private int getJavaVersion() {
		try {
			return runningJvm.getJavaVersion();
		}
		catch (AgentException e) {
			log.warn("Failed to get java version for remote code execution", e);
			return Runtime.version().feature();
		}
	}

	/**
	 * Updates the output text value.
	 */
	private void setOutputText(String header, String body) {
		output.setText(header + System.lineSeparator() + System.lineSeparator() + body);
	}

}
