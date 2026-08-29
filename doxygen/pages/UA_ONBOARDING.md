# JVM Explorer 新成员指南 {#onboarding}

> 由 Understand-Anything 基于项目知识图谱自动生成。最后分析提交：`8efd8ad`。

## 一、项目概览

| 项 | 内容 |
|---|---|
| 名称 | jvm-explorer |
| 描述 | 一个用于浏览本地运行 JVM 中已加载类文件的 Java 桌面应用，支持查看反编译/反汇编字节码、修改方法实现、远程执行代码与导出类文件 |
| 语言 | Java 21（另有 FXML、CSS、XML、properties、Markdown、Groovy/Jenkinsfile） |
| 框架/技术 | JavaFX、JUnit、Jenkins、Maven、ASM、Vineflower、KryoNet |
| 结构 | Maven 多模块：`protocol`、`agent`、`launch-agent`、`explorer` |

项目 fork 自 Naton1/jvm-explorer，核心玩法是通过 Java Instrumentation API 附加到本地 JVM，读取/修改类字节码与静态字段，并可远程执行代码。

## 二、架构分层（9 层）

1. **应用入口与主控制器** — 应用启动、JavaFX 主控制器与项目概览文档。
   - 关键文件：`JvmExplorer.java`、`JvmExplorerController.java`、`Startup.java`、`README.md`
2. **JavaFX 界面与样式** — 图形界面控制器、视图组件、FXML 布局、CSS 样式。
   - 关键：`fx/classes/LoadedClassesController.java`、`fx/method/ModifyMethodController.java`、`fx/openclass/CurrentClassController.java`、`main.fxml`
3. **字节码处理与编译** — 反编译、反汇编、重组与动态编译。
   - 关键：`VineflowerDecompiler.java`、`OpenJdkJasmAssembler.java`、`compile/Compiler.java`
4. **Java Agent 与 JVM 交互** — 基于 Instrumentation 读取/修改目标 JVM。
   - 关键：`agent/.../JvmExplorerAgent.java`、`InstrumentationHelper.java`、`JvmConnectionImpl.java`、`explorer/.../agent/RunningJvm.java`、`JdkPatcher.java`
5. **启动补丁 Agent** — 通过补丁 ProcessBuilder 移除 `-XX:+DisableAttachMechanism`。
   - 关键：`launch-agent/.../LaunchPatchAgent.java`、`LaunchPatchClassVisitor.java`
6. **通信协议与数据模型** — 客户端与 agent 之间的消息类型、数据模型与连接接口。
   - 关键：`Protocol.java`、`JvmClient.java`、`LoadedClass.java`、`ClassContent.java`、`AgentConfiguration.java`
7. **网络通信实现** — 基于 KryoNet 的服务器/客户端实现。
   - 关键：`JvmExplorerServer.java`、`ServerLauncher.java`、`JvmClientImpl.java`、`PacketResponseHandler.java`
8. **工具与代码模板** — 对话框、剪贴板、代码区、导出、高亮等通用辅助。
   - 关键：`CodeAreaHelper.java`、`ExportHelper.java`、`PatchHelper.java`、`CodeTemplateHelper.java`
9. **配置与构建** — Maven 构建、国际化、日志、偏好设置与 CI/CD。
   - 关键：`pom.xml`、`explorer/pom.xml`、`logback.xml`、`messages*.properties`、`Jenkinsfile`、`JvmExplorerSettings.java`

## 三、核心概念

- **Java Instrumentation（动态附加）**：`agent` 模块通过 `agentmain` 入口注入目标 JVM，用 `redefine`/`retransform` 热更新类字节码。
- **跨进程通信**：`explorer`（客户端/服务端）与注入到目标 JVM 的 agent 之间通过 KryoNet（Kryo 序列化）通信，`protocol` 模块定义契约。
- **字节码全链路**：Vineflower 反编译、ASM/JASM 反汇编与重组、Javac 动态编译修改后的方法体。
- **JavaFX MVC**：FXML 声明式布局 + Controller 代码分离，通过 `fx:id` 与 `@FXML` 绑定。
- **启动补丁**：`launch-agent` 用补丁过的 `ProcessBuilder` 移除 attach 限制，配合 `jdk_patch` 下的 JNI 库实现。
- **国际化与偏好**：`messages*.properties` 支持 en/zh/ja，`AppPreferences`/`JvmExplorerSettings` 管理运行时配置。

