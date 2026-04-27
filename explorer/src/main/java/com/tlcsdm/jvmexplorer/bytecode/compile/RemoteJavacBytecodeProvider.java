package com.tlcsdm.jvmexplorer.bytecode.compile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlcsdm.jvmexplorer.agent.RunningJvm;
import com.tlcsdm.jvmexplorer.net.ClientHandler;
import com.tlcsdm.jvmexplorer.protocol.LoadedClass;
import com.tlcsdm.jvmexplorer.protocol.helper.ClassNameHelper;

import javax.tools.JavaFileObject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides the remote javac bytecode provider implementation used by the com.tlcsdm.jvmexplorer.bytecode.compile package.
 */
public class RemoteJavacBytecodeProvider implements JavacBytecodeProvider {

	private static final Logger log = LoggerFactory.getLogger(RemoteJavacBytecodeProvider.class);


	private final ClientHandler clientHandler;
	private final RunningJvm runningJvm;

	private final List<LoadedClass> classpath;

	/**
	 * Handles the list request.
	 */
	@Override
	public List<JavaFileObject> list(String packageName, boolean recurse) {
		return classpath.stream()
		                .filter(l -> isClassInPackage(l, packageName, recurse))
		                .map(this::getProvidedJavaFileObject)
		                .collect(Collectors.toList());
	}

	/**
	 * Returns whether class in package is enabled or currently true.
	 */
	private boolean isClassInPackage(LoadedClass loadedClass, String packageName, boolean recurse) {
		final String classPackageName = ClassNameHelper.getPackageName(loadedClass.getName());
		if (recurse && classPackageName.startsWith(packageName + ".")) {
			return true;
		}
		return classPackageName.equals(packageName);
	}

	/**
	 * Returns the provided java file object value.
	 */
	private ProvidedJavaFileObject getProvidedJavaFileObject(LoadedClass loadedClass) {
		return new ProvidedJavaFileObject(loadedClass.getName(),
		                                  JavaFileObject.Kind.CLASS,
		                                  () -> clientHandler.getClassBytes(runningJvm, loadedClass));
	}


	/**
	 * Creates a new RemoteJavacBytecodeProvider instance.
	 */
	public RemoteJavacBytecodeProvider(ClientHandler clientHandler, RunningJvm runningJvm, List<LoadedClass> classpath) {
		this.clientHandler = clientHandler;
		this.runningJvm = runningJvm;
		this.classpath = classpath;
	}

}
