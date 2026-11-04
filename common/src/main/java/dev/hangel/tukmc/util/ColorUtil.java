package dev.hangel.tukmc.util;

/** Same maths as java.awt.Color#brighter / #darker, without touching AWT. */
public class ColorUtil {

    private static final double FACTOR = 0.7;

    public static int brighter(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        int i = (int) (1.0 / (1.0 - FACTOR));
        if (r == 0 && g == 0 && b == 0)
            return 0xFF000000 | i << 16 | i << 8 | i;
        if (r > 0 && r < i) r = i;
        if (g > 0 && g < i) g = i;
        if (b > 0 && b < i) b = i;
        return 0xFF000000 | Math.min((int) (r / FACTOR), 255) << 16 | Math.min((int) (g / FACTOR), 255) << 8
                | Math.min((int) (b / FACTOR), 255);
    }

    public static int darker(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        return 0xFF000000 | Math.max((int) (r * FACTOR), 0) << 16 | Math.max((int) (g * FACTOR), 0) << 8
                | Math.max((int) (b * FACTOR), 0);
    }

    public static int rgb(int r, int g, int b) {
        return 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }
}
