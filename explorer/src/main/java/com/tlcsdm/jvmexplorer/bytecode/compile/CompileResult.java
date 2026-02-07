package com.tlcsdm.jvmexplorer.bytecode.compile;


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
		return "CompileResult(" + "stdOut=" + stdOut + ", success=" + success + ", classContent=" + classContent" + ")";
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompileResult other = (CompileResult) o;
		return java.util.Objects.equals(this.stdOut, other.stdOut) && java.util.Objects.equals(this.success, other.success) && java.util.Objects.equals(this.classContent, other.classContent);
	}


	@Override
	public int hashCode() {
		return java.util.Objects.hash(stdOut, success, classContent);
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
