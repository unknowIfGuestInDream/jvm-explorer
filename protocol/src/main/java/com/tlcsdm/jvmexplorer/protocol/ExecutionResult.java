package com.tlcsdm.jvmexplorer.protocol;

/**
 * Provides the execution result implementation used by the com.tlcsdm.jvmexplorer.protocol package.
 */
public class ExecutionResult {

	private final boolean success;
	private final String message;


	/**
	 * Creates a new ExecutionResult instance.
	 */
	public ExecutionResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	/**
	 * Creates a new ExecutionResult instance.
	 */
	public ExecutionResult() {
		this.success = false;
		this.message = null;
	}

	/**
	 * Returns whether success is enabled or currently true.
	 */
	public boolean isSuccess() {
		return this.success;
	}

	/**
	 * Returns the message value.
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Returns a readable description of this instance.
	 */
	@Override
	public String toString() {
		return "ExecutionResult(success=" + success + ", message=" + message + ")";
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ExecutionResult other = (ExecutionResult) o;
		return java.util.Objects.equals(this.success, other.success) && java.util.Objects.equals(this.message, other.message);
	}

	/**
	 * Returns the hash code for this instance.
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(success, message);
	}

	/**
	 * Builds the configured result object.
	 */
	public static ExecutionResultBuilder builder() {
		return new ExecutionResultBuilder();
	}

	/**
	 * Provides the execution result builder implementation used by the com.tlcsdm.jvmexplorer.protocol package.
	 */
	public static class ExecutionResultBuilder {
		private boolean success;
		private String message;

		/**
		 * Performs the success operation.
		 */
		public ExecutionResultBuilder success(boolean success) {
			this.success = success;
			return this;
		}

		/**
		 * Performs the message operation.
		 */
		public ExecutionResultBuilder message(String message) {
			this.message = message;
			return this;
		}

		/**
		 * Builds the configured result object.
		 */
		public ExecutionResult build() {
			return new ExecutionResult(success, message);
		}
	}

}
