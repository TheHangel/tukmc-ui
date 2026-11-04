package dev.hangel.tukmc.util;

import dev.hangel.tukmc.platform.Services;

/**
 * Settings storage: config/tukmc-client.toml on NeoForge, config/tukmc.json on Fabric. The rest of the mod keeps
 * reading the in-memory values (Config nodes, TukMCReference colors, TukMC chat flags).
 */
public final class ConfigFile {

    private ConfigFile() {
    }

    /** Writes the in-memory values to the file. */
    public static void save() {
        Services.PLATFORM.saveConfig();
    }

    /** Puts the saved colors back (leaving the color screen without saving). */
    public static void loadColors() {
        Services.PLATFORM.loadColors();
    }
}
