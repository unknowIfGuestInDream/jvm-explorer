package com.tlcsdm.jvmexplorer.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tlcsdm.jvmexplorer.JvmExplorer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides the agent preparer implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
public class AgentPreparer {

	private static final Logger log = LoggerFactory.getLogger(AgentPreparer.class);

	private static final Path AGENT_DIR = JvmExplorer.APP_DIR.toPath().resolve("agents");

	private final Map<String, String> loadedAgentFiles = new ConcurrentHashMap<>();

	/**
	 * Loads agent on file system.
	 *
	 * @startuml
	 * start
	 * :loadAgentOnFileSystem(resourcePath);
	 * if (path cached?) then (yes)
	 *   :return cached absolute path;
	 * else (no)
	 *   if (resource exists in working directory?) then (yes)
	 *     :cache and return local file;
	 *   else (no)
	 *     :create app agents directory;
	 *     :read agent resource bytes;
	 *     :overwrite stable agent file;
	 *     :cache and return extracted path;
	 *   endif
	 * endif
	 * stop
	 * @enduml
	 */
	public String loadAgentOnFileSystem(String agentResourcePath) {
		return loadedAgentFiles.computeIfAbsent(agentResourcePath, k -> {
			// First, try to load from the running path (working directory)
			Path localFile = Paths.get(agentResourcePath);
			if (Files.exists(localFile)) {
				String localPath = localFile.toAbsolutePath().toString();
				log.debug("Loaded agent from local path: {}", localPath);
				return localPath;
			}

			try {
				// Use a stable, descriptive file name in the application directory
				// instead of a randomly-named file in the system temp directory.
				// This avoids triggering security software that monitors temp directories
				// for suspicious file creation patterns.
				// Always overwrite to ensure the agent matches the current explorer version.
				Files.createDirectories(AGENT_DIR);
				final String fileName = Paths.get(agentResourcePath).getFileName().toString();
				final Path agentFile = AGENT_DIR.resolve(fileName);
				try (InputStream inputStream = AgentPreparer.class.getClassLoader()
				                                                   .getResourceAsStream(agentResourcePath)) {
					if (inputStream == null) {
						throw new IOException("Failed to find input stream for agent");
					}
					final byte[] agentBytes = inputStream.readAllBytes();
					Files.write(agentFile, agentBytes,
					            StandardOpenOption.CREATE,
					            StandardOpenOption.TRUNCATE_EXISTING,
					            StandardOpenOption.WRITE);
				}
				final String localPath = agentFile.toAbsolutePath().toString();
				log.debug("Loaded agent into app directory: {}", localPath);
				return localPath;
			}
			catch (IOException e) {
				log.warn("Failed to load agent into file", e);
				throw new UncheckedIOException(e);
			}
		});

	}

}
