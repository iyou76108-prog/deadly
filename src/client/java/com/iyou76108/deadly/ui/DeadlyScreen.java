package com.iyou76108.deadly.ui;

import com.iyou76108.deadly.config.ConfigManager;
import com.iyou76108.deadly.core.Setting;
import com.iyou76108.deadly.core.VisualModule;
import com.iyou76108.deadly.core.VisualRegistry;
import com.iyou76108.deadly.ui.HudEditorScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Deadly Visuals' category-based control centre.
 * The Skins page is intentionally a roadmap placeholder until its feature work begins.
 */
public final class DeadlyScreen extends Screen {
    private static final int ACCENT = 0xFFE34B68;
    private static final int PANEL = 0xE8121722;
    private static final int MUTED = 0xFF9AA7B8;
    private static final List<String> TABS = List.of("HUD", "Visuals", "Player", "Interface", "Effects", "Skins");

    private final Screen parent;
    private final String activeTab;

    public DeadlyScreen(Screen parent) {
        this(parent, "HUD");
    }

    private DeadlyScreen(Screen parent, String activeTab) {
        super(Component.literal("Deadly Visuals"));
        this.parent = parent;
        this.activeTab = activeTab;
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(760, this.width - 32);
        int left = (this.width - panelWidth) / 2;
        int navY = 54;
        int navGap = 5;
        int navWidth = (panelWidth - navGap * (TABS.size() - 1)) / TABS.size();

        for (int i = 0; i < TABS.size(); i++) {
            String tab = TABS.get(i);
            int x = left + i * (navWidth + navGap);
            this.addRenderableWidget(Button.builder(Component.literal(tab), button ->
                    this.minecraft.setScreen(new DeadlyScreen(parent, tab)))
                    .bounds(x, navY, navWidth, 22)
                    .build());
        }

        if (activeTab.equals("Skins") || activeTab.equals("Interface") || activeTab.equals("Effects")) {
            if (activeTab.equals("Interface")) {
                this.addRenderableWidget(Button.builder(Component.literal("Open HUD Editor"), button ->
                        this.minecraft.setScreen(new HudEditorScreen(this)))
                        .bounds(this.width / 2 - 90, this.height / 2 + 20, 180, 24).build());
            }
            addFooterButtons(left, panelWidth);
            return;
        }

        List<VisualModule> modules = filteredModules();
        int contentTop = 91;
        int cardWidth = (panelWidth - 12) / 2;
        int rowHeight = 54;
        int maxRows = Math.max(1, (this.height - 145) / rowHeight);
        int visible = Math.min(modules.size(), maxRows * 2);

        for (int i = 0; i < visible; i++) {
            VisualModule module = modules.get(i);
            int column = i % 2;
            int row = i / 2;
            int x = left + column * (cardWidth + 12);
            int y = contentTop + row * rowHeight;

            this.addRenderableWidget(Button.builder(toggleLabel(module), button -> {
                module.toggle();
                button.setMessage(toggleLabel(module));
                ConfigManager.save();
            }).bounds(x + 8, y + 6, cardWidth - 16, 20).build());

            List<Setting<?>> settings = module.getSettings();
            if (!settings.isEmpty()) {
                Setting<?> setting = settings.get(0);
                this.addRenderableWidget(Button.builder(settingLabel(setting), button -> {
                    setting.cycle();
                    button.setMessage(settingLabel(setting));
                    ConfigManager.save();
                }).bounds(x + 8, y + 29, cardWidth - 16, 18).build());
            }
        }

        addFooterButtons(left, panelWidth);
    }

    private void addFooterButtons(int left, int panelWidth) {
        int bottom = this.height - 31;
        this.addRenderableWidget(Button.builder(Component.literal("Save profile"), button -> {
            ConfigManager.saveProfile("default");
            button.setMessage(Component.literal("Saved ✓"));
        }).bounds(left + panelWidth / 2 - 158, bottom, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Load profile"), button -> {
            boolean loaded = ConfigManager.loadProfile("default");
            button.setMessage(Component.literal(loaded ? "Loaded ✓" : "No profile"));
            this.minecraft.setScreen(new DeadlyScreen(parent, activeTab));
        }).bounds(left + panelWidth / 2 - 52, bottom, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(left + panelWidth / 2 + 54, bottom, 100, 20).build());
    }

