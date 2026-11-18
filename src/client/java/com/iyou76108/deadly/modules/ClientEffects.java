package com.iyou76108.deadly.modules;

import com.iyou76108.deadly.core.Setting;
import com.iyou76108.deadly.core.VisualModule;
import com.iyou76108.deadly.core.VisualRegistry;
import net.minecraft.client.Minecraft;

public final class ClientEffects {
    private static Double previousGamma;

    private ClientEffects() {
    }

    public static void tick(Minecraft client) {
        for (VisualModule module : VisualRegistry.all()) {
            if (module.isEnabled()) {
                module.tick(client);
            }
        }

        VisualModule gammaModule = VisualRegistry.get("gamma");
        boolean gammaEnabled = gammaModule != null && gammaModule.isEnabled();

        if (gammaEnabled) {
            if (previousGamma == null) {
                previousGamma = client.options.gamma().get();
            }
            Setting<?> setting = gammaModule.getSetting("brightness");
            double brightness = setting == null ? 1.0 : ((Number) setting.getValue()).doubleValue();
            client.options.gamma().set(brightness);
        } else if (previousGamma != null) {
            client.options.gamma().set(previousGamma);
            previousGamma = null;
        }
    }

    public static void restore(Minecraft client) {
        if (previousGamma != null) {
            client.options.gamma().set(previousGamma);
            previousGamma = null;
        }
    }
}
