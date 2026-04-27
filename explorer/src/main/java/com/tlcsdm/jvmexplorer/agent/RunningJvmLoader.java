package com.tlcsdm.jvmexplorer.agent;

import com.sun.tools.attach.VirtualMachine;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides the running jvm loader implementation used by the com.tlcsdm.jvmexplorer.agent package.
 */
public class RunningJvmLoader {

	private static final String THIS_PID = ManagementFactory.getRuntimeMXBean().getPid() + "";

	/**
	 * Performs the list operation.
	 */
	public List<RunningJvm> list() {
		return VirtualMachine.list()
		                     .stream()
		                     .filter(vmd -> !vmd.id().equals(THIS_PID))
		                     .map(vmd -> new RunningJvm(vmd.id(), vmd.displayName()))
		                     .collect(Collectors.toList());
	}

}
