# Gradle and Java Compiler

This file covers the build setup for SimpleKiss.

## Requirements

- Java 25
- The Gradle wrapper from this repository

This project uses Gradle. You do not need to install Gradle separately.

## Required Java Version

SimpleKiss needs Java 25 to compile.

If a build fails because of the Java version, install or switch to Java 25 before running it again.

## Build Targets

The project has separate modules for:

- Bukkit / Paper
- BungeeCord
- Velocity

Use the command that matches the platform you want to build.

### Build the Bukkit module

```bash
./gradlew :bukkit:build
```

### Build the BungeeCord module

```bash
./gradlew :bungee:build
```

### Build the Velocity module

```bash
./gradlew :velocity:build
```

### Build everything

```bash
./gradlew clean build
```

This builds the full project and writes the platform jars into the target folder.

## Common Commands

From the project root, use:

```bash
./gradlew test
./gradlew clean build
```

On Windows PowerShell:

```powershell
.\gradlew.bat test
.\gradlew.bat clean build
```

## Build Output

The build produces the release jars for the supported platforms in the target folder.

## Notes

- Use the Gradle wrapper for this project.
- Keep the Java version at 25 when building.
- Use the module-specific commands when you only need one platform built.
