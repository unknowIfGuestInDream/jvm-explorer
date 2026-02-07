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

public class ClientHandler extends Listener {

	private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);


	private final Set<JvmClientImpl> clients = ConcurrentHashMap.newKeySet();

	private final BiConsumer<RunningJvm, Connection> onConnect;
	private final Consumer<RunningJvm> onDisconnect;

	public ClassContent getClassContent(RunningJvm runningJvm, LoadedClass loadedClass) {
		return getJvmConnection(runningJvm).map(j -> j.getClassContent(loadedClass)).orElse(null);
	}

	private Optional<JvmConnection> getJvmConnection(RunningJvm runningJvm) {
		return getServerTracker(runningJvm).map(JvmClientImpl::getJvmConnection);
	}

	private Optional<JvmClientImpl> getServerTracker(RunningJvm runningJvm) {
		return clients.stream()
		              .filter(JvmClientImpl::isRegistered)
		              .filter(s -> s.getRunningJvm().equals(runningJvm))
		              .findFirst();
	}

	public boolean setField(RunningJvm runningJvm, ClassFieldPath classFieldPath, Object newValue) {
		return getJvmConnection(runningJvm).map(j -> j.setField(classFieldPath, newValue)).orElse(false);
	}

	public ClassFields getFields(RunningJvm runningJvm, ClassFieldPath classFieldPath) {
		return getJvmConnection(runningJvm).map(j -> j.getFields(classFieldPath)).orElse(null);
	}

	public byte[] getClassBytes(RunningJvm runningJvm, LoadedClass loadedClass) {
		return getJvmConnection(runningJvm).map(j -> j.getClassBytes(loadedClass)).orElse(null);
	}

	public List<LoadedClass> getLoadedClasses(RunningJvm runningJvm, Consumer<Integer> onUpdateCount) {
		return getServerTracker(runningJvm).map(serverTracker -> serverTracker.<LoadedClass>getPacketStream(PacketType.LOADED_CLASSES,
		                                                                                                    onUpdateCount))
		                                   .map(str -> str.collect(Collectors.toList()))
		                                   .orElse(null);
	}

	public void close(RunningJvm runningJvm) {
		getServerTracker(runningJvm).ifPresent(Connection::close);
	}

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

	@Override
	public void connected(Connection connection) {
		final JvmClientImpl serverTrackerImpl = (JvmClientImpl) connection;
		clients.add(serverTrackerImpl);
		serverTrackerImpl.setOnRegister(jvm -> this.onConnect.accept(jvm, connection));
	}

	@Override
	public void disconnected(Connection connection) {
		final JvmClientImpl serverTrackerImpl = (JvmClientImpl) connection;
		clients.remove(serverTrackerImpl);
		if (serverTrackerImpl.isRegistered()) {
			onDisconnect.accept(serverTrackerImpl.getRunningJvm());
		}
	}


	public ClientHandler(BiConsumer<RunningJvm, Connection> onConnect, Consumer<RunningJvm> onDisconnect) {
		this.onConnect = onConnect;
		this.onDisconnect = onDisconnect;
	}

	public static ClientHandlerBuilder builder() {
		return new ClientHandlerBuilder();
	}

	public static class ClientHandlerBuilder {
		private BiConsumer<RunningJvm, Connection> onConnect;
		private Consumer<RunningJvm> onDisconnect;

		public ClientHandlerBuilder onConnect(BiConsumer<RunningJvm, Connection> onConnect) {
			this.onConnect = onConnect;
			return this;
		}

		public ClientHandlerBuilder onDisconnect(Consumer<RunningJvm> onDisconnect) {
			this.onDisconnect = onDisconnect;
			return this;
		}

		public ClientHandler build() {
			return new ClientHandler(onConnect, onDisconnect);
		}
	}

}
