package dev.hangel.tukmc.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * The box primitives every TukMC screen used (drawDoubleOutlinedBox & co). The original drew them with a 0.5 scaled
 * GL matrix so the outlines are half a GUI pixel thick; same thing here with the pose stack.
 */
public final class TukDraw {

    private TukDraw() {
    }

    public static void drawDoubleOutlinedBox(GuiGraphics g, int x, int y, int width, int height, int color,
            int outlineColor) {
        drawDoubleOutlinedBox(g, x, y, width, height, color, outlineColor, color);
    }

    public static void drawDoubleOutlinedBox(GuiGraphics g, int x, int y, int width, int height, int color,
            int outlineColor, int outline2Color) {
        g.pose().pushPose();
        g.pose().scale(0.5F, 0.5F, 1F);
        drawSolidRect(g, x * 2 - 2, y * 2 - 2, (x + width) * 2 + 2, (y + height) * 2 + 2, color);
        drawSolidRect(g, x * 2 - 1, y * 2 - 1, (x + width) * 2 + 1, (y + height) * 2 + 1, outlineColor);
        drawSolidRect(g, x * 2, y * 2, (x + width) * 2, (y + height) * 2, outline2Color);
        g.pose().popPose();
    }

    public static void drawOutlinedBox(GuiGraphics g, int x, int y, int width, int height, int color,
            int outlineColor) {
        g.pose().pushPose();
        g.pose().scale(0.5F, 0.5F, 1F);
        drawSolidRect(g, x * 2 - 2, y * 2 - 2, (x + width) * 2 + 2, (y + height) * 2 + 2, outlineColor);
        drawSolidRect(g, x * 2 - 1, y * 2 - 1, (x + width) * 2 + 1, (y + height) * 2 + 1, color);
        g.pose().popPose();
    }

    /** Opaque rectangle between two corners, in the current pose. */
    public static void drawSolidRect(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y2, 0xFF000000 | color);
    }

    /** Vertical gradient: color1 at the bottom, color2 at the top (as in the original). */
    public static void drawSolidGradientRect(GuiGraphics g, int x, int y, int width, int height, int color1,
            int color2) {
        if (width <= 0 || height <= 0)
            return;
        g.fillGradient(x, y, x + width, y + height, 0xFF000000 | color2, 0xFF000000 | color1);
    }

    public static void drawHalfString(GuiGraphics g, Font font, String s, int x, int y, int color, boolean shadow) {
        g.pose().pushPose();
        g.pose().scale(0.5F, 0.5F, 1F);
        g.drawString(font, s, x, y, color, shadow);
        g.pose().popPose();
    }

    public static void drawHalfString(GuiGraphics g, Font font, Component s, int x, int y, int color, boolean shadow) {
        g.pose().pushPose();
        g.pose().scale(0.5F, 0.5F, 1F);
        g.drawString(font, s, x, y, color, shadow);
        g.pose().popPose();
    }
}
