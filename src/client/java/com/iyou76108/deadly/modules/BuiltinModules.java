package com.iyou76108.deadly.modules;

import com.iyou76108.deadly.core.VisualModule;
import com.iyou76108.deadly.core.VisualRegistry;

public final class BuiltinModules {
    private BuiltinModules() {
    }

    public static void register() {
        VisualModule watermark = new VisualModule(
                "watermark", "Watermark", "Displays the Deadly Visuals brand on the HUD.",
                VisualModule.Category.HUD, true
        );
        watermark.addBoolean("showVersion", "Show version", true);
        VisualRegistry.register(watermark);

        VisualModule coordinates = new VisualModule(
                "coordinates", "Coordinates", "Shows your current X, Y and Z position.",
                VisualModule.Category.PLAYER, true
        );
        coordinates.addBoolean("showDimension", "Show dimension", true);
        VisualRegistry.register(coordinates);

        VisualModule fps = new VisualModule(
                "fps", "FPS Counter", "Displays the current client frame rate.",
                VisualModule.Category.HUD, true
        );
        fps.addBoolean("showLabel", "Show FPS label", true);
        VisualRegistry.register(fps);

        VisualModule direction = new VisualModule(
                "direction", "Direction", "Displays the direction you are facing.",
                VisualModule.Category.PLAYER, true
        );
        direction.addBoolean("showDegrees", "Show degrees", false);
        VisualRegistry.register(direction);

        VisualModule keystrokes = new VisualModule(
                "keystrokes", "Keystrokes", "Shows live movement key states.",
                VisualModule.Category.HUD, false
        );
        keystrokes.addBoolean("showJump", "Show jump key", true);
        VisualRegistry.register(keystrokes);

        VisualModule armor = new VisualModule(
                "armor", "Armor Status", "Shows equipped armor count and durability.",
                VisualModule.Category.PLAYER, true
        );
        armor.addBoolean("showPercent", "Show durability", true);
        VisualRegistry.register(armor);

        VisualModule potions = new VisualModule(
                "potions", "Effect Status", "Shows the number of active status effects.",
                VisualModule.Category.PLAYER, true
        );
        potions.addBoolean("showCount", "Show effect count", true);
        VisualRegistry.register(potions);

        VisualModule target = new VisualModule(
                "target", "Target Info", "Shows information about the entity under your crosshair.",
                VisualModule.Category.WORLD, true
        );
        target.addDecimal("range", "Max range", 6.0, 3.0, 12.0, 1.0);
        VisualRegistry.register(target);

        VisualModule moduleList = new VisualModule(
                "module_list", "Module List", "Lists currently enabled visual modules.",
                VisualModule.Category.HUD, true
        );
        moduleList.addBoolean("rightAligned", "Right aligned", true);
        VisualRegistry.register(moduleList);

        VisualModule crosshair = new VisualModule(
                "crosshair", "Custom Crosshair", "Draws an adjustable custom crosshair.",
                VisualModule.Category.HUD, false
        );
        crosshair.addInteger("size", "Crosshair size", 5, 2, 12, 1);
        VisualRegistry.register(crosshair);

        VisualRegistry.register(new EntityGlowModule());

        VisualModule gamma = new VisualModule(
                "gamma", "Gamma Control", "Overrides the vanilla brightness slider while enabled.",
                VisualModule.Category.ENVIRONMENT, false
        );
        gamma.addDecimal("brightness", "Brightness", 1.0, 0.5, 1.0, 0.1);
        VisualRegistry.register(gamma);

        VisualModule clock = new VisualModule(
                "clock", "Clock", "Displays the local system time on the HUD.",
                VisualModule.Category.HUD, false
        );
        clock.addBoolean("showSeconds", "Show seconds", true);
        VisualRegistry.register(clock);

        VisualModule speed = new VisualModule(
                "speed", "Movement Speed", "Displays horizontal movement speed in blocks per second.",
                VisualModule.Category.PLAYER, false
        );
        speed.addBoolean("showUnit", "Show unit", true);
        VisualRegistry.register(speed);

        VisualModule biome = new VisualModule(
                "biome", "Biome", "Displays the biome at your current position.",
                VisualModule.Category.PLAYER, false
        );
        biome.addBoolean("replaceUnderscores", "Format biome name", true);
        VisualRegistry.register(biome);

        VisualModule server = new VisualModule(
                "server", "Server Info", "Displays the current world or connected server.",
                VisualModule.Category.HUD, false
        );
        server.addBoolean("showAddress", "Show server address", true);
        VisualRegistry.register(server);
    }
}
