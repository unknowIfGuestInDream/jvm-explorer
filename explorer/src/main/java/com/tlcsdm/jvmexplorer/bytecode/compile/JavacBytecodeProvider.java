package com.tlcsdm.jvmexplorer.bytecode.compile;

import javax.tools.JavaFileObject;
import java.util.List;

public interface JavacBytecodeProvider {

	List<JavaFileObject> list(String packageName, boolean recurse);

}
