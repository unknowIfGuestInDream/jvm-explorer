package com.tlcsdm.jvmexplorer.fx.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlcsdm.jvmexplorer.JvmExplorer;
import com.tlcsdm.jvmexplorer.agent.RunningJvm;
import com.tlcsdm.jvmexplorer.helper.AlertHelper;
import com.tlcsdm.jvmexplorer.helper.ClassTreeHelper;
import com.tlcsdm.jvmexplorer.helper.ExportHelper;
import com.tlcsdm.jvmexplorer.helper.FileHelper;
import com.tlcsdm.jvmexplorer.helper.PatchHelper;
import com.tlcsdm.jvmexplorer.helper.RemoteCodeHelper;
import com.tlcsdm.jvmexplorer.net.ClientHandler;
import com.tlcsdm.jvmexplorer.protocol.ClassLoaderDescriptor;
import com.tlcsdm.jvmexplorer.protocol.LoadedClass;
import com.tlcsdm.jvmexplorer.protocol.PatchResult;
import com.tlcsdm.jvmexplorer.settings.JvmExplorerSettings;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ClassCellFactory implements Callback<TreeView<ClassTreeNode>, TreeCell<ClassTreeNode>> {

	private static final Logger log = LoggerFactory.getLogger(ClassCellFactory.class);


	private static final Map<LoadedClass.MetaType, Image> CLASS_IMAGES = new ConcurrentHashMap<>();
	private final PatchHelper patchHelper = new PatchHelper();
	private final ClassTreeHelper classTreeHelper = new ClassTreeHelper();
	private final FileHelper fileHelper = new FileHelper();
	private final RemoteCodeHelper remoteCodeHelper = new RemoteCodeHelper();
	private final ExecutorService executorService;
	private final AlertHelper alertHelper;
	private final ObjectProperty<RunningJvm> currentJvm;
	private final ClientHandler clientHandler;
	private final FilterableTreeItem<ClassTreeNode> classesTreeRoot;
	private final ExportHelper exportHelper;
	private final Consumer<RunningJvm> onLoadClasses;
	private final JvmExplorerSettings settings;

	@Override
	public TreeCell<ClassTreeNode> call(TreeView<ClassTreeNode> classes) {
		final TreeCell<ClassTreeNode> treeCell = new TreeCell<>();
		setupImageBinding(treeCell);
		setupTextBinding(treeCell);
		setupTooltipBinding(treeCell);
		setupContextMenu(treeCell, classes);
		return treeCell;
	}

	private void setupImageBinding(TreeCell<ClassTreeNode> treeCell) {
		treeCell.graphicProperty().bind(Bindings.createObjectBinding(() -> {
			final ClassTreeNode item = treeCell.getItem();
			if (item == null) {
				return null;
			}
			if (item.getType() == ClassTreeNode.Type.CLASS) {
				return new ImageView(getClassImage(item.getLoadedClass()));
			}
			return new ImageView(item.getType().getImage());
		}, treeCell.itemProperty()));
	}

	private void setupTextBinding(TreeCell<ClassTreeNode> treeCell) {
		treeCell.textProperty()
		        .bind(Bindings.when(treeCell.itemProperty().isNotNull())
		                      .then(treeCell.itemProperty().asString())
		                      .otherwise(""));
	}

	private void setupTooltipBinding(TreeCell<ClassTreeNode> treeCell) {
		final Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(Bindings.createStringBinding(() -> {
			final ClassTreeNode classTreeNode = treeCell.getItem();
			if (classTreeNode == null) {
				return "";
			}
			switch (classTreeNode.getType()) {
			case CLASSLOADER:
				return classTreeNode.getClassLoaderDescriptor().getDescription();
			case PACKAGE:
				return classTreeNode.getPackageSegment();
			case CLASS:
				return classTreeNode.getLoadedClass().toString();
			default:
				log.warn("Unknown type: {}", classTreeNode.getType());
				return "";
			}
		}, treeCell.itemProperty()));
		treeCell.tooltipProperty()
		        .bind(Bindings.when(treeCell.itemProperty().isNotNull()).then(tooltip).otherwise((Tooltip) null));
	}

	private void setupContextMenu(TreeCell<ClassTreeNode> treeCell, TreeView<ClassTreeNode> classes) {
		final ContextMenu classesContextMenu = new ContextMenu();

		final MenuItem scopedExport = createScopedExport(treeCell, classes);
		final MenuItem exportClasses = createExportClasses(classes);
		final MenuItem reloadClasses = createReloadClasses();
		final MenuItem scopedReplace = createScopedReplace(treeCell, classes);
		final MenuItem replaceClasses = createReplaceClasses(classes);
		final MenuItem includeClassLoader = createShowClassLoader(reloadClasses);
		final MenuItem executeCode = createExecuteCode(treeCell, classes);

		treeCell.itemProperty().addListener((obs, old, newv) -> {
			classesContextMenu.getItems().clear();
			if (newv != null) {
				classesContextMenu.getItems().addAll(scopedExport, scopedReplace, new SeparatorMenuItem());
			}
			classesContextMenu.getItems()
			                  .addAll(executeCode,
			                          new SeparatorMenuItem(),
			                          exportClasses,
			                          replaceClasses,
			                          reloadClasses,
			                          new SeparatorMenuItem(),
			                          includeClassLoader);
		});

		treeCell.setContextMenu(classesContextMenu);
	}

	private Image getClassImage(LoadedClass loadedClass) {
		if (loadedClass.getMetaType() == null) {
			return ClassTreeNode.Type.CLASS.getImage();
		}
		return CLASS_IMAGES.computeIfAbsent(loadedClass.getMetaType(), type -> {
			final String imagePath;
			switch (type) {
			case INNER:
				imagePath = "icons/innerclass.png";
				break;
			case INTERFACE:
				imagePath = "icons/interface.png";
				break;
			case ABSTRACT:
				imagePath = "icons/abstractClass.png";
				break;
			case ENUM:
				imagePath = "icons/enum.png";
				break;
			case ANNOTATION:
				imagePath = "icons/annotationtype.png";
				break;
			case EXCEPTION:
				imagePath = "icons/exceptionClass.png";
				break;
			case ABSTRACT_EXCEPTION:
				imagePath = "icons/abstractException.png";
				break;
			case ANONYMOUS:
				imagePath = "icons/anonymousClass.png";
				break;
			default:
				log.warn("Unknown type: {}", type);
				return ClassTreeNode.Type.CLASS.getImage();
			}
			return new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(imagePath)));
		});
	}

	private MenuItem createScopedExport(TreeCell<ClassTreeNode> treeCell, TreeView<ClassTreeNode> classes) {
		final MenuItem scopedExport = new MenuItem();
		scopedExport.textProperty().bind(Bindings.createStringBinding(() -> {
			final ResourceBundle bundle = JvmExplorer.getBundle();
			final ClassTreeNode classTreeNode = treeCell.getItem();
			if (classTreeNode == null) {
				return "";
			}
			switch (classTreeNode.getType()) {
			case CLASSLOADER:
				return bundle.getString("ctx.exportClassLoader");
			case PACKAGE:
				return bundle.getString("ctx.exportPackage");
			case CLASS:
				return bundle.getString("ctx.exportClass");
			}
			log.warn("Unknown type: {}", classTreeNode.getType());
			return "";
		}, treeCell.itemProperty()));
		scopedExport.setOnAction(e -> {
			final RunningJvm activeJvm = currentJvm.get();
			if (activeJvm == null) {
				return;
			}
			final ClassTreeNode classTreeNode = treeCell.getItem();
			if (classTreeNode == null) {
				return;
			}
			switch (classTreeNode.getType()) {
			case CLASSLOADER:
				final File exportClassLoader = selectExportJarFile(classTreeNode.getPackageSegment(),
				                                                   classes.getScene().getWindow());
				if (exportClassLoader == null) {
					return;
				}
				final ClassLoaderDescriptor classLoaderDescriptor = classTreeNode.getClassLoaderDescriptor();
				log.debug("Exporting class loader: {}", classLoaderDescriptor);
				final List<LoadedClass> classesInClassLoader = classTreeHelper.getClassesInPackage(classesTreeRoot,
				                                                                                   "",
				                                                                                   classLoaderDescriptor);
				executorService.submit(() -> export(exportClassLoader, classesInClassLoader, activeJvm));
				break;
			case PACKAGE:
				final File exportPackage = selectExportJarFile(classTreeNode.getPackageSegment(),
				                                               classes.getScene().getWindow());
				if (exportPackage == null) {
					return;
				}
				final String fullPackageName = classTreeHelper.getPackageName(treeCell.getTreeItem());
				final ClassLoaderDescriptor packageClassLoader = this.settings.getShowClassLoader().get()
				                                                 ?
				                                                 classTreeHelper.getNodeClassLoader(treeCell.getTreeItem())
				                                                 : null;
				log.debug("Exporting package: {} in classloader: {}", fullPackageName, packageClassLoader);
				final List<LoadedClass> classesInPackage = classTreeHelper.getClassesInPackage(classesTreeRoot,
				                                                                               fullPackageName,
				                                                                               packageClassLoader);
				executorService.submit(() -> export(exportPackage, classesInPackage, activeJvm));
				break;
			case CLASS:
				final LoadedClass loadedClass = classTreeNode.getLoadedClass();
				final File selectedFile = selectExportClassFile(loadedClass.getSimpleName() + ".class",
				                                                classes.getScene().getWindow());
				if (selectedFile == null) {
					return;
				}
				log.debug("Exporting class: {}", loadedClass);
				executorService.submit(() -> export(selectedFile, loadedClass, activeJvm));
				break;
			}
		});
		return scopedExport;
	}

	private MenuItem createExportClasses(TreeView<ClassTreeNode> classes) {
		final MenuItem exportClasses = new MenuItem(JvmExplorer.getBundle().getString("ctx.exportClasses"));
		exportClasses.setOnAction(e -> {
			final RunningJvm activeJvm = currentJvm.get();
			if (activeJvm == null) {
				return;
			}
			final File selectedFile = selectExportJarFile(activeJvm.getName(), classes.getScene().getWindow());
			if (selectedFile == null) {
				return;
			}
			final List<LoadedClass> loadedClasses = classesTreeRoot.streamVisible()
			                                                       .map(ClassTreeNode::getLoadedClass)
			                                                       .filter(Objects::nonNull)
			                                                       .collect(Collectors.toList());
			executorService.submit(() -> export(selectedFile, loadedClasses, activeJvm));
		});
		return exportClasses;
	}

	private MenuItem createReloadClasses() {
		final MenuItem reloadClasses = new MenuItem(JvmExplorer.getBundle().getString("ctx.refreshClasses"));
		reloadClasses.setOnAction(e -> {
			final RunningJvm activeJvm = currentJvm.get();
			if (activeJvm == null) {
				return;
			}
			classesTreeRoot.getSourceChildren().clear();
			// I think my java version is broken... it can't compile obvious things like this without casting
			executorService.submit((Runnable) () -> onLoadClasses.accept(activeJvm));
		});
		return reloadClasses;
	}

	private MenuItem createScopedReplace(TreeCell<ClassTreeNode> treeCell, TreeView<ClassTreeNode> classes) {
		final MenuItem scopedReplace = new MenuItem(JvmExplorer.getBundle().getString("ctx.replaceClass"));
		scopedReplace.textProperty().bind(Bindings.createStringBinding(() -> {
			final ResourceBundle bundle = JvmExplorer.getBundle();
			final ClassTreeNode classTreeNode = treeCell.getItem();
			if (classTreeNode == null) {
				return "";
			}
			switch (classTreeNode.getType()) {
			case CLASSLOADER:
				return bundle.getString("ctx.replaceClassLoader");
			case PACKAGE:
				return bundle.getString("ctx.replacePackage");
			case CLASS:
				return bundle.getString("ctx.replaceClass");
			}
			log.warn("Unknown type: {}", classTreeNode.getType());
			return "";
		}, treeCell.itemProperty()));
		scopedReplace.setOnAction(e -> {
			final RunningJvm jvm = currentJvm.get();
			if (jvm == null) {
				return;
			}
			final ClassTreeNode classTreeNode = treeCell.getItem();
			if (classTreeNode == null) {
				return;
			}
			switch (classTreeNode.getType()) {
			case CLASSLOADER:
				final File importClassLoader = selectImportJarFile(classes.getScene().getWindow());
				if (importClassLoader == null) {
					return;
				}
				final ClassLoaderDescriptor classLoaderDescriptor = classTreeNode.getClassLoaderDescriptor();
				executorService.submit(() -> replaceClasses(importClassLoader, jvm, classLoaderDescriptor));
				break;
			case PACKAGE:
				final File importPackage = selectImportJarFile(classes.getScene().getWindow());
				if (importPackage == null) {
					return;
				}
				final ClassLoaderDescriptor packageClassLoader =
						classTreeHelper.getNodeClassLoader(treeCell.getTreeItem());
				executorService.submit(() -> replaceClasses(importPackage, jvm, packageClassLoader));
				break;
			case CLASS:
				final File selectedFile = selectImportClassFile(classes.getScene().getWindow());
				if (selectedFile == null) {
					return;
				}
				executorService.submit(() -> replaceClass(selectedFile, classTreeNode.getLoadedClass(), jvm));
				break;
			}
		});
		return scopedReplace;
	}

	private MenuItem createReplaceClasses(TreeView<ClassTreeNode> classes) {
		final MenuItem replaceClasses = new MenuItem(JvmExplorer.getBundle().getString("ctx.replaceClasses"));
		replaceClasses.setOnAction(e -> {
			final RunningJvm jvm = currentJvm.get();
			if (jvm == null) {
				return;
			}
			final File selectedFile = selectImportJarFile(classes.getScene().getWindow());
			if (selectedFile == null) {
				return;
			}
			executorService.submit(() -> replaceClasses(selectedFile, jvm, null));
		});
		return replaceClasses;
	}

	private MenuItem createShowClassLoader(MenuItem reloadClasses) {
		final CheckMenuItem includeClassLoader = new CheckMenuItem(JvmExplorer.getBundle().getString("ctx.showClassLoaders"));
		includeClassLoader.setOnAction(e -> {
			settings.getShowClassLoader().set(includeClassLoader.isSelected());
			final RunningJvm runningJvm = currentJvm.get();
			if (runningJvm == null) {
				return;
			}
			// Reload classes, if applicable
			reloadClasses.getOnAction().handle(e);
		});
		settings.getShowClassLoader().addListener((obs, old, newv) -> includeClassLoader.setSelected(newv));
		includeClassLoader.setSelected(settings.getShowClassLoader().getValue());
		return includeClassLoader;
	}

	private MenuItem createExecuteCode(TreeCell<ClassTreeNode> treeCell, TreeView<ClassTreeNode> treeView) {
		final MenuItem executeCode = new MenuItem();
		executeCode.textProperty().bind(Bindings.createStringBinding(() -> {
			final ResourceBundle bundle = JvmExplorer.getBundle();
			final ClassTreeNode classTreeNode = treeCell.getItem();
			if (classTreeNode != null) {
				switch (classTreeNode.getType()) {
				case CLASSLOADER:
					return bundle.getString("ctx.runCodeInClassLoader");
				case PACKAGE:
				case CLASS:
					return bundle.getString("ctx.runCodeInPackage");
				}
				log.warn("Unknown type: {}", classTreeNode.getType());
			}
			return bundle.getString("ctx.runCode");
		}, treeCell.itemProperty()));
		executeCode.setOnAction(e -> {
			final RunningJvm activeJvm = currentJvm.get();
			if (activeJvm == null) {
				return;
			}
			final Window owner = treeView.getScene().getWindow();
			final ClassTreeNode classTreeNode = treeCell.getItem();
			if (classTreeNode == null) {
				final List<LoadedClass> classpath = classTreeHelper.getLoadedClassScope(classesTreeRoot, null);
				remoteCodeHelper.showExecuteCode(owner,
				                                 classpath,
				                                 null,
				                                 null,
				                                 executorService,
				                                 clientHandler,
				                                 currentJvm);
				return;
			}
			switch (classTreeNode.getType()) {
			case CLASSLOADER:
				final List<LoadedClass> classLoaderScope = classTreeHelper.getLoadedClassScope(classesTreeRoot,
				                                                                               treeCell.getTreeItem());
				final ClassLoaderDescriptor classLoaderDescriptor = classTreeNode.getClassLoaderDescriptor();
				remoteCodeHelper.showExecuteCode(owner,
				                                 classLoaderScope,
				                                 classLoaderDescriptor,
				                                 null,
				                                 executorService,
				                                 clientHandler,
				                                 currentJvm);
				break;
			case PACKAGE:
				final String packageName = classTreeHelper.getPackageName(treeCell.getTreeItem());
				final TreeItem<ClassTreeNode> packageClassLoaderNodeItem = classTreeHelper.getNodeClassLoaderTreeItem(
						treeCell.getTreeItem());
				final ClassLoaderDescriptor packageClassLoader;
				if (packageClassLoaderNodeItem != null) {
					packageClassLoader = packageClassLoaderNodeItem.getValue().getClassLoaderDescriptor();
				}
				else {
					final FilterableTreeItem<ClassTreeNode> node =
							((FilterableTreeItem<ClassTreeNode>) treeCell.getTreeItem());
					packageClassLoader = node.streamSource()
					                         .filter(c -> c.getType() == ClassTreeNode.Type.CLASS)
					                         .map(ClassTreeNode::getLoadedClass)
					                         .map(LoadedClass::getClassLoaderDescriptor)
					                         .filter(Objects::nonNull)
					                         .findFirst()
					                         .orElse(null);
				}
				final List<LoadedClass> packageClassPath = classTreeHelper.getLoadedClassScope(classesTreeRoot,
				                                                                               packageClassLoaderNodeItem);
				remoteCodeHelper.showExecuteCode(owner,
				                                 packageClassPath,
				                                 packageClassLoader,
				                                 packageName,
				                                 executorService,
				                                 clientHandler,
				                                 currentJvm);
				break;
			case CLASS:
				final TreeItem<ClassTreeNode> packageNode = treeCell.getTreeItem().getParent();
				final String classPackageName = packageNode != null && packageNode.getValue() != null
				                                && packageNode.getValue().getType() == ClassTreeNode.Type.PACKAGE
				                                ? classTreeHelper.getPackageName(packageNode) : null;
				final TreeItem<ClassTreeNode> classLoaderNodeItem =
						classTreeHelper.getNodeClassLoaderTreeItem(treeCell.getTreeItem());
				final ClassLoaderDescriptor classLoader = treeCell.getTreeItem()
				                                                  .getValue()
				                                                  .getLoadedClass()
				                                                  .getClassLoaderDescriptor();
				final List<LoadedClass> classPath = classTreeHelper.getLoadedClassScope(classesTreeRoot,
				                                                                        classLoaderNodeItem);
				remoteCodeHelper.showExecuteCode(owner,
				                                 classPath,
				                                 classLoader,
				                                 classPackageName,
				                                 executorService,
				                                 clientHandler,
				                                 currentJvm);
				break;
			}
		});
		return executeCode;
	}

	private File selectExportJarFile(String initialFileName, Window owner) {
		return fileHelper.saveJar(owner, JvmExplorer.getBundle().getString("ctx.exportClasses"), initialFileName);
	}

	private void export(File selectedFile, List<LoadedClass> classes, RunningJvm activeJvm) {
		final File exportParentFile = selectedFile.getParentFile();
		if (exportParentFile != null) {
			exportParentFile.mkdirs();
		}
		final SimpleIntegerProperty progress = new SimpleIntegerProperty(0);
		final SimpleBooleanProperty isComplete = new SimpleBooleanProperty(false);
		final SimpleBooleanProperty success = new SimpleBooleanProperty(false);
		Platform.runLater(() -> {
			final ResourceBundle bundle = JvmExplorer.getBundle();
			final StringBinding titleText = Bindings.when(isComplete)
			                                        .then(bundle.getString("status.exportFinished"))
			                                        .otherwise(bundle.getString("status.exportInProgress"));
			final StringBinding contentText = Bindings.createStringBinding(() -> {
				if (isComplete.get()) {
					return success.get() ? bundle.getString("status.exportSucceeded") : bundle.getString("status.exportFailed");
				}
				else {
					return MessageFormat.format(bundle.getString("status.creatingJar"), progress.get());
				}
			}, isComplete, success, progress);
			alertHelper.showObservableInfo(titleText, contentText);
		});
		final boolean result = exportHelper.export(activeJvm,
		                                           classes,
		                                           selectedFile,
		                                           currentExportProgress -> Platform.runLater(() -> progress.set(
				                                           currentExportProgress)));
		Platform.runLater(() -> {
			isComplete.set(true);
			success.set(result);
		});
	}

	private File selectExportClassFile(String initialFileName, Window owner) {
		return fileHelper.saveClass(owner, JvmExplorer.getBundle().getString("ctx.exportClass"), initialFileName);
	}

	private void export(File selectedFile, LoadedClass loadedClass, RunningJvm activeJvm) {
		try {
			final byte[] classContent = clientHandler.getClassBytes(activeJvm, loadedClass);
			Files.write(selectedFile.toPath(), classContent);
		}
		catch (IOException ex) {
			log.warn("Failed to write class file {}", loadedClass.getName(), ex);
		}
	}

	private File selectImportJarFile(Window owner) {
		return fileHelper.openJar(owner, JvmExplorer.getBundle().getString("ctx.replaceClasses"));
	}

	private void replaceClasses(File selectedFile, RunningJvm activeJvm, ClassLoaderDescriptor classLoaderDescriptor) {
		final SimpleIntegerProperty progress = new SimpleIntegerProperty(0);
		final SimpleBooleanProperty isComplete = new SimpleBooleanProperty(false);
		final SimpleBooleanProperty success = new SimpleBooleanProperty(false);
		Platform.runLater(() -> {
			final ResourceBundle bundle = JvmExplorer.getBundle();
			final StringBinding titleText = Bindings.when(isComplete)
			                                        .then(bundle.getString("status.patchingFinished"))
			                                        .otherwise(bundle.getString("status.patchInProgress"));
			final StringBinding contentText = Bindings.createStringBinding(() -> {
				if (isComplete.get()) {
					return success.get() ? bundle.getString("status.patchSucceeded") : bundle.getString("status.patchFailed");
				}
				else {
					return MessageFormat.format(bundle.getString("status.patching"), progress.get());
				}
			}, isComplete, success, progress);
			alertHelper.showObservableInfo(titleText, contentText);
		});
		final boolean result = patchHelper.patch(selectedFile,
		                                         activeJvm,
		                                         clientHandler,
		                                         classLoaderDescriptor,
		                                         currentExportProgress -> Platform.runLater(() -> progress.set(
				                                         currentExportProgress)));
		Platform.runLater(() -> {
			isComplete.set(true);
			success.set(result);
		});
	}

	private File selectImportClassFile(Window owner) {
		return fileHelper.openClass(owner, JvmExplorer.getBundle().getString("ctx.replaceClass"));
	}

	private void replaceClass(File selectedFile, LoadedClass loadedClass, RunningJvm activeJvm) {
		final byte[] contents;
		try {
			contents = Files.readAllBytes(selectedFile.toPath());
		}
		catch (IOException ex) {
			log.warn("Failed to read file", ex);
			return;
		}
		final PatchResult replaced = clientHandler.replaceClass(activeJvm, loadedClass, contents);
		Platform.runLater(() -> {
			final ResourceBundle bundle = JvmExplorer.getBundle();
			if (!replaced.isSuccess()) {
				alertHelper.showError(bundle.getString("status.replaceFailed"), replaced.getMessage());
				return;
			}
			alertHelper.show(Alert.AlertType.INFORMATION, bundle.getString("status.replacedClass"), bundle.getString("status.replacedClassSuccess"));
		});
	}


	public ClassCellFactory(ExecutorService executorService, AlertHelper alertHelper, ObjectProperty<RunningJvm> currentJvm, ClientHandler clientHandler, FilterableTreeItem<ClassTreeNode> classesTreeRoot, ExportHelper exportHelper, Consumer<RunningJvm> onLoadClasses, JvmExplorerSettings settings) {
		this.executorService = executorService;
		this.alertHelper = alertHelper;
		this.currentJvm = currentJvm;
		this.clientHandler = clientHandler;
		this.classesTreeRoot = classesTreeRoot;
		this.exportHelper = exportHelper;
		this.onLoadClasses = onLoadClasses;
		this.settings = settings;
	}

}
