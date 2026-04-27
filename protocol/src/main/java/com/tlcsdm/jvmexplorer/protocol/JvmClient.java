package com.tlcsdm.jvmexplorer.protocol;

// Implemented in the server
/**
 * Defines the contract for jvm client behavior in the com.tlcsdm.jvmexplorer.protocol package.
 */
public interface JvmClient {

	/**
	 * Registers the agent connection with its identifier.
	 */
	void register(String identifier);

	/**
	 * Sends a batch of packets to the remote endpoint.
	 */
	<T> void sendPacket(PacketType packetType, T[] packets);

	/**
	 * Marks a packet transfer as complete.
	 */
	void endPacketTransfer(PacketType packetType, int packetsSent);

}
