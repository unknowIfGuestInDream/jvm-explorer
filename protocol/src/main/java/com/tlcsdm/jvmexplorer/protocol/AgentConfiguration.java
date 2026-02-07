package com.tlcsdm.jvmexplorer.protocol;


import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

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

	public static AgentConfiguration parseAgentArgs(String agentArgs) {
		final Properties properties = new Properties();
		try {
			properties.load(new StringReader(agentArgs));
		}
		catch (IOException e) {
			// Should never happen
			throw new IllegalStateException(e);
		}
		return AgentConfiguration.builder()
		                         .port(Integer.parseInt(properties.getProperty(PORT_KEY)))
		                         .hostName(properties.getProperty(HOST_NAME_KEY))
		                         .identifier(properties.getProperty(IDENTIFIER_KEY))
		                         .logLevel(Integer.parseInt(properties.getProperty(LOG_LEVEL_KEY)))
		                         .logFilePath(properties.getProperty(LOG_FILE_PATH_KEY))
		                         .build();
	}

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


	public AgentConfiguration(static final String PORT_KEY, static final String IDENTIFIER_KEY, static final String HOST_NAME_KEY, static final String LOG_LEVEL_KEY, static final String LOG_FILE_PATH_KEY, int port, String identifier, String hostName, int logLevel, String logFilePath) {
		this.PORT_KEY = PORT_KEY;
		this.IDENTIFIER_KEY = IDENTIFIER_KEY;
		this.HOST_NAME_KEY = HOST_NAME_KEY;
		this.LOG_LEVEL_KEY = LOG_LEVEL_KEY;
		this.LOG_FILE_PATH_KEY = LOG_FILE_PATH_KEY;
		this.port = port;
		this.identifier = identifier;
		this.hostName = hostName;
		this.logLevel = logLevel;
		this.logFilePath = logFilePath;
	}


	public AgentConfiguration() {
	}

	public static final String getPORT_KEY() {
		return this.PORT_KEY;
	}

	public static final String getIDENTIFIER_KEY() {
		return this.IDENTIFIER_KEY;
	}

	public static final String getHOST_NAME_KEY() {
		return this.HOST_NAME_KEY;
	}

	public static final String getLOG_LEVEL_KEY() {
		return this.LOG_LEVEL_KEY;
	}

	public static final String getLOG_FILE_PATH_KEY() {
		return this.LOG_FILE_PATH_KEY;
	}

	public int getPort() {
		return this.port;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public String getHostName() {
		return this.hostName;
	}

	public int getLogLevel() {
		return this.logLevel;
	}

	public String getLogFilePath() {
		return this.logFilePath;
	}


	@Override
	public String toString() {
		return "AgentConfiguration(" + "PORT_KEY=" + PORT_KEY + ", IDENTIFIER_KEY=" + IDENTIFIER_KEY + ", HOST_NAME_KEY=" + HOST_NAME_KEY + ", LOG_LEVEL_KEY=" + LOG_LEVEL_KEY + ", LOG_FILE_PATH_KEY=" + LOG_FILE_PATH_KEY + ", port=" + port + ", identifier=" + identifier + ", hostName=" + hostName + ", logLevel=" + logLevel + ", logFilePath=" + logFilePath" + ")";
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentConfiguration other = (AgentConfiguration) o;
		return java.util.Objects.equals(this.PORT_KEY, other.PORT_KEY) && java.util.Objects.equals(this.IDENTIFIER_KEY, other.IDENTIFIER_KEY) && java.util.Objects.equals(this.HOST_NAME_KEY, other.HOST_NAME_KEY) && java.util.Objects.equals(this.LOG_LEVEL_KEY, other.LOG_LEVEL_KEY) && java.util.Objects.equals(this.LOG_FILE_PATH_KEY, other.LOG_FILE_PATH_KEY) && java.util.Objects.equals(this.port, other.port) && java.util.Objects.equals(this.identifier, other.identifier) && java.util.Objects.equals(this.hostName, other.hostName) && java.util.Objects.equals(this.logLevel, other.logLevel) && java.util.Objects.equals(this.logFilePath, other.logFilePath);
	}


	@Override
	public int hashCode() {
		return java.util.Objects.hash(PORT_KEY, IDENTIFIER_KEY, HOST_NAME_KEY, LOG_LEVEL_KEY, LOG_FILE_PATH_KEY, port, identifier, hostName, logLevel, logFilePath);
	}


	public static AgentConfigurationBuilder builder() {
		return new AgentConfigurationBuilder();
	}

	public static class AgentConfigurationBuilder {
		private static final String PORT_KEY;
		private static final String IDENTIFIER_KEY;
		private static final String HOST_NAME_KEY;
		private static final String LOG_LEVEL_KEY;
		private static final String LOG_FILE_PATH_KEY;
		private int port;
		private String identifier;
		private String hostName;
		private int logLevel;
		private String logFilePath;

		public AgentConfigurationBuilder PORT_KEY(static final String PORT_KEY) {
			this.PORT_KEY = PORT_KEY;
			return this;
		}
		public AgentConfigurationBuilder IDENTIFIER_KEY(static final String IDENTIFIER_KEY) {
			this.IDENTIFIER_KEY = IDENTIFIER_KEY;
			return this;
		}
		public AgentConfigurationBuilder HOST_NAME_KEY(static final String HOST_NAME_KEY) {
			this.HOST_NAME_KEY = HOST_NAME_KEY;
			return this;
		}
		public AgentConfigurationBuilder LOG_LEVEL_KEY(static final String LOG_LEVEL_KEY) {
			this.LOG_LEVEL_KEY = LOG_LEVEL_KEY;
			return this;
		}
		public AgentConfigurationBuilder LOG_FILE_PATH_KEY(static final String LOG_FILE_PATH_KEY) {
			this.LOG_FILE_PATH_KEY = LOG_FILE_PATH_KEY;
			return this;
		}
		public AgentConfigurationBuilder port(int port) {
			this.port = port;
			return this;
		}
		public AgentConfigurationBuilder identifier(String identifier) {
			this.identifier = identifier;
			return this;
		}
		public AgentConfigurationBuilder hostName(String hostName) {
			this.hostName = hostName;
			return this;
		}
		public AgentConfigurationBuilder logLevel(int logLevel) {
			this.logLevel = logLevel;
			return this;
		}
		public AgentConfigurationBuilder logFilePath(String logFilePath) {
			this.logFilePath = logFilePath;
			return this;
		}

		public AgentConfiguration build() {
			return new AgentConfiguration(PORT_KEY, IDENTIFIER_KEY, HOST_NAME_KEY, LOG_LEVEL_KEY, LOG_FILE_PATH_KEY, port, identifier, hostName, logLevel, logFilePath);
		}
	}

}
