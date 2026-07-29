# Gradle and Java

This file covers the build setup for SimpleKiss.

## Requirements

- A compatible JDK for building
- The Gradle wrapper from this repository

This project uses Gradle. You do not need to install Gradle separately.

## Java Version

SimpleKiss is configured to use the Java version that is available to the current Gradle runtime. In practice, a recent JDK 21 or newer installation is the expected baseline.

If a build fails because of Java, make sure the correct JDK is selected in your shell before running Gradle. On most systems this means setting `JAVA_HOME` to the desired JDK installation.

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

This builds the full project, packages Bukkit/Spigot, BungeeCord, and Velocity platform jars with shared runtime code included, and writes the resulting jars into the `target` folder.

### Verify platform jar packaging

```bash
./gradlew clean verifyPlatformJars
```

This task builds the platform jars and checks that required shared classes (for `common`, `bootstrap`, and `universal`) are present in each generated plugin jar.

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
- If you need a specific Java version, point `JAVA_HOME` at that JDK before running Gradle.
- Use the module-specific commands when you only need one platform built.
