<div align="center">

# Yuki

**A high-performance Minecraft anti-cheat plugin based on PacketEvents**

English | [简体中文](./README.md)

</div>

## Overview

Yuki is a Minecraft anti-cheat plugin built on top of GrimAC, incorporating detection concepts from Matrix, Vulcan, Medusa, Karhu, Hawk, GrimAC, and Raven. The plugin uses a prediction engine architecture that simulates client-side movement to accurately detect various cheats.

## Features

- **Prediction Engine**: Full simulation of client movement, physics, fluids, and vehicles
- **Comprehensive Checks**: 200+ checks across Movement, Combat, Player, Scaffold, Chat, and MultiActions categories
- **Multi-Version Support**: Supports Minecraft 1.13 - 1.21+, compatible with ViaVersion protocol translation
- **Multi-Language**: Built-in Chinese and English language packs
- **Platform Abstraction**: Platform-agnostic API layer (AntiCheatUser, EventBus) for easy extension to Velocity and other proxies
- **Storage Abstraction**: Supports SQLite, MySQL, H2, and MongoDB with BackendRegistry for custom backends
- **Feature Flags**: Runtime toggling of check categories and features
- **Discord Webhook**: Push violation alerts to Discord
- **Diagnostic Dump**: Export plugin state as JSON for troubleshooting
- **Performance Monitoring**: Real-time TPS, MSPT, and check execution statistics

## Requirements

| Dependency | Version |
|------------|---------|
| Java | 21+ |
| Minecraft | 1.13+ |
| Server | Paper / Spigot (Paper recommended) |
| PacketEvents | Bundled (2.13.0) |

## Installation

