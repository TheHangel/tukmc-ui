package dev.hangel.tukmc.util;

import java.io.File;

import dev.hangel.tukmc.platform.Services;

public class IOUtils {

    public static File getTukMCFolder() {
        return new File(Services.PLATFORM.getGameDir().toFile(), "tukmc");
    }
}
