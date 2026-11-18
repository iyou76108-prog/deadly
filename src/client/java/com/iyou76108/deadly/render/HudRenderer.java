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
        if (player == null || client.level == null) return;

        Font font = client.font;
        if (enabled("watermark")) {
            VisualModule module = VisualRegistry.get("watermark");
            Setting<?> showVersion = module.getSetting("showVersion");
            String value = Boolean.TRUE.equals(showVersion.getValue())
                    ? "DEADLY VISUALS  v" + DeadlyClient.VERSION : "DEADLY VISUALS";
            drawPanel(graphics, font, "watermark", value);
        }

        if (enabled("fps")) {
            VisualModule module = VisualRegistry.get("fps");
            boolean label = Boolean.TRUE.equals(module.getSetting("showLabel").getValue());
            drawPanel(graphics, font, "fps",
                    label ? "FPS: " + client.getFps() : Integer.toString(client.getFps()));
        }

        if (enabled("coordinates")) {
            VisualModule module = VisualRegistry.get("coordinates");
            String position = String.format("XYZ: %d %d %d",
                    player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ());
            if (Boolean.TRUE.equals(module.getSetting("showDimension").getValue())) {
                position += "  |  " + client.level.dimension().identifier().getPath();
            }
            drawPanel(graphics, font, "coordinates", position);
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
            drawPanel(graphics, font, "direction", value);
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
                        totalDurability += Math.max(0,
                                100 - (stack.getDamageValue() * 100 / stack.getMaxDamage()));
                        durableItems++;
                    }
                }
            }
            String armorText = "Armor: " + equipped + "/4";
            if (Boolean.TRUE.equals(module.getSetting("showPercent").getValue()) && durableItems > 0) {
                armorText += "  " + (totalDurability / durableItems) + "%";
            }
            drawPanel(graphics, font, "armor", armorText);
        }

        if (enabled("potions")) {
            VisualModule module = VisualRegistry.get("potions");
            if (Boolean.TRUE.equals(module.getSetting("showCount").getValue())) {
                drawPanel(graphics, font, "potions", "Effects: " + player.getActiveEffects().size());
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
                    drawPanel(graphics, font, "target",
                            target.getName().getString() + "  "
                                    + String.format(java.util.Locale.ROOT, "%.1fm", distance));
                }
            }
        }

        if (enabled("keystrokes")) renderKeystrokes(graphics, client);
        if (enabled("module_list")) renderModuleList(graphics, font, client);
        if (enabled("crosshair")) renderCrosshair(graphics, client, VisualRegistry.get("crosshair"));
    }

    private static void renderKeystrokes(GuiGraphics graphics, Minecraft client) {
        VisualModule module = VisualRegistry.get("keystrokes");
        HudLayoutManager.Widget layout = HudLayoutManager.get("keystrokes");
        if (layout == null) return;
        int x = HudLayoutManager.resolveX("keystrokes", client.getWindow().getGuiScaledWidth(), 64);
        int y = HudLayoutManager.getY("keystrokes");

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(layout.scale(), layout.scale(), 1.0f);
        drawKey(graphics, client.font, "W", 22, 0, client.options.keyUp.isDown(),
                layout.opacity());
        drawKey(graphics, client.font, "A", 2, 20, client.options.keyLeft.isDown(),
                layout.opacity());
        drawKey(graphics, client.font, "S", 22, 20, client.options.keyDown.isDown(),
                layout.opacity());
        drawKey(graphics, client.font, "D", 42, 20, client.options.keyRight.isDown(),
                layout.opacity());
        if (Boolean.TRUE.equals(module.getSetting("showJump").getValue())) {
            drawKey(graphics, client.font, "JUMP", 2, 40, client.options.keyJump.isDown(),
                    layout.opacity(), 60);
        }
        graphics.pose().popPose();
    }

    private static void drawKey(GuiGraphics graphics, Font font, String text, int x, int y,
                                boolean active, float opacity) {
        drawKey(graphics, font, text, x, y, active, opacity, 18);
    }

    private static void drawKey(GuiGraphics graphics, Font font, String text, int x, int y,
                                boolean active, float opacity, int width) {
        graphics.fill(x, y, x + width, y + 18,
                withOpacity(active ? 0xDD26749A : PANEL, opacity));
        graphics.renderOutline(x, y, width, 18,
                withOpacity(active ? ACCENT : 0x665B6A7F, opacity));
        graphics.drawString(font, text, x + (width - font.width(text)) / 2,
                y + 5, withOpacity(TEXT, opacity), true);
    }

    private static void renderModuleList(GuiGraphics graphics, Font font, Minecraft client) {
        VisualModule listModule = VisualRegistry.get("module_list");
        HudLayoutManager.Widget layout = HudLayoutManager.get("module_list");
        if (layout == null) return;
        boolean rightAligned = Boolean.TRUE.equals(listModule.getSetting("rightAligned").getValue());
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int y = HudLayoutManager.getY("module_list");

        for (VisualModule module : VisualRegistry.all()) {
            if (!module.isEnabled() || module.getId().equals("module_list")) continue;
            String text = module.getName();
            int width = font.width(text) + 12;
            int x;
            if (rightAligned) {
                x = layout.x() < 0 ? screenWidth + layout.x() - width : screenWidth - width - 8;
            } else {
                x = HudLayoutManager.resolveX("module_list", screenWidth, width);
            }
            drawPanelAt(graphics, font, x, y, text, layout);
            y += Math.max(12, Math.round(18 * layout.scale()));
        }
    }

    private static void renderCrosshair(GuiGraphics graphics, Minecraft client, VisualModule module) {
        int size = ((Number) module.getSetting("size").getValue()).intValue();
        HudLayoutManager.Widget layout = HudLayoutManager.get("crosshair");
        if (layout == null) return;
        int gap = 3;
        int thickness = 2;
        int centerX = client.getWindow().getGuiScaledWidth() / 2 + layout.x();
        int centerY = client.getWindow().getGuiScaledHeight() / 2 + layout.y();
        int color = withOpacity(ACCENT, layout.opacity());

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0);
        graphics.pose().scale(layout.scale(), layout.scale(), 1.0f);
        graphics.fill(-gap - size, -thickness / 2, -gap, (thickness + 1) / 2, color);
        graphics.fill(gap, -thickness / 2, gap + size, (thickness + 1) / 2, color);
        graphics.fill(-thickness / 2, -gap - size, (thickness + 1) / 2, -gap, color);
        graphics.fill(-thickness / 2, gap, (thickness + 1) / 2, gap + size, color);
        graphics.pose().popPose();
    }

    private static void drawPanel(GuiGraphics graphics, Font font, String id, String text) {
        HudLayoutManager.Widget layout = HudLayoutManager.get(id);
        if (layout == null) return;
        int width = font.width(text) + 12;
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int x = HudLayoutManager.resolveX(id, screenWidth, width);
        drawPanelAt(graphics, font, x, HudLayoutManager.getY(id), text, layout);
    }

    private static void drawPanelAt(GuiGraphics graphics, Font font, int x, int y,
                                    String text, HudLayoutManager.Widget layout) {
        int width = font.width(text) + 12;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(layout.scale(), layout.scale(), 1.0f);
        graphics.fill(0, 0, width, 16, withOpacity(PANEL, layout.opacity()));
        graphics.fill(0, 0, 2, 16, withOpacity(ACCENT, layout.opacity()));
        graphics.drawString(font, text, 6, 4, withOpacity(TEXT, layout.opacity()), true);
        graphics.pose().popPose();
    }

    private static int withOpacity(int color, float opacity) {
        int alpha = (color >>> 24) & 0xFF;
        int adjusted = Math.max(0, Math.min(255, Math.round(alpha * opacity)));
        return (color & 0x00FFFFFF) | (adjusted << 24);
    }

    private static boolean enabled(String id) {
        VisualModule module = VisualRegistry.get(id);
        return module != null && module.isEnabled();
    }
}