1. Download the latest `Yuki-<version>.jar` from [Releases](https://github.com/Aeth520/Yuki/releases)
2. Place the jar file in your server's `plugins/` directory
3. Start the server - configuration files will be auto-generated
4. Modify files in `plugins/Yuki/` as needed

## Commands

Main command: `/yuki` (alias `/yk`)

| Subcommand | Description | Permission |
|------------|-------------|------------|
| `help` | Show help | `yuki.commands` |
| `reload` | Reload configuration | `yuki.commands` |
| `alerts` | Toggle alert display | `yuki.commands.alert` |
| `spectate` / `spec` | Spectate a player | `yuki.commands.spectate` |
| `stopspectate` / `stopspec` | Stop spectating | `yuki.commands.stopspectating` |
| `freeze` | Freeze a player | `yuki.commands.freeze` |
| `unfreeze` | Unfreeze a player | `yuki.commands.freeze` |
| `setback` | Force setback a player | `yuki.commands.setback` |
| `mitigate` | Mitigation management | `yuki.commands.mitigate` |
| `verbose` | Toggle verbose output | `yuki.commands` |
| `perf` / `benchmark` | Performance monitor stats | `yuki.commands` |
| `debug` | Debug mode | `yuki.commands` |
| `profile` | Player profile | `yuki.commands` |
| `log` / `logs` | Violation log query | `yuki.commands` |
| `history` / `hist` | History records | `yuki.commands` |
| `dump` | Export diagnostic dump | `yuki.commands.dump` |
| `features` | Feature flag management | `yuki.commands.features` |
| `discordtest` | Test Discord Webhook | `yuki.commands` |
| `crash` | Crash player's client | `yuki.commands` |
| `decrypt` | Decrypt operation | `yuki.commands.decrypt` |

## Permissions

| Permission Node | Description | Default |
|-----------------|-------------|---------|
| `yuki.staff` | Staff permission (includes all sub-permissions below) | op |
| `yuki.commands` | Basic command permission | op |
| `yuki.exempt` | Exempt from all checks | op |
| `yuki.exempt.cancel` | Exempt from event cancellation | op |
| `yuki.exempt.setback` | Exempt from setback | op |
| `yuki.exempt.highpingkick` | Exempt from high-ping kick | op |
| `yuki.antiplugin` | Plugin list disguise | op |
| `yuki.brand` | View client brand | op |

## Check Categories

### Movement Checks
- **Elytra**: A-K, detects flight cheats
- **GroundSpoof**: A-C
- **NoSlow**: A-G
- **Sprint**: A
- **Vehicle**: Fly A-B, NoSaddle A-B
- **MovementValidation**

### Combat Checks
- **Aim**: A-W (23 checks), includes statistical analysis Analysis A-H
- **KillAura**: A-M
- **Reach**: A-E
- **Velocity**: A-F
- **AutoBlock**: A-G

### Player Checks
- **AutoClicker**: A-T
- **BadPackets**: A-AA
- **Scaffold**: A-K
- **Timer**: A-AA
- **Crash**: A-M
- **Inventory**: A-N
- **PingSpoof**: A-F
- **Baritone**: A-D
- **FastBreak** / **FastPlace** / **FarBreak** and other break/place checks
- **MultiActions**: A-G
- **Exploit**: A-G

### Misc Checks
- **Chat**: A-D
- **Spam**: A-B
- **Client**: A
- **GhostBlock** mitigation
- **Visual**: Equipment/metadata hiding

## Configuration

Configuration files are located in `plugins/Yuki/`:

| File | Description |
|------|-------------|
| `settings.yml` | Main config (language, output, feature flags, mitigations, etc.) |
| `check.yml` | Check parameter configuration |
| `punishments.yml` | Punishment configuration |
| `messages.yml` | Message text |
| `database.yml` | Database configuration |
| `models/analysis-h.json` | AnalysisH ML model weights (externally loaded) |

### Database Configuration

```yaml
# database.yml
data-type: sqlite  # sqlite | mysql | h2 | mongodb
```

- **SQLite**: Default, no extra configuration needed
- **MySQL/MariaDB**: Configure host/port/database/username/password
- **H2**: Embedded database
- **MongoDB**: Document-oriented database

### Feature Flags

```yaml
# settings.yml
features:
  checks:
    movement: true
    combat: true
    scaffold: true
    # ...
  performance:
    monitor: true
  discord:
    webhook: false
  dump:
    diagnostic: true
```

Can also be toggled at runtime via `/yuki features` command.

## Optional Hook Plugins

| Plugin | Function |
|--------|----------|
| ViaVersion / ViaBackwards / ViaRewind | Multi-version protocol support |
| Geyser-Spigot | Bedrock Edition player support |
| Floodgate | Bedrock Edition account management |
| PlaceholderAPI | Variable replacement |
| MythicMobs | Mob compatibility |
| MyPet | Pet compatibility |

## Building

```bash
git clone https://github.com/Aeth520/Yuki.git
cd Yuki
./gradlew build
```

Build output: `build/libs/Yuki-<version>.jar`

### Custom Database Driver Packaging

By default, only the SQLite driver is bundled. To include additional drivers:

```bash
./gradlew build -PdbDrivers=mysql,mongodb
```

## Tech Stack

- **Java 21** + **Gradle** (Shadow plugin)
- **PacketEvents 2.13.0**: Packet listening and protocol handling
- **Kyori Adventure**: Text component handling
- **ORMLite**: ORM mapping
- **HikariCP**: Connection pooling
- **Manifold**: Compile-time processor
- **Lombok**: Code simplification

## Acknowledgements

- [GrimAC](https://github.com/GrimAnticheat/Grim) - Prediction engine base architecture
- [PacketEvents](https://github.com/retrooper/packetevents) - Packet event framework
- Matrix / Vulcan / Medusa / Karhu / Hawk / Raven - Detection concept references

## License

This project is for educational and research purposes only.

## Links

- [GitHub Repository](https://github.com/Aeth520/Yuki)
- [Issue Tracker](https://github.com/Aeth520/Yuki/issues)
