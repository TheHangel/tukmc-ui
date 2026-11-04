package dev.hangel.tukmc.handlers;

import dev.hangel.tukmc.TukMC;
import dev.hangel.tukmc.client.TukChatOverlay;
import dev.hangel.tukmc.client.TukHud;
import dev.hangel.tukmc.gui.GuiChat;
import dev.hangel.tukmc.gui.McMMOIntegration;
import dev.hangel.tukmc.util.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;

public class TickHandler {

    public static boolean deathadded = false;
    private int statsTimer = 0;

    /** At the end of every client tick. */
    public void onClientTick(Minecraft mc) {
        Screen gui = mc.screen;
        if (gui instanceof DeathScreen && !deathadded) {
            TukMC.deaths++;
            deathadded = true;
        } else if (!(gui instanceof DeathScreen) && deathadded) {
            deathadded = false;
        }

        McMMOIntegration.tick();
        TukChatOverlay.tick();
        if (mc.player != null) {
            TukHud.INSTANCE.tick();
            // 1.6.4 counted kills client side; now the server owns stats, ask for them now and then like the stats screen does
            if (Config.get(Config.NODE_STATBAR) && mc.getConnection() != null && --statsTimer <= 0) {
                statsTimer = 200;
                mc.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
            }
        }
    }

    /** The original replaced vanilla GuiChat / GuiSleepMP every tick; each loader calls this as a screen opens. */
    public static Screen onScreenOpening(Screen screen) {
        if (!Config.get(Config.NODE_DEFAULT_CHAT) && screen instanceof ChatScreen chat && !(chat instanceof GuiChat)) {
            return new GuiChat(chat.initial, chat instanceof InBedChatScreen);
        }
        return screen;
    }
}
