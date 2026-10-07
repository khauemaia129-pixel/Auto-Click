package com.eric.mods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoClickerConfig {
    private static final Set<String> selectedMobs = new HashSet<>(Set.of("SLIME", "MAGMA_CUBE"));
    private static final Set<String> ignoredEntities = new HashSet<>();
    private static final List<String> HOLD_KEY_OPTIONS = List.of("SHIFT", "CTRL", "ALT", "NONE");

    private static boolean enabled = false;
    private static double range = 8.0;

    // Golpes dentro da rajada (ticks, 20 = 1s). Sorteado entre mín e máx a cada golpe.
    private static int hitMinTicks = 8;
    private static int hitMaxTicks = 16;

    // Duração da rajada (segundos). Sorteado a cada rajada.
    private static int burstMinSeconds = 2;
    private static int burstMaxSeconds = 5;

    // Espera entre rajadas (segundos). Sorteado a cada rajada.
    private static int waitMinSeconds = 30;
    private static int waitMaxSeconds = 120;

    // Tecla segurada durante a rajada
    private static boolean holdKeyEnabled = true;
    private static String holdKey = "SHIFT";

    // Auto Sell: inventário cheio e parado por N segundos -> envia o comando
    private static boolean autoSellEnabled = true;
    private static int fullSeconds = 15;
    private static final String SELL_COMMAND = "vender auto";
    // Depois de detectar o travamento, espera um tempo aleatório antes de enviar
    private static int sellDelayMinSeconds = 5;
    private static int sellDelayMaxSeconds = 50;

    // Proteção: Auto Sell caiu logo depois de religar = provável moderador, não religa
    private static boolean noReenableEnabled = true;
    private static int noReenableMinutes = 3;

    // Proteção: teleporte brusco (provável puxada de moderador) = desconecta
    private static boolean quitOnTeleport = true;
    private static int teleportBlocks = 3;

    // Câmera: olhar para o mob antes de bater e mexer um pouco enquanto espera
    private static boolean lookAtTarget = true;
    private static int lookSpeed = 8;        // graus por tick (máximo, com variação)
    private static boolean driftEnabled = true;
    private static int driftAmplitude = 12;  // graus

    // Cliente: enviar "vanilla" como marca ao entrar no servidor (vale no próximo login)
    private static boolean spoofBrand = true;

    // Chat: alerta quando citarem seu nick
    private static boolean chatAlertEnabled = true;
    private static boolean chatAlertPause = true;

    // ===== MOBS / IGNORADOS =====
    public static Set<String> getSelectedMobs() { return new HashSet<>(selectedMobs); }
    public static boolean isMobSelected(String mob) { return selectedMobs.contains(mob); }
    public static void toggleMobSelection(String mob) {
        if (!selectedMobs.remove(mob)) selectedMobs.add(mob);
    }

    public static Set<String> getIgnoredEntities() { return new HashSet<>(ignoredEntities); }
    public static boolean isEntityIgnored(String type) { return ignoredEntities.contains(type); }
    public static void toggleIgnoreEntity(String type) {
        if (!ignoredEntities.remove(type)) ignoredEntities.add(type);
    }

    // ===== GERAL =====
    public static boolean isEnabled() { return enabled; }
    public static void toggleEnabled() { enabled = !enabled; }
    public static void setEnabled(boolean v) { enabled = v; }

    public static double getRange() { return range; }
    public static void setRange(double v) { range = Math.max(1.0, Math.min(v, 64.0)); }

    // ===== GOLPES / RAJADA / ESPERA =====
    public static int getHitMinTicks() { return hitMinTicks; }
    public static void setHitMinTicks(int v) { hitMinTicks = clamp(v, 1, 40); }
    public static int getHitMaxTicks() { return hitMaxTicks; }
    public static void setHitMaxTicks(int v) { hitMaxTicks = clamp(v, 1, 40); }

    public static int getBurstMinSeconds() { return burstMinSeconds; }
    public static void setBurstMinSeconds(int v) { burstMinSeconds = clamp(v, 1, 60); }
    public static int getBurstMaxSeconds() { return burstMaxSeconds; }
    public static void setBurstMaxSeconds(int v) { burstMaxSeconds = clamp(v, 1, 60); }

    public static int getWaitMinSeconds() { return waitMinSeconds; }
    public static void setWaitMinSeconds(int v) { waitMinSeconds = clamp(v, 5, 300); }
    public static int getWaitMaxSeconds() { return waitMaxSeconds; }
    public static void setWaitMaxSeconds(int v) { waitMaxSeconds = clamp(v, 5, 300); }

    // ===== TECLA SEGURADA =====
    public static boolean isHoldKeyEnabled() { return holdKeyEnabled; }
    public static void setHoldKeyEnabled(boolean v) { holdKeyEnabled = v; }
    public static String getHoldKey() { return holdKey; }
    public static void setHoldKey(String key) { if (HOLD_KEY_OPTIONS.contains(key)) holdKey = key; }
    public static List<String> getHoldKeyOptions() { return new ArrayList<>(HOLD_KEY_OPTIONS); }

    // ===== AUTO SELL =====
    public static boolean isAutoSellEnabled() { return autoSellEnabled; }
    public static void setAutoSellEnabled(boolean v) { autoSellEnabled = v; }
    public static int getFullSeconds() { return fullSeconds; }
    public static void setFullSeconds(int v) { fullSeconds = clamp(v, 5, 60); }
    public static String getSellCommand() { return SELL_COMMAND; }

    public static int getSellDelayMinSeconds() { return sellDelayMinSeconds; }
    public static void setSellDelayMinSeconds(int v) { sellDelayMinSeconds = clamp(v, 1, 120); }
    public static int getSellDelayMaxSeconds() { return sellDelayMaxSeconds; }
    public static void setSellDelayMaxSeconds(int v) { sellDelayMaxSeconds = clamp(v, 1, 120); }

    // ===== PROTEÇÕES =====
    public static boolean isNoReenableEnabled() { return noReenableEnabled; }
    public static void setNoReenableEnabled(boolean v) { noReenableEnabled = v; }
    public static int getNoReenableMinutes() { return noReenableMinutes; }
    public static void setNoReenableMinutes(int v) { noReenableMinutes = clamp(v, 1, 30); }

    public static boolean isQuitOnTeleport() { return quitOnTeleport; }
    public static void setQuitOnTeleport(boolean v) { quitOnTeleport = v; }
    public static int getTeleportBlocks() { return teleportBlocks; }
    public static void setTeleportBlocks(int v) { teleportBlocks = clamp(v, 2, 30); }

    public static boolean isLookAtTarget() { return lookAtTarget; }
    public static void setLookAtTarget(boolean v) { lookAtTarget = v; }
    public static int getLookSpeed() { return lookSpeed; }
    public static void setLookSpeed(int v) { lookSpeed = clamp(v, 2, 30); }
    public static boolean isDriftEnabled() { return driftEnabled; }
    public static void setDriftEnabled(boolean v) { driftEnabled = v; }
    public static int getDriftAmplitude() { return driftAmplitude; }
    public static void setDriftAmplitude(int v) { driftAmplitude = clamp(v, 3, 45); }

    public static boolean isChatAlertEnabled() { return chatAlertEnabled; }
    public static void setChatAlertEnabled(boolean v) { chatAlertEnabled = v; }
    public static boolean isChatAlertPause() { return chatAlertPause; }
    public static void setChatAlertPause(boolean v) { chatAlertPause = v; }

    public static boolean isSpoofBrand() { return spoofBrand; }
    public static void setSpoofBrand(boolean v) { spoofBrand = v; }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(v, max)); }
}
