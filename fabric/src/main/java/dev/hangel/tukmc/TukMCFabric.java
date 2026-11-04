package dev.hangel.tukmc;

import dev.hangel.tukmc.handlers.ChatListener;
import dev.hangel.tukmc.handlers.KeyRegister;
import dev.hangel.tukmc.handlers.TickHandler;
import dev.hangel.tukmc.util.FabricConfigFile;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

public class TukMCFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricConfigFile.load();

        // HUD, chat screen swap, link coloring, health bars and nameplates are hooked through mixins (see
        // dev.hangel.tukmc.mixin); the Config button of the mod list comes from Mod Menu (see compat.ModMenuCompat).
        KeyBindingHelper.registerKeyBinding(KeyRegister.showTooltipKB);
        TickHandler ticks = new TickHandler();
        ClientTickEvents.END_CLIENT_TICK.register(ticks::onClientTick);
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signed, sender, params, time) -> ChatListener.allow(message));
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> overlay || ChatListener.allow(message));
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, time) -> ChatListener.received());
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay)
                ChatListener.received();
        });

        TukMC.initDevTools();
    }
}
