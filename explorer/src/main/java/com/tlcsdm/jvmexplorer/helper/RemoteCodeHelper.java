package com.tlcsdm.jvmexplorer.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlcsdm.jvmexplorer.agent.RunningJvm;
import com.tlcsdm.jvmexplorer.fx.compile.RemoteCodeExecutorController;
import com.tlcsdm.jvmexplorer.net.ClientHandler;
import com.tlcsdm.jvmexplorer.protocol.ClassLoaderDescriptor;
import com.tlcsdm.jvmexplorer.protocol.LoadedClass;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RemoteCodeHelper {

	private static final Logger log = LoggerFactory.getLogger(RemoteCodeHelper.class);


	public void showExecuteCode(Window owner, List<LoadedClass> classpath, ClassLoaderDescriptor classLoaderDescriptor,
	                            String packageName, ExecutorService executorService, ClientHandler clientHandler,
	                            ObjectProperty<RunningJvm> currentJvm) {
		try {
			final Dialog<?> dialog = new Dialog<>();
			final FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
			                                                   .getResource("fxml/remote_code_executor.fxml"));
			final Parent root = loader.load();
			final RemoteCodeExecutorController remoteCodeExecutorController = loader.getController();
			remoteCodeExecutorController.initialize(executorService,
			                                        clientHandler,
			                                        currentJvm.get(),
			                                        classLoaderDescriptor,
			                                        packageName,
			                                        classpath);
			dialog.getDialogPane().setContent(root);
			final String title = Stream.of("Remote Code Executor", classLoaderDescriptor, packageName)
			                           .filter(Objects::nonNull)
			                           .map(Object::toString)
			                           .map(String::trim)
			                           .filter(s -> !s.isEmpty())
			                           .collect(Collectors.joining(" - "));
			dialog.setTitle(title);
			dialog.initOwner(owner);
			dialog.setResizable(true);
			DialogHelper.initCustomDialog(dialog, currentJvm);
			dialog.show();
		}
		catch (IOException e) {
			log.warn("Failed to initialize code executor", e);
		}
	}

}
