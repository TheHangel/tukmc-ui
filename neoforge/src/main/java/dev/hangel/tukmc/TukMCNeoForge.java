package dev.hangel.tukmc;

import dev.hangel.tukmc.client.TukHudLayers;
import dev.hangel.tukmc.gui.GuiNewConfig;
import dev.hangel.tukmc.util.NeoForgeConfigFile;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = TukMC.MODID, dist = Dist.CLIENT)
public class TukMCNeoForge {

    public TukMCNeoForge(IEventBus modBus, ModContainer container) {
        NeoForgeConfigFile.register(container, modBus);

        // Mods menu > TukMC > Config opens the TukMC config screen (replaces the old K key binding)
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new GuiNewConfig(parent));

        modBus.addListener(TukHudLayers::register);
        modBus.addListener(NeoForgeEvents::registerKeys);
        NeoForge.EVENT_BUS.register(new NeoForgeEvents());

        TukMC.initDevTools();
    }
}
