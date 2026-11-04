package dev.hangel.tukmc.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.jetbrains.annotations.Nullable;

import dev.hangel.tukmc.util.Config;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;

/**
 * Port of the original TukMC 1.6.4 GuiNewChat#drawChat. Messages are still stored by the vanilla ChatComponent (so signing,
 * deletion, history... keep working); only the drawing and the hit-testing use the TukMC layout.
 */
public final class TukChatOverlay {

    /** gui tick a line was added at -> wall clock time, so we can show "Received: hh:mm:ss" like TimedChatLine. */
    private static final TreeMap<Integer, Long> RECEIVED = new TreeMap<>();

    private TukChatOverlay() {
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static int getMaxView(ChatComponent chat) {
        int value = chat.getHeight() / 9;
        return value > 15 ? 15 : value;
    }

    private static int renderPosX() {
        return Config.get(Config.NODE_BOTTOM_ADORNMENTS) ? 12 : 0;
    }

    public static void render(GuiGraphics g) {
        Minecraft mc = mc();
        ChatComponent chat = mc.gui.getChat();
        if (mc.options.chatVisibility().get() == ChatVisiblity.HIDDEN)
            return;
        List<GuiMessage.Line> chatLines = chat.trimmedMessages;
        if (chatLines.isEmpty())
            return;

        int maxView = getMaxView(chat);
        boolean chatOpen = chat.isChatFocused();
        float chatBoxColor = (float) (mc.options.chatOpacity().get() * 0.9F + 0.1F);
        float chatScale = (float) chat.getScale();
        int chatWidth = Mth.ceil(chat.getWidth() / chatScale);
        int scrollDist = chat.chatScrollbarPos;
        int updatePass = mc.gui.getGuiTicks();

        g.pose().pushPose();
        g.pose().translate(0.0F, g.guiHeight() - 48, 0.0F);
        g.pose().scale(chatScale, chatScale, 1.0F);
        for (int renderLine = 0; renderLine + scrollDist < chatLines.size() && renderLine < maxView; ++renderLine) {
            GuiMessage.Line chatLine = chatLines.get(renderLine + scrollDist);
            int updateInt = updatePass - chatLine.addedTime();
            if (updateInt < 200 || chatOpen) {
                int finColor;
                if (!chatOpen) {
                    double fade = updateInt / 200.0D;
                    fade = 1.0D - fade;
                    fade *= 10.0D;
                    fade = Mth.clamp(fade, 0.0D, 1.0D);
                    fade *= fade;
                    finColor = (int) (255.0D * fade);
                } else {
                    finColor = 255;
                }
                finColor = (int) (finColor * chatBoxColor);

                if (finColor > 3) {
                    int renderPosX = renderPosX();
                    int renderPosY = renderLine * -9 + 17;
                    g.fill(3 + renderPosX, renderPosY - 1, chatWidth + 7 + renderPosX, renderPosY + 8,
                            finColor / 2 << 24);
                    g.drawString(mc.font, chatLine.content(), 3 + renderPosX, renderPosY, 0xFFFFFF + (finColor << 24),
                            true);
                }
            }
        }
        g.pose().popPose();
    }

    @Nullable
    public static GuiMessage.Line lineAt(double mouseX, double mouseY) {
        Minecraft mc = mc();
        ChatComponent chat = mc.gui.getChat();
        if (!chat.isChatFocused())
            return null;
        float s = (float) chat.getScale();
        int height = mc.getWindow().getGuiScaledHeight();
        int r = Mth.floor(((height - 48) + 25 * s - mouseY) / (9 * s));
        if (r < 0 || r >= getMaxView(chat))
            return null;
        int idx = r + chat.chatScrollbarPos;
        if (idx >= chat.trimmedMessages.size())
            return null;
        double cx = mouseX / s - (3 + renderPosX());
        if (cx < 0 || cx > Mth.ceil(chat.getWidth() / s) + 4)
            return null;
        return chat.trimmedMessages.get(idx);
    }

    @Nullable
    public static Style styleAt(double mouseX, double mouseY) {
        GuiMessage.Line line = lineAt(mouseX, mouseY);
        if (line == null)
            return null;
        float s = (float) mc().gui.getChat().getScale();
        int cx = Mth.floor(mouseX / s - (3 + renderPosX()));
        return mc().font.getSplitter().componentStyleAtWidth(line.content(), cx);
    }

    // ---------------------------------------------------------------- TimedChatLine replacement

    public static void tick() {
        Minecraft mc = mc();
        if (mc.gui == null)
            return;
        List<GuiMessage.Line> lines = mc.gui.getChat().trimmedMessages;
        if (lines.isEmpty()) {
            RECEIVED.clear();
            return;
        }
        int newest = lines.get(0).addedTime();
        if (!RECEIVED.containsKey(newest))
            RECEIVED.put(newest, System.currentTimeMillis());
        int oldest = lines.get(lines.size() - 1).addedTime();
        RECEIVED.headMap(oldest).clear();
    }

    public static long getMillisOfCreation(GuiMessage.Line line) {
        Long t = RECEIVED.get(line.addedTime());
        if (t == null) {
            var e = RECEIVED.floorEntry(line.addedTime());
            t = e == null ? System.currentTimeMillis() : e.getValue();
        }
        return t;
    }

    public static String getTime(GuiMessage.Line line) {
        return new SimpleDateFormat("kk:mm:ss").format(new Date(getMillisOfCreation(line)));
    }

    public static String getElapsedTime(GuiMessage.Line line) {
        long timeElapsed = (System.currentTimeMillis() - getMillisOfCreation(line)) / 1000L;
        long h = timeElapsed / 3600, m = timeElapsed / 60 % 60, sec = timeElapsed % 60;
        String formatted = timeElapsed >= 3600 ? String.format("%02dh, %02dm, %02ds", h, m, sec)
                : timeElapsed >= 60 ? String.format("%02dm, %02ds", m, sec) : String.format("%02ds", sec);
        return I18n.get("tukmc.chat.ago", formatted);
    }

    public static String toPlainString(FormattedCharSequence seq) {
        StringBuilder sb = new StringBuilder();
        seq.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return sb.toString();
    }
}
