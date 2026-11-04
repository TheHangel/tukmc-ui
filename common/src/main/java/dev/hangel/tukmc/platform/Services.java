package dev.hangel.tukmc.platform;

import java.util.ServiceLoader;

import dev.hangel.tukmc.platform.services.IPlatformHelper;

/** Loads the loader specific implementations (META-INF/services in the neoforge and fabric projects). */
public final class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    private Services() {
    }

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, Services.class.getClassLoader()).findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }
}
