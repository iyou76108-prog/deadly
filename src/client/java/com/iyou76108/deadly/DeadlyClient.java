package com.iyou76108.deadly;

import com.iyou76108.deadly.config.ConfigManager;
import com.iyou76108.deadly.core.VisualRegistry;
import com.iyou76108.deadly.modules.ClientEffects;
import com.iyou76108.deadly.render.HudRenderer;
import com.iyou76108.deadly.render.HudLayoutManager;
import com.iyou76108.deadly.ui.DeadlyScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DeadlyClient implements ClientModInitializer {
    public static final String MOD_ID = "deadly";
    public static final String VERSION = "1.1.0";
    public static final Logger LOGGER = LoggerFactory.getLogger("Deadly Visuals");

    private static KeyMapping openMenuKey;

    @Override
    public void onInitializeClient() {
        VisualRegistry.registerDefaults();
        HudLayoutManager.initialize();
        ConfigManager.initialize();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "visuals")
        );
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.deadly.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
        ));

        HudRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new DeadlyScreen(null));
                }
            }
            ClientEffects.tick(client);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null && client.level == null) {
                ClientEffects.restore(client);
            }
        });

        LOGGER.info("Deadly Visuals {} initialized with {} registered modules.",
                VERSION, VisualRegistry.all().size());
    }
}
