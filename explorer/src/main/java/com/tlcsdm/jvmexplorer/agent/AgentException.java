package com.tlcsdm.jvmexplorer.agent;

/**
 * Provides the agent exception implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
public class AgentException extends Exception {

	/**
	 * Creates a new AgentException instance.
	 */
	public AgentException(String message) {
		super(message);
	}

	/**
	 * Creates a new AgentException instance.
	 */
	public AgentException(String message, Exception source) {
		super(message, source);
	}

}
