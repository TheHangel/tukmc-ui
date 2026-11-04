package dev.hangel.tukmc.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

/** How many times each link was opened from the chat, kept in tukmc/websites.json. */
public final class WebsiteVisits {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = IOUtils.getTukMCFolder().toPath().resolve("websites.json");
    private static final Map<String, Integer> VISITS = new TreeMap<>();
    private static boolean loaded;
    /** The file could not be parsed: keep it as is rather than overwrite it. */
    private static boolean broken;

    private WebsiteVisits() {
    }

    public static int get(String url) {
        load();
        return VISITS.getOrDefault(url, 0);
    }

    public static void add(String url) {
        load();
        VISITS.merge(url, 1, Integer::sum);
        save();
    }

    private static void load() {
        if (loaded)
            return;
        loaded = true;
        if (!Files.exists(FILE))
            return;
        try (Reader r = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, JsonElement> e : JsonParser.parseReader(r).getAsJsonObject().entrySet())
                VISITS.put(e.getKey(), e.getValue().getAsInt());
        } catch (Exception e) {
            broken = true;
            LogUtils.getLogger().error("[TukMC] Could not read {}, visit counts won't be saved", FILE, e);
        }
    }

    private static void save() {
        if (broken)
            return;
        JsonObject root = new JsonObject();
        VISITS.forEach(root::addProperty);
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
        } catch (IOException e) {
            LogUtils.getLogger().error("[TukMC] Could not save {}", FILE, e);
        }
    }
}
