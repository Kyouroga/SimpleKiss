# SimpleKiss

A proximity-based kiss effects.
Players will sneak, look at the player, and hold their aim long enough to trigger heart particles.

## Features

- Configurable interaction distance and viewing angle.
- Adjustable charge time before the effect is triggered.
- Per-player cooldowns.
- Heart particles shown only to the appropriate players.

## Requirements

- Minecraft Java Edition 1.21.x, 26.1.x, 26.2, or 26.3
- To build from source: JDK 25 or newer, required by the currently published Paper 26.3 API
- To run: use the Java runtime required by your Minecraft server version; the plugin is compiled to Java 21-compatible bytecode

> [!NOTE]
> The Gradle wrapper is included in the repository, so a separate Gradle installation is not required.

## Installation

1. Build the project with the command below.
2. Copy `target/SimpleKiss-Spigot.jar` to the server's `plugins` directory.
3. Start the server once to generate the configuration file.
4. Edit `plugins/SimpleKiss/config.yml` if needed.
5. Restart the server.

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/spkiss toggle true` | Everyone | Enables your kiss interactions. |
| `/spkiss toggle false` | Everyone | Disables your kiss interactions, including receiving kisses. |
| `/spkiss reload` | `simplekiss.reload` (operators by default) | Reloads the entire configuration; hidden from unauthorized tab completion. |

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
| `particles.count` | Number of heart particles. |
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

Building against the currently published Paper 26.3 API requires JDK 25 or newer. This is a build-time requirement only: generated plugin classes target Java 21-compatible bytecode. The Minecraft server still needs the Java runtime required by its own Minecraft version.

```text
target/
├── SimpleKiss-Spigot.jar
├── SimpleKiss-BungeeCord.jar
└── SimpleKiss-Velocity.jar
```

The `common`, `bootstrap`, and `universal` modules provide shared code.

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

Contributors can build and test the project locally.
Read [Contributing](docs/CONTRIBUTING.md) for more information.
Also, you can read [Workflow](docs/WORKFLOW.md) for extra information.
