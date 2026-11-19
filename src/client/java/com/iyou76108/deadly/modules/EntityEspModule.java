package com.iyou76108.deadly.modules;

import com.iyou76108.deadly.core.VisualModule;
import net.minecraft.client.Minecraft;

public final class EntityEspModule extends VisualModule {
    public EntityEspModule() {
        super("entity_esp", "Entity ESP",
                "Highlights selected entities through the world with configurable boxes and labels.",
                Category.CHEATS, false);
        addBoolean("players", "Players", true);
        addBoolean("hostile", "Hostile mobs", true);
        addBoolean("animals", "Animals", true);
        addBoolean("other", "Other entities", false);
        addBoolean("includeInvisible", "Include invisible", false);
        addInteger("range", "Render range", 96, 8, 160, 8);
        addDecimal("lineWidth", "Line width", 2.0, 1.0, 6.0, 0.5);
        addBoolean("fill", "Filled boxes", false);
        addInteger("fillOpacity", "Fill opacity", 30, 0, 100, 5);
        addBoolean("throughWalls", "Show through walls", true);
        addBoolean("showNames", "Show entity names", true);
        addBoolean("showDistance", "Show distance", true);
        addInteger("red", "Red", 255, 0, 255, 16);
        addInteger("green", "Green", 64, 0, 255, 16);
        addInteger("blue", "Blue", 96, 0, 255, 16);
    }
    @Override
    public void tick(Minecraft client) {
        EntityBoxRenderer.render(client, this, true);
    }
}
