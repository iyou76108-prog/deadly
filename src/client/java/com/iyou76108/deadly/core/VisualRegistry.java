package com.iyou76108.deadly.core;

import com.iyou76108.deadly.modules.BuiltinModules;
import net.minecraft.client.Minecraft;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class VisualRegistry {
    private static final Map<String, VisualModule> MODULES = new LinkedHashMap<>();
    private static final Collection<VisualModule> VIEW = Collections.unmodifiableCollection(MODULES.values());
    private VisualRegistry() { }

    public static void registerDefaults() {
        MODULES.clear();
        BuiltinModules.register();
    }
    public static void register(VisualModule module) {
        if (MODULES.putIfAbsent(module.getId(), module) != null) {
            throw new IllegalArgumentException("Duplicate visual module id: " + module.getId());
        }
        module.setChangeListener(com.iyou76108.deadly.config.ConfigManager::save);
    }
    public static VisualModule get(String id) { return MODULES.get(id); }
    public static Collection<VisualModule> all() { return VIEW; }
    public static long enabledCount() {
        return MODULES.values().stream().filter(VisualModule::isEnabled).count();
    }
    /** Called once per client tick; modules must avoid doing per-frame work here. */
    public static void tickAll(Minecraft client) {
        for (VisualModule module : MODULES.values()) module.tick(client);
    }
}
