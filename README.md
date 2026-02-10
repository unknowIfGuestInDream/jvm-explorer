# JVM Explorer

JVM Explorer is a Java desktop application for browsing loaded class files inside locally running Java Virtual Machines.

## Features

- Browse loaded classes in running JVMs
- View decompiled source, bytecode, and disassembled code
- Edit and redefine classes at runtime
- Inspect class fields and values
- Execute remote code in target JVMs

## Prerequisites

- Java 21 or later
- Maven 3.9+

## Build

This is a multi-module Maven project. The `explorer` application depends on `protocol`, `agent`, and `launch-agent` modules. The `agent` and `launch-agent` shaded JARs must exist in their respective `target/` directories so that the `maven-antrun-plugin` can copy them into `explorer`'s classpath.

### Step 1 — Install dependency modules

Install `protocol`, `agent`, and `launch-agent` to the local Maven repository:

```bash
mvn clean install -pl protocol,agent,launch-agent -am -DskipTests
```

### Step 2 — Package the application

```bash
mvn package -pl explorer -DskipTests
```

Or build everything from the root:

```bash
mvn package -DskipTests
```

### Full build (single command)

```bash
mvn clean install -pl protocol,agent,launch-agent -am -DskipTests && mvn package -DskipTests
```

## Run

```bash
mvn -pl explorer javafx:run
```

## Project Structure

| Module         | Description                                  |
|----------------|----------------------------------------------|
| `protocol`     | Shared protocol classes for communication    |
| `agent`        | JVM agent injected into target processes     |
| `launch-agent` | Launch patch agent for JDK compatibility     |
| `explorer`     | JavaFX desktop application (main module)     |

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
