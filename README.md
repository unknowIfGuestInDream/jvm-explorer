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

**Important**: This is a multi-module Maven project. Always build from the root directory:

```bash
# Clean and build all modules
mvn clean install

# Or just package without installing to local repository
mvn clean package
```

**Note**: Building individual modules alone (e.g., `cd explorer && mvn compile`) will fail with dependency resolution errors unless you have first run `mvn install` from the root directory to install dependencies to your local Maven repository.

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
