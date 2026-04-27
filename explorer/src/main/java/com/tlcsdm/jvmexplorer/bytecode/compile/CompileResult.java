package com.tlcsdm.jvmexplorer.bytecode.compile;

import java.util.Arrays;

/**
 * Provides the compile result implementation used by the com.tlcsdm.jvmexplorer.bytecode.compile package.
 */
public class CompileResult {

	private final String stdOut;
	private final boolean success;
	private final byte[] classContent;


	/**
	 * Creates a new CompileResult instance.
	 */
	public CompileResult(String stdOut, boolean success, byte[] classContent) {
		this.stdOut = stdOut;
		this.success = success;
		this.classContent = classContent;
	}

	/**
	 * Creates a new CompileResult instance.
	 */
	public CompileResult() {
		this.stdOut = null;
		this.success = false;
		this.classContent = null;
	}

	/**
	 * Returns the std out value.
	 */
	public String getStdOut() {
		return this.stdOut;
	}

	/**
	 * Returns whether success is enabled or currently true.
	 */
	public boolean isSuccess() {
		return this.success;
	}

	/**
	 * Returns the class content value.
	 */
	public byte[] getClassContent() {
		return this.classContent;
	}

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return "CompileResult(stdOut=" + stdOut + ", success=" + success + ", classContent=" + Arrays.toString(classContent) + ")";
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompileResult other = (CompileResult) o;
		return java.util.Objects.equals(this.stdOut, other.stdOut) && java.util.Objects.equals(this.success, other.success) && Arrays.equals(this.classContent, other.classContent);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(stdOut, success, Arrays.hashCode(classContent));
	}

	/**
	 * Builds the configured result object.
	 */
	public static CompileResultBuilder builder() {
		return new CompileResultBuilder();
	}

	/**
	 * Provides the compile result builder implementation used by the com.tlcsdm.jvmexplorer.bytecode.compile package.
	 */
	public static class CompileResultBuilder {
		private String stdOut;
		private boolean success;
		private byte[] classContent;

		/**
		 * Performs the std out operation.
		 */
		public CompileResultBuilder stdOut(String stdOut) {
			this.stdOut = stdOut;
			return this;
		}

		/**
		 * Performs the success operation.
		 */
		public CompileResultBuilder success(boolean success) {
			this.success = success;
			return this;
		}

		/**
		 * Performs the class content operation.
		 */
		public CompileResultBuilder classContent(byte[] classContent) {
			this.classContent = classContent;
			return this;
		}

		/**
		 * Builds the configured result object.
		 */
		public CompileResult build() {
			return new CompileResult(stdOut, success, classContent);
		}
	}

}
