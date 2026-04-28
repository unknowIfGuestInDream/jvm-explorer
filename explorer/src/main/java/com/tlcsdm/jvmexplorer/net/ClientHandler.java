package com.tlcsdm.jvmexplorer.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.tlcsdm.jvmexplorer.agent.RunningJvm;
import com.tlcsdm.jvmexplorer.protocol.ClassContent;
import com.tlcsdm.jvmexplorer.protocol.ClassFieldPath;
import com.tlcsdm.jvmexplorer.protocol.ClassFields;
import com.tlcsdm.jvmexplorer.protocol.ClassLoaderDescriptor;
import com.tlcsdm.jvmexplorer.protocol.ExecutionResult;
import com.tlcsdm.jvmexplorer.protocol.JvmConnection;
import com.tlcsdm.jvmexplorer.protocol.LoadedClass;
import com.tlcsdm.jvmexplorer.protocol.PacketType;
import com.tlcsdm.jvmexplorer.protocol.PatchResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Provides the client handler implementation used by the com.tlcsdm.jvmexplorer.net package.
 */
public class ClientHandler extends Listener {

	private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);


	private final Set<JvmClientImpl> clients = ConcurrentHashMap.newKeySet();

	private final BiConsumer<RunningJvm, Connection> onConnect;
	private final Consumer<RunningJvm> onDisconnect;

	/**
	 * Returns the class content value.
	 */
	public ClassContent getClassContent(RunningJvm runningJvm, LoadedClass loadedClass) {
		return getJvmConnection(runningJvm).map(j -> j.getClassContent(loadedClass)).orElse(null);
	}

	/**
	 * Returns the jvm connection value.
	 */
	private Optional<JvmConnection> getJvmConnection(RunningJvm runningJvm) {
		return getServerTracker(runningJvm).map(JvmClientImpl::getJvmConnection);
	}

	/**
	 * Returns the server tracker value.
	 */
	private Optional<JvmClientImpl> getServerTracker(RunningJvm runningJvm) {
		return clients.stream()
		              .filter(JvmClientImpl::isRegistered)
		              .filter(s -> s.getRunningJvm().equals(runningJvm))
		              .findFirst();
	}

	/**
	 * Updates the field value.
	 */
	public boolean setField(RunningJvm runningJvm, ClassFieldPath classFieldPath, Object newValue) {
		return getJvmConnection(runningJvm).map(j -> j.setField(classFieldPath, newValue)).orElse(false);
	}

	/**
	 * Returns the fields value.
	 */
	public ClassFields getFields(RunningJvm runningJvm, ClassFieldPath classFieldPath) {
		return getJvmConnection(runningJvm).map(j -> j.getFields(classFieldPath)).orElse(null);
	}

	/**
	 * Returns the class bytes value.
	 */
	public byte[] getClassBytes(RunningJvm runningJvm, LoadedClass loadedClass) {
		return getJvmConnection(runningJvm).map(j -> j.getClassBytes(loadedClass)).orElse(null);
	}

	/**
	 * Returns the loaded classes value.
	 */
	public List<LoadedClass> getLoadedClasses(RunningJvm runningJvm, Consumer<Integer> onUpdateCount) {
		return getServerTracker(runningJvm).map(serverTracker -> serverTracker.<LoadedClass>getPacketStream(PacketType.LOADED_CLASSES,
		                                                                                                    onUpdateCount))
		                                   .map(str -> str.collect(Collectors.toList()))
		                                   .orElse(null);
	}

	/**
	 * Closes the associated resource.
	 */
	public void close(RunningJvm runningJvm) {
		getServerTracker(runningJvm).ifPresent(Connection::close);
	}

	/**
	 * Handles the replace class workflow.
	 */
	public PatchResult replaceClass(RunningJvm runningJvm, LoadedClass loadedClass, byte[] bytes) {
		return getJvmConnection(runningJvm).map(jvmConnection -> jvmConnection.redefineClass(loadedClass, bytes))
		                                   .orElse(null);
	}

	public ExecutionResult executeCallable(RunningJvm runningJvm, String className, byte[] classFile,
	                                       ClassLoaderDescriptor classLoaderDescriptor) {
		return getJvmConnection(runningJvm).map(jvmConnection -> jvmConnection.executeCallable(className,
		                                                                                       classFile,
		                                                                                       classLoaderDescriptor))
		                                   .orElse(null);
	}

	/**
	 * Handles the connected callback.
	 *
	 * @startuml
	 * participant "KryoNet server" as Server
	 * participant "ClientHandler" as Handler
	 * participant "JvmClientImpl" as Client
	 * participant "Explorer workflow" as UI
	 *
	 * Server -> Handler: connected(connection)
	 * Handler -> Client: track connection
	 * Handler -> Client: set onRegister callback
	 * Client -> Handler: register(RunningJvm)
	 * Handler -> UI: onConnect(RunningJvm, connection)
	 * UI -> Handler: getLoadedClasses/getClassContent/replaceClass
	 * Handler -> Client: route request by RunningJvm
	 * Client --> Handler: JvmConnection result/packet stream
	 * Handler --> UI: protocol response
	 * @enduml
	 */
	@Override
	public void connected(Connection connection) {
		final JvmClientImpl serverTrackerImpl = (JvmClientImpl) connection;
		clients.add(serverTrackerImpl);
		serverTrackerImpl.setOnRegister(jvm -> this.onConnect.accept(jvm, connection));
	}

	/**
	 * Handles the disconnected callback.
	 */
	@Override
	public void disconnected(Connection connection) {
		final JvmClientImpl serverTrackerImpl = (JvmClientImpl) connection;
		clients.remove(serverTrackerImpl);
		if (serverTrackerImpl.isRegistered()) {
			onDisconnect.accept(serverTrackerImpl.getRunningJvm());
		}
	}


	/**
	 * Creates a new ClientHandler instance.
	 */
	public ClientHandler(BiConsumer<RunningJvm, Connection> onConnect, Consumer<RunningJvm> onDisconnect) {
		this.onConnect = onConnect;
		this.onDisconnect = onDisconnect;
	}

	/**
	 * Builds the configured result object.
	 */
	public static ClientHandlerBuilder builder() {
		return new ClientHandlerBuilder();
	}

	/**
	 * Provides the client handler builder implementation used by the com.tlcsdm.jvmexplorer.net package.
	 */
	public static class ClientHandlerBuilder {
		private BiConsumer<RunningJvm, Connection> onConnect;
		private Consumer<RunningJvm> onDisconnect;

		/**
		 * Handles the connect event.
		 */
		public ClientHandlerBuilder onConnect(BiConsumer<RunningJvm, Connection> onConnect) {
			this.onConnect = onConnect;
			return this;
		}

		/**
		 * Handles the disconnect event.
		 */
		public ClientHandlerBuilder onDisconnect(Consumer<RunningJvm> onDisconnect) {
			this.onDisconnect = onDisconnect;
			return this;
		}

		/**
		 * Builds the configured result object.
		 */
		public ClientHandler build() {
			return new ClientHandler(onConnect, onDisconnect);
		}
	}

}
