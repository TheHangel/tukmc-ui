package dev.hangel.tukmc.gui;

import dev.hangel.tukmc.client.TukDraw;
import dev.hangel.tukmc.util.TukMCReference;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class GuiMenuSlider extends AbstractSliderButton {

    private final int id;

    public GuiMenuSlider(int ident, int x, int y) {
        super(x, y, 150, 20, Component.empty(), get(ident) / 255.0);
        id = ident;
        updateMessage();
    }

    private static int get(int id) {
        return switch (id) {
            case 0 -> TukMCReference.RED_INNER;
            case 1 -> TukMCReference.GREEN_INNER;
            case 2 -> TukMCReference.BLUE_INNER;
            case 3 -> TukMCReference.RED_OUTER;
            case 4 -> TukMCReference.GREEN_OUTER;
            case 5 -> TukMCReference.BLUE_OUTER;
            default -> 0;
        };
    }

    @Override
    protected void applyValue() {
        int v = (int) Math.round(value * 255);
        switch (id) {
            case 0 -> TukMCReference.RED_INNER = v;
            case 1 -> TukMCReference.GREEN_INNER = v;
            case 2 -> TukMCReference.BLUE_INNER = v;
            case 3 -> TukMCReference.RED_OUTER = v;
            case 4 -> TukMCReference.GREEN_OUTER = v;
            case 5 -> TukMCReference.BLUE_OUTER = v;
            default -> {
            }
        }
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(getKeyBinding(id)));
    }

    public void refresh() {
        value = get(id) / 255.0;
        updateMessage();
    }

    public String getKeyBinding(int id) {
        return switch (id) {
            case 0 -> I18n.get("tukmc.colors.red", TukMCReference.RED_INNER);
            case 1 -> I18n.get("tukmc.colors.green", TukMCReference.GREEN_INNER);
            case 2 -> I18n.get("tukmc.colors.blue", TukMCReference.BLUE_INNER);
            case 3 -> I18n.get("tukmc.colors.red", TukMCReference.RED_OUTER);
            case 4 -> I18n.get("tukmc.colors.green", TukMCReference.GREEN_OUTER);
            case 5 -> I18n.get("tukmc.colors.blue", TukMCReference.BLUE_OUTER);
            default -> "This shouldn't exist.";
        };
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        TukDraw.drawDoubleOutlinedBox(g, getX(), getY(), width, height, TukMCReference.BOX_INNER_COLOR,
                TukMCReference.BOX_OUTLINE_COLOR);
        TukDraw.drawOutlinedBox(g, getX() + (int) (value * (width - 8)), getY() + 1, 7, 18,
                TukMCReference.BOX_INNER_COLOR, TukMCReference.BOX_OUTLINE_COLOR);
        g.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2,
                0xFFFFFF);
    }
}
