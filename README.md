# Deadly Visuals

A client-side visual customization mod for Minecraft Java Edition **1.21.11** using Fabric.

## Compatibility

- Minecraft 1.21.11
- Fabric Loader 0.19.2 or newer
- Fabric API 0.141.6+1.21.11
- Java 21

## Current feature set

- HUD watermark and FPS counter
- XYZ coordinates with optional dimension name
- Compass direction with optional yaw degrees
- Keystrokes display
- Armor count and average durability
- Active status-effect count
- Local clock, movement speed, biome and server information (opt-in modules)
- Crosshair target name and distance
- Enabled-module list
- Adjustable custom crosshair
- Entity Glow outlines for nearby living entities
- Gamma control with restoration of the previous vanilla value
- In-game configuration menu and JSON persistence
- Interactive HUD Editor: drag widgets, adjust scale and opacity, and reset positions
- HUD positions, size and opacity saved in `config/deadly/hud-layout.json`
- JSON profile persistence

The menu opens with **Right Shift**. Open the **Interface** tab and choose **Open HUD Editor** to position widgets. Drag an element to move it, use the scale/opacity controls, or scroll over a selected element to resize it. Module settings are saved to `config/deadly/config.json`; HUD layout is saved to `config/deadly/hud-layout.json`; profiles are stored in `config/deadly/profiles/`. The **Skins** tab is reserved for a later update.

## Build

The included CI uses JDK 25 to run the current Fabric Loom version and compiles the mod with Java release 21.

Linux/macOS:
```sh
./gradlew build
```

Windows:
```bat
gradlew.bat build
```

The remapped distributable JAR is generated in `build/libs/`.
