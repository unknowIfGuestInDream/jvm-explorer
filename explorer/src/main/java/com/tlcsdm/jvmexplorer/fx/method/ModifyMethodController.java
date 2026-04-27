package com.tlcsdm.jvmexplorer.fx.method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlcsdm.jvmexplorer.JvmExplorer;
import com.tlcsdm.jvmexplorer.agent.AgentException;
import com.tlcsdm.jvmexplorer.agent.RunningJvm;
import com.tlcsdm.jvmexplorer.bytecode.AsmDisassembler;
import com.tlcsdm.jvmexplorer.bytecode.Disassembler;
import com.tlcsdm.jvmexplorer.bytecode.compile.CompileResult;
import com.tlcsdm.jvmexplorer.bytecode.compile.Compiler;
import com.tlcsdm.jvmexplorer.bytecode.compile.JavacBytecodeProvider;
import com.tlcsdm.jvmexplorer.bytecode.compile.RemoteJavacBytecodeProvider;
import com.tlcsdm.jvmexplorer.helper.AcceleratorHelper;
import com.tlcsdm.jvmexplorer.helper.AsmHelper;
import com.tlcsdm.jvmexplorer.helper.CodeAreaHelper;
import com.tlcsdm.jvmexplorer.helper.CodeTemplateHelper;
import com.tlcsdm.jvmexplorer.net.ClientHandler;
import com.tlcsdm.jvmexplorer.protocol.LoadedClass;
import com.tlcsdm.jvmexplorer.protocol.PatchResult;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.fxmisc.richtext.CodeArea;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Provides the modify method controller implementation used by the com.tlcsdm.jvmexplorer.fx.method package.
 */
public class ModifyMethodController {

	private static final Logger log = LoggerFactory.getLogger(ModifyMethodController.class);


	private static final String CLASS_NAME = "MethodModification";
	private static final String METHOD_NAME = "onMethodCall";

	@FXML
	private CodeArea code;

	@FXML
	private TextArea output;

	@FXML
	private ComboBox<ModifyType> modifyType;

	@FXML
	private ComboBox<MethodDescriptor> method;

	@FXML
	private Button compileButton;

	@FXML
	private Button modifyButton;

	private ExecutorService executorService;
	private ClientHandler clientHandler;
	private RunningJvm runningJvm;
	private LoadedClass initialClass;
	private List<LoadedClass> classpath;
	private Consumer<Boolean> onClose;
	private byte[] classFile;

	private ClassNode classNode;

	// Prevent garbage collection
	// Note if the controller is gc'd then the template is gc'd too
	private StringBinding template;

	public void initialize(ExecutorService executorService, ClientHandler clientHandler, RunningJvm runningJvm,
	                       LoadedClass initialClass, Consumer<Boolean> onClose, List<LoadedClass> classpath,
	                       byte[] classFile) {
		this.executorService = executorService;
		this.clientHandler = clientHandler;
		this.runningJvm = runningJvm;
		this.classpath = classpath;
		this.initialClass = initialClass;
		this.onClose = onClose;
		this.classFile = classFile;
		setupCodeArea();
	}

	/**
	 * Sets up the code area used by the method editor.
	 */
	private void setupCodeArea() {
		final CodeAreaHelper codeAreaHelper = new CodeAreaHelper(executorService);

		modifyType.getItems().setAll(ModifyType.values());
		modifyType.getSelectionModel().selectFirst();

		resetClassNode();

		template = Bindings.createStringBinding(() -> buildBaseCode(method.getValue(), modifyType.getValue()),
		                                        modifyType.valueProperty(),
		                                        method.valueProperty());

		final ChangeListener<String> bindingListener = (obs, old, newv) -> {
			code.replaceText(newv);
			codeAreaHelper.triggerHighlightUpdate(code);
		};
		template.addListener(bindingListener);
		bindingListener.changed(template, null, template.getValue());

		codeAreaHelper.initializeJavaEditor(code);
		setupContextMenu();
	}

