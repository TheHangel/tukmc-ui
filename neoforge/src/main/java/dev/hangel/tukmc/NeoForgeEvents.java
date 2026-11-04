package dev.hangel.tukmc;

import dev.hangel.tukmc.handlers.ChatListener;
import dev.hangel.tukmc.handlers.KeyRegister;
import dev.hangel.tukmc.handlers.TickHandler;
import dev.hangel.tukmc.render.MobHealthBars;
import dev.hangel.tukmc.render.RenderPlayerTuk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.util.TriState;

/** NeoForge events, handed over to the common handlers. */
public class NeoForgeEvents {

    private final TickHandler ticks = new TickHandler();

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KeyRegister.showTooltipKB);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        ticks.onClientTick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public void onScreenOpening(ScreenEvent.Opening event) {
        Screen screen = TickHandler.onScreenOpening(event.getNewScreen());
        if (screen != event.getNewScreen())
            event.setNewScreen(screen);
    }

    @SubscribeEvent
    public void onChatMsgReceived(ClientChatReceivedEvent event) {
        if (event instanceof ClientChatReceivedEvent.System sys && sys.isOverlay())
            return;
        Component message = event.getMessage();
        if (!ChatListener.allow(message)) {
            event.setCanceled(true);
            return;
        }
        event.setMessage(ChatListener.decorate(message));
        ChatListener.received();
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        MobHealthBars.INSTANCE.onRenderLiving(event.getEntity(), event.getPoseStack(), event.getMultiBufferSource());
    }

    @SubscribeEvent
    public void onNameTag(RenderNameTagEvent event) {
        if (event.canRender() == TriState.FALSE)
            return;
        if (RenderPlayerTuk.INSTANCE.onNameTag(event.getEntity(), event.getContent(), event.getPoseStack(),
                event.getMultiBufferSource(), event.getPackedLight()))
            event.setCanRender(TriState.FALSE);
    }
}
