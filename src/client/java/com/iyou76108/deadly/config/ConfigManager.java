package com.iyou76108.deadly.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iyou76108.deadly.core.Setting;
import com.iyou76108.deadly.core.VisualModule;
import com.iyou76108.deadly.core.VisualRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

public final class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Deadly/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9_-]{1,32}");
    private static final Path DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("deadly");
    private static final Path CONFIG_FILE = DIRECTORY.resolve("config.json");
    private static boolean applying;

    private ConfigManager() {
    }

    public static void initialize() {
        try {
            Files.createDirectories(DIRECTORY.resolve("profiles"));
            load();
        } catch (IOException exception) {
            LOGGER.error("Could not initialize the configuration directory", exception);
        }
    }

    public static synchronized void save() {
        if (applying) {
            return;
        }
        try {
            Files.createDirectories(DIRECTORY);
            writeAtomically(CONFIG_FILE, snapshot());
        } catch (IOException exception) {
            LOGGER.error("Could not save Deadly Visuals configuration", exception);
        }
    }

    public static synchronized void saveProfile(String name) {
        Path path = profilePath(name);
        try {
            Files.createDirectories(path.getParent());
            writeAtomically(path, snapshot());
        } catch (IOException exception) {
            LOGGER.error("Could not save profile '{}'", name, exception);
        }
    }

    public static synchronized boolean loadProfile(String name) {
        Path path = profilePath(name);
        if (!Files.isRegularFile(path)) {
            return false;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            apply(JsonParser.parseReader(reader).getAsJsonObject());
            save();
            return true;
        } catch (Exception exception) {
            LOGGER.error("Could not load profile '{}'", name, exception);
            return false;
        }
    }

    private static synchronized void load() {
        if (!Files.isRegularFile(CONFIG_FILE)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
            apply(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (Exception exception) {
            LOGGER.error("Configuration is invalid; defaults will be used", exception);
        }
    }

    private static void apply(JsonObject root) {
        applying = true;
        try {
            JsonObject modules = root.has("modules") && root.get("modules").isJsonObject()
                    ? root.getAsJsonObject("modules") : new JsonObject();

            for (VisualModule module : VisualRegistry.all()) {
                if (!modules.has(module.getId()) || !modules.get(module.getId()).isJsonObject()) {
                    continue;
                }
                JsonObject moduleData = modules.getAsJsonObject(module.getId());
                if (moduleData.has("enabled")) {
                    module.setEnabled(moduleData.get("enabled").getAsBoolean());
                }
                if (moduleData.has("settings") && moduleData.get("settings").isJsonObject()) {
                    JsonObject settings = moduleData.getAsJsonObject("settings");
                    for (Setting<?> setting : module.getSettings()) {
                        JsonElement value = settings.get(setting.getId());
                        setting.readJson(value);
                    }
                }
            }
        } finally {
            applying = false;
        }
    }

    private static JsonObject snapshot() {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", 1);
        JsonObject modules = new JsonObject();

        for (VisualModule module : VisualRegistry.all()) {
            JsonObject moduleData = new JsonObject();
            moduleData.addProperty("enabled", module.isEnabled());
            JsonObject settings = new JsonObject();
            for (Setting<?> setting : module.getSettings()) {
                settings.add(setting.getId(), toJson(setting));
            }
            moduleData.add("settings", settings);
            modules.add(module.getId(), moduleData);
        }

        root.add("modules", modules);
        return root;
    }

    private static JsonElement toJson(Setting<?> setting) {
        Object value = setting.getValue();
        if (value instanceof Boolean booleanValue) {
            return new com.google.gson.JsonPrimitive(booleanValue);
        }
        if (value instanceof Number numberValue) {
            return new com.google.gson.JsonPrimitive(numberValue);
        }
        return com.google.gson.JsonNull.INSTANCE;
    }

    private static void writeAtomically(Path target, JsonObject content) throws IOException {
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporary)) {
            GSON.toJson(content, writer);
        }
        try {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Path profilePath(String name) {
        if (name == null || !SAFE_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Profile names may contain 1-32 letters, digits, '_' or '-'.");
        }
        return DIRECTORY.resolve("profiles").resolve(name + ".json");
    }
}
