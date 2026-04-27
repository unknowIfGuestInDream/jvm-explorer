package com.tlcsdm.jvmexplorer.agent;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.tlcsdm.jvmexplorer.protocol.JvmClient;
import com.tlcsdm.jvmexplorer.protocol.PacketType;

import java.util.Queue;

/**
 * Provides the idle packet sender implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
public class IdlePacketSender<T> extends Listener {

	private final Queue<T[]> packets;
	private final PacketType packetType;
	private final JvmClient jvmClient;
	private final boolean autoEnd;

	private volatile boolean end;

	private int packetsSent = 0;

	private long nextSendPeriod = System.currentTimeMillis();

	// Sending too many objects at once overflows a buffer. Not sure why kryonet can't automatically handle it but
	// this works around that by only sending when the buffer isn't too full
	/**
	 * Performs the idle operation.
	 */
	@Override
	public void idle(Connection connection) {
		if (packets.isEmpty()) {
			if (autoEnd || end) {
				jvmClient.endPacketTransfer(packetType, packetsSent);
				connection.removeListener(this);
			}
		}
		else {
			// This is to work around an issue with kryonet. If too much writes are sent at once, it will prevent any
			// reading and kryonet will think it timed out.
			// Send data for 9 seconds, sleep for 1 second.
			if (System.currentTimeMillis() > nextSendPeriod + 9000) {
				nextSendPeriod = System.currentTimeMillis() + 1000;
			}
			if (System.currentTimeMillis() < nextSendPeriod) {
				// Waiting a bit for kryonet to process...
				return;
			}
			final T[] next = packets.poll();
			if (next == null) {
				return;
			}
			packetsSent += next.length;
			jvmClient.sendPacket(packetType, next);
		}
	}

	/**
	 * Performs the end operation.
	 */
	public void end() {
		this.end = true;
	}


	/**
	 * Creates a new IdlePacketSender instance.
	 */
	public IdlePacketSender(Queue<T[]> packets, PacketType packetType, JvmClient jvmClient, boolean autoEnd) {
		this.packets = packets;
		this.packetType = packetType;
		this.jvmClient = jvmClient;
		this.autoEnd = autoEnd;
	}

}
