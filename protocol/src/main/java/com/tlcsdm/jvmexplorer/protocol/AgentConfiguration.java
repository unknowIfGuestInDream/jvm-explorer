package com.tlcsdm.jvmexplorer.protocol;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

/**
 * Provides the agent configuration implementation used by the com.tlcsdm.jvmexplorer.protocol package.
 */
public class AgentConfiguration {

	private static final String PORT_KEY = "port";
	private static final String IDENTIFIER_KEY = "identifier";
	private static final String HOST_NAME_KEY = "hostName";
	private static final String LOG_LEVEL_KEY = "logLevel";
	private static final String LOG_FILE_PATH_KEY = "logFilePath";

	private final int port;
	private final String identifier;
	private final String hostName;
	private final int logLevel;
	private final String logFilePath;

	/**
	 * Parses agent args.
	 */
	public static AgentConfiguration parseAgentArgs(String agentArgs) {
		if (agentArgs == null || agentArgs.isBlank()) {
			throw new IllegalArgumentException("Agent args must not be null or blank");
		}
		final Properties properties = new Properties();
		try {
			properties.load(new StringReader(agentArgs));
		}
		catch (IOException e) {
			// Should never happen
			throw new IllegalStateException(e);
		}
		try {
			return AgentConfiguration.builder()
			                         .port(Integer.parseInt(properties.getProperty(PORT_KEY)))
			                         .hostName(properties.getProperty(HOST_NAME_KEY))
			                         .identifier(properties.getProperty(IDENTIFIER_KEY))
			                         .logLevel(Integer.parseInt(properties.getProperty(LOG_LEVEL_KEY)))
			                         .logFilePath(properties.getProperty(LOG_FILE_PATH_KEY))
			                         .build();
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid agent args: failed to parse numeric properties", e);
		}
	}

	/**
	 * Converts this instance to agent args.
	 */
	public String toAgentArgs() {
		final Properties properties = new Properties();
		properties.setProperty(PORT_KEY, Integer.toString(port));
		properties.setProperty(IDENTIFIER_KEY, identifier);
		properties.setProperty(HOST_NAME_KEY, hostName);
		properties.setProperty(LOG_LEVEL_KEY, Integer.toString(logLevel));
		properties.setProperty(LOG_FILE_PATH_KEY, logFilePath);
		final StringWriter stringWriter = new StringWriter();
		try {
			properties.store(stringWriter, null);
		}
		catch (IOException e) {
			// Should never happen
			throw new IllegalStateException(e);
		}
		return stringWriter.toString();
	}


	/**
	 * Creates a new AgentConfiguration instance.
	 */
	public AgentConfiguration(int port, String identifier, String hostName, int logLevel, String logFilePath) {
		this.port = port;
		this.identifier = identifier;
		this.hostName = hostName;
		this.logLevel = logLevel;
		this.logFilePath = logFilePath;
	}

	/**
	 * Creates a new AgentConfiguration instance.
	 */
	public AgentConfiguration() {
		this.port = 0;
		this.identifier = null;
		this.hostName = null;
		this.logLevel = 0;
		this.logFilePath = null;
	}

	/**
	 * Returns the port value.
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Returns the identifier value.
	 */
	public String getIdentifier() {
		return this.identifier;
	}

	/**
	 * Returns the host name value.
	 */
	public String getHostName() {
		return this.hostName;
	}

	/**
	 * Returns the log level value.
	 */
	public int getLogLevel() {
		return this.logLevel;
	}

	/**
	 * Returns the log file path value.
	 */
	public String getLogFilePath() {
		return this.logFilePath;
	}

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return "AgentConfiguration(port=" + port + ", identifier=" + identifier + ", hostName=" + hostName + ", logLevel=" + logLevel + ", logFilePath=" + logFilePath + ")";
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentConfiguration other = (AgentConfiguration) o;
		return java.util.Objects.equals(this.port, other.port) && java.util.Objects.equals(this.identifier, other.identifier) && java.util.Objects.equals(this.hostName, other.hostName) && java.util.Objects.equals(this.logLevel, other.logLevel) && java.util.Objects.equals(this.logFilePath, other.logFilePath);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(port, identifier, hostName, logLevel, logFilePath);
	}

	/**
	 * Builds the configured result object.
	 */
	public static AgentConfigurationBuilder builder() {
		return new AgentConfigurationBuilder();
	}

	/**
	 * Provides the agent configuration builder implementation used by the com.tlcsdm.jvmexplorer.protocol package.
	 */
	public static class AgentConfigurationBuilder {
		private int port;
		private String identifier;
		private String hostName;
		private int logLevel;
		private String logFilePath;

		/**
		 * Sets the port value on this builder.
		 */
		public AgentConfigurationBuilder port(int port) {
			this.port = port;
			return this;
		}

		/**
		 * Sets the identifier value on this builder.
		 */
		public AgentConfigurationBuilder identifier(String identifier) {
			this.identifier = identifier;
			return this;
		}

		/**
		 * Sets the host name value on this builder.
		 */
		public AgentConfigurationBuilder hostName(String hostName) {
			this.hostName = hostName;
			return this;
		}

		/**
		 * Sets the log level value on this builder.
		 */
		public AgentConfigurationBuilder logLevel(int logLevel) {
			this.logLevel = logLevel;
			return this;
		}

		/**
		 * Handles the log file path workflow.
		 */
		public AgentConfigurationBuilder logFilePath(String logFilePath) {
			this.logFilePath = logFilePath;
			return this;
		}

		/**
		 * Builds the configured result object.
		 */
		public AgentConfiguration build() {
			return new AgentConfiguration(port, identifier, hostName, logLevel, logFilePath);
		}
	}

}
