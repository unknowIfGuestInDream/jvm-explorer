# Module Guide {#module-guide}

JVM Explorer is organized as a small Maven reactor. Each module has a narrow
responsibility so that the desktop UI, injected runtime agent, launch-time
patching support and shared wire protocol can evolve independently.

@dot
digraph "JVM Explorer module relationships" {
    graph [
        bgcolor="transparent",
        rankdir=LR,
        nodesep=0.6,
        ranksep=0.8,
        fontname="Inter"
    ];
    node [
        shape=box,
        style="rounded,filled",
        color="#bfdbfe",
        fillcolor="#eff6ff",
        fontname="Inter",
        fontsize=11,
        margin="0.14,0.10"
    ];
    edge [
        color="#64748b",
        fontname="Inter",
        fontsize=10,
        arrowsize=0.8
    ];

    explorer [
        label="explorer\nJavaFX desktop UI"
    ];
    protocol [
        label="protocol\nshared packets and models"
    ];
    agent [
        label="agent\nruntime JVM instrumentation"
    ];
    launch_agent [
        label="launch-agent\nProcessBuilder launch patch"
    ];
    target [
        label="target JVM\nloaded classes and fields",
        fillcolor="#f8fafc",
        color="#cbd5e1"
    ];

    explorer -> protocol [label="serializes requests"];
    agent -> protocol [label="serializes responses"];
    explorer -> agent [label="attaches and connects"];
    agent -> target [label="inspects / patches"];
    explorer -> launch_agent [label="packages patch agent"];
    launch_agent -> target [label="removes disabled attach flags"];
}
@enddot

## protocol

Shared packet, model and protocol helper classes used by both the desktop UI and
the Java agent. This module defines the common language for requests, responses,
class metadata, field descriptors and runtime configuration so both sides can
communicate without duplicating model code.

Typical consumers:

- `explorer`, which sends commands and renders responses.
- `agent`, which receives commands from the desktop process and returns runtime
  information from the target JVM.

## agent

Runtime agent code loaded into target JVM processes. It exposes inspection,
execution and class-patching capabilities to the explorer application.

The agent is responsible for:

- Accepting the client connection from the desktop explorer.
- Enumerating loaded classes and class loaders.
- Reading class bytes, fields, methods and system properties from the target JVM.
- Applying class redefinition and method/body changes through the JVM
  instrumentation APIs.
- Reporting errors back through protocol packets instead of leaking UI concepts
  into the target process.

## launch-agent

Helper launcher code used when starting Java processes with the required agent
setup. It patches launch-time behavior such as `ProcessBuilder` arguments so JVM
Explorer can start child JVMs without attach-blocking flags like
`-XX:+DisableAttachMechanism`.

This module is intentionally small and separate from the runtime agent because it
executes before the normal attach connection exists.

## explorer

JavaFX desktop application code, including controllers, UI helpers, tree views,
dialogs and bytecode/class manipulation helpers.

The explorer module coordinates the user-facing workflow:

- Discover local JVM processes and let the user select a target.
- Attach the agent and open the client connection.
- Browse class loaders, packages, classes, fields, methods and system
  properties.
- Display source, bytecode, disassembly and decompiled views.
- Compile or assemble user edits and send the resulting bytecode back to the
  target JVM.

The UI keeps long-running attach, compile and network operations outside the
JavaFX application thread so generated documentation should be read with the
agent/protocol boundary in mind: UI classes describe workflow orchestration,
while protocol and agent classes describe the runtime contract.

## Documentation inputs

The Doxygen configuration reads production sources from these module paths:

| Module | Input path | Documentation focus |
|--------|------------|---------------------|
| protocol | `protocol/src/main/java` | Packet contracts, descriptors and helpers shared across processes |
| agent | `agent/src/main/java` | Runtime instrumentation, class inspection and patching |
| launch-agent | `launch-agent/src/main/java` | Launch-time JVM patch support |
| explorer | `explorer/src/main/java` | JavaFX UI, attach workflow, editing tools and network client |

## Excluded paths

Generated outputs and non-production sources are excluded from the API
documentation:

- Maven `target/` directories
- Generic `build/` directories
- Test sources
- Integration-test screenshots
