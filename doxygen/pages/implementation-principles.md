# Implementation Principles {#implementation-principles}

JVM Explorer is organized around a small desktop client, a runtime agent and a
shared protocol. The generated API reference follows those runtime boundaries so
the implementation can be read from the same perspective as the application.

## Runtime attachment flow

The explorer application discovers local JVM processes, then attaches an agent
to the selected process through the Java Attach API. The agent runs inside the
target JVM and exposes controlled operations for class inspection, field access,
method execution and class replacement.

## Client and agent responsibilities

- The desktop client owns JavaFX views, user actions, bytecode display,
  decompilation, compilation and request orchestration.
- The runtime agent owns operations that must execute in the target JVM, such as
  reading loaded classes, invoking code and applying transformed class bytes.
- The launch agent supports startup scenarios where JVM launch arguments need to
  be adjusted before the target process starts.

## Protocol boundary

The protocol module contains shared request, response and descriptor classes.
Both the client and the agent use these classes so packet contracts stay stable
across module boundaries. Protocol classes should remain UI-independent and avoid
agent-only runtime assumptions.

## Bytecode and source editing flow

Class bytes can be viewed as decompiled Java source or disassembled bytecode.
When a user edits Java source, the explorer module compiles the code in memory
and sends the resulting class bytes back through the protocol. When a user edits
bytecode, the assembler path produces replacement bytes directly.

## Documentation expectations

The Doxygen output intentionally includes public, protected, package-private and
private members. This makes the generated reference useful for maintainers who
need to trace implementation details across the client, agent and shared
protocol modules.
