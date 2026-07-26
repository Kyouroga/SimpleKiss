# Contributing to SimpleKiss

Thanks for helping improve SimpleKiss.

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

## Pull Requests

Pull requests should:

- explain what changed
- mention the tests or build steps that were run
- avoid unrelated edits

## Documentation

If you change behavior, configuration, or build steps, update the relevant documentation in the repository.

## Secrets and Publishing

Publishing to Modrinth is handled by repository maintainers through GitHub Actions secrets. Contributors do not need to add or expose those secrets locally.
