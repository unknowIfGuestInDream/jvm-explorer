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
  graph [rankdir=LR, bgcolor="transparent", fontname="Helvetica"];
  node [shape=box, style="rounded,filled", fontname="Helvetica", color="#bfdbfe", fillcolor="#eff6ff"];
  edge [fontname="Helvetica", color="#2563eb"];

  explorer [label="explorer\nJavaFX desktop client"];
  protocolClient [label="protocol\nshared packets"];
  agent [label="agent\nruntime JVM operations"];
  target [label="target JVM\nloaded classes and fields"];
  launch [label="launch-agent\nstartup patching"];

  explorer -> protocolClient [label="request / response"];
  protocolClient -> agent [label="KryoNet packets"];
  agent -> target [label="Instrumentation API"];
  launch -> target [label="optional startup patch"];
}
@enddot

## Module map

| Module | Runtime role | Main responsibility |
|--------|--------------|---------------------|
| `explorer` | Desktop process | JavaFX application, JVM discovery, class browsing, editors and user workflows. |
| `agent` | Target JVM process | Runtime inspection, class byte access, method execution and class redefinition. |
| `launch-agent` | JVM startup helper | Compatibility setup for JVMs that must be patched during launch. |
| `protocol` | Shared library | Request/response packets, descriptors and configuration models used by both sides. |

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
  graph [rankdir=TB, bgcolor="transparent", fontname="Helvetica"];
  node [shape=box, style="rounded,filled", fontname="Helvetica", color="#cbd5e1", fillcolor="#f8fafc"];
  edge [fontname="Helvetica", color="#475569"];

  discover [label="Discover local JVMs"];
  prepare [label="Prepare agent configuration"];
  attach [label="Attach through Java Attach API"];
  connect [label="Agent connects to explorer server"];
  inspect [label="Inspect, execute or patch classes"];

  discover -> prepare -> attach -> connect -> inspect;
}
@enddot

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

## Documentation expectations

The Doxygen output intentionally includes public, protected, package-private and
private members. This makes the generated reference useful for maintainers who
need to trace implementation details across the client, agent and shared
protocol modules.
