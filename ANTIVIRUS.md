# Antivirus Software Compatibility

## Why Antivirus Software May Flag JVM Explorer

JVM Explorer uses standard Java instrumentation APIs to inspect and interact with running JVM processes. Some antivirus software may flag these legitimate operations as suspicious because the underlying techniques overlap with patterns used by malicious software:

| Behavior | Purpose | Why It May Be Flagged |
|---|---|---|
| `VirtualMachine.attach()` | Connects to a running JVM to load the inspection agent | Resembles process injection |
| Agent JAR extraction to temp directory | Prepares the agent for loading into the target JVM | Temp file creation with executable content |
| `Instrumentation.retransformClasses()` | Reads class bytecode for decompilation and browsing | In-memory code modification |
| `Instrumentation.redefineClasses()` | Applies user-edited class changes at runtime | Runtime code replacement |
| Network socket communication | Transfers class data between target JVM and explorer UI | Inter-process communication |
| DLL extraction (Windows only) | Patches older JDKs that lack instrumentation support | Binary file write to JDK directory |

**All of these operations use official Java APIs (`com.sun.tools.attach`, `java.lang.instrument`) and are standard practice in Java development tools such as debuggers, profilers, and IDE integrations.**

## How to Resolve Antivirus Warnings

### Option 1: Add Exclusions (Recommended)

Configure your antivirus software to exclude the following:

1. **JVM Explorer installation directory** — the folder containing the application JAR files
2. **Temporary agent directory** — files matching `jvm-explorer/jvm-explorer-agent.jar` in your system temp directory:
   - Windows: `%TEMP%\jvm-explorer*\`
   - Linux/macOS: `/tmp/jvm-explorer*/`
3. **JVM Explorer process** — the Java process running JVM Explorer (typically `java` or `javaw`)

### Option 2: Allowlist the Application

If your organization uses centrally managed antivirus software, ask your IT/security team to add JVM Explorer to the allowlist. You can reference this document to explain the tool's purpose and the reason for the detected behaviors.

### Option 3: Run from the Local Path

If agent JARs are present in the working directory under `agents/`, JVM Explorer will load them directly without creating temporary files. You can place `agent.jar` and `launch-agent.jar` in an `agents/` subdirectory of your working directory to avoid temp file creation entirely.

## Technical Details

JVM Explorer extracts its agent JARs into an application-specific temporary directory (`jvm-explorer/jvm-explorer-agent.jar`) rather than using generic temporary file names. The temporary files are automatically cleaned up when the application exits.

The agent communicates with the explorer application through a local TCP socket. No data is sent to external servers.
