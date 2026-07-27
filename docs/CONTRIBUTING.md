# Contributing to SimpleKiss

Thank you for your interest in contributing to SimpleKiss! We appreciate your time and effort in helping improve the project.

## Before You Start

- Install a compatible JDK for building (JDK 21 or newer is recommended).
- Use the Gradle wrapper from the repository root.
- Keep changes focused and easy to review.

## Development Workflow

1. Fork the repository and create a branch.
2. Make your changes.
3. Run the tests and build locally:

```bash
./gradlew test
./gradlew clean build
```

4. Open a pull request with a short summary of the change and any relevant testing notes.

## Gradle and Java

Build details are in [GRADLE.md](GRADLE.md).

The project uses Gradle and follows the Java version provided by the current Gradle runtime, so make sure your shell points to a recent JDK before running builds.

## General Style Guide

Keep the code simple and easy to read. The project is small enough that clear structure matters more than clever tricks.

- Use lowercase package names.
- Use PascalCase for classes and camelCase for methods and variables.
- Keep names descriptive. Avoid short names unless they are common, like UUID or config.
- Keep fields private and make dependencies final when they should not change.
- Keep methods focused. If a method does too much, split it up.
- Use early returns when they make the logic easier to follow.
- Add comments only when they explain something that is not obvious from the code.
- Use Javadoc for public classes and methods when the purpose is not clear from the name.
- Avoid magic numbers. Use constants or config values instead.
- Keep platform-specific code in the matching module. Put shared logic in the common module.
- Keep formatting consistent: clear indentation, simple spacing, and one change per commit.

These are the general coding guidelines used throughout the project:
```java
public class ExamplePlugin extends SomeBasePlugin implements SomeBridge {
    private SomeManager manager; // Keep fields private and initialize them clearly.

    /**
     * Starts shared services and registers the plugin command.
     */
    public void onEnable() {
        saveDefaultConfig(); // Keep setup steps simple and visible.

        manager = SomeInitializer.initialize(this); // Set shared state early.
        registerCommands(); // Move setup work into a helper method.
    }

    private void registerCommands() {
        SomeCommand command = getCommand("example");
        if (command != null) {
            SomeCommandHandler handler = new SomeCommandHandler(this);
            command.setExecutor(handler); // Keep command wiring direct.
            command.setTabCompleter(handler);
        }
    }
}
```

That is the general direction to follow. Keep it straightforward.

## Pull Requests

Pull requests should:

- explain what changed
- mention the tests or build steps that were run
- avoid unrelated edits

## Documentation

If you change behavior, configuration, or build steps, update the relevant documentation in the repository.

## Secrets and Publishing

Publishing to Modrinth is handled by repository maintainers through GitHub Actions secrets. Contributors do not need to add or expose those secrets locally.
