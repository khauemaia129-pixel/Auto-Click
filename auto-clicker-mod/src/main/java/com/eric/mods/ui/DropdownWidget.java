package com.eric.mods.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;

/** Clique alterna entre as opções. */
public class DropdownWidget extends ClickableWidget {
    private static final int BOX_OFFSET = 95;
    private static final int BOX_WIDTH = 70;

    private final List<String> options;
    private final Runnable onChange;
    private int index;

    public DropdownWidget(int x, int y, String label, List<String> options,
                          String initial, Runnable onChange) {
        super(x, y, BOX_OFFSET + BOX_WIDTH, 20, Text.literal(label));
        this.options = options;
        this.onChange = onChange;
        this.index = Math.max(0, options.indexOf(initial));
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        var font = MinecraftClient.getInstance().textRenderer;
        int bx = getX() + BOX_OFFSET;
        context.drawTextWithShadow(font, getMessage(), getX(), getY() + 6, 0xFFFFFFFF);
        context.fill(bx, getY(), bx + BOX_WIDTH, getY() + 20, isHovered() ? 0xFFB0B0B0 : 0xFF8B8B8B);
        context.fill(bx + 1, getY() + 1, bx + BOX_WIDTH - 1, getY() + 19, 0xFF1F1F1F);
        context.drawTextWithShadow(font, options.get(index), bx + 6, getY() + 6, 0xFF00FF00);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        index = (index + 1) % options.size();
        onChange.run();
    }

    public String getSelectedValue() {
        return options.get(index);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
