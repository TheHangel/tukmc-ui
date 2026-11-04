package dev.hangel.tukmc.dev;

import java.io.File;

import dev.hangel.tukmc.gui.GuiChat;
import dev.hangel.tukmc.gui.GuiColorConfig;
import dev.hangel.tukmc.gui.GuiSpecialChars;
import dev.hangel.tukmc.util.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Dev only (run with ./gradlew :fabric:runSelftest, never shipped in the jar): creates a flat world, sets a scene up and takes
 * screenshots of the HUD and the TukMC screens into run/screenshots, then quits.
 */
public class SelfTest {

    private static final String WORLD = "tukmc-selftest";
    private static boolean started = false;
    private static int t = 0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(SelfTest::tick);
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, "tukmc_" + name + ".png", mc.getMainRenderTarget(), c -> {
        });
    }

    private static void cmd(Minecraft mc, String c) {
        MinecraftServer server = mc.getSingleplayerServer();
        server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), c));
    }

    private static void tick(Minecraft mc) {
        if (!started && mc.screen instanceof TitleScreen) {
            started = true;
            if (new File(mc.gameDirectory, "saves/" + WORLD).exists()) {
                mc.createWorldOpenFlows().openWorld(WORLD, () -> mc.setScreen(new TitleScreen()));
            } else {
                LevelSettings settings = new LevelSettings(WORLD, GameType.SURVIVAL, false, Difficulty.NORMAL, true,
                        new GameRules(), WorldDataConfiguration.DEFAULT);
                mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(1234L, false, false),
                        reg -> reg.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value()
                                .createWorldDimensions(),
                        mc.screen);
            }
            return;
        }
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null)
            return;
        t++;
        String p = mc.player.getName().getString();
        switch (t) {
            case 40 -> {
                for (String c : new String[] { "time set 18000", "weather clear", "gamerule doDaylightCycle false",
                        "gamerule doMobSpawning false", "gamemode survival " + p, "clear " + p,
                        "tp " + p + " 0 -60 0 0 25",
                        "item replace entity " + p + " armor.head with diamond_helmet",
                        "item replace entity " + p + " armor.chest with iron_chestplate",
                        "item replace entity " + p + " armor.legs with golden_leggings",
                        "item replace entity " + p + " armor.feet with leather_boots",
                        "give " + p + " diamond_sword[enchantments={levels:{unbreaking:3}},damage=400]",
                        "give " + p + " bow", "give " + p + " arrow 5", "give " + p + " bread 12",
                        "give " + p + " cobblestone 64", "give " + p + " torch 30", "give " + p + " golden_apple 2",
                        "damage " + p + " 7", "effect give " + p + " speed 300 1", "effect give " + p + " regeneration 60 0",
                        "effect give " + p + " hunger 8 40",
                        "summon zombie 0 -60 5 {NoAI:1b,PersistenceRequired:1b}",
                        "summon cow 3 -60 6 {NoAI:1b,PersistenceRequired:1b}",
                        "setblock 1 -60 2 minecraft:crafting_table",
                        "bossbar add tukmc:test \"Ender Dragon\"", "bossbar set tukmc:test players " + p,
                        "bossbar set tukmc:test value 70",
                        "scoreboard objectives add kills dummy \"Kills\"", "scoreboard objectives setdisplay sidebar kills",
                        "scoreboard players set " + p + " kills 12", "scoreboard players set Notch kills 3",
                        "xp add " + p + " 30 points", "xp add " + p + " 7 levels" })
                    cmd(mc, c);
            }
            case 100 -> {
                cmd(mc, "say Hello from TukMC on Fabric 1.21.1!");
                cmd(mc, "tellraw " + p + " \"Check out fabricmc.net or https://hangel.dev for more\"");
                mc.player.getInventory().selected = 0;
            }
            case 130 -> mc.gui.setNowPlaying(Component.literal("C418 - cat"));
            case 140 -> shot(mc, "1_hud");
            case 150 -> mc.setScreen(new GuiChat("hello world", false));
            case 175 -> shot(mc, "2_chat");
            case 180 -> mc.setScreen(new dev.hangel.tukmc.compat.ModMenuCompat().getModConfigScreenFactory().create(mc.screen));
            case 200 -> shot(mc, "3_config");
            case 205 -> mc.setScreen(new GuiColorConfig(null));
            case 225 -> shot(mc, "4_colors");
            case 230 -> mc.setScreen(new GuiSpecialChars(new GuiChat("", false), "", GuiSpecialChars.Kind.CHARACTERS));
            case 245 -> shot(mc, "5_chars");
            case 246 -> mc.setScreen(new GuiSpecialChars(new GuiChat("", false), "", GuiSpecialChars.Kind.EMOJIS));
            case 252 -> shot(mc, "5b_emojis");
            case 255 -> {
                mc.setScreen(null);
                Config.nodes.get(Config.NODE_ALT_STATUS).set(true);
                Config.nodes.get(Config.NODE_BOTTOM_ADORNMENTS).set(true);
                Config.nodes.get(Config.NODE_STATBAR).set(true);
                mc.player.getInventory().selected = 1;
            }
            case 285 -> shot(mc, "6_hud_alt");
            case 290 -> {
                Config.nodes.get(Config.NODE_ALT_STATUS).set(false);
                Config.nodes.get(Config.NODE_BOTTOM_ADORNMENTS).set(false);
                Config.nodes.get(Config.NODE_STATBAR).set(false);
                Config.nodes.get(Config.NODE_CUSTOM_BARS).set(false);
            }
            case 310 -> shot(mc, "7_hud_vanilla_bars");
            case 315 -> Config.nodes.get(Config.NODE_CUSTOM_BARS).set(true);
            case 330 -> mc.stop();
            default -> {
            }
        }
    }
}
