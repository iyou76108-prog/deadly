package com.iyou76108.deadly.modules;

import com.iyou76108.deadly.core.VisualModule;
import net.minecraft.client.Minecraft;

public final class EntityHitboxesModule extends VisualModule {
    public EntityHitboxesModule() {
        super("hitboxes", "Entity Hitboxes",
                "Draws configurable entity bounding boxes, similar to Meteor's visual hitbox overlay.",
                Category.CHEATS, false);
        addBoolean("players", "Players", true);
        addBoolean("hostile", "Hostile mobs", true);
        addBoolean("animals", "Animals", true);
        addBoolean("other", "Other entities", false);
        addBoolean("includeInvisible", "Include invisible", false);
        addInteger("range", "Render range", 64, 8, 128, 8);
        addDecimal("lineWidth", "Line width", 2.0, 1.0, 6.0, 0.5);
        addBoolean("fill", "Filled boxes", false);
        addInteger("fillOpacity", "Fill opacity", 35, 0, 100, 5);
        addBoolean("throughWalls", "Show through walls", false);
        addInteger("red", "Red", 255, 0, 255, 16);
        addInteger("green", "Green", 64, 0, 255, 16);
        addInteger("blue", "Blue", 96, 0, 255, 16);
    }
    @Override
    public void tick(Minecraft client) {
        EntityBoxRenderer.render(client, this, false);
    }
}
