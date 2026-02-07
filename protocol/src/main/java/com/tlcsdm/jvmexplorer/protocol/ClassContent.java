package com.tlcsdm.jvmexplorer.protocol;


public class ClassContent {

	private final LoadedClass loadedClass;
	private final byte[] classContent;
	private final ClassFields classFields;


	public ClassContent(LoadedClass loadedClass, byte[] classContent, ClassFields classFields) {
		this.loadedClass = loadedClass;
		this.classContent = classContent;
		this.classFields = classFields;
	}


	public ClassContent() {
	}

	public LoadedClass getLoadedClass() {
		return this.loadedClass;
	}

	public byte[] getClassContent() {
		return this.classContent;
	}

	public ClassFields getClassFields() {
		return this.classFields;
	}


	@Override
	public String toString() {
		return "ClassContent(" + "loadedClass=" + loadedClass + ", classContent=" + classContent + ", classFields=" + classFields" + ")";
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassContent other = (ClassContent) o;
		return java.util.Objects.equals(this.loadedClass, other.loadedClass) && java.util.Objects.equals(this.classContent, other.classContent) && java.util.Objects.equals(this.classFields, other.classFields);
	}


	@Override
	public int hashCode() {
		return java.util.Objects.hash(loadedClass, classContent, classFields);
	}

}
