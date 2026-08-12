# GitHub Workflows

> [!WARNING]
> The workflow files are currently disabled for maintenance and local validation is the recommended path.

- Local verification should be done with the Gradle wrapper:

```bash
./gradlew test
./gradlew clean build
```

Read [docs/GRADLE.md](docs/GRADLE.md) for more information.

> [!NOTE]
> Contributors do not need to configure Modrinth secrets while the workflow automation is disabled.
> Maintainers with repository access can re-enable the workflows later if publishing or CI automation is needed again.
