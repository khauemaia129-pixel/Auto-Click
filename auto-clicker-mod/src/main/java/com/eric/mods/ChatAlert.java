package com.eric.mods;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Pattern;

/** Detecta seu nick no chat e avisa (tela + som). Só lê o que o cliente já recebeu; não envia nada. */
public class ChatAlert {
    private static final int ALERT_TICKS = 200;       // 10 s de aviso
    private static final long COOLDOWN_MS = 30_000;   // no máx. 1 alerta a cada 30 s

    private static int alertTicksLeft = 0;
    private static String alertText = "";
    private static long lastAlertMs = 0;

    public static void register() {
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, ts) -> {
            String nick = MinecraftClient.getInstance().getSession().getUsername();
            if (sender != null && nick.equalsIgnoreCase(sender.getName())) {
                return; // mensagem sua
            }
            check(message.getString());
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                check(message.getString());
            }
        });
    }

    private static void check(String text) {
        if (!AutoClickerConfig.isChatAlertEnabled() || !AutoClickerConfig.isEnabled()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        String nick = client.getSession().getUsername();
        Pattern p = Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(nick) + "(?![A-Za-z0-9_])",
                Pattern.CASE_INSENSITIVE);
        if (!p.matcher(text).find()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastAlertMs < COOLDOWN_MS) {
            return;
        }
        lastAlertMs = now;

        String shown = text.length() > 70 ? text.substring(0, 70) + "..." : text;
        alertText = "ALERTA - citaram seu nick: " + shown;
        AutoClickerMod.LOGGER.warn("Alerta de chat: {}", text);
        if (AutoClickerConfig.isChatAlertPause()) {
            AutoClickerConfig.setEnabled(false);
            alertText += " (farm pausado, M retoma)";
        }
        alertTicksLeft = ALERT_TICKS;
    }

    /** Chamar todo tick: repete o aviso na tela a cada 1 s e toca 6 bipes no começo. */
    public static void tick(MinecraftClient client) {
        if (alertTicksLeft <= 0 || client.player == null) {
            return;
        }
        int elapsed = ALERT_TICKS - alertTicksLeft;
        if (elapsed % 20 == 0) {
            client.player.sendMessage(Text.literal(alertText).formatted(Formatting.RED), true);
        }
        if (elapsed < 60 && elapsed % 10 == 0) {
            float pitch = (elapsed / 10) % 2 == 0 ? 1.0f : 1.4f;
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_PLAYER_LEVELUP, pitch, 1.0f));
        }
        alertTicksLeft--;
    }
}
