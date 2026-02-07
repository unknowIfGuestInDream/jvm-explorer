package com.tlcsdm.jvmexplorer.protocol;

public class PatchResult {

	private final boolean success;
	private final String message;


	public PatchResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	public PatchResult() {
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
		return "PatchResult(success=" + success + ", message=" + message + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PatchResult other = (PatchResult) o;
		return java.util.Objects.equals(this.success, other.success) && java.util.Objects.equals(this.message, other.message);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(success, message);
	}

	public static PatchResultBuilder builder() {
		return new PatchResultBuilder();
	}

	public static class PatchResultBuilder {
		private boolean success;
		private String message;

		public PatchResultBuilder success(boolean success) {
			this.success = success;
			return this;
		}

		public PatchResultBuilder message(String message) {
			this.message = message;
			return this;
		}

		public PatchResult build() {
			return new PatchResult(success, message);
		}
	}

}
