package dev.hangel.tukmc.handlers;

import java.util.Optional;
import java.util.regex.Matcher;

import dev.hangel.tukmc.TukMC;
import dev.hangel.tukmc.gui.GuiChat;
import dev.hangel.tukmc.gui.McMMOIntegration;
import dev.hangel.tukmc.gui.McMMOIntegration.LevelUpData;
import dev.hangel.tukmc.gui.McMMOIntegration.SkillData;
import dev.hangel.tukmc.util.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class ChatListener {

    // Each loader hands received chat messages (not the action bar) over to allow, decorate and received.

    /** mcMMO notifications are eaten and shown in the HUD instead. @return false to cancel the message. */
    public static boolean allow(Component message) {
        if (!Config.get(Config.NODE_MCMMO))
            return true;
        String legacy = toLegacy(message);
        if (McMMOIntegration.passMessage(legacy)) {
            if (TukMC.displayNotification)
                play(SoundEvents.EXPERIENCE_ORB_PICKUP, 1F);
            return false;
        }
        LevelUpData levelData = LevelUpData.fromString(legacy);
        if (levelData != null) {
            McMMOIntegration.setLevelUpData(levelData);
            if (TukMC.displayNotification && levelData.getLevel() % 5 == 0)
                play(SoundEvents.PLAYER_LEVELUP, 3F);
            return false;
        }
        SkillData skillData = SkillData.fromString(legacy);
        if (skillData != null) {
            McMMOIntegration.addSkillData(skillData);
            if (TukMC.displayNotification)
                play(SoundEvents.EXPERIENCE_ORB_PICKUP, 1F);
            return false;
        }
        return true;
    }

    /** A message was added to the chat. */
    public static void received() {
        if (TukMC.displayNotification && !Config.get(Config.NODE_DEFAULT_CHAT)) {
            play(SoundEvents.EXPERIENCE_ORB_PICKUP, 1F);
        }
    }

    /** Link coloring, for every message added to the chat. */
    public static Component decorate(Component message) {
        if (Config.get(Config.NODE_COLORBLIND_MODE))
            return message;
        Component linked = colorLinks(message);
        return linked != null ? linked : message;
    }

    private static void play(SoundEvent sound, float volume) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1F, volume));
    }

    /** Words that look like links become aqua (and clickable, as 1.6.4 chat did automatically). */
    private static Component colorLinks(Component message) {
        MutableComponent out = Component.empty();
        boolean[] found = { false };
        message.visit((style, text) -> {
            // already clickable (vanilla screenshot / chat log file names, server links...): leave it alone
            if (style.getClickEvent() != null) {
                out.append(Component.literal(text).withStyle(style));
                return Optional.empty();
            }
            StringBuilder plain = new StringBuilder();
            for (String word : text.split("(?<= )|(?= )")) {
                Matcher m = GuiChat.pattern.matcher(word);
                if (!word.isBlank() && m.matches()) {
                    if (plain.length() > 0) {
                        out.append(Component.literal(plain.toString()).withStyle(style));
                        plain.setLength(0);
                    }
                    String url = m.group(1) == null ? "http://" + word : word;
                    Style linkStyle = style.withColor(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                    out.append(Component.literal(word).withStyle(linkStyle));
                    found[0] = true;
                } else {
                    plain.append(word);
                }
            }
            if (plain.length() > 0)
                out.append(Component.literal(plain.toString()).withStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return found[0] ? out : null;
    }

    /** Rebuilds the "§a**YOU READY YOUR..." style strings the mcMMO parser was written against. */
    public static String toLegacy(Component component) {
        StringBuilder sb = new StringBuilder();
        component.visit((style, text) -> {
            TextColor color = style.getColor();
            if (color != null) {
                ChatFormatting f = ChatFormatting.getByName(color.serialize());
                if (f != null)
                    sb.append(f);
            }
            if (style.isBold())
                sb.append(ChatFormatting.BOLD);
            if (style.isItalic())
                sb.append(ChatFormatting.ITALIC);
            if (style.isUnderlined())
                sb.append(ChatFormatting.UNDERLINE);
            if (style.isStrikethrough())
                sb.append(ChatFormatting.STRIKETHROUGH);
            if (style.isObfuscated())
                sb.append(ChatFormatting.OBFUSCATED);
            sb.append(text);
            return Optional.empty();
        }, Style.EMPTY);
        return sb.toString();
    }
}
