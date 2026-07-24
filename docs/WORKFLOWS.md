# GitHub Workflows

This repository uses GitHub Actions for build validation, artifact uploads, and release publishing.

## Workflows

### Build workflow

- File: .github/workflows/build.yml
- Runs on pushes to the main branches.
- Builds the project and uploads the generated platform jars as workflow artifacts.

### Pull request workflow

- File: .github/workflows/pull-request.yml
- Runs for pull requests.
- Validates the code by running the Gradle test and build steps.

### Publish workflow

- File: .github/workflows/publish.yml
- Handles Modrinth publishing.
- Uses the current project version and the latest commit message to populate the release metadata.

## Notes for Contributors

- Contributors do not need to configure Modrinth secrets.
- Maintainers with repository access are responsible for the GitHub Actions secrets used for publishing.
- If the build or release process changes, update this document to match.
