package com.iyou76108.deadly.modules;

import com.iyou76108.deadly.core.Setting;
import com.iyou76108.deadly.core.VisualModule;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import java.util.Locale;

final class EntityBoxRenderer {
    private EntityBoxRenderer() { }

    static void render(Minecraft client, VisualModule module, boolean showLabels) {
        if (client == null || client.level == null || client.player == null || !module.isEnabled()) return;
        double range = number(module, "range", 64);
        double rangeSquared = range * range;
        int red = clamp(number(module, "red", 255), 0, 255);
        int green = clamp(number(module, "green", 64), 0, 255);
        int blue = clamp(number(module, "blue", 96), 0, 255);
        int strokeColor = 0xFF000000 | (red << 16) | (green << 8) | blue;
        int alpha = (int) Math.round(clamp(number(module, "fillOpacity", 35), 0, 100) * 2.55);
        int fillColor = (alpha << 24) | (red << 16) | (green << 8) | blue;
        float lineWidth = (float) Math.max(1.0, Math.min(6.0, number(module, "lineWidth", 2.0)));
        boolean filled = bool(module, "fill");
        boolean throughWalls = bool(module, "throughWalls");
        boolean includeInvisible = bool(module, "includeInvisible");
        boolean showNames = showLabels && bool(module, "showNames");
        boolean showDistance = showLabels && bool(module, "showDistance");
        GizmoStyle style = filled
                ? GizmoStyle.strokeAndFill(strokeColor, lineWidth, fillColor)
                : GizmoStyle.stroke(strokeColor, lineWidth);

        try {
            for (Entity entity : client.level.entitiesForRendering()) {
                if (entity == client.player || entity.isRemoved()) continue;
                if (!includeInvisible && entity.isInvisible()) continue;
                if (!matchesCategory(module, entity)) continue;
                double distanceSquared = client.player.distanceToSqr(entity);
                if (distanceSquared > rangeSquared) continue;

                GizmoProperties box = Gizmos.cuboid(entity.getBoundingBox(), style);
                box.persistForMillis(100);
                if (throughWalls) box.setAlwaysOnTop();

                if (showNames) {
                    String label = entity.getName().getString();
                    if (showDistance) {
                        label += "  " + String.format(Locale.ROOT, "%.1fm", Math.sqrt(distanceSquared));
                    }
                    Gizmos.billboardTextOverMob(entity, 0, label, strokeColor, 0.8f).persistForMillis(100);
                }
            }
        } catch (IllegalStateException ignored) {
            // A client tick can run outside the active gizmo collection window.
        }
    }

    private static boolean matchesCategory(VisualModule module, Entity entity) {
        if (entity instanceof Player) return bool(module, "players");
        if (entity instanceof Monster) return bool(module, "hostile");
        if (entity instanceof Animal) return bool(module, "animals");
        return bool(module, "other");
    }

    private static boolean bool(VisualModule module, String id) {
        Setting<?> setting = module.getSetting(id);
        return setting != null && Boolean.TRUE.equals(setting.getValue());
    }

    private static double number(VisualModule module, String id, double fallback) {
        Setting<?> setting = module.getSetting(id);
        return setting != null && setting.getValue() instanceof Number number
                ? number.doubleValue() : fallback;
    }

    private static int clamp(double value, int min, int max) {
        return (int) Math.max(min, Math.min(max, value));
    }
}
