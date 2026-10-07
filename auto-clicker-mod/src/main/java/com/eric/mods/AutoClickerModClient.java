package com.eric.mods;

import com.eric.mods.ui.AutoClickerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class AutoClickerModClient implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static KeyBinding configKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.auto-clicker-mod.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.auto-clicker-mod"
        ));

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.auto-clicker-mod.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.auto-clicker-mod"
        ));

        ChatAlert.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null) {
                AutoClickerEventListener.onClientTick(client);
                ChatAlert.tick(client);

                if (toggleKey.wasPressed()) {
                    AutoClickerConfig.toggleEnabled();
                    logStatus();
                }

                if (configKey.wasPressed()) {
                    client.setScreen(new AutoClickerScreen(client.currentScreen));
                }
            }
        });
    }

    private static void logStatus() {
        boolean enabled = AutoClickerConfig.isEnabled();
        String status = enabled ? "ATIVADO" : "DESATIVADO";
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.sendMessage(Text.literal("Auto Clicker: " + status), true);
        }
        AutoClickerMod.LOGGER.info("Auto Clicker " + status);
    }
}
