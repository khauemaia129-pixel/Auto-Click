package com.eric.mods.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class NumericInputWidget extends ClickableWidget {
    private static final int SLIDER_OFFSET = 95;
    private static final int SLIDER_WIDTH = 120;

    private final double min;
    private final double max;
    private final boolean isInteger;
    private final Runnable onChange;
    private double value;

    public NumericInputWidget(int x, int y, String label, double initial,
                              double min, double max, boolean isInteger, Runnable onChange) {
        super(x, y, SLIDER_OFFSET + SLIDER_WIDTH + 40, 20, Text.literal(label));
        this.min = min;
        this.max = max;
        this.isInteger = isInteger;
        this.onChange = onChange;
        this.value = Math.max(min, Math.min(initial, max));
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        var font = MinecraftClient.getInstance().textRenderer;
        int sx = getX() + SLIDER_OFFSET;
        int sy = getY() + 2;

        context.drawTextWithShadow(font, getMessage(), getX(), getY() + 6, 0xFFFFFFFF);
        context.fill(sx, sy, sx + SLIDER_WIDTH, sy + 16, 0xFF8B8B8B);
        context.fill(sx + 1, sy + 1, sx + SLIDER_WIDTH - 1, sy + 15, 0xFF1F1F1F);

        double pct = (value - min) / (max - min);
        int hx = sx + (int) (pct * (SLIDER_WIDTH - 6));
        context.fill(hx, sy - 1, hx + 6, sy + 17, 0xFF00FF00);

        String text = isInteger ? String.valueOf((int) value) : String.format("%.1f", value);
        context.drawTextWithShadow(font, text, sx + SLIDER_WIDTH + 6, getY() + 6, 0xFFFFFFFF);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        updateFromMouse(mouseX);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        updateFromMouse(mouseX);
    }

    private void updateFromMouse(double mouseX) {
        double pct = (mouseX - (getX() + SLIDER_OFFSET)) / SLIDER_WIDTH;
        pct = Math.max(0.0, Math.min(1.0, pct));
        double v = min + pct * (max - min);
        if (isInteger) {
            v = Math.round(v);
        }
        if (v != value) {
            value = v;
            onChange.run();
        }
    }

    public double getValue() {
        return value;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
