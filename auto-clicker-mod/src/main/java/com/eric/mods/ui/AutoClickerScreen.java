package com.eric.mods.ui;

import com.eric.mods.AutoClickerConfig;
import com.eric.mods.AutoClickerEventListener;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Predicate;

public class AutoClickerScreen extends Screen {
    private static final int START_Y = 40;
    private static final int ROW = 20;
    private static final int STEP = 24;
    private static final int PER_COLUMN = 9;
    private static final int COLUMN_STEP = 180;
    private static final int RIGHT_X = 275;

    private static final List<String> IGNORE_OPTIONS = List.of(
            "PLAYER", "ARMOR_STAND", "ITEM_FRAME", "PAINTING", "VILLAGER", "WANDERING_TRADER",
            "IRON_GOLEM", "SNOW_GOLEM", "WITHER", "ENDER_DRAGON", "GUARDIAN", "ELDER_GUARDIAN");

    private final Screen parent;
    private int currentTab = 0; // 0 Mobs, 1 Ignorar, 2 Config, 3 Proteção

    public AutoClickerScreen(Screen parent) {
        super(Text.literal("Auto Clicker Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addTab("Mobs", 0, 10);
        addTab("Ignorar", 1, 95);
        addTab("Config", 2, 180);
        addTab("Proteção", 3, 265);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Fechar"), b -> this.close())
                .dimensions(this.width - 110, 10, 100, 20).build());

        switch (currentTab) {
            case 0 -> initList(AutoClickerEventListener.getMobTypesList(),
                    AutoClickerConfig::isMobSelected, AutoClickerConfig::toggleMobSelection);
            case 1 -> initList(IGNORE_OPTIONS,
                    AutoClickerConfig::isEntityIgnored, AutoClickerConfig::toggleIgnoreEntity);
            case 2 -> initConfig();
            default -> initProtection();
        }
    }

    private void addTab(String label, int tab, int x) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> {
            this.currentTab = tab;
            this.clearAndInit();
        }).dimensions(x, 10, 80, 20).build());
    }

    private void initList(List<String> items, Predicate<String> isOn, Consumer<String> toggle) {
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            int x = 10 + (i / PER_COLUMN) * COLUMN_STEP;
            int y = START_Y + (i % PER_COLUMN) * ROW;
            this.addDrawableChild(new CheckboxWidget(x, y, item, isOn.test(item), () -> toggle.accept(item)));
        }
    }

    private void initConfig() {
        int lx = 10;
        int y = START_Y;

        addSlider(lx, y, "Distância:", AutoClickerConfig.getRange(), 1, 64, false, AutoClickerConfig::setRange);
        y += STEP;
        addSlider(lx, y, "Golpe mín (t):", AutoClickerConfig.getHitMinTicks(), 1, 40, true,
                v -> AutoClickerConfig.setHitMinTicks((int) v));
        y += STEP;
        addSlider(lx, y, "Golpe máx (t):", AutoClickerConfig.getHitMaxTicks(), 1, 40, true,
                v -> AutoClickerConfig.setHitMaxTicks((int) v));
        y += STEP;
        addSlider(lx, y, "Rajada mín (s):", AutoClickerConfig.getBurstMinSeconds(), 1, 60, true,
                v -> AutoClickerConfig.setBurstMinSeconds((int) v));
        y += STEP;
        addSlider(lx, y, "Rajada máx (s):", AutoClickerConfig.getBurstMaxSeconds(), 1, 60, true,
                v -> AutoClickerConfig.setBurstMaxSeconds((int) v));
        y += STEP;
        addSlider(lx, y, "Espera mín (s):", AutoClickerConfig.getWaitMinSeconds(), 5, 300, true,
                v -> AutoClickerConfig.setWaitMinSeconds((int) v));
        y += STEP;
        addSlider(lx, y, "Espera máx (s):", AutoClickerConfig.getWaitMaxSeconds(), 5, 300, true,
                v -> AutoClickerConfig.setWaitMaxSeconds((int) v));

        int ry = START_Y;
        addCheck(RIGHT_X, ry, "Segurar tecla", AutoClickerConfig.isHoldKeyEnabled(),
                AutoClickerConfig::setHoldKeyEnabled);
        ry += STEP;

        DropdownWidget[] keyRef = new DropdownWidget[1];
        keyRef[0] = new DropdownWidget(RIGHT_X, ry, "Tecla:", AutoClickerConfig.getHoldKeyOptions(),
                AutoClickerConfig.getHoldKey(), () -> AutoClickerConfig.setHoldKey(keyRef[0].getSelectedValue()));
        this.addDrawableChild(keyRef[0]);
        ry += STEP + 6;

        addCheck(RIGHT_X, ry, "Olhar para o mob", AutoClickerConfig.isLookAtTarget(),
                AutoClickerConfig::setLookAtTarget);
        ry += STEP;
        addSlider(RIGHT_X, ry, "Câmera (°/t):", AutoClickerConfig.getLookSpeed(), 2, 30, true,
                v -> AutoClickerConfig.setLookSpeed((int) v));
        ry += STEP + 6;

        addCheck(RIGHT_X, ry, "Mexer câmera esperando", AutoClickerConfig.isDriftEnabled(),
                AutoClickerConfig::setDriftEnabled);
        ry += STEP;
        addSlider(RIGHT_X, ry, "Amplitude (°):", AutoClickerConfig.getDriftAmplitude(), 3, 45, true,
                v -> AutoClickerConfig.setDriftAmplitude((int) v));
    }

    private void initProtection() {
        int y = START_Y + 18;

        // Esquerda: Auto Sell
        addCheck(10, y, "Auto Sell automático", AutoClickerConfig.isAutoSellEnabled(),
                AutoClickerConfig::setAutoSellEnabled);
        y += STEP;
        addSlider(10, y, "Cheio por (s):", AutoClickerConfig.getFullSeconds(), 5, 60, true,
                v -> AutoClickerConfig.setFullSeconds((int) v));
        y += STEP;
        addSlider(10, y, "Atraso mín (s):", AutoClickerConfig.getSellDelayMinSeconds(), 1, 120, true,
                v -> AutoClickerConfig.setSellDelayMinSeconds((int) v));
        y += STEP;
        addSlider(10, y, "Atraso máx (s):", AutoClickerConfig.getSellDelayMaxSeconds(), 1, 120, true,
                v -> AutoClickerConfig.setSellDelayMaxSeconds((int) v));
        y += STEP + 16;

        // Esquerda embaixo: chat
        addCheck(10, y, "Alertar se citarem meu nick", AutoClickerConfig.isChatAlertEnabled(),
                AutoClickerConfig::setChatAlertEnabled);
        y += STEP;
        addCheck(10, y, "Pausar farm ao alertar", AutoClickerConfig.isChatAlertPause(),
                AutoClickerConfig::setChatAlertPause);

        // Direita: moderador
        int ry = START_Y + 18;
        addCheck(RIGHT_X, ry, "Não religar se cair rápido", AutoClickerConfig.isNoReenableEnabled(),
                AutoClickerConfig::setNoReenableEnabled);
        ry += STEP;
        addSlider(RIGHT_X, ry, "Janela (min):", AutoClickerConfig.getNoReenableMinutes(), 1, 30, true,
                v -> AutoClickerConfig.setNoReenableMinutes((int) v));
        ry += STEP + 12;
        addCheck(RIGHT_X, ry, "Sair se for puxado", AutoClickerConfig.isQuitOnTeleport(),
                AutoClickerConfig::setQuitOnTeleport);
        ry += STEP;
        addSlider(RIGHT_X, ry, "Distância (bl):", AutoClickerConfig.getTeleportBlocks(), 2, 30, true,
                v -> AutoClickerConfig.setTeleportBlocks((int) v));
        ry += STEP + 20;
        addCheck(RIGHT_X, ry, "Disfarçar cliente (vanilla)", AutoClickerConfig.isSpoofBrand(),
                AutoClickerConfig::setSpoofBrand);
    }

    private void addSlider(int x, int y, String label, double initial, double min, double max,
                           boolean isInt, DoubleConsumer onChange) {
        NumericInputWidget[] ref = new NumericInputWidget[1];
        ref[0] = new NumericInputWidget(x, y, label, initial, min, max, isInt,
                () -> onChange.accept(ref[0].getValue()));
        this.addDrawableChild(ref[0]);
    }

    private void addCheck(int x, int y, String label, boolean initial, Consumer<Boolean> onChange) {
        CheckboxWidget[] ref = new CheckboxWidget[1];
        ref[0] = new CheckboxWidget(x, y, label, initial, () -> onChange.accept(ref[0].isChecked()));
        this.addDrawableChild(ref[0]);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        switch (currentTab) {
            case 0 -> {
                int n = AutoClickerConfig.getSelectedMobs().size();
                String msg = n == 0 ? "Selecione pelo menos um mob!" : "Mobs selecionados: " + n;
                context.drawTextWithShadow(this.textRenderer, msg, 10, this.height - 20,
                        n == 0 ? 0xFFFF5555 : 0xFF55FF55);
            }
            case 2 -> context.drawTextWithShadow(this.textRenderer,
                    "Rajada = tempo batendo. Espera = pausa entre rajadas. Tudo sorteado entre mín e máx.",
                    10, this.height - 20, 0xFFAAAAAA);
            case 3 -> {
                context.drawTextWithShadow(this.textRenderer, "Auto Sell", 10, START_Y + 2, 0xFFFFFF55);
                context.drawTextWithShadow(this.textRenderer, "Moderador", RIGHT_X, START_Y + 2, 0xFFFFFF55);
                context.drawTextWithShadow(this.textRenderer, "Chat", 10, START_Y + 2 + 4 * STEP + 16, 0xFFFFFF55);
                context.drawTextWithShadow(this.textRenderer, "Cliente (vale no próximo login)", RIGHT_X, START_Y + 130, 0xFFFFFF55);
                context.drawTextWithShadow(this.textRenderer,
                        "Cheio por = tempo parado antes de agir. Atraso = sorteio antes de enviar o comando.",
                        10, this.height - 30, 0xFFAAAAAA);
                context.drawTextWithShadow(this.textRenderer,
                        "Janela = não religa se cair antes disso. Puxado = desconecta. Chat = aviso na tela e som.",
                        10, this.height - 20, 0xFFAAAAAA);
            }
            default -> { }
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
