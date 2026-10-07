package com.eric.mods.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class CheckboxWidget extends ClickableWidget {
    private static final int BOX = 16;
    private boolean checked;
    private final Runnable onToggle;

    public CheckboxWidget(int x, int y, String label, boolean checked, Runnable onToggle) {
        super(x, y, 170, 20, Text.literal(label));
        this.checked = checked;
        this.onToggle = onToggle;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = getX(), y = getY();
        context.fill(x, y, x + BOX, y + BOX, 0xFF8B8B8B);
        context.fill(x + 1, y + 1, x + BOX - 1, y + BOX - 1, 0xFF1F1F1F);
        if (checked) {
            context.fill(x + 3, y + 3, x + BOX - 3, y + BOX - 3, 0xFF00FF00);
        }
        if (isHovered()) {
            context.fill(x, y, x + BOX, y + BOX, 0x4400FF00);
        }
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                getMessage(), x + BOX + 8, y + 4, 0xFFFFFFFF);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        checked = !checked;
        onToggle.run();
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
