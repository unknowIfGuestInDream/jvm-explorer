# Implementation Principles {#implementation-principles}

JVM Explorer is organized around a small desktop client, a runtime agent and a
shared protocol. The generated API reference follows those runtime boundaries so
the implementation can be read from the same perspective as the application.

## Module map

| Module | Runtime role | Main responsibility |
|--------|--------------|---------------------|
| `explorer` | Desktop process | JavaFX application, JVM discovery, class browsing, editors and user workflows. |
| `agent` | Target JVM process | Runtime inspection, class byte access, method execution and class redefinition. |
| `launch-agent` | JVM startup helper | Compatibility setup for processes that must be launched with an agent. |
| `protocol` | Shared library | Request/response packets, descriptors and configuration models used by both sides. |

## Runtime attachment flow

The explorer application discovers local JVM processes, then attaches an agent
to the selected process through the Java Attach API. The agent runs inside the
target JVM and exposes controlled operations for class inspection, field access,
method execution and class replacement.

The attachment flow keeps the desktop UI and target JVM separated:

1. The explorer module lists candidate JVMs and lets the user choose one.
2. The agent package is prepared and loaded into the selected JVM.
3. The client opens a protocol connection to the agent.
4. UI actions are translated into protocol packets.
5. Agent responses are converted back into class trees, editors and result
   dialogs in the desktop application.

## Client and agent responsibilities

- The desktop client owns JavaFX views, user actions, bytecode display,
  decompilation, compilation and request orchestration.
- The runtime agent owns operations that must execute in the target JVM, such as
  reading loaded classes, invoking code and applying transformed class bytes.
- The launch agent supports startup scenarios where JVM launch arguments need to
  be adjusted before the target process starts.

This split keeps UI code out of the target JVM and keeps agent code independent
from JavaFX. When adding features, prefer placing presentation logic in
`explorer`, target-JVM actions in `agent`, and shared data contracts in
`protocol`.

## Protocol boundary

The protocol module contains shared request, response and descriptor classes.
Both the client and the agent use these classes so packet contracts stay stable
across module boundaries. Protocol classes should remain UI-independent and avoid
agent-only runtime assumptions.

Protocol objects are the compatibility boundary of the project. They should be
small, serializable data carriers that describe the requested operation and the
result. This makes packet handling easier to test and keeps future UI changes
from leaking into the agent.

## Bytecode and source editing flow

Class bytes can be viewed as decompiled Java source or disassembled bytecode.
When a user edits Java source, the explorer module compiles the code in memory
and sends the resulting class bytes back through the protocol. When a user edits
bytecode, the assembler path produces replacement bytes directly.

The editing pipeline is intentionally staged:

1. Read class bytes from the target JVM through the agent.
2. Present those bytes as decompiled source, bytecode text or raw class data.
3. Compile or assemble user changes inside the explorer process.
4. Send replacement bytes to the agent.
5. Redefine or patch the target class and refresh the UI state.

Keeping compilation and assembly in the client avoids loading editor-only
dependencies into the target JVM.

## UI and long-running work

JavaFX controllers should keep blocking work out of the application thread.
Network calls, class loading, decompilation and compilation are expected to be
orchestrated through helper classes so the UI remains responsive while the
selected JVM is inspected or modified.

## Error handling and diagnostics

Errors may originate from the local UI process, the transport layer or the
target JVM. Prefer returning structured protocol responses when the agent can
recover and surfacing actionable messages in the explorer UI when user action is
required. Logging should preserve enough context to identify the target JVM,
class name and requested operation without changing protocol contracts.

## Documentation expectations

The Doxygen output intentionally includes public, protected, package-private and
private members. This makes the generated reference useful for maintainers who
need to trace implementation details across the client, agent and shared
protocol modules.
