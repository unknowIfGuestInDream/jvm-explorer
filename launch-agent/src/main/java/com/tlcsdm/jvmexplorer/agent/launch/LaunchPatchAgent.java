package com.tlcsdm.jvmexplorer.agent.launch;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * Provides the launch patch agent implementation used by the com.tlcsdm.jvmexplorer.agent.launch package.
 */
public class LaunchPatchAgent {

	/**
	 * Starts the launch patch agent when it is supplied with {@code -javaagent}.
	 */
	public static void premain(String agentArgs, Instrumentation instrumentation) {
		main(agentArgs, instrumentation);
	}

	/**
	 * Implements shared launch patch agent initialization logic.
	 */
	private static void main(String agentArgs, Instrumentation instrumentation) {
		final ClassFileTransformer transformer = new LaunchPatchClassFileTransformer();
		instrumentation.addTransformer(transformer, true);
		try {
			instrumentation.retransformClasses(ProcessBuilder.class);
		}
		catch (UnmodifiableClassException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts the launch patch agent when it is attached to a running JVM.
	 */
	public static void agentmain(String agentArgs, Instrumentation instrumentation) {
		main(agentArgs, instrumentation);
	}

}
