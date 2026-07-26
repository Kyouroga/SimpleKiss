# SimpleKiss

SimpleKiss is a Minecraft plugin for small, proximity-based kiss effects.
Players sneak, look at another player, and hold their aim long enough to trigger heart particles.

## Features

- Configurable interaction distance and viewing angle.
- Adjustable charge time before the effect is triggered.
- Per-player cooldowns.
- Heart particles shown only to the appropriate players.
- Permission-protected reload command.
- Shared codebase for Bukkit, BungeeCord, and Velocity compatibility modules.
- Lightweight and optimized for performance.
- Fully configurable through the configuration file.

## Requirements

- Minecraft 1.21 or newer
- A compatible JDK for building (JDK 21 or newer is recommended; the build uses the currently available Java runtime automatically)

The Gradle wrapper is included in the repository, so a separate Gradle installation is not required.

## Installation

1. Build the project with the command below.
2. Copy `target/SimpleKiss-Spigot.jar` to the server's `plugins` directory.
3. Start the server once to generate the configuration file.
4. Edit `plugins/SimpleKiss/config.yml` if needed.
5. Restart the server.

The BungeeCord and Velocity jars are compatibility artifacts. They are not replacements for the Paper plugin jar.

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/spkiss reload` | `simplekiss.reload` | Reloads the configuration and resets active charge and cooldown state. |

## Configuration

The default configuration is:

```yaml
distance: 8.0
charge-time: 20
look-angle: 15.0
cooldown: 100
particles:
  count: 10
  offset: 0.5
```

`charge-time` and `cooldown` use server ticks. There are 20 ticks in one second.

| Setting | Description |
| --- | --- |
| `distance` | Maximum distance between the two players. |
| `charge-time` | Number of valid ticks required before the kiss triggers. |
| `look-angle` | Maximum angle between the player's view direction and the target. |
| `cooldown` | Number of ticks before that player can trigger another kiss. |
| `particles.count` | Number of heart particles spawned. |
| `particles.offset` | Particle spread around the target's position. |

## Building

Use the included Gradle wrapper from the project root:

```bash
./gradlew clean build
```

On Windows PowerShell:

```powershell
.\gradlew.bat clean build
```

The build outputs the release jars to `target/`:

## Gradle and Java

Full build notes are in [docs/GRADLE.md](docs/GRADLE.md).

This project uses Gradle and follows the Java version available to the current Gradle runtime. In practice, a recent JDK 21+ installation is the expected baseline.

Build targets:

```bash
./gradlew :bukkit:build
./gradlew :bungee:build
./gradlew :velocity:build
./gradlew clean build
```

Use the single-platform commands when you only need one server type built.

```text
target/
├── SimpleKiss-Spigot.jar
├── SimpleKiss-BungeeCord.jar
└── SimpleKiss-Velocity.jar
```

The `common`, `bootstrap`, and `universal` modules provide shared or internal code. They are not release jars.

## Project Layout

```text
common/      Shared configuration, services, API, and update state
bootstrap/   Starts and reloads the shared services
universal/   Shared compatibility checks and status reporting
bukkit/      Paper plugin and Bukkit commands
bungee/      BungeeCord compatibility layer
velocity/    Velocity compatibility layer
target/      Platform jars copied by the root build
```

## Development and Contributions

Contributors can build and test the project locally with:

```bash
./gradlew test
./gradlew clean build
```

Use these commands before opening a pull request. The build should complete successfully and the tests should pass.

## Documentation

Additional project documentation lives in the docs folder:

- [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md)
- [docs/WORKFLOWS.md](docs/WORKFLOWS.md)

## License

SimpleKiss is licensed under the GNU General Public License, version 3.0.
See [LICENSE](LICENSE) for the complete license text.
