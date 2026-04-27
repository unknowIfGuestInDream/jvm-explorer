package com.tlcsdm.jvmexplorer.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.rmi.ObjectSpace;
import com.esotericsoftware.kryonet.rmi.RemoteObject;
import com.tlcsdm.jvmexplorer.agent.RunningJvm;
import com.tlcsdm.jvmexplorer.protocol.JvmClient;
import com.tlcsdm.jvmexplorer.protocol.JvmConnection;
import com.tlcsdm.jvmexplorer.protocol.PacketType;
import com.tlcsdm.jvmexplorer.protocol.Protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Provides the jvm client impl implementation used by the com.tlcsdm.jvmexplorer.net package.
 */
public class JvmClientImpl extends Connection implements JvmClient {

	private static final Logger log = LoggerFactory.getLogger(JvmClientImpl.class);


	private final Map<PacketType, PacketResponseHandler<?>> packetResponseHandlers = new ConcurrentHashMap<>();
	private final ScheduledExecutorService executorService;

	private final JvmConnection jvmConnection;

	private volatile Consumer<RunningJvm> onRegister;

	private volatile RunningJvm runningJvm;

	/**
	 * Creates a new JvmClientImpl instance.
	 */
	public JvmClientImpl(ScheduledExecutorService executorService) {
		this.executorService = executorService;
		final ObjectSpace objectSpace = new ObjectSpace(this);
		objectSpace.setExecutor(executorService);
		objectSpace.register(Protocol.RMI_JVM_CLIENT, this);
		jvmConnection = ObjectSpace.getRemoteObject(this, Protocol.RMI_JVM_CONNECTION, JvmConnection.class);
		// Loading class names can take a bit
		((RemoteObject) jvmConnection).setResponseTimeout(30000);
		((RemoteObject) jvmConnection).setTransmitExceptions(false);
	}

	/**
	 * Registers the endpoint.
	 */
	@Override
	public void register(String identifier) {
		if (this.runningJvm != null) {
			close();
			return;
		}
		final String[] id = identifier.split(":", 2);
		if (id.length != 2) {
			close();
			return;
		}
		this.runningJvm = new RunningJvm(id[0], id[1]);
		final Consumer<RunningJvm> onRegister = this.onRegister;
		if (onRegister != null) {
			onRegister.accept(this.runningJvm);
		}
	}

	/**
	 * Sends packet.
	 */
	@Override
	public <T> void sendPacket(PacketType packetType, T[] packet) {
		final PacketResponseHandler<T> packetResponseHandler = (PacketResponseHandler<T>) packetResponseHandlers.get(
				packetType);
		if (packetResponseHandler != null) {
			packetResponseHandler.onPacketReceived(packet);
		}
		else {
			log.warn("Received packets but no packet handler set");
		}
	}

	/**
	 * Handles the end packet transfer workflow.
	 */
	@Override
	public void endPacketTransfer(PacketType packetType, int packetsSent) {
		final PacketResponseHandler<?> packetResponseHandler = packetResponseHandlers.get(packetType);
		if (packetResponseHandler != null) {
			log.debug("Received all packets for {}", packetType);
			packetResponseHandler.receivedEnd(packetsSent);
		}
		else {
			log.warn("Received packets but no packet handler set");
		}
	}

	/**
	 * Closes the associated resource.
	 */
	@Override
	public void close() {
		super.close();
		packetResponseHandlers.values().forEach(PacketResponseHandler::interrupt);
		packetResponseHandlers.clear();
	}

	/**
	 * Returns whether registered is enabled or currently true.
	 */
	public boolean isRegistered() {
		return runningJvm != null;
	}

	/**
	 * Returns the packet stream value.
	 */
	public <T> Stream<T> getPacketStream(PacketType packetType, Consumer<Integer> onUpdateCount) {
		final AtomicReference<Future<?>> scheduledCleanup = new AtomicReference<>();
		final PacketResponseHandler<T> packetResponseHandler = new PacketResponseHandler<>(() -> {
			log.debug("Cleaning up packet stream for {}", packetType);
			packetResponseHandlers.remove(packetType);
			final Future<?> cleanup = scheduledCleanup.get();
			if (cleanup != null) {
				log.debug("Cancelling cleanup task for {}", packetType);
				cleanup.cancel(false);
			}
		}, onUpdateCount);
		packetResponseHandlers.put(packetType, packetResponseHandler);
		final Future<?> cleanup = executorService.schedule(packetResponseHandler::interrupt, 310, TimeUnit.SECONDS);
		scheduledCleanup.set(cleanup);
		getJvmConnection().requestPackets(packetType);
		// Exports can take some time
		return packetResponseHandler.getPacketStream(300, TimeUnit.SECONDS);
	}


	/**
	 * Returns the jvm connection value.
	 */
	public JvmConnection getJvmConnection() {
		return this.jvmConnection;
	}

	/**
	 * Updates the on register value.
	 */
	public void setOnRegister(Consumer<RunningJvm> onRegister) {
		this.onRegister = onRegister;
	}

	/**
	 * Returns the running jvm value.
	 */
	public RunningJvm getRunningJvm() {
		return this.runningJvm;
	}

}
