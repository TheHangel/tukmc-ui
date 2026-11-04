package dev.hangel.tukmc.gui;

import dev.hangel.tukmc.client.TukDraw;
import dev.hangel.tukmc.util.ColorUtil;
import dev.hangel.tukmc.util.TukMCReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class GuiTukButton extends Button {

    public final int id;
    private final Integer outlineColor;
    private final Integer innerColor;

    public GuiTukButton(int id, int x, int y, int width, int height, String text, OnPress onPress) {
        this(id, x, y, width, height, text, null, null, onPress);
    }

    public GuiTukButton(int id, int x, int y, int width, int height, String text, Integer outlineColor,
            Integer innerColor, OnPress onPress) {
        super(x, y, width, height, Component.literal(text), onPress, DEFAULT_NARRATION);
        this.id = id;
        this.outlineColor = outlineColor;
        this.innerColor = innerColor;
    }

    protected int inner() {
        return innerColor != null ? innerColor : TukMCReference.BOX_INNER_COLOR;
    }

    protected int outline() {
        return outlineColor != null ? outlineColor : TukMCReference.BOX_OUTLINE_COLOR;
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int color;
        if (!active) {
            color = ColorUtil.darker(ColorUtil.darker(inner()));
        } else if (!isHovered()) {
            color = TukMCReference.BOX_INNER_COLOR;
        } else {
            color = ColorUtil.brighter(ColorUtil.brighter(inner()));
        }
        TukDraw.drawDoubleOutlinedBox(g, getX(), getY(), width, height, color, outline());
        renderExtra(g);
        int l = !active ? -6250336 : 0xFFFFFF;
        g.drawCenteredString(Minecraft.getInstance().font, getMessage().getString(), getX() + width / 2,
                getY() + (height - 8) / 2, l);
    }

    protected void renderExtra(GuiGraphics g) {
    }

    public boolean isMouseOver() {
        return isHovered();
    }
}
