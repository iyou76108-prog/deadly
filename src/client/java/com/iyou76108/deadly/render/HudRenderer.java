package com.iyou76108.deadly.render;

import com.iyou76108.deadly.DeadlyClient;
import com.iyou76108.deadly.core.Setting;
import com.iyou76108.deadly.core.VisualModule;
import com.iyou76108.deadly.core.VisualRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public final class HudRenderer {
    private static final int ACCENT = 0xFF67D7FF;
    private static final int PANEL = 0xB8101420;
    private static final int TEXT = 0xFFF2F6FF;

    private HudRenderer() {
    }

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(DeadlyClient.MOD_ID, "visual_hud"),
                HudRenderer::render
        );
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            return;
        }

        Font font = client.font;
        int left = 8;
        int y = 8;

        if (enabled("watermark")) {
            VisualModule module = VisualRegistry.get("watermark");
            Setting<?> showVersion = module.getSetting("showVersion");
            String value = Boolean.TRUE.equals(showVersion.getValue())
                    ? "DEADLY VISUALS  v" + DeadlyClient.VERSION
                    : "DEADLY VISUALS";
            drawPanel(graphics, font, value, left, y);
            y += 18;
        }

        if (enabled("fps")) {
            VisualModule module = VisualRegistry.get("fps");
            boolean label = Boolean.TRUE.equals(module.getSetting("showLabel").getValue());
            drawPanel(graphics, font, label ? "FPS: " + client.getFps() : Integer.toString(client.getFps()), left, y);
            y += 18;
        }

        if (enabled("coordinates")) {
            VisualModule module = VisualRegistry.get("coordinates");
            String position = String.format("XYZ: %d %d %d",
                    player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ());
            if (Boolean.TRUE.equals(module.getSetting("showDimension").getValue())) {
                position += "  |  " + client.level.dimension().identifier().getPath();
            }
            drawPanel(graphics, font, position, left, y);
            y += 18;
        }

        if (enabled("direction")) {
            VisualModule module = VisualRegistry.get("direction");
            float yaw = player.getYRot();
            String[] directions = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
            int index = Math.floorMod(Math.round(yaw / 45.0f), 8);
            String value = "Facing: " + directions[index];
            if (Boolean.TRUE.equals(module.getSetting("showDegrees").getValue())) {
                value += " (" + Math.round(yaw) + "°)";
            }
            drawPanel(graphics, font, value, left, y);
            y += 18;
        }

        if (enabled("armor")) {
            VisualModule module = VisualRegistry.get("armor");
            int equipped = 0;
            int totalDurability = 0;
            int durableItems = 0;
            for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                ItemStack stack = player.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    equipped++;
                    if (stack.getMaxDamage() > 0) {
                        totalDurability += Math.max(0, 100 - (stack.getDamageValue() * 100 / stack.getMaxDamage()));
                        durableItems++;
                    }
                }
            }
            String armorText = "Armor: " + equipped + "/4";
            if (Boolean.TRUE.equals(module.getSetting("showPercent").getValue()) && durableItems > 0) {
                armorText += "  " + (totalDurability / durableItems) + "%";
            }
            drawPanel(graphics, font, armorText, left, y);
            y += 18;
        }

        if (enabled("potions")) {
            VisualModule module = VisualRegistry.get("potions");
            if (Boolean.TRUE.equals(module.getSetting("showCount").getValue())) {
                drawPanel(graphics, font, "Effects: " + player.getActiveEffects().size(), left, y);
                y += 18;
            }
        }

        if (enabled("target")) {
            VisualModule module = VisualRegistry.get("target");
            double range = ((Number) module.getSetting("range").getValue()).doubleValue();
            HitResult hit = client.hitResult;
            if (hit instanceof EntityHitResult entityHit) {
                Entity target = entityHit.getEntity();
                double distance = player.distanceTo(target);
                if (distance <= range) {
                    drawPanel(graphics, font,
                            target.getName().getString() + "  " + String.format(java.util.Locale.ROOT, "%.1fm", distance),
                            left, y);
                    y += 18;
                }
            }
        }

        if (enabled("keystrokes")) {
            renderKeystrokes(graphics, client);
        }

        if (enabled("module_list")) {
            renderModuleList(graphics, font, client);
        }

        if (enabled("crosshair")) {
            renderCrosshair(graphics, client, VisualRegistry.get("crosshair"));
        }
    }

    private static void renderKeystrokes(GuiGraphics graphics, Minecraft client) {
        VisualModule module = VisualRegistry.get("keystrokes");
        int x = client.getWindow().getGuiScaledWidth() - 76;
        int y = 12;
        drawKey(graphics, client.font, "W", x + 20, y, client.options.keyUp.isDown());
        drawKey(graphics, client.font, "A", x, y + 20, client.options.keyLeft.isDown());
        drawKey(graphics, client.font, "S", x + 20, y + 20, client.options.keyDown.isDown());
        drawKey(graphics, client.font, "D", x + 40, y + 20, client.options.keyRight.isDown());
        if (Boolean.TRUE.equals(module.getSetting("showJump").getValue())) {
            drawKey(graphics, client.font, "JUMP", x, y + 40, client.options.keyJump.isDown(), 60);
        }
    }

    private static void drawKey(GuiGraphics graphics, Font font, String text, int x, int y, boolean active) {
        drawKey(graphics, font, text, x, y, active, 18);
    }

    private static void drawKey(GuiGraphics graphics, Font font, String text, int x, int y,
                                boolean active, int width) {
        graphics.fill(x, y, x + width, y + 18, active ? 0xDD26749A : PANEL);
        graphics.renderOutline(x, y, width, 18, active ? ACCENT : 0x665B6A7F);
        graphics.drawString(font, text, x + (width - font.width(text)) / 2,
                y + 5, TEXT, true);
    }

    private static void renderModuleList(GuiGraphics graphics, Font font, Minecraft client) {
        VisualModule listModule = VisualRegistry.get("module_list");
        boolean rightAligned = Boolean.TRUE.equals(listModule.getSetting("rightAligned").getValue());
        List<VisualModule> modules = new ArrayList<>();
        for (VisualModule module : VisualRegistry.all()) {
            if (module.isEnabled() && !module.getId().equals("module_list")) {
                modules.add(module);
            }
        }
        int y = 10;
        for (VisualModule module : modules) {
            String text = module.getName();
            int width = font.width(text) + 12;
            int x = rightAligned ? client.getWindow().getGuiScaledWidth() - width - 8 : 8;
            graphics.fill(x, y, x + width, y + 16, PANEL);
            graphics.fill(rightAligned ? x + width - 2 : x, y,
                    rightAligned ? x + width : x + 2, y + 16, ACCENT);
            graphics.drawString(font, text, x + 6, y + 4, TEXT, true);
            y += 18;
        }
    }

    private static void renderCrosshair(GuiGraphics graphics, Minecraft client, VisualModule module) {
        int size = ((Number) module.getSetting("size").getValue()).intValue();
        int gap = 3;
        int thickness = 2;
        int centerX = client.getWindow().getGuiScaledWidth() / 2;
        int centerY = client.getWindow().getGuiScaledHeight() / 2;

        graphics.fill(centerX - gap - size, centerY - thickness / 2,
                centerX - gap, centerY + (thickness + 1) / 2, ACCENT);
        graphics.fill(centerX + gap, centerY - thickness / 2,
                centerX + gap + size, centerY + (thickness + 1) / 2, ACCENT);
        graphics.fill(centerX - thickness / 2, centerY - gap - size,
                centerX + (thickness + 1) / 2, centerY - gap, ACCENT);
        graphics.fill(centerX - thickness / 2, centerY + gap,
                centerX + (thickness + 1) / 2, centerY + gap + size, ACCENT);
    }

    private static void drawPanel(GuiGraphics graphics, Font font, String text, int x, int y) {
        int width = font.width(text) + 12;
        graphics.fill(x, y, x + width, y + 16, PANEL);
        graphics.fill(x, y, x + 2, y + 16, ACCENT);
        graphics.drawString(font, text, x + 6, y + 4, TEXT, true);
    }

    private static boolean enabled(String id) {
        VisualModule module = VisualRegistry.get(id);
        return module != null && module.isEnabled();
    }
}
