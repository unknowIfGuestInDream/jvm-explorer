# Implementation Principles {#implementation-principles}

JVM Explorer is organized around a JavaFX desktop client, a runtime agent, a
launch-time patch agent and a shared protocol module. The generated API
reference follows those runtime boundaries so maintainers can read the
implementation from the same perspective as the application.

## Architecture overview

The desktop application never inspects another JVM directly. It prepares an
agent, attaches it to the selected process and communicates through protocol
messages. That keeps JavaFX code, compiler/decompiler dependencies and UI state
outside of the target JVM.

@dot
digraph architecture {
  graph [rankdir=TB, bgcolor="transparent", fontname="Helvetica", compound=true, nodesep=0.35, ranksep=0.55];
  node [shape=box, style="rounded,filled", fontname="Helvetica", fontsize=11, color="#bfdbfe", fillcolor="#eff6ff", margin="0.16,0.10"];
  edge [fontname="Helvetica", fontsize=10, color="#2563eb", arrowsize=0.75];

  subgraph cluster_desktop {
    label="explorer desktop process";
    color="#93c5fd";
    style="rounded,dashed";
    ui [label="JavaFX UI\nJVM tree, class tabs, field editor"];
    tools [label="local tools\ndecompile, compile, assemble, export"];
    server [label="KryoNet server\nconnection registry + progress"];
    prepare [label="agent preparation\nstable JAR + port/log arguments"];
  }

  protocol [label="protocol module\nserializable DTOs + RMI interfaces\nJvmClient / JvmConnection", fillcolor="#ecfeff", color="#67e8f9"];

  subgraph cluster_target {
    label="selected target JVM";
    color="#cbd5e1";
    style="rounded,dashed";
    agent [label="agent module\nexecutor, packet processor, client connection"];
    inspect [label="Instrumentation + reflection\nclass bytes, fields, execute, redefine"];
    runtime [label="application runtime\nclass loaders, loaded classes, fields", fillcolor="#f8fafc", color="#cbd5e1"];
  }

  launch [label="launch-agent\npatch ProcessBuilder startup\nremove DisableAttachMechanism", fillcolor="#fffbeb", color="#f59e0b"];

  ui -> server [label="user operations"];
  ui -> tools [label="source / bytecode edits"];
  prepare -> agent [label="Attach API loads agentmain"];
  server -> protocol [label="registers Kryo types"];
  protocol -> agent [label="requests"];
  agent -> protocol [label="responses / streams"];
  agent -> inspect [label="delegates operations"];
  inspect -> runtime [label="inspect / execute / redefine"];
  tools -> protocol [label="replacement bytes"];
  launch -> runtime [label="optional startup patch"];
}
@enddot

## Module map

| Module | Runtime role | Main responsibility |
|--------|--------------|---------------------|
| `explorer` | Desktop process | JavaFX application, JVM discovery, class browsing, editors and user workflows. |
| `agent` | Target JVM process | Runtime inspection, class byte access, method execution and class redefinition. |
| `launch-agent` | JVM startup helper | Compatibility setup for JVMs that must be patched during launch. |
| `protocol` | Shared library | Request/response packets, descriptors and configuration models used by both sides. |

Each module has a narrow dependency direction. The desktop module may depend on
editor, compiler, JavaFX and attach APIs because it runs in the user's process.
The runtime agent should stay small and avoid UI dependencies because every class
it loads becomes part of the target JVM. The protocol module is deliberately
plain Java data and service interfaces so both sides can register the same Kryo
types without loading desktop-only or agent-only implementation classes.

## Runtime attachment flow

The explorer application discovers local JVM processes, then attaches an agent
to the selected process through the Java Attach API. The agent runs inside the
target JVM and exposes controlled operations for class inspection, field access,
method execution and class replacement.

The attachment flow is intentionally staged:

1. The explorer module lists local JVMs and lets the user choose one.
2. The client prepares the agent JAR and runtime configuration.
3. The Java Attach API loads the agent into the selected JVM.
4. The agent starts its client connection back to the explorer server.
5. UI actions are serialized as protocol packets and executed inside the target
   JVM by the agent.

