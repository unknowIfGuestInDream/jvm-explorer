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

## Build & Run

**Important**: This is a multi-module Maven project. Always build from the root directory.

```bash
# Build all modules (agent JARs are automatically copied to explorer)
mvn clean package

# Run the application
mvn -pl explorer javafx:run
```

You can also build and run in a single command:

```bash
mvn clean package -DskipTests && mvn -pl explorer javafx:run
```

### IDE Development

When developing in an IDE (IntelliJ IDEA, Eclipse, etc.):

1. Import the project as a Maven multi-module project from the root `pom.xml`
2. Run `mvn clean package -DskipTests` from the root directory at least once to generate agent JARs
3. After modifying agent or launch-agent code, re-run `mvn clean package -DskipTests` from the root before launching the application

**Note**: A separate `mvn install` step is **not** required. The Maven reactor builds modules in the correct order (protocol → agent → launch-agent → explorer), so agent JARs are available when the explorer module needs them.

## Project Structure

| Module         | Description                                  |
|----------------|----------------------------------------------|
| `protocol`     | Shared protocol classes for communication    |
| `agent`        | JVM agent injected into target processes     |
| `launch-agent` | Launch patch agent for JDK compatibility     |
| `explorer`     | JavaFX desktop application (main module)     |

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
