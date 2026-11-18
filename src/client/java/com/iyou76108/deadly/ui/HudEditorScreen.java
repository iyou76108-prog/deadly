package com.iyou76108.deadly.ui;

import com.iyou76108.deadly.render.HudLayoutManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * In-game HUD layout editor. Widgets can be dragged with the mouse;
 * scale and opacity controls apply to the selected widget and persist immediately.
 */
public final class HudEditorScreen extends Screen {
    private static final int ACCENT = 0xFFE34B68;
    private static final int PANEL = 0xE8121722;
    private static final int MUTED = 0xFF9AA7B8;
    private static final Map<String, String> PREVIEWS = Map.ofEntries(
            Map.entry("watermark", "DEADLY VISUALS"),
            Map.entry("fps", "FPS: 144"),
            Map.entry("coordinates", "XYZ: 120 64 -320"),
            Map.entry("direction", "Facing: NW"),
            Map.entry("armor", "Armor: 4/4  100%"),
            Map.entry("potions", "Effects: 2"),
            Map.entry("target", "Target: Entity  4.2m"),
            Map.entry("clock", "TIME: 12:45:30"),
            Map.entry("speed", "Speed: 4.3 m/s"),
            Map.entry("biome", "Biome: plains"),
            Map.entry("server", "Server: play.example.net"),
            Map.entry("keystrokes", "W   A   S   D"),
            Map.entry("module_list", "Module List"),
            Map.entry("crosshair", "+")
    );

