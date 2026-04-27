package com.tlcsdm.jvmexplorer.agent;

/**
 * Provides the clean class loader store implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
public class CleanClassLoaderStore implements Runnable {

	private final ClassLoaderStore classLoaderStore;

	/**
	 * Runs the configured task.
	 */
	@Override
	public void run() {
		classLoaderStore.clean();
	}


	/**
	 * Creates a new CleanClassLoaderStore instance.
	 */
	public CleanClassLoaderStore(ClassLoaderStore classLoaderStore) {
		this.classLoaderStore = classLoaderStore;
	}

}
