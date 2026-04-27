package com.tlcsdm.jvmexplorer.protocol;

/**
 * Provides the patch result implementation used by the com.tlcsdm.jvmexplorer.protocol package.
 */
public class PatchResult {

	private final boolean success;
	private final String message;


	/**
	 * Creates a new PatchResult instance.
	 */
	public PatchResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	/**
	 * Creates a new PatchResult instance.
	 */
	public PatchResult() {
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
		return "PatchResult(success=" + success + ", message=" + message + ")";
	}

	/**
	 * Compares this instance with another object for logical equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PatchResult other = (PatchResult) o;
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
	public static PatchResultBuilder builder() {
		return new PatchResultBuilder();
	}

	/**
	 * Provides the patch result builder implementation used by the com.tlcsdm.jvmexplorer.protocol package.
	 */
	public static class PatchResultBuilder {
		private boolean success;
		private String message;

		/**
		 * Performs the success operation.
		 */
		public PatchResultBuilder success(boolean success) {
			this.success = success;
			return this;
		}

		/**
		 * Performs the message operation.
		 */
		public PatchResultBuilder message(String message) {
			this.message = message;
			return this;
		}

		/**
		 * Builds the configured result object.
		 */
		public PatchResult build() {
			return new PatchResult(success, message);
		}
	}

}