    private final Screen parent;
    private final List<WidgetBounds> bounds = new ArrayList<>();
    private String selected;
    private String dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("Deadly Visuals — HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(760, this.width - 24);
        int left = (this.width - panelWidth) / 2;
        int gap = 5;
        int buttonWidth = Math.max(76, (panelWidth - gap * 5) / 6);
        String[] labels = {"Scale −", "Scale +", "Opacity −", "Opacity +", "Reset all", "Done"};

        for (int i = 0; i < labels.length; i++) {
            final int action = i;
            int x = left + i * (buttonWidth + gap);
            this.addRenderableWidget(Button.builder(Component.literal(labels[i]), button -> {
                switch (action) {
                    case 0 -> adjustScale(-0.1f);
                    case 1 -> adjustScale(0.1f);
                    case 2 -> adjustOpacity(-0.1f);
                    case 3 -> adjustOpacity(0.1f);
                    case 4 -> {
                        HudLayoutManager.reset();
                        selected = null;
                    }
                    default -> onClose();
                }
                HudLayoutManager.save();
            }).bounds(x, this.height - 31, buttonWidth, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        int panelWidth = Math.min(760, this.width - 24);
        int left = (this.width - panelWidth) / 2;
        int bottom = this.height - 42;

        graphics.fill(left - 8, 10, left + panelWidth + 8, bottom, PANEL);
        graphics.fill(left - 8, 10, left + panelWidth + 8, 12, ACCENT);
        graphics.drawString(this.font, "HUD EDITOR", left + 4, 21, 0xFFFFFFFF, true);
        graphics.drawString(this.font, "Drag widgets to move  •  Select one, then use scale / opacity controls",
                left + 4, 37, MUTED, false);

        // A subtle editor grid makes alignment easier without affecting the actual HUD.
        for (int x = left; x < left + panelWidth; x += 20) {
            graphics.fill(x, 54, x + 1, bottom - 4, 0x223E4B5C);
        }
        for (int y = 54; y < bottom - 4; y += 20) {
            graphics.fill(left, y, left + panelWidth, y + 1, 0x223E4B5C);
        }

        bounds.clear();
        for (String id : HudLayoutManager.ids()) {
            drawWidget(graphics, id, left, panelWidth);
        }

        HudLayoutManager.Widget widget = selected == null ? null : HudLayoutManager.get(selected);
        String selection = widget == null ? "Selected: none"
                : "Selected: " + selected + "   Scale: " + String.format(java.util.Locale.ROOT, "%.1f", widget.scale())
                + "   Opacity: " + Math.round(widget.opacity() * 100) + "%";
        graphics.drawString(this.font, selection, left + 4, bottom - 15, 0xFFFFFFFF, false);
        super.render(graphics, mouseX, mouseY, delta);
    }

    private void drawWidget(GuiGraphics graphics, String id, int left, int panelWidth) {
        String text = PREVIEWS.getOrDefault(id, id);
        HudLayoutManager.Widget widget = HudLayoutManager.get(id);
        if (widget == null) return;

        float scale = widget.scale();
        int contentWidth = id.equals("keystrokes") ? 64
                : id.equals("crosshair") ? 18 : this.font.width(text) + 12;
        int contentHeight = id.equals("crosshair") ? 18 : 18;
        int x;
        int y;
        if (id.equals("crosshair")) {
            x = this.width / 2 - 9;
            y = this.height / 2 - 9;
        } else {
            x = HudLayoutManager.resolveX(id, this.width, contentWidth);
            y = HudLayoutManager.getY(id);
        }

        int scaledWidth = Math.max(12, Math.round(contentWidth * scale));
        int scaledHeight = Math.max(12, Math.round(contentHeight * scale));
        int alpha = Math.max(25, Math.min(255, Math.round(widget.opacity() * 220)));
        int background = (alpha << 24) | 0x101722;
        int outline = id.equals(selected) ? ACCENT : 0xAA667487;

        graphics.fill(x, y, x + scaledWidth, y + scaledHeight, background);
        graphics.renderOutline(x, y, scaledWidth, scaledHeight, outline);

        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        if (id.equals("crosshair")) {
            graphics.fill(7, 2, 11, 16, 0xFFE34B68);
            graphics.fill(2, 7, 16, 11, 0xFFE34B68);
        } else {
            graphics.drawString(this.font, text, 6, 5, 0xFFFFFFFF, true);
        }
        graphics.pose().popMatrix();

        bounds.add(new WidgetBounds(id, x, y, scaledWidth, scaledHeight));
    }

    private void adjustScale(float amount) {
        if (selected != null) HudLayoutManager.adjustScale(selected, amount);
    }

    private void adjustOpacity(float amount) {
        if (selected != null) HudLayoutManager.adjustOpacity(selected, amount);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        if (super.mouseClicked(click, doubleClick)) return true;
        if (click.button() != 0) return false;

        double mouseX = click.x();
        double mouseY = click.y();
        for (int i = bounds.size() - 1; i >= 0; i--) {
            WidgetBounds item = bounds.get(i);
            if (mouseX >= item.x && mouseX <= item.x + item.width
                    && mouseY >= item.y && mouseY <= item.y + item.height) {
                selected = item.id;
                dragging = item.id;
                dragOffsetX = mouseX - item.x;
                dragOffsetY = mouseY - item.y;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dragX, double dragY) {
        if (dragging != null && click.button() == 0) {
            double mouseX = click.x() + dragX;
            double mouseY = click.y() + dragY;
            int x = (int) Math.round(mouseX - dragOffsetX);
            int y = (int) Math.round(mouseY - dragOffsetY);
            if ("crosshair".equals(dragging)) {
                HudLayoutManager.moveCrosshair(x - this.width / 2 + 9, y - this.height / 2 + 9);
            } else {
                HudLayoutManager.move(dragging, x, y);
            }
            return true;
        }
        return super.mouseDragged(click, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (dragging != null) {
            dragging = null;
            HudLayoutManager.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (selected != null && scrollY != 0) {
            HudLayoutManager.adjustScale(selected, scrollY > 0 ? 0.05f : -0.05f);
            HudLayoutManager.save();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        HudLayoutManager.save();
        this.minecraft.setScreen(parent);
    }

    private record WidgetBounds(String id, int x, int y, int width, int height) {
    }
}