	/**
	 * Handles the reset class node workflow.
	 */
	private void resetClassNode() {
		// We modify the ClassNode methods in-place after compiling.
		// Therefore, if there is some failure, we need to reset the class node, so it doesn't stay corrupted.
		classNode = AsmHelper.parse(classFile);
		final List<MethodDescriptor> methods = classNode.methods.stream()
		                                                        .filter(mn -> !Modifier.isAbstract(mn.access))
		                                                        .map(MethodDescriptor::new)
		                                                        .collect(Collectors.toList());
		final int selectedIndex = method.getSelectionModel().getSelectedIndex();
		method.getItems().setAll(methods);
		if (selectedIndex == -1) {
			// Initial setup
			method.getSelectionModel().selectFirst();
		}
		else {
			// Select same method as before, after resetting
			method.getSelectionModel().select(selectedIndex);
		}
	}

	/**
	 * Builds the configured result object.
	 */
	private String buildBaseCode(MethodDescriptor methodDesc, ModifyType modifyType) {
		if (methodDesc == null) {
			return JvmExplorer.getBundle().getString("status.noMethodSelected");
		}
		final String returnType =
				modifyType.isExpectsReturnValue() ? Type.getReturnType(methodDesc.getMethodNode().desc).getClassName()
				                                  : "void";
		final String method = methodDesc.buildTemplate(METHOD_NAME, returnType);
		final CodeTemplateHelper codeTemplateHelper = new CodeTemplateHelper();
		final String code = modifyType.getComment();
		return codeTemplateHelper.loadModifyMethod(CLASS_NAME, method, code);
	}

	/**
	 * Sets up the context menu for compile and modify actions.
	 */
	private void setupContextMenu() {
		final ContextMenu contextMenu = new ContextMenu();

		final MenuItem compile = new MenuItem(JvmExplorer.getBundle().getString("ctx.compileCode"));
		compile.setOnAction(e -> compileButton.fire());

		final KeyCodeCombination compileShortcut = new KeyCodeCombination(KeyCode.B, KeyCodeCombination.CONTROL_DOWN);
		compile.setAccelerator(compileShortcut);

		AcceleratorHelper.process(code, compileShortcut, compile);

		contextMenu.getItems().add(compile);

		final MenuItem modifyCode = new MenuItem(JvmExplorer.getBundle().getString("ctx.modifyCode"));
		modifyCode.setOnAction(e -> modifyButton.fire());

		final KeyCodeCombination modifyShortcut = new KeyCodeCombination(KeyCode.M, KeyCodeCombination.CONTROL_DOWN);
		modifyCode.setAccelerator(modifyShortcut);

		AcceleratorHelper.process(code, modifyShortcut, modifyCode);

		contextMenu.getItems().add(modifyCode);

		code.setContextMenu(contextMenu);
	}

	/**
	 * Handles the compile action from the method editor.
	 */
	@FXML
	void onCompile() {
		onCompile(c -> Platform.runLater(() -> setOutputText(JvmExplorer.getBundle().getString("status.compiledSuccessfully"), c.getStdOut())));
	}

	/**
	 * Handles the compile event.
	 */
	private void onCompile(Consumer<CompileResult> onCompilation) {
		setOutputText(JvmExplorer.getBundle().getString("status.compiling"), JvmExplorer.getBundle().getString("status.pleaseWait"));
		// I love when java can't compile my lambda without casting
		executorService.submit(() -> {
			log.debug("Compiling class with {} classes on classpath", classpath.size());
			final Compiler compiler = new Compiler();
			final JavacBytecodeProvider javacBytecodeProvider = new RemoteJavacBytecodeProvider(clientHandler,
			                                                                                    runningJvm,
			                                                                                    classpath);
			final int targetJavaVersion = Math.min(getJavaVersion(), Runtime.version().feature());
			final CompileResult compileResult = compiler.compile(targetJavaVersion,
			                                                     CLASS_NAME,
			                                                     code.getText(),
			                                                     javacBytecodeProvider);
			log.debug("Compile result: {}", compileResult);
			if (!compileResult.isSuccess()) {
				Platform.runLater(() -> setOutputText(JvmExplorer.getBundle().getString("status.compilationFailed"), compileResult.getStdOut()));
				return;
			}
			onCompilation.accept(compileResult);
		});
	}

