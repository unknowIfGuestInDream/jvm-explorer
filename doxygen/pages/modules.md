# Module Guide {#module-guide}

The Doxygen input includes all production Java sources from the Maven modules
listed below.

## protocol

Shared packet, model and protocol helper classes used by both the desktop UI
and the Java agent.

## agent

Runtime agent code loaded into target JVM processes. It exposes inspection,
execution and class-patching capabilities to the explorer application.

## launch-agent

Helper launcher code used when starting Java processes with the required agent
setup.

## explorer

JavaFX desktop application code, including controllers, UI helpers, tree views,
dialogs and bytecode/class manipulation helpers.

## Excluded paths

Generated outputs and non-production sources are excluded from the API
documentation:

- Maven `target/` directories
- Generic `build/` directories
- Test sources
- Integration-test screenshots
