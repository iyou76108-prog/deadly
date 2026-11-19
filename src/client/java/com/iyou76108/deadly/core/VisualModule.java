package com.iyou76108.deadly.core;

import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VisualModule {
    public enum Category {
        HUD("HUD"), PLAYER("Player"), WORLD("World"), ENVIRONMENT("Environment"),
        CHEATS("Cheats"), EFFECTS("Effects");
        private final String title;
        Category(String title) { this.title = title; }
        public String getTitle() { return title; }
    }

    private final String id;
    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private final boolean defaultEnabled;
    private boolean enabled;
    private Runnable changeListener = () -> { };

    public VisualModule(String id, String name, String description, Category category, boolean defaultEnabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.defaultEnabled = defaultEnabled;
        this.enabled = defaultEnabled;
    }

    public <T> Setting<T> addSetting(Setting<T> setting) {
        setting.setChangeListener(this::changed);
        settings.add(setting);
        return setting;
    }
    public Setting<Boolean> addBoolean(String id, String label, boolean defaultValue) {
        return addSetting(Setting.bool(id, label, defaultValue));
    }
    public Setting<Integer> addInteger(String id, String label, int defaultValue, int min, int max, int step) {
        return addSetting(Setting.integer(id, label, defaultValue, min, max, step));
    }
    public Setting<Double> addDecimal(String id, String label, double defaultValue, double min, double max, double step) {
        return addSetting(Setting.decimal(id, label, defaultValue, min, max, step));
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
        changed();
    }
    public void toggle() { setEnabled(!enabled); }
    public void resetSettings() {
        for (Setting<?> setting : settings) setting.reset();
        setEnabled(defaultEnabled);
    }
    public List<Setting<?>> getSettings() { return Collections.unmodifiableList(settings); }
    public Setting<?> getSetting(String id) {
        return settings.stream().filter(setting -> setting.getId().equals(id)).findFirst().orElse(null);
    }
    public void setChangeListener(Runnable listener) {
        this.changeListener = listener == null ? () -> { } : listener;
    }
    protected void onEnable() { }
    protected void onDisable() { }
    public void tick(Minecraft client) { }
    private void changed() { changeListener.run(); }
}
