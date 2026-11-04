package dev.hangel.tukmc.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import dev.hangel.tukmc.TukMC;
import dev.hangel.tukmc.util.Config.Node;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

/**
 * Settings storage for Fabric: config/tukmc.json, read and written with Gson. The rest of the mod keeps reading the
 * in-memory values (Config nodes, TukMCReference colors, TukMC chat flags); this class copies them from/to the file.
 */
public final class FabricConfigFile {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve(TukMC.MODID + ".json");

    private FabricConfigFile() {
    }

    /** Copies the file values into memory, then rewrites the file so it lists every option (unless it is broken). */
    public static void load() {
        JsonObject read = read();
        JsonObject root = read == null ? new JsonObject() : read;
        JsonObject options = GsonHelper.getAsJsonObject(root, "options", new JsonObject());
        for (Node node : Config.nodes.values())
            node.set(GsonHelper.getAsBoolean(options, node.getLabel(), node.isEnabledByDefault()));
        loadColors(root);
        JsonObject chat = GsonHelper.getAsJsonObject(root, "chat", new JsonObject());
        TukMC.closeOnFinish = GsonHelper.getAsBoolean(chat, "lockChat", false);
        TukMC.displayNotification = GsonHelper.getAsBoolean(chat, "notifications", true);
        if (read != null)
            save();
    }

    /** Puts the saved colors back (leaving the color screen without saving). */
    public static void loadColors() {
        JsonObject read = read();
        loadColors(read == null ? new JsonObject() : read);
    }

    private static void loadColors(JsonObject root) {
        JsonObject colors = GsonHelper.getAsJsonObject(root, "colors", new JsonObject());
        TukMCReference.RED_INNER = color(colors, "innerRed", 26);
        TukMCReference.GREEN_INNER = color(colors, "innerGreen", 26);
        TukMCReference.BLUE_INNER = color(colors, "innerBlue", 26);
        TukMCReference.RED_OUTER = color(colors, "outlineRed", 150);
        TukMCReference.GREEN_OUTER = color(colors, "outlineGreen", 154);
        TukMCReference.BLUE_OUTER = color(colors, "outlineBlue", 165);
        TukMCReference.BOX_INNER_COLOR = ColorUtil.rgb(TukMCReference.RED_INNER, TukMCReference.GREEN_INNER,
                TukMCReference.BLUE_INNER);
        TukMCReference.BOX_OUTLINE_COLOR = ColorUtil.rgb(TukMCReference.RED_OUTER, TukMCReference.GREEN_OUTER,
                TukMCReference.BLUE_OUTER);
    }

    private static int color(JsonObject colors, String key, int fallback) {
        return Mth.clamp(GsonHelper.getAsInt(colors, key, fallback), 0, 255);
    }

    /** Writes the in-memory values to the file. */
    public static void save() {
        JsonObject options = new JsonObject();
        for (Node node : Config.nodes.values())
            options.addProperty(node.getLabel(), node.isEnabled());

        JsonObject colors = new JsonObject();
        colors.addProperty("innerRed", TukMCReference.RED_INNER);
        colors.addProperty("innerGreen", TukMCReference.GREEN_INNER);
        colors.addProperty("innerBlue", TukMCReference.BLUE_INNER);
        colors.addProperty("outlineRed", TukMCReference.RED_OUTER);
        colors.addProperty("outlineGreen", TukMCReference.GREEN_OUTER);
        colors.addProperty("outlineBlue", TukMCReference.BLUE_OUTER);

        JsonObject chat = new JsonObject();
        chat.addProperty("lockChat", TukMC.closeOnFinish);
        chat.addProperty("notifications", TukMC.displayNotification);

        JsonObject root = new JsonObject();
        root.add("options", options);
        root.add("colors", colors);
        root.add("chat", chat);
        try (Writer w = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
            GSON.toJson(root, w);
        } catch (IOException e) {
            LogUtils.getLogger().error("[TukMC] Could not save {}", FILE, e);
        }
    }

    /** The file content, an empty object when there is no file yet, or null when it can't be parsed. */
    private static JsonObject read() {
        if (!Files.exists(FILE))
            return new JsonObject();
        try (Reader r = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(r).getAsJsonObject();
        } catch (Exception e) {
            LogUtils.getLogger().error("[TukMC] Could not read {}, using defaults", FILE, e);
            return null;
        }
    }
}
