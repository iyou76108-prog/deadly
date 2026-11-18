# Deadly Visuals

A client-side visual customization mod for Minecraft Java Edition **1.21.11** using Fabric.

## Current feature set

- HUD watermark and FPS counter
- XYZ coordinates with optional dimension name
- Compass direction with optional yaw degrees
- Keystrokes display
- Armor count and average durability
- Active status-effect count
- Crosshair target name and distance
- Enabled-module list
- Adjustable custom crosshair
- Entity Glow outlines for nearby living entities
- Gamma control with restoration of the previous vanilla value
- In-game configuration menu and JSON persistence
- JSON profile persistence (default profile controls are in the menu)

The menu opens with **Right Shift**. Changes are saved to `config/deadly/config.json`; profiles are stored in `config/deadly/profiles/`.

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.20.1+
- Fabric API 0.141.6+1.21.11
- Java 21
- Gradle 9.8.0+ (the included bootstrap script downloads it when no Gradle installation is found)

## Build

Linux/macOS:
```sh
./gradlew build
```

Windows:
```bat
gradlew.bat build
```

The remapped distributable JAR is generated in `build/libs/`. A GitHub Actions workflow builds the project on pushes and pull requests.
