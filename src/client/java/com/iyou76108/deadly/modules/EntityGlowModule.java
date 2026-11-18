package com.iyou76108.deadly.modules;

import com.iyou76108.deadly.core.Setting;
import com.iyou76108.deadly.core.VisualModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class EntityGlowModule extends VisualModule {
    private final Map<Entity, Boolean> originalGlowStates = new IdentityHashMap<>();

    public EntityGlowModule() {
        super("entity_glow", "Entity Glow",
                "Adds client-side outlines to nearby living entities.", Category.WORLD, false);
        addInteger("range", "Glow range", 48, 16, 128, 16);
    }

    @Override
    public void tick(Minecraft client) {
        if (!isEnabled() || client.level == null || client.player == null) {
            restoreAll();
            return;
        }

        Setting<?> rangeSetting = getSetting("range");
        double range = rangeSetting == null ? 48.0
                : ((Number) rangeSetting.getValue()).doubleValue();

        Set<Entity> activeThisTick = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity == client.player || !(entity instanceof LivingEntity) || !entity.isAlive()) {
                continue;
            }
            if (client.player.distanceTo(entity) > range) {
                continue;
            }

            originalGlowStates.putIfAbsent(entity, entity.hasGlowingTag());
            entity.setGlowingTag(true);
            activeThisTick.add(entity);
        }

        Iterator<Map.Entry<Entity, Boolean>> iterator = originalGlowStates.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Entity, Boolean> entry = iterator.next();
            if (!activeThisTick.contains(entry.getKey())) {
                entry.getKey().setGlowingTag(entry.getValue());
                iterator.remove();
            }
        }
    }

    @Override
    protected void onDisable() {
        restoreAll();
    }

    private void restoreAll() {
        for (Map.Entry<Entity, Boolean> entry : originalGlowStates.entrySet()) {
            entry.getKey().setGlowingTag(entry.getValue());
        }
        originalGlowStates.clear();
    }
}
