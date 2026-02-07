package com.tlcsdm.jvmexplorer.agent;

public class CleanClassLoaderStore implements Runnable {

	private final ClassLoaderStore classLoaderStore;

	@Override
	public void run() {
		classLoaderStore.clean();
	}


	public CleanClassLoaderStore(ClassLoaderStore classLoaderStore) {
		this.classLoaderStore = classLoaderStore;
	}

}
