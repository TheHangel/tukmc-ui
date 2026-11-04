package dev.hangel.tukmc.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.hangel.tukmc.gui.GuiNewConfig;

/** Mods menu > TukMC > Config opens the TukMC config screen (Fabric equivalent of NeoForge's IConfigScreenFactory). */
public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return GuiNewConfig::new;
    }
}
