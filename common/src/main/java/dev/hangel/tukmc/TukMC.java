package dev.hangel.tukmc;

import dev.hangel.tukmc.util.ColorUtil;
import dev.hangel.tukmc.util.ConfigFile;
import dev.hangel.tukmc.util.TukMCReference;

/** Shared state and settings helpers. The entry points are TukMCNeoForge and TukMCFabric. */
public class TukMC {

    public static final String MODID = "tukmc";

    public static boolean closeOnFinish = false;
    public static boolean displayNotification = true;

    public static int deaths = 0;
    public static int negativeMobKills = 0;
    public static int negativePKills = 0;

    /** Dev only: -Dtukmc.selftest or -Dtukmc.showcase starts the matching scripted run (classes left out of the jar). */
    public static void initDevTools() {
        String devClass = System.getProperty("tukmc.selftest") != null ? "SelfTest"
                : System.getProperty("tukmc.showcase") != null ? "Showcase" : null;
        if (devClass != null) {
            try {
                Class.forName("dev.hangel.tukmc.dev." + devClass).getMethod("init").invoke(null);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void setCloseOnFinish(boolean b) {
        closeOnFinish = b;
        ConfigFile.save();
    }

    public static void setDisplayNotification(boolean b) {
        displayNotification = b;
        ConfigFile.save();
    }

    /** Recomputes the box colors from the RGB values, without saving them. */
    public static void applyColorSettings() {
        TukMCReference.BOX_INNER_COLOR = ColorUtil.rgb(TukMCReference.RED_INNER, TukMCReference.GREEN_INNER,
                TukMCReference.BLUE_INNER);
        TukMCReference.BOX_OUTLINE_COLOR = ColorUtil.rgb(TukMCReference.RED_OUTER, TukMCReference.GREEN_OUTER,
                TukMCReference.BLUE_OUTER);
        TukMCReference.BOX_HIGHLIGHT_COLOR = ColorUtil
                .brighter(ColorUtil.brighter(ColorUtil.brighter(TukMCReference.BOX_INNER_COLOR)));
    }

    public static void saveColorSettings() {
        applyColorSettings();
        ConfigFile.save();
    }

    public static void loadColorSettings() {
        ConfigFile.loadColors();
    }

    public static void defaultColorSettings() {
        TukMCReference.BOX_INNER_COLOR = 0x1A1A1A;
        TukMCReference.BOX_HIGHLIGHT_COLOR = 0x4C4C4C;
        TukMCReference.BOX_OUTLINE_COLOR = 0x969AA5;
        TukMCReference.RED_INNER = 26;
        TukMCReference.GREEN_INNER = 26;
        TukMCReference.BLUE_INNER = 26;
        TukMCReference.RED_OUTER = 150;
        TukMCReference.GREEN_OUTER = 154;
        TukMCReference.BLUE_OUTER = 165;
        saveColorSettings();
    }
}
