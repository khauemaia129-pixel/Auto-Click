package com.eric.mods;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class AutoClickerEventListener {
    // Nomes batem com o id do registro (zombie -> ZOMBIE)
    private static final List<String> MOB_TYPES = List.of(
            "SLIME", "MAGMA_CUBE", "ZOMBIE", "CREEPER", "SKELETON", "SPIDER", "ENDERMAN", "WITCH",
            "ZOMBIE_VILLAGER", "HUSK", "DROWNED", "ZOMBIFIED_PIGLIN", "CAVE_SPIDER",
            "SILVERFISH", "GHAST", "BLAZE", "STRIDER");

    private static final Map<String, Integer> KEY_MAP = Map.of(
            "SHIFT", GLFW.GLFW_KEY_LEFT_SHIFT,
            "CTRL", GLFW.GLFW_KEY_LEFT_CONTROL,
            "ALT", GLFW.GLFW_KEY_LEFT_ALT);

    /** Só bate quando a mira estiver a menos de N graus do mob (se "olhar para o mob" estiver ligado). */
    private static final double AIM_TOLERANCE_DEG = 6.0;

    private enum Phase { WAITING, BURST }

    private static final Random RNG = new Random();

    // ciclo de farm
    private static Phase phase = Phase.WAITING;
    private static int waitTicks = 0;
    private static int burstTicks = 0;
    private static int hitCooldown = 0;
    private static int heldKeyCode = -1;
    private static long tickCounter = 0;

    // câmera parada (movimento leve enquanto espera)
    private static int driftIdx = 0;
    private static int driftN = 0;
    private static float driftTotalYaw = 0f;
    private static float driftTotalPitch = 0f;
    private static int nextDriftTicks = 0;

    // monitor de inventário / auto sell
    private static int lastSignature = 0;
    private static int stableTicks = 0;
    private static int pendingSell = -1;
    private static boolean sellSent = false;
    private static boolean suspectNotified = false;
    private static long lastSellSentTick = -1;

    // detecção de teleporte
    private static Vec3d lastPos = null;
    private static Object lastPlayerRef = null;
    private static Object lastWorldRef = null;

    public static void onClientTick(MinecraftClient client) {
        if (!AutoClickerConfig.isEnabled() || client.player == null
                || client.world == null || client.interactionManager == null) {
            reset();
            return;
        }

        tickCounter++;

        if (checkTeleport(client)) {
            return;
        }

        monitorInventory(client);

        if (phase == Phase.WAITING) {
            idleDrift(client);
            if (waitTicks > 0) {
                waitTicks--;
                return;
            }
            // Sem alvo: não ataca, confere de novo em 1-2 s
            if (AutoClickerConfig.getSelectedMobs().isEmpty() || findNearestMob(client) == null) {
                waitTicks = randomBetween(20, 40);
                return;
            }
            startBurst();
            return;
        }

        // BURST
        Entity target = findNearestMob(client);
        if (target == null || burstTicks-- <= 0) {
            endBurst();
            return;
        }

        setHoldKey(true);

        boolean aimed = !AutoClickerConfig.isLookAtTarget() || aimAt(client, target) < AIM_TOLERANCE_DEG;

        if (hitCooldown > 0) {
            hitCooldown--;
            return;
        }
        if (!aimed) {
            return;
        }

        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
        hitCooldown = randomBetween(AutoClickerConfig.getHitMinTicks(), AutoClickerConfig.getHitMaxTicks());
    }

    private static void startBurst() {
        phase = Phase.BURST;
        driftIdx = driftN; // cancela qualquer movimento leve pendente
        burstTicks = randomBetween(AutoClickerConfig.getBurstMinSeconds() * 20,
                AutoClickerConfig.getBurstMaxSeconds() * 20);
        hitCooldown = randomBetween(4, 10); // segura a tecla um instante antes do 1º golpe
    }

    private static void endBurst() {
        setHoldKey(false);
        phase = Phase.WAITING;
        waitTicks = randomBetween(AutoClickerConfig.getWaitMinSeconds() * 20,
                AutoClickerConfig.getWaitMaxSeconds() * 20);
    }

    private static void reset() {
        phase = Phase.WAITING;
        waitTicks = randomBetween(20, 60);
        burstTicks = 0;
        hitCooldown = 0;
        tickCounter = 0;
        driftIdx = 0;
        driftN = 0;
        nextDriftTicks = randomBetween(40, 120);
        lastSignature = 0;
        stableTicks = 0;
        pendingSell = -1;
        sellSent = false;
        suspectNotified = false;
        lastSellSentTick = -1;
        lastPos = null;
        lastPlayerRef = null;
        lastWorldRef = null;
        setHoldKey(false);
    }

    /**
     * Gira a câmera um pouco por tick em direção ao centro do mob (passo proporcional ao erro,
     * limitado pela velocidade configurada, com variação e ruído). Retorna o erro restante em graus.
     */
    private static double aimAt(MinecraftClient client, Entity target) {
        var p = client.player;
        Vec3d to = target.getBoundingBox().getCenter().subtract(p.getEyePos());
        double horiz = Math.sqrt(to.x * to.x + to.z * to.z);
        float wantYaw = (float) (Math.toDegrees(Math.atan2(to.z, to.x)) - 90.0);
        float wantPitch = (float) -Math.toDegrees(Math.atan2(to.y, horiz));

        float dYaw = MathHelper.wrapDegrees(wantYaw - p.getYaw());
        float dPitch = wantPitch - p.getPitch();

        float maxStep = AutoClickerConfig.getLookSpeed() * (0.7f + RNG.nextFloat() * 0.6f);
        float stepYaw = MathHelper.clamp(dYaw * 0.35f, -maxStep, maxStep);
        float stepPitch = MathHelper.clamp(dPitch * 0.35f, -maxStep, maxStep);
        if (Math.hypot(dYaw, dPitch) > 1.5) {
            stepYaw += (RNG.nextFloat() - 0.5f) * 0.4f;
            stepPitch += (RNG.nextFloat() - 0.5f) * 0.3f;
        }

        p.setYaw(p.getYaw() + stepYaw);
        p.setPitch(MathHelper.clamp(p.getPitch() + stepPitch, -90f, 90f));
        return Math.hypot(dYaw - stepYaw, dPitch - stepPitch);
    }

    /** Enquanto espera, de tempos em tempos move a câmera um pouco, com início e fim suaves. */
    private static void idleDrift(MinecraftClient client) {
        if (!AutoClickerConfig.isDriftEnabled() || client.currentScreen != null) {
            return;
        }
        var p = client.player;
        if (driftIdx < driftN) {
            float e = ease((float) (driftIdx + 1) / driftN) - ease((float) driftIdx / driftN);
            p.setYaw(p.getYaw() + driftTotalYaw * e);
            p.setPitch(MathHelper.clamp(p.getPitch() + driftTotalPitch * e, -90f, 90f));
            driftIdx++;
            return;
        }
        if (nextDriftTicks-- > 0) {
            return;
        }
        int amp = AutoClickerConfig.getDriftAmplitude();
        driftN = randomBetween(6, 16);
        driftIdx = 0;
        driftTotalYaw = (RNG.nextFloat() * 2f - 1f) * amp;
        driftTotalPitch = (RNG.nextFloat() * 2f - 1f) * amp * 0.4f;
        nextDriftTicks = randomBetween(60, 300); // próximo movimento em 3 a 15 s
    }

    private static float ease(float t) {
        return (float) ((1.0 - Math.cos(Math.PI * t)) / 2.0);
    }

    /**
     * Teleporte = deslocamento num único tick maior que o limite E maior que a velocidade
     * do jogador (assim queda longa e elytra não disparam). Trocar de mundo/respawn zera a referência.
     * Ao detectar: desliga o mod e desconecta do servidor.
     */
    private static boolean checkTeleport(MinecraftClient client) {
        Vec3d cur = client.player.getPos();
        boolean sameContext = lastPlayerRef == client.player && lastWorldRef == client.world;
        Vec3d prev = lastPos;
        lastPos = cur;
        lastPlayerRef = client.player;
        lastWorldRef = client.world;

        if (!AutoClickerConfig.isQuitOnTeleport() || !sameContext || prev == null) {
            return false;
        }

        double moved = cur.distanceTo(prev);
        double expected = client.player.getVelocity().length();
        if (moved > AutoClickerConfig.getTeleportBlocks() && moved > expected + 1.5) {
            String reason = String.format(Locale.ROOT, "Auto Clicker: teleporte de %.1f blocos detectado, desconectado.", moved);
            AutoClickerMod.LOGGER.warn(reason);
            AutoClickerConfig.setEnabled(false);
            reset();
            if (client.getNetworkHandler() != null) {
                client.getNetworkHandler().getConnection().disconnect(Text.literal(reason));
            }
            return true;
        }
        return false;
    }

    /**
     * Auto Sell desligado = inventário (36 slots) fica cheio e PARADO (nenhum item entra/sai).
     * Se isso dura N segundos, espera um atraso aleatório e envia o comando uma vez.
     * Se o Auto Sell já tinha sido religado por nós há menos de X minutos, considera que
     * alguém desligou (moderador) e NÃO religa. Só reenvia depois que o inventário voltar a ter espaço.
     */
    private static void monitorInventory(MinecraftClient client) {
        boolean full = true;
        int sig = 1;
        var inv = client.player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getStack(i);
            if (s.isEmpty()) {
                full = false;
                break;
            }
            sig = 31 * sig + Registries.ITEM.getRawId(s.getItem()) * 131 + s.getCount();
        }

        if (!full) {
            stableTicks = 0;
            pendingSell = -1;
            sellSent = false;
            suspectNotified = false;
            return;
        }
        if (sig != lastSignature) { // entrou/saiu item: está vendendo ou ainda enchendo
            lastSignature = sig;
            stableTicks = 0;
            pendingSell = -1;
            return;
        }
        stableTicks++;

        if (!AutoClickerConfig.isAutoSellEnabled() || sellSent || suspectNotified) {
            return;
        }

        if (pendingSell > 0) {
            pendingSell--;
        } else if (pendingSell == 0) {
            client.player.networkHandler.sendChatCommand(AutoClickerConfig.getSellCommand());
            client.player.sendMessage(Text.literal("Auto Clicker: enviei /"
                    + AutoClickerConfig.getSellCommand()), true);
            AutoClickerMod.LOGGER.info("Inventário cheio e parado, enviado /" + AutoClickerConfig.getSellCommand());
            pendingSell = -1;
            sellSent = true;
            lastSellSentTick = tickCounter;
        } else if (stableTicks >= AutoClickerConfig.getFullSeconds() * 20) {
            boolean droppedTooSoon = AutoClickerConfig.isNoReenableEnabled()
                    && lastSellSentTick >= 0
                    && tickCounter - lastSellSentTick < AutoClickerConfig.getNoReenableMinutes() * 1200L;
            if (droppedTooSoon) {
                suspectNotified = true;
                client.player.sendMessage(Text.literal("Auto Clicker: Auto Sell caiu em menos de "
                        + AutoClickerConfig.getNoReenableMinutes() + " min, não vou religar."), true);
                AutoClickerMod.LOGGER.warn("Auto Sell caiu logo após religar: possível moderador, não religando.");
                return;
            }
            pendingSell = randomBetween(AutoClickerConfig.getSellDelayMinSeconds() * 20,
                    AutoClickerConfig.getSellDelayMaxSeconds() * 20);
        }
    }

    private static Entity findNearestMob(MinecraftClient client) {
        Entity closest = null;
        double best = AutoClickerConfig.getRange();
        Vec3d pos = client.player.getPos();

        for (Entity e : client.world.getEntities()) {
            if (e == client.player || !(e instanceof LivingEntity living) || !living.isAlive()) {
                continue;
            }
            String id = Registries.ENTITY_TYPE.getId(e.getType()).getPath().toUpperCase(Locale.ROOT);
            if (AutoClickerConfig.isEntityIgnored(id) || !AutoClickerConfig.isMobSelected(id)) {
                continue;
            }
            double d = pos.distanceTo(e.getPos());
            if (d < best) {
                best = d;
                closest = e;
            }
        }
        return closest;
    }

    /** Segura/solta de verdade a tecla escolhida (afeta o keybind vinculado a ela, ex: sneak). */
    private static void setHoldKey(boolean pressed) {
        Integer code = KEY_MAP.get(AutoClickerConfig.getHoldKey());
        int want = (pressed && AutoClickerConfig.isHoldKeyEnabled() && code != null) ? code : -1;
        if (want == heldKeyCode) {
            return;
        }
        if (heldKeyCode != -1) {
            KeyBinding.setKeyPressed(InputUtil.fromKeyCode(heldKeyCode, 0), false);
        }
        if (want != -1) {
            KeyBinding.setKeyPressed(InputUtil.fromKeyCode(want, 0), true);
        }
        heldKeyCode = want;
    }

    private static int randomBetween(int a, int b) {
        int lo = Math.min(a, b);
        int hi = Math.max(a, b);
        return lo + RNG.nextInt(hi - lo + 1);
    }

    public static List<String> getMobTypesList() {
        return new ArrayList<>(MOB_TYPES);
    }
}