## 四、学习路径（导览）

1. **项目概览** — `README.md`
2. **应用入口** — `Startup.java` → `JvmExplorer.java`（JavaFX Application）
3. **主控制器** — `JvmExplorerController.java`（界面与业务枢纽）
4. **通信协议与数据模型** — `Protocol.java`、`JvmClient.java`、`LoadedClass.java`
5. **Java Agent 服务端** — `JvmExplorerAgent.java`、`InstrumentationHelper.java`
6. **启动补丁 Agent** — `LaunchPatchAgent.java`
7. **网络通信** — `JvmExplorerServer.java`、`JvmClientImpl.java`
8. **字节码处理** — `VineflowerDecompiler.java`、`OpenJdkJasmAssembler.java`、`Compiler.java`
9. **JavaFX 界面** — `main.fxml`、`LoadedClassesController.java`、`ModifyMethodController.java`
10. **工具与代码模板** — `ExportHelper.java`、`CodeTemplateHelper.java`
11. **配置与构建** — `pom.xml`、`logback.xml`、`Jenkinsfile`

## 五、文件地图（按层要点）

- **应用入口**：`Startup.java` 启动主进程；`JvmExplorer.java` 是 JavaFX Application 入口，负责初始化主界面与主题。
- **界面**：`JvmExplorerController` 是全局中枢；`fx/classes` 负责类浏览，`fx/method` 负责修改方法，`fx/openclass` 负责当前类/字段查看，`fx/jvms` 负责 JVM 列表，`fx/compile` 负责远程执行。
- **字节码**：`Decompiler`/`Assembler`/`Disassembler` 为抽象接口，`VineflowerDecompiler`、`OpenJdkJasm*`、`AsmDisassembler` 为具体实现；`compile/*` 负责通过 Javac 编译并管理自定义 `JavaFileObject`。
- **Agent**：`agent` 模块是被注入目标 JVM 的“服务端”；`explorer/agent` 是客户端侧的连接与管理（`RunningJvm`、`AgentPreparer`、`JdkPatcher`）。
- **协议**：`protocol` 是纯数据/接口模块，被其余三个模块共享；`helper` 提供类名、字段值与调度服务辅助。
- **网络**：`ServerLauncher` 启动 KryoNet 服务，`ClientHandler`/`PacketResponseHandler` 处理请求，`JvmClientImpl` 实现 `JvmClient` 会话。
- **工具**：`ExportHelper`（导出 JAR）、`PatchHelper`（打补丁）、`CodeAreaHelper`（编辑器/高亮）、`CodeTemplateHelper`（模板加载）、`ClassTreeHelper`（类加载器树构建）。
- **配置**：根 `pom.xml` 管理四模块与依赖版本；`Jenkinsfile` 定义 CI/CD；`messages*.properties` 做国际化。

## 六、复杂度热点（新成员需谨慎）

以下 16 个文件复杂度为「复杂」，阅读/修改前建议先理解其职责：

- `JvmExplorerController.java`（全局控制器）
- `fx/classes/LoadedClassesController.java`、`ClassCellFactory.java`、`ClassTreeNode.java`
- `fx/openclass/CurrentClassController.java`、`ClassFieldCellFactory.java`
- `fx/method/ModifyMethodController.java`
- `helper/CodeAreaHelper.java`
- `agent/.../InstrumentationHelper.java`、`JvmConnectionImpl.java`
- `protocol/.../AgentConfiguration.java`、`helper/VerboseScheduledExecutorService.java`
- `Jenkinsfile`、`pom.xml`、`explorer/pom.xml`
- `explorer/src/main/resources/jdk_patch/instrument-32.dll`（二进制资源，行数统计导致的误报，实际为 JNI 库，无需阅读）