    private List<VisualModule> filteredModules() {
        List<VisualModule> result = new ArrayList<>();
        for (VisualModule module : VisualRegistry.all()) {
            boolean matches = switch (activeTab) {
                case "HUD" -> module.getCategory() == VisualModule.Category.HUD;
                case "Player" -> module.getCategory() == VisualModule.Category.PLAYER;
                case "Visuals" -> module.getCategory() == VisualModule.Category.WORLD
                        || module.getCategory() == VisualModule.Category.ENVIRONMENT;
                default -> false;
            };
            if (matches) result.add(module);
        }
        return result;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        int panelWidth = Math.min(760, this.width - 32);
        int left = (this.width - panelWidth) / 2;
        graphics.fill(left - 8, 12, left + panelWidth + 8, this.height - 12, PANEL);
        graphics.fill(left - 8, 12, left + panelWidth + 8, 14, ACCENT);
        graphics.drawString(this.font, "DEADLY", left + 4, 22, 0xFFFFFFFF, true);
        graphics.drawString(this.font, "VISUALS  /  CLIENT CUSTOMIZATION", left + 78, 23, MUTED, false);
        graphics.drawString(this.font, "Right Shift  •  " + VisualRegistry.enabledCount() + " modules enabled",
                left + 4, 38, MUTED, false);

        if (activeTab.equals("Skins")) {
            graphics.drawCenteredString(this.font, "SKINS", this.width / 2, this.height / 2 - 24, 0xFFFFFFFF);
            graphics.drawCenteredString(this.font, "Coming later — custom player skin tools are planned.",
                    this.width / 2, this.height / 2 - 7, MUTED);
            graphics.drawCenteredString(this.font, "This page is reserved for a future update.",
                    this.width / 2, this.height / 2 + 9, 0xFF718096);
        } else if (activeTab.equals("Interface")) {
            graphics.drawCenteredString(this.font, "INTERFACE TOOLS",
                    this.width / 2, this.height / 2 - 28, 0xFFFFFFFF);
            graphics.drawCenteredString(this.font, "Arrange HUD elements, resize them and adjust opacity.",
                    this.width / 2, this.height / 2 - 10, MUTED);
        } else if (activeTab.equals("Effects")) {
            graphics.drawCenteredString(this.font, "EFFECTS",
                    this.width / 2, this.height / 2 - 16, 0xFFFFFFFF);
            graphics.drawCenteredString(this.font, "Additional visual effects are planned for a future update.",
                    this.width / 2, this.height / 2 + 2, MUTED);
        } else {
            graphics.drawString(this.font, activeTab.toUpperCase(Locale.ROOT) + " MODULES",
                    left + 4, 82, 0xFFFFFFFF, true);
            List<VisualModule> modules = filteredModules();
            int cardWidth = (panelWidth - 12) / 2;
            int maxRows = Math.max(1, (this.height - 145) / 54);
            for (int i = 0; i < Math.min(modules.size(), maxRows * 2); i++) {
                int x = left + (i % 2) * (cardWidth + 12);
                int y = 91 + (i / 2) * 54;
                graphics.fill(x, y, x + cardWidth, y + 51, 0xB51D2633);
                graphics.fill(x, y, x + 2, y + 51, ACCENT);
                graphics.drawString(this.font, modules.get(i).getDescription(),
                        x + 9, y + 49 - this.font.lineHeight, MUTED, false);
            }
            if (modules.isEmpty()) {
                graphics.drawCenteredString(this.font, "No modules in this category yet.",
                        this.width / 2, this.height / 2, MUTED);
            }
        }
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        ConfigManager.save();
        this.minecraft.setScreen(parent);
    }

    private static Component toggleLabel(VisualModule module) {
        return Component.literal((module.isEnabled() ? "●  " : "○  ")
                + module.getName() + (module.isEnabled() ? "   ON" : "   OFF"));
    }

    private static Component settingLabel(Setting<?> setting) {
        return Component.literal(setting.getLabel() + ": " + setting.formattedValue());
    }
}
