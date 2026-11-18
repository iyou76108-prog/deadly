package com.iyou76108.deadly.ui;

import com.iyou76108.deadly.config.ConfigManager;
import com.iyou76108.deadly.core.Setting;
import com.iyou76108.deadly.core.VisualModule;
import com.iyou76108.deadly.core.VisualRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DeadlyScreen extends Screen {
    private final Screen parent;

    public DeadlyScreen(Screen parent) {
        super(Component.literal("Deadly Visuals"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 205;
        int top = 48;
        int columnWidth = 205;
        int rowHeight = 34;
        int index = 0;

        for (VisualModule module : VisualRegistry.all()) {
            int column = index % 2;
            int row = index / 2;
            int x = left + column * columnWidth;
            int y = top + row * rowHeight;

            Button toggle = Button.builder(toggleLabel(module), button -> {
                module.toggle();
                button.setMessage(toggleLabel(module));
                ConfigManager.save();
            }).bounds(x, y, 105, 20).build();
            this.addRenderableWidget(toggle);

            if (!module.getSettings().isEmpty()) {
                Setting<?> setting = module.getSettings().get(0);
                Button settingButton = Button.builder(settingLabel(setting), button -> {
                    setting.cycle();
                    button.setMessage(settingLabel(setting));
                    ConfigManager.save();
                }).bounds(x + 109, y, 94, 20).build();
                this.addRenderableWidget(settingButton);
            }
            index++;
        }

        int bottom = Math.min(this.height - 28, top + ((index + 1) / 2) * rowHeight + 4);
        this.addRenderableWidget(Button.builder(Component.literal("Save profile"), button -> {
            ConfigManager.saveProfile("default");
            button.setMessage(Component.literal("Profile saved"));
        }).bounds(this.width / 2 - 156, bottom, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Load profile"), button -> {
            boolean loaded = ConfigManager.loadProfile("default");
            button.setMessage(Component.literal(loaded ? "Profile loaded" : "No saved profile"));
            this.minecraft.setScreen(new DeadlyScreen(parent));
        }).bounds(this.width / 2 - 50, bottom, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(this.width / 2 + 56, bottom, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font,
                "Right Shift opens this menu  |  Changes save automatically",
                this.width / 2, 31, 0xFF9EACBF);
    }

    @Override
    public void onClose() {
        ConfigManager.save();
        this.minecraft.setScreen(parent);
    }

    private static Component toggleLabel(VisualModule module) {
        return Component.literal(module.getName() + ": " + (module.isEnabled() ? "ON" : "OFF"));
    }

    private static Component settingLabel(Setting<?> setting) {
        return Component.literal(setting.getLabel() + ": " + setting.formattedValue());
    }
}
