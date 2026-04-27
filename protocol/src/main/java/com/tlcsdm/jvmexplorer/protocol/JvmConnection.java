package com.tlcsdm.jvmexplorer.protocol;

// Implemented in the client
/**
 * Defines the contract for jvm connection behavior in the com.tlcsdm.jvmexplorer.protocol package.
 */
public interface JvmConnection {

	/**
	 * Loads the class bytes and field snapshot for a class in the target JVM.
	 */
	ClassContent getClassContent(LoadedClass loadedClass);

	/**
	 * Updates a field value in the target JVM.
	 */
	boolean setField(ClassFieldPath classFieldPath, Object newValue);

	/**
	 * Loads fields for a class or nested field path from the target JVM.
	 */
	ClassFields getFields(ClassFieldPath classFieldPath);

	/**
	 * Loads raw class bytes for a class in the target JVM.
	 */
	byte[] getClassBytes(LoadedClass loadedClass);

	/**
	 * Requests the next packet batch for a streaming packet type.
	 */
	void requestPackets(PacketType packetType);

	/**
	 * Applies replacement class bytes to a loaded class.
	 */
	PatchResult redefineClass(LoadedClass loadedClass, byte[] bytes);

	/**
	 * Executes a callable class in the selected class loader.
	 */
	ExecutionResult executeCallable(String className, byte[] classFile, ClassLoaderDescriptor classLoaderDescriptor);

}
