package com.tlcsdm.jvmexplorer.bytecode.compile;

import java.util.Arrays;

public class CompileResult {

	private final String stdOut;
	private final boolean success;
	private final byte[] classContent;


	public CompileResult(String stdOut, boolean success, byte[] classContent) {
		this.stdOut = stdOut;
		this.success = success;
		this.classContent = classContent;
	}

	public CompileResult() {
		this.stdOut = null;
		this.success = false;
		this.classContent = null;
	}

	public String getStdOut() {
		return this.stdOut;
	}

	public boolean isSuccess() {
		return this.success;
	}

	public byte[] getClassContent() {
		return this.classContent;
	}

	@Override
	public String toString() {
		return "CompileResult(stdOut=" + stdOut + ", success=" + success + ", classContent=" + Arrays.toString(classContent) + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompileResult other = (CompileResult) o;
		return java.util.Objects.equals(this.stdOut, other.stdOut) && java.util.Objects.equals(this.success, other.success) && Arrays.equals(this.classContent, other.classContent);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(stdOut, success, Arrays.hashCode(classContent));
	}

	public static CompileResultBuilder builder() {
		return new CompileResultBuilder();
	}

	public static class CompileResultBuilder {
		private String stdOut;
		private boolean success;
		private byte[] classContent;

		public CompileResultBuilder stdOut(String stdOut) {
			this.stdOut = stdOut;
			return this;
		}

		public CompileResultBuilder success(boolean success) {
			this.success = success;
			return this;
		}

		public CompileResultBuilder classContent(byte[] classContent) {
			this.classContent = classContent;
			return this;
		}

		public CompileResult build() {
			return new CompileResult(stdOut, success, classContent);
		}
	}

}
