package com.tlcsdm.jvmexplorer.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgentPreparer {

	private static final Logger log = LoggerFactory.getLogger(AgentPreparer.class);


	private final Map<String, String> tempAgentFiles = new ConcurrentHashMap<>();

	public String loadAgentOnFileSystem(String agentResourcePath) {
		return tempAgentFiles.computeIfAbsent(agentResourcePath, k -> {
			// First, try to load from the running path (working directory)
			Path localFile = Paths.get(agentResourcePath);
			if (Files.exists(localFile)) {
				String localPath = localFile.toAbsolutePath().toString();
				log.debug("Loaded agent from local path: {}", localPath);
				return localPath;
			}

			try {
				final Path tempDir = Files.createTempDirectory("jvm-explorer");
				// Register directory deletion first so it is deleted last
				// (deleteOnExit hooks execute in reverse registration order:
				// file is deleted first, then the empty directory)
				tempDir.toFile().deleteOnExit();
				final Path tempFile = tempDir.resolve("jvm-explorer-agent.jar");
				tempFile.toFile().deleteOnExit();
				try (InputStream inputStream = AgentPreparer.class.getClassLoader()
				                                                   .getResourceAsStream(agentResourcePath)) {
					if (inputStream == null) {
						throw new IOException("Failed to find input stream for agent");
					}
					final byte[] agentBytes = inputStream.readAllBytes();
					Files.write(tempFile, agentBytes);
				}
				final String localPath = tempFile.toAbsolutePath().toString();
				log.debug("Loaded agent into temp file: {}", localPath);
				return localPath;
			}
			catch (IOException e) {
				log.warn("Failed to load agent into file", e);
				throw new UncheckedIOException(e);
			}
		});

	}

}