@dot
digraph attach_flow {
  graph [rankdir=TB, bgcolor="transparent", fontname="Helvetica", nodesep=0.45, ranksep=0.55];
  node [shape=box, style="rounded,filled", fontname="Helvetica", fontsize=10, color="#cbd5e1", fillcolor="#f8fafc"];
  edge [fontname="Helvetica", fontsize=9, color="#475569", arrowsize=0.75];

  discover [label="1. Discover local JVMs\nVirtualMachine descriptors"];
  select [label="2. User selects target\nRunningJvm context"];
  server [label="3. Start explorer server\nchoose open port"];
  prepare [label="4. Prepare agent artifact\nstable JAR path + log file"];
  attach [label="5. Attach API loadAgent\nport and config arguments"];
  bootstrap [label="6. agentmain bootstrap\nlogger + executor service"];
  connect [label="7. Agent connects back\nregister JvmClient/JvmConnection"];
  operate [label="8. Runtime operations\nclass list, bytes, fields, execute"];
  patch [label="9. Optional redefinition\ncompile/assemble result bytes"];
  cleanup [label="10. Disconnect cleanup\nUI state + agent resources"];

  discover -> select -> server -> prepare -> attach -> bootstrap -> connect -> operate;
  operate -> patch [label="when user saves changes"];
  operate -> cleanup [label="connection closes"];
  patch -> operate [label="refresh affected class"];
}
@enddot

The explorer side prepares the agent JAR on the local filesystem before attach.
It prefers a stable application-owned location for extracted agent artifacts so
the file path is predictable and the same binary can be reused across repeated
attach attempts. The attach arguments carry the explorer server port, logging
configuration and any other runtime settings the agent needs before it can
connect back.

After `agentmain` is invoked, the agent parses the configuration, installs a
file-backed logger and creates a small scheduled executor. Network startup,
packet processing and periodic housekeeping are run through that executor rather
than on JVM attach threads. If agent startup fails, the agent closes its logger
and shuts the executor down so a failed attach does not leave avoidable
resources in the target process.

The network direction is intentionally reversed after attach: the explorer opens
a local KryoNet server on an available port and the newly loaded agent connects
to that port as a client. This lets the desktop process own connection tracking,
UI updates and reconnect/cleanup behavior while the target JVM only maintains
the client connection required for the active session.

## Client and agent responsibilities

- The desktop client owns JavaFX views, user actions, bytecode display,
  decompilation, compilation and request orchestration.
- The runtime agent owns operations that must execute in the target JVM, such as
  reading loaded classes, invoking code and applying transformed class bytes.
- The launch agent supports startup scenarios where JVM launch arguments need to
  be adjusted before the target process starts.

This separation keeps UI code out of the target JVM and keeps agent code
independent from JavaFX. New features should place presentation and workflow
logic in `explorer`, target-JVM operations in `agent`, startup instrumentation
in `launch-agent` and shared data contracts in `protocol`.

In practice this means that UI controllers should call helper or network classes
instead of using instrumentation directly. The `explorer` module should convert
user actions into high-level operations such as "load class content", "replace
class" or "set field". The `agent` module should implement those operations
against `Instrumentation`, reflection and class loaders, then return compact
protocol objects that the UI can render.

Target-JVM code should be conservative about class loading. Class lookup first
uses the selected class loader context, then falls back to already loaded classes
reported by `Instrumentation`. This avoids forcing new application classes to
initialize merely because they appeared in the UI and keeps class-loader-specific
views accurate.

## Protocol boundary

The protocol module contains shared request, response and descriptor classes.
Both the client and the agent use these classes so packet contracts stay stable
across module boundaries. Protocol classes should remain UI-independent and avoid
agent-only runtime assumptions.

Protocol objects are the compatibility boundary of the project. They should be
small serializable data carriers that describe the requested operation and its
result. Keeping protocol objects simple makes the agent easier to load in
different JVMs and prevents JavaFX or editor implementation details from leaking
into the target process.

Kryo registration is centralized in the protocol module. When adding a request,
response or descriptor type, register it in one place and keep registration order
stable for both endpoints. Prefer explicit value objects and primitive arrays
over arbitrary object graphs because the write and object buffers are finite and
large target classes or field values can otherwise exceed transport limits.

The bidirectional API is split by responsibility:

- `JvmClient` represents callbacks from the target JVM to the explorer, such as
  registration and packet stream completion.
- `JvmConnection` represents operations the explorer can request from the
  target JVM, such as reading class bytes, enumerating loaded classes, changing
  fields, executing a compiled callable or redefining class bytes.

Large result sets should be streamed in bounded packets instead of sent as one
object. Loaded class enumeration, for example, batches classes before sending
them to the client so the UI can receive progress updates and the transport does
not need to allocate one very large message.

## Bytecode and source editing flow

Class bytes can be viewed as decompiled Java source or disassembled bytecode.
When a user edits Java source, the explorer module compiles the code in memory
and sends the resulting class bytes back through the protocol. When a user edits
bytecode, the assembler path produces replacement bytes directly.

The editing pipeline is intentionally client-heavy:

