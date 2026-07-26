# GitHub Workflows

This repository previously used GitHub Actions for build validation, artifact uploads, and release publishing, but the workflow files are currently disabled for maintenance and local validation is the recommended path.

## Current Status

- The workflow files are retained in the repository but are not actively used while maintenance work is in progress.
- Local verification should be done with the Gradle wrapper:

```bash
./gradlew test
./gradlew clean build
```

## Notes for Contributors

- Contributors do not need to configure Modrinth secrets while the workflow automation is disabled.
- Maintainers with repository access can re-enable the workflows later if publishing or CI automation is needed again.
- If the build or release process changes, update this document to match.
