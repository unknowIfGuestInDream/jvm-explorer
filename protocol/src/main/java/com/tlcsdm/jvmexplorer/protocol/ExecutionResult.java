package com.tlcsdm.jvmexplorer.protocol;

public class ExecutionResult {

	private final boolean success;
	private final String message;


	public ExecutionResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	public ExecutionResult() {
		this.success = false;
		this.message = null;
	}

	public boolean isSuccess() {
		return this.success;
	}

	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		return "ExecutionResult(success=" + success + ", message=" + message + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ExecutionResult other = (ExecutionResult) o;
		return java.util.Objects.equals(this.success, other.success) && java.util.Objects.equals(this.message, other.message);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(success, message);
	}

	public static ExecutionResultBuilder builder() {
		return new ExecutionResultBuilder();
	}

	public static class ExecutionResultBuilder {
		private boolean success;
		private String message;

		public ExecutionResultBuilder success(boolean success) {
			this.success = success;
			return this;
		}

		public ExecutionResultBuilder message(String message) {
			this.message = message;
			return this;
		}

		public ExecutionResult build() {
			return new ExecutionResult(success, message);
		}
	}

}