	/**
	 * Updates the output text value.
	 */
	private void setOutputText(String header, String body) {
		output.setText(header + System.lineSeparator() + System.lineSeparator() + body);
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
	 * Handles the method modification action from the method editor.
	 */
	@FXML
	void onModify() {
		final MethodDescriptor selectedMethod = method.getValue();
		final ModifyType selectedModifyType = modifyType.getValue();
		onCompile(compileResult -> {
			final byte[] finishedClass;
			try {
				postProcess(compileResult, selectedMethod, selectedModifyType);
				// We can use the stack frames from the valid bytecode we already have
				finishedClass = AsmHelper.parse(ClassWriter.COMPUTE_MAXS, classNode);
			}
			catch (Exception e) {
				log.warn("Failed to post-process method", e);
				Platform.runLater(() -> {
					setOutputText(JvmExplorer.getBundle().getString("status.classProcessorFailed"), e.getMessage());
					resetClassNode();
				});
				return;
			}

			final PatchResult patchResult = clientHandler.replaceClass(runningJvm, initialClass, finishedClass);
			if (!patchResult.isSuccess()) {
				final Disassembler disassembler = new AsmDisassembler();
				final String disassembledClass = disassembler.process(finishedClass);
				log.debug("Failed to patch class\n{}\n{}", patchResult.getMessage(), disassembledClass);
				Platform.runLater(() -> {
					setOutputText(JvmExplorer.getBundle().getString("status.patchFailedTitle"), patchResult.getMessage());
					resetClassNode();
				});
				return;
			}
			log.debug("Patched class successfully");
			Platform.runLater(() -> onClose.accept(true));
		});
	}

	private void postProcess(CompileResult compileResult, MethodDescriptor selectedMethod,
	                         ModifyType selectedModifyType) {
		final ClassNode compiledClass = AsmHelper.parse(compileResult.getClassContent());
		final MethodNode updatedMethod = compiledClass.methods.stream()
		                                                      .filter(mn -> mn.name.equals(METHOD_NAME))
		                                                      .findFirst()
		                                                      .orElseThrow();
		final MethodNode methodToModify = classNode.methods.stream()
		                                                   .filter(mn -> mn == selectedMethod.getMethodNode())
		                                                   .findFirst()
		                                                   .orElseThrow();

		switch (selectedModifyType) {
		case ADD_BEFORE:
			// Patch returns to continue to the actual method
			final Label after = new Label();
			replaceReturn(updatedMethod, after);
			final AbstractInsnNode frame = new FrameNode(Opcodes.F_SAME, 0, new Object[0], 0, new Object[0]);
			methodToModify.instructions.insert(new LabelNode(after));
			methodToModify.instructions.insert(frame);
			methodToModify.instructions.insert(updatedMethod.instructions);
			methodToModify.tryCatchBlocks.addAll(updatedMethod.tryCatchBlocks);
			if (classNode.version < Opcodes.V1_6) {
				// F_SAME requires >= V1_6
				classNode.version = Opcodes.V1_6;
			}
			// new instructions -> label -> new frame -> old instructions
			break;
		case REPLACE:
			methodToModify.instructions.clear();
			methodToModify.instructions.add(updatedMethod.instructions);
			methodToModify.tryCatchBlocks.clear();
			methodToModify.tryCatchBlocks.addAll(updatedMethod.tryCatchBlocks);
			break;
		}
		delegateCalls(classNode, methodToModify);
	}

	/**
	 * Handles the replace return workflow.
	 */
	private void replaceReturn(MethodNode methodNodeToUpdate, Label goToLabel) {
		final List<AbstractInsnNode> returns = new ArrayList<>();
		for (AbstractInsnNode insn : methodNodeToUpdate.instructions) {
			if (insn.getOpcode() == Opcodes.RETURN) {
				returns.add(insn);
			}
		}
		returns.forEach(ret -> {
			final AbstractInsnNode jump = new JumpInsnNode(Opcodes.GOTO, new LabelNode(goToLabel));
			methodNodeToUpdate.instructions.set(ret, jump);
		});
	}

	/**
	 * Handles the delegate calls workflow.
	 */
	private void delegateCalls(ClassNode owner, MethodNode updatedMethod) {
		// Delegate calls to the real class, not the class we compiled against
		updatedMethod.instructions.forEach(insn -> {
			if (insn instanceof FieldInsnNode) {
				final FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
				if (fieldInsnNode.owner.equals(CLASS_NAME)) {
					fieldInsnNode.owner = owner.name;
				}
			}
			else if (insn instanceof MethodInsnNode) {
				final MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
				if (methodInsnNode.owner.equals(CLASS_NAME)) {
					methodInsnNode.owner = owner.name;
				}
			}
		});
	}

	/**
	 * Handles the cancel action from the method editor.
	 */
	@FXML
	void onCancel() {
		onClose.accept(false);
	}

	/**
	 * Enumerates the supported modify type values used by the com.tlcsdm.jvmexplorer.fx.method package.
	 */
	private enum ModifyType {
		ADD_BEFORE("Add Code Before Method",
		           false,
		           "// The following method body will be called before the rest of the method"),
		REPLACE("Replace Method Body", true, "// The following method body will replace the specified method body");
		private final String description;
		private final boolean expectsReturnValue;
		private final String comment;

		/**
		 * Creates a new ModifyType value.
		 */
		ModifyType(String description, boolean expectsReturnValue, String comment) {
			this.description = description;
			this.expectsReturnValue = expectsReturnValue;
			this.comment = comment;
		}

		/**
		 * Returns whether expects return value is enabled or currently true.
		 */
		public boolean isExpectsReturnValue() {
			return this.expectsReturnValue;
		}

		/**
		 * Returns the comment value.
		 */
		public String getComment() {
			return this.comment;
		}

		/**
		 * Returns a readable description of this instance.
		 */
		@Override
		public String toString() {
			return description;
		}
	}

	/**
	 * Provides the method descriptor implementation used by the com.tlcsdm.jvmexplorer.fx.method package.
	 */
	private static class MethodDescriptor {
		private final MethodNode methodNode;

		/**
		 * Creates a new MethodDescriptor instance.
		 */
		public MethodDescriptor(MethodNode methodNode) {
			this.methodNode = methodNode;
		}

		/**
		 * Returns the method node value.
		 */
		public MethodNode getMethodNode() {
			return this.methodNode;
		}

		/**
		 * Returns a readable description of this instance.
		 */
		@Override
		public String toString() {
			final String returnType = Type.getReturnType(methodNode.desc).getClassName();
			return buildTemplate(methodNode.name, returnType);
		}

		/**
		 * Builds the configured result object.
		 */
		private String buildTemplate(String methodName, String returnType) {
			final AtomicInteger paramIndex = new AtomicInteger(0);
			if (!Modifier.isStatic(methodNode.access)) {
				// 'this' is index 0, so push everything up 1
				paramIndex.incrementAndGet();
			}
			final String arguments = Arrays.stream(Type.getArgumentTypes(methodNode.desc))
			                               .map(Type::getClassName)
			                               .map(type -> type + " var" + paramIndex.get())
			                               .peek(param -> paramIndex.incrementAndGet())
			                               .collect(Collectors.joining(", "));
			final boolean isStatic = Modifier.isStatic(methodNode.access);
			final String methodPrefix = "public " + (isStatic ? "static " : "");
			return methodPrefix + returnType + " " + methodName + "(" + arguments + ")";
		}

	}


	/**
	 * Handles the modify method controller workflow.
	 */
	public ModifyMethodController(CodeArea code, TextArea output, ComboBox<ModifyType> modifyType, ComboBox<MethodDescriptor> method, Button compileButton, Button modifyButton, ExecutorService executorService, ClientHandler clientHandler, RunningJvm runningJvm, LoadedClass initialClass, List<LoadedClass> classpath, Consumer<Boolean> onClose, byte[] classFile, ClassNode classNode, StringBinding template) {
		this.code = code;
		this.output = output;
		this.modifyType = modifyType;
		this.method = method;
		this.compileButton = compileButton;
		this.modifyButton = modifyButton;
		this.executorService = executorService;
		this.clientHandler = clientHandler;
		this.runningJvm = runningJvm;
		this.initialClass = initialClass;
		this.classpath = classpath;
		this.onClose = onClose;
		this.classFile = classFile;
		this.classNode = classNode;
		this.template = template;
	}

	/**
	 * Handles the modify method controller workflow.
	 */
	public ModifyMethodController() {
	}

	/**
	 * Returns the code value.
	 */
	public CodeArea getCode() {
		return this.code;
	}

	/**
	 * Returns the output value.
	 */
	public TextArea getOutput() {
		return this.output;
	}

	/**
	 * Returns the modify type value.
	 */
	public ComboBox<ModifyType> getModifyType() {
		return this.modifyType;
	}

	/**
	 * Returns the method value.
	 */
	public ComboBox<MethodDescriptor> getMethod() {
		return this.method;
	}

	/**
	 * Returns the compile button value.
	 */
	public Button getCompileButton() {
		return this.compileButton;
	}

	/**
	 * Returns the modify button value.
	 */
	public Button getModifyButton() {
		return this.modifyButton;
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
	 * Returns the running jvm value.
	 */
	public RunningJvm getRunningJvm() {
		return this.runningJvm;
	}

	/**
	 * Returns the initial class value.
	 */
	public LoadedClass getInitialClass() {
		return this.initialClass;
	}

	/**
	 * Returns the classpath value.
	 */
	public List<LoadedClass> getClasspath() {
		return this.classpath;
	}

	/**
	 * Returns the on close value.
	 */
	public Consumer<Boolean> getOnClose() {
		return this.onClose;
	}

	/**
	 * Returns the class file value.
	 */
	public byte[] getClassFile() {
		return this.classFile;
	}

	/**
	 * Returns the class node value.
	 */
	public ClassNode getClassNode() {
		return this.classNode;
	}

	/**
	 * Returns the template value.
	 */
	public StringBinding getTemplate() {
		return this.template;
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ModifyMethodController other = (ModifyMethodController) o;
		return java.util.Objects.equals(this.code, other.code) && java.util.Objects.equals(this.output, other.output) && java.util.Objects.equals(this.modifyType, other.modifyType) && java.util.Objects.equals(this.method, other.method) && java.util.Objects.equals(this.compileButton, other.compileButton) && java.util.Objects.equals(this.modifyButton, other.modifyButton) && java.util.Objects.equals(this.executorService, other.executorService) && java.util.Objects.equals(this.clientHandler, other.clientHandler) && java.util.Objects.equals(this.runningJvm, other.runningJvm) && java.util.Objects.equals(this.initialClass, other.initialClass) && java.util.Objects.equals(this.classpath, other.classpath) && java.util.Objects.equals(this.onClose, other.onClose) && java.util.Objects.equals(this.classFile, other.classFile) && java.util.Objects.equals(this.classNode, other.classNode) && java.util.Objects.equals(this.template, other.template);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(code, output, modifyType, method, compileButton, modifyButton, executorService, clientHandler, runningJvm, initialClass, classpath, onClose, classFile, classNode, template);
	}

}
