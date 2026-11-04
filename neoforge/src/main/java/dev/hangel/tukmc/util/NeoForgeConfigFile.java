package dev.hangel.tukmc.util;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.hangel.tukmc.TukMC;
import dev.hangel.tukmc.util.Config.Node;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

/**
 * Settings storage backed by NeoForge's config system: config/tukmc-client.toml. The rest of the mod keeps reading the
 * in-memory values (Config nodes, TukMCReference colors, TukMC chat flags); this class copies them from/to the spec.
 */
public final class NeoForgeConfigFile {

    private static final Map<String, BooleanValue> OPTIONS = new LinkedHashMap<>();
    private static final IntValue INNER_RED, INNER_GREEN, INNER_BLUE, OUTER_RED, OUTER_GREEN, OUTER_BLUE;
    private static final BooleanValue LOCK_CHAT, NOTIFICATIONS;
    private static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("HUD and chat options, same as the TukMC config screen").push("options");
        for (Node node : Config.nodes.values())
            OPTIONS.put(node.getLabel(), b.comment(node.getDescription()).define(node.getLabel(), node.isEnabledByDefault()));
        b.pop();

        b.comment("Box colors (RGB, 0-255)").push("colors");
        INNER_RED = b.defineInRange("innerRed", 26, 0, 255);
        INNER_GREEN = b.defineInRange("innerGreen", 26, 0, 255);
        INNER_BLUE = b.defineInRange("innerBlue", 26, 0, 255);
        OUTER_RED = b.defineInRange("outlineRed", 150, 0, 255);
        OUTER_GREEN = b.defineInRange("outlineGreen", 154, 0, 255);
        OUTER_BLUE = b.defineInRange("outlineBlue", 165, 0, 255);
        b.pop();

        b.comment("Chat buttons").push("chat");
        LOCK_CHAT = b.comment("L button: close the chat after sending a message").define("lockChat", false);
        NOTIFICATIONS = b.comment("N button: play a sound on new messages").define("notifications", true);
        b.pop();

        SPEC = b.build();
    }

    private NeoForgeConfigFile() {
    }

    public static void register(ModContainer container, net.neoforged.bus.api.IEventBus modBus) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
        modBus.addListener(ModConfigEvent.Loading.class, e -> onLoad(e.getConfig()));
        modBus.addListener(ModConfigEvent.Reloading.class, e -> onLoad(e.getConfig()));
    }

    private static void onLoad(ModConfig config) {
        if (config.getSpec() == SPEC)
            load();
    }

    /** Copies the file values into memory. */
    public static void load() {
        OPTIONS.forEach((label, value) -> Config.nodes.get(label).set(value.get()));
        loadColors();
        TukMC.closeOnFinish = LOCK_CHAT.get();
        TukMC.displayNotification = NOTIFICATIONS.get();
    }

    /** Puts the saved colors back (leaving the color screen without saving). */
    public static void loadColors() {
        TukMCReference.RED_INNER = INNER_RED.get();
        TukMCReference.GREEN_INNER = INNER_GREEN.get();
        TukMCReference.BLUE_INNER = INNER_BLUE.get();
        TukMCReference.RED_OUTER = OUTER_RED.get();
        TukMCReference.GREEN_OUTER = OUTER_GREEN.get();
        TukMCReference.BLUE_OUTER = OUTER_BLUE.get();
        TukMCReference.BOX_INNER_COLOR = ColorUtil.rgb(TukMCReference.RED_INNER, TukMCReference.GREEN_INNER,
                TukMCReference.BLUE_INNER);
        TukMCReference.BOX_OUTLINE_COLOR = ColorUtil.rgb(TukMCReference.RED_OUTER, TukMCReference.GREEN_OUTER,
                TukMCReference.BLUE_OUTER);
    }

    /** Writes the in-memory values to the file. */
    public static void save() {
        OPTIONS.forEach((label, value) -> value.set(Config.nodes.get(label).isEnabled()));
        INNER_RED.set(TukMCReference.RED_INNER);
        INNER_GREEN.set(TukMCReference.GREEN_INNER);
        INNER_BLUE.set(TukMCReference.BLUE_INNER);
        OUTER_RED.set(TukMCReference.RED_OUTER);
        OUTER_GREEN.set(TukMCReference.GREEN_OUTER);
        OUTER_BLUE.set(TukMCReference.BLUE_OUTER);
        LOCK_CHAT.set(TukMC.closeOnFinish);
        NOTIFICATIONS.set(TukMC.displayNotification);
        SPEC.save();
    }
}