1. The agent reads class bytes from the target JVM.
2. The explorer presents those bytes as decompiled source, bytecode text or
   structured class information.
3. The explorer compiles or assembles user changes locally.
4. Replacement bytes are sent to the agent through the protocol.
5. The agent applies the replacement and the UI refreshes the affected class
   state.

Keeping compilation, assembly and decompilation in the client avoids loading
editor-only dependencies into the target JVM.

The client has two editing pipelines:

- Java source is decompiled for display, edited by the user and compiled through
  the JDK compiler API. A custom file manager can ask the attached JVM for class
  bytes so the compiler can resolve types that are already loaded in the target
  process.
- Bytecode text is produced and consumed by assembler/disassembler helpers. This
  path bypasses Java source reconstruction and is useful when decompiled source
  cannot be compiled back into an equivalent class.

Both pipelines converge on the same patch step. The explorer sends replacement
bytes with the selected `LoadedClass` descriptor. The agent resolves the class in
the matching class loader and delegates to `Instrumentation.redefineClasses`.
Patch results should include a success flag and a message that can be shown
directly in the UI because failures often depend on JVM constraints such as
schema changes, missing classes or unmodifiable targets.

Export and bulk patch workflows reuse the same lower-level primitives. Exporting
asks the agent for original class bytes and writes them from the desktop process.
Bulk replacement reads class entries from a user-provided JAR, maps paths back
to binary class names and applies replacement requests one class at a time so
errors can identify the class that failed.

## Class and field inspection details

The agent filters loaded classes before sending them to the UI. Arrays,
primitive pseudo-classes, unmodifiable classes and classes loaded from the agent
artifact are skipped so the class tree focuses on application classes that can be
inspected or patched. Each remaining class is paired with a class-loader
descriptor so two classes with the same binary name can still be distinguished.

Field inspection is path-based. The UI represents nested field navigation as a
sequence of class and field keys. The agent walks that path with reflection,
using `setAccessible(true)` where possible, and returns compact field
descriptors instead of arbitrary live objects. Large arrays and long string
representations are capped before they cross the protocol boundary to reduce
the chance of exhausting network buffers or making the UI unresponsive.

Field writes follow the same path resolution. The final field in the path is
updated inside the target JVM, allowing static values and reachable nested
objects to be modified without bringing those objects into the desktop process.
Because JVM access rules and final-field behavior differ by runtime, write
failures should be reported as normal operation results rather than treated as
desktop UI failures.

## Launch-time patching

The launch agent exists for cases where attach must be enabled before the target
application is fully running. It installs a transformer around `ProcessBuilder`
startup behavior so child Java commands can remove attach-blocking flags such as
`-XX:+DisableAttachMechanism`. Keep this module isolated from the runtime agent:
launch-time patching is about making future JVMs attachable, while the runtime
agent is about inspecting and modifying an already selected JVM.

## UI responsiveness and diagnostics

JavaFX controllers should keep blocking work off the application thread. JVM
discovery, class loading, decompilation, compilation, export and patch requests
are coordinated through helper classes and executors so the UI stays responsive
while the selected JVM is inspected or modified.

Failures may originate from the desktop process, the transport layer or the
target JVM. Prefer structured protocol responses when the agent can recover, and
surface actionable messages in the UI when user action is required. Logs should
preserve the selected JVM, class name and requested operation so issues can be
traced without changing protocol contracts.

Connection state should be owned by the explorer server. When an agent connects,
the client handler associates the connection with a `RunningJvm`; when it
disconnects, UI state for that JVM should be cleared or marked inactive. Long
operations should expose progress callbacks where possible, especially class
enumeration and bulk patch/export flows.

Diagnostics should distinguish between three failure scopes:

- Desktop failures: missing local JDK tools, compiler errors, unreadable JARs or
  JavaFX workflow problems.
- Transport failures: closed KryoNet connections, packet streams ending early or
  serialization errors.
- Target JVM failures: class-loader mismatches, inaccessible fields, linkage
  errors, unmodifiable classes or redefine limitations.

Keeping these scopes visible in logs and result messages helps users understand
whether they should change the edited class, reconnect to the JVM or inspect the
desktop environment.

## Documentation expectations

The Doxygen output intentionally includes public, protected, package-private and
private members. This makes the generated reference useful for maintainers who
need to trace implementation details across the client, agent and shared
protocol modules.

Class pages should document APIs and relationships without embedding complete
Java source listings inline. Source browsing may remain available through
dedicated source pages, but the class reference should stay focused on
navigation, member summaries, inheritance, collaboration diagrams and concise
implementation notes.
