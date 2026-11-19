# Deadly Visuals

A client-side Fabric visual customization mod for Minecraft 1.21.11.

## Compatibility

- Minecraft 1.21.11
- Fabric Loader 0.19.2+
- Fabric API 0.141.6+1.21.11
- Java 21+

## Features

- Dark, red-accented ClickGUI (Right Shift)
- HUD modules: watermark, FPS, coordinates, direction, armor, potion count, keystrokes, clock, speed, biome, server and enabled-module list
- HUD editor with drag positioning, scale, opacity and layout reset
- JSON configuration and named profiles
- World visuals including target info, entity glow and gamma control
- Cheats category with client-side configurable Entity Hitboxes and Entity ESP
- Hitboxes: entity filters, range, line width, RGB color, fill opacity and through-wall option
- ESP: entity filters, range, line width, RGB color, fill opacity, names, distance labels and through-wall highlighting
- All modules are disabled by default; legacy v1 configurations are migrated to the all-off default

Hitboxes and ESP modify local rendering only; they do not send gameplay or combat packets. Always follow the rules of the server you play on.

## Build

Run ./gradlew build. The remapped mod JAR is generated in build/libs.
