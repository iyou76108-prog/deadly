package com.iyou76108.deadly.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Persistent layout model shared by the live HUD renderer and the HUD editor.
 * Coordinates are GUI-scaled pixels; negative X values are right-edge anchors.
 */
public final class HudLayoutManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Deadly/HudLayout");
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("deadly").resolve("hud-layout.json");
    private static final Map<String, Widget> WIDGETS = new LinkedHashMap<>();

    private HudLayoutManager() {
    }

    public static void initialize() {
        WIDGETS.clear();
        register("watermark", 8, 8);
        register("fps", 8, 26);
        register("coordinates", 8, 44);
        register("direction", 8, 62);
        register("armor", 8, 80);
        register("potions", 8, 98);
        register("target", 8, 116);
        register("keystrokes", -12, 12);
        register("module_list", -8, 10);
        register("crosshair", 0, 0);
        load();
    }

    private static void register(String id, int x, int y) {
        WIDGETS.put(id, new Widget(x, y, 1.0f, 1.0f));
    }

    public static Set<String> ids() {
        return Set.copyOf(WIDGETS.keySet());
    }

    public static Widget get(String id) {
        return WIDGETS.get(id);
    }

    public static int resolveX(String id, int screenWidth, int widgetWidth) {
        Widget widget = WIDGETS.get(id);
        if (widget == null) return 8;
        return widget.x < 0 ? screenWidth + widget.x - widgetWidth : widget.x;
    }

    public static int getY(String id) {
        Widget widget = WIDGETS.get(id);
        return widget == null ? 8 : widget.y;
    }

    public static void move(String id, int x, int y) {
        Widget widget = WIDGETS.get(id);
        if (widget == null || "crosshair".equals(id)) return;
        widget.x = Math.max(0, x);
        widget.y = Math.max(0, y);
    }

    public static void adjustScale(String id, float amount) {
        Widget widget = WIDGETS.get(id);
        if (widget != null) widget.scale = clamp(widget.scale + amount, 0.5f, 2.0f);
    }

    public static void adjustOpacity(String id, float amount) {
        Widget widget = WIDGETS.get(id);
        if (widget != null) widget.opacity = clamp(widget.opacity + amount, 0.1f, 1.0f);
    }

    public static void reset() {
        initializeDefaults();
        save();
    }

    public static void reset(String id) {
        Widget widget = WIDGETS.get(id);
        if (widget == null) return;
        int[] position = defaultPosition(id);
        widget.x = position[0];
        widget.y = position[1];
        widget.scale = 1.0f;
        widget.opacity = 1.0f;
        save();
    }

    private static void initializeDefaults() {
        WIDGETS.clear();
        register("watermark", 8, 8);
        register("fps", 8, 26);
        register("coordinates", 8, 44);
        register("direction", 8, 62);
        register("armor", 8, 80);
        register("potions", 8, 98);
        register("target", 8, 116);
        register("keystrokes", -76, 12);
        register("module_list", -8, 10);
        register("crosshair", 0, 0);
    }

    private static int[] defaultPosition(String id) {
        return switch (id) {
            case "watermark" -> new int[]{8, 8};
            case "fps" -> new int[]{8, 26};
            case "coordinates" -> new int[]{8, 44};
            case "direction" -> new int[]{8, 62};
            case "armor" -> new int[]{8, 80};
            case "potions" -> new int[]{8, 98};
            case "target" -> new int[]{8, 116};
            case "keystrokes" -> new int[]{-12, 12};
            case "module_list" -> new int[]{-8, 10};
            default -> new int[]{0, 0};
        };
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(FILE.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("schemaVersion", 1);
            JsonObject widgets = new JsonObject();
            for (Map.Entry<String, Widget> entry : WIDGETS.entrySet()) {
                Widget widget = entry.getValue();
                JsonObject data = new JsonObject();
                data.addProperty("x", widget.x);
                data.addProperty("y", widget.y);
                data.addProperty("scale", widget.scale);
                data.addProperty("opacity", widget.opacity);
                widgets.add(entry.getKey(), data);
            }
            root.add("widgets", widgets);
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
            }
        } catch (Exception exception) {
            LOGGER.error("Could not save HUD layout", exception);
        }
    }

    private static synchronized void load() {
        if (!Files.isRegularFile(FILE)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(FILE)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (!root.has("widgets") || !root.get("widgets").isJsonObject()) return;
            JsonObject widgets = root.getAsJsonObject("widgets");
            for (Map.Entry<String, Widget> entry : WIDGETS.entrySet()) {
                if (!widgets.has(entry.getKey()) || !widgets.get(entry.getKey()).isJsonObject()) continue;
                JsonObject data = widgets.getAsJsonObject(entry.getKey());
                Widget widget = entry.getValue();
                if (data.has("x")) widget.x = data.get("x").getAsInt();
                if (data.has("y")) widget.y = data.get("y").getAsInt();
                if (data.has("scale")) widget.scale = clamp(data.get("scale").getAsFloat(), 0.5f, 2.0f);
                if (data.has("opacity")) widget.opacity = clamp(data.get("opacity").getAsFloat(), 0.1f, 1.0f);
            }
        } catch (Exception exception) {
            LOGGER.error("HUD layout is invalid; using defaults", exception);
            save();
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static final class Widget {
        private int x;
        private int y;
        private float scale;
        private float opacity;

        private Widget(int x, int y, float scale, float opacity) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.opacity = opacity;
        }

        public int x() { return x; }
        public int y() { return y; }
        public float scale() { return scale; }
        public float opacity() { return opacity; }
    }
}
