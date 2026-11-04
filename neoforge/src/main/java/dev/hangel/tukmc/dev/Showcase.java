package dev.hangel.tukmc.dev;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import dev.hangel.tukmc.TukMC;
import dev.hangel.tukmc.client.TukChatOverlay;
import dev.hangel.tukmc.client.TukHud;
import dev.hangel.tukmc.gui.GuiChat;
import dev.hangel.tukmc.gui.GuiChatConfirmLink;
import dev.hangel.tukmc.gui.GuiColorConfig;
import dev.hangel.tukmc.gui.GuiNewConfig;
import dev.hangel.tukmc.gui.GuiSpecialChars;
import dev.hangel.tukmc.handlers.KeyRegister;
import dev.hangel.tukmc.util.Config;
import dev.hangel.tukmc.util.TukMCReference;
import dev.hangel.tukmc.util.WebsiteVisits;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Dev only (./gradlew :neoforge:runShowcase, never shipped in the jar): opens the "readme" world and screenshots the TukMC
 * features one by one into run/screenshots, for the README. With -Dtukmc.showcase=gif, takes the frames of the
 * README gif instead.
 */
public class Showcase {

    private static final String WORLD = "readme";
    private static final String LINK = "https://hangel.dev";
    private static final Map<Integer, Consumer<Minecraft>> STEPS = new TreeMap<>();
    private static boolean started = false;
    private static int t = 0;
    private static float yaw, pitch;

    public static void init() {
        NeoForge.EVENT_BUS.addListener(Showcase::tick);
    }

    private static void at(int tick, Consumer<Minecraft> step) {
        STEPS.merge(tick, step, Consumer::andThen);
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, "readme_" + name + ".png", mc.getMainRenderTarget(), c -> {
        });
    }

    private static void cmd(Minecraft mc, String... cmds) {
        MinecraftServer server = mc.getSingleplayerServer();
        server.execute(() -> {
            for (String c : cmds)
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), c);
        });
    }

    private static void node(String label, boolean on) {
        Config.nodes.get(label).set(on);
    }

    private static void look(Minecraft mc, float y, float p) {
        mc.player.setYRot(y);
        mc.player.setXRot(p);
        mc.player.yRotO = y;
        mc.player.xRotO = p;
    }

    /** Puts the mouse at a GUI position, as if the player had moved it there. */
    private static void mouse(Minecraft mc, double guiX, double guiY) {
        try {
            double sx = (double) mc.getWindow().getScreenWidth() / mc.getWindow().getGuiScaledWidth();
            double sy = (double) mc.getWindow().getScreenHeight() / mc.getWindow().getGuiScaledHeight();
            Field x = MouseHandler.class.getDeclaredField("xpos");
            Field y = MouseHandler.class.getDeclaredField("ypos");
            x.setAccessible(true);
            y.setAccessible(true);
            x.setDouble(mc.mouseHandler, guiX * sx);
            y.setDouble(mc.mouseHandler, guiY * sy);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void click(Minecraft mc, double guiX, double guiY) {
        mc.screen.mouseClicked(guiX, guiY, 0);
    }

    /** Clicks entry n of the character/emoji picker (14 columns of 8). */
    private static void pick(Minecraft mc, int n) {
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        click(mc, w / 2 - 135 + 17 * (n / 8 + 1) + 7, h / 2 - 75 + 17 * (n % 8 + 1) + 7);
    }

    static {
        setUp();
        if ("gif".equals(System.getProperty("tukmc.showcase")))
            gif();
        else
            readme();
    }

    private static void setUp() {
        // world and player
        at(40, mc -> {
            String p = mc.player.getName().getString();
            yaw = mc.player.getYRot();
            pitch = 5;
            cmd(mc, "gamerule sendCommandFeedback false", "gamerule doDaylightCycle false",
                    "gamerule doMobSpawning false", "gamerule doWeatherCycle false", "difficulty normal",
                    "time set 1000", "weather clear", "gamemode survival " + p, "clear " + p, "effect clear " + p,
                    "kill @e[type=!player]",
                    "item replace entity " + p + " armor.head with diamond_helmet",
                    "item replace entity " + p + " armor.chest with iron_chestplate[damage=120]",
                    "item replace entity " + p + " armor.legs with golden_leggings",
                    "item replace entity " + p + " armor.feet with leather_boots[damage=40]",
                    "item replace entity " + p + " hotbar.0 with diamond_sword[enchantments={levels:{sharpness:4,unbreaking:3}},damage=600]",
                    "item replace entity " + p + " hotbar.1 with iron_pickaxe[damage=90]",
                    "item replace entity " + p + " hotbar.2 with bow",
                    "item replace entity " + p + " hotbar.3 with bread 12",
                    "item replace entity " + p + " hotbar.4 with sandstone 64",
                    "item replace entity " + p + " hotbar.5 with torch 30",
                    "item replace entity " + p + " hotbar.6 with golden_apple 2",
                    "item replace entity " + p + " hotbar.7 with water_bucket",
                    "item replace entity " + p + " hotbar.8 with map",
                    "give " + p + " arrow 24", "give " + p + " cobblestone 64",
                    "xp set " + p + " 23 levels", "xp set " + p + " 40 points");
        });
        at(50, mc -> {
            MinecraftServer server = mc.getSingleplayerServer();
            server.execute(() -> {
                ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                sp.setHealth(14);
                sp.getFoodData().setFoodLevel(13);
            });
        });
    }

    private static void readme() {
        // default HUD
        at(90, mc -> {
            mc.gui.getChat().clearMessages(false);
            mc.player.getInventory().selected = 0;
            look(mc, yaw, pitch);
        });
        at(130, mc -> shot(mc, "01_hud"));

        // health/food/xp bars, holding food to show the food prediction
        at(135, mc -> mc.player.getInventory().selected = 3);
        at(160, mc -> shot(mc, "02_bars_default"));
        at(165, mc -> node(Config.NODE_ALT_STATUS, true));
        at(190, mc -> shot(mc, "03_bars_alt"));
        at(195, mc -> node(Config.NODE_ALT_STATUS, false));

        // potion effects
        at(200, mc -> cmd(mc, "effect give @p speed 600 1", "effect give @p haste 95 0",
                "effect give @p fire_resistance 300 0", "effect give @p regeneration 40 0"));
        at(225, mc -> shot(mc, "04_effects"));
        at(230, mc -> cmd(mc, "effect clear @p"));

        // boss bar and jukebox song
        at(235, mc -> {
            cmd(mc, "bossbar add tukmc:readme \"Wither\"", "bossbar set tukmc:readme players @a",
                    "bossbar set tukmc:readme value 64", "bossbar set tukmc:readme color purple");
            mc.gui.setNowPlaying(Component.literal("C418 - cat"));
        });
        at(260, mc -> shot(mc, "05_boss_record"));
        at(265, mc -> cmd(mc, "bossbar remove tukmc:readme"));

        // bow: arrow count
        at(270, mc -> mc.player.getInventory().selected = 2);
        at(290, mc -> shot(mc, "06_arrows"));

        // mob health bars
        at(295, mc -> {
            mc.player.getInventory().selected = 0;
            String r = "execute at @p rotated " + yaw + " 0 positioned ";
            String nbt = " ~ ~ ~ {NoAI:1b,PersistenceRequired:1b,Rotation:[";
            cmd(mc, r + "^2.6 ^ ^5 positioned over motion_blocking run summon camel" + nbt + (yaw + 150) + "f,0f]}",
                    r + "^0 ^ ^3.6 positioned over motion_blocking run summon husk" + nbt + (yaw + 180) + "f,0f]}",
                    r + "^-2.4 ^ ^4.4 positioned over motion_blocking run summon creeper" + nbt + (yaw + 210) + "f,0f]}",
                    "execute as @e[type=husk] run damage @s 6", "execute as @e[type=creeper] run damage @s 13");
        });
        at(300, mc -> look(mc, yaw, 14));
        at(330, mc -> shot(mc, "07_mobs"));
        at(335, mc -> cmd(mc, "tp @e[type=!player] ~ -300 ~"));

        // block at pointer
        at(345, mc -> look(mc, yaw, 50));
        at(365, mc -> shot(mc, "08_block"));
        at(370, mc -> look(mc, yaw, pitch));

        // side stats bar and scoreboard
        at(375, mc -> {
            node(Config.NODE_STATBAR, true);
            TukMC.deaths = 3;
            TukMC.negativeMobKills = TukHud.getMobKills() - 41;
            TukMC.negativePKills = TukHud.getPlayerKills() - 2;
            cmd(mc, "scoreboard objectives add readme dummy \"Treasure\"",
                    "scoreboard objectives setdisplay sidebar readme", "scoreboard players set @p readme 18",
                    "scoreboard players set Alex readme 11", "scoreboard players set Steve readme 7");
        });
        at(400, mc -> shot(mc, "09_stats"));
        at(405, mc -> {
            node(Config.NODE_STATBAR, false);
            cmd(mc, "scoreboard objectives remove readme");
        });

        // held item tooltip
        at(410, mc -> node(Config.NODE_TOOLTIP_ALWAYS_ON, true));
        at(430, mc -> shot(mc, "10_tooltip"));
        at(435, mc -> node(Config.NODE_TOOLTIP_ALWAYS_ON, false));

        // danger zone at night
        at(440, mc -> cmd(mc, "time set 18000"));
        at(465, mc -> shot(mc, "11_danger"));
        at(470, mc -> cmd(mc, "time set 1000"));

        // chat
        at(475, mc -> {
            WebsiteVisits.add(LINK);
            WebsiteVisits.add(LINK);
            WebsiteVisits.add(LINK);
            cmd(mc, "tellraw @a \"Welcome back! The desert temple is 300 blocks north.\"");
        });
        at(480, mc -> mc.player.connection.sendChat("anyone want to come with me?"));
        at(484, mc -> cmd(mc, "tellraw @a \"<Alex> sure, wait for me at spawn\""));
        at(488, mc -> cmd(mc, "tellraw @a \"<Steve> bring torches, it gets dark down there\""));
        at(492, mc -> cmd(mc, "tellraw @a \"Map and rules are on " + LINK + "\""));
        at(510, mc -> mc.setScreen(new GuiChat("on my way ", false)));
        at(515, mc -> mouse(mc, 18 + 5 * 12, 173 + 5));
        at(535, mc -> shot(mc, "12_chat_buttons"));
        at(540, mc -> {
            // chat line timestamps: hover a line with the tooltip key held
            KeyRegister.showTooltipKB.setDown(true);
            int h = mc.getWindow().getGuiScaledHeight();
            for (int y = h - 20; y > 0; y--) {
                if (TukChatOverlay.lineAt(40, y) != null) {
                    mouse(mc, 40, y - 4);
                    break;
                }
            }
        });
        at(560, mc -> shot(mc, "13_chat_time"));
        at(565, mc -> {
            KeyRegister.showTooltipKB.setDown(false);
            mc.setScreen(new GuiChatConfirmLink((GuiChat) mc.screen, LINK));
            mouse(mc, 0, 0);
        });
        at(585, mc -> shot(mc, "14_link"));

        // emoji and character pickers
        at(590, mc -> {
            mc.setScreen(new GuiChat("on my way ", false));
            mc.setScreen(new GuiSpecialChars((GuiChat) mc.screen, "on my way ", GuiSpecialChars.Kind.EMOJIS));
        });
        at(595, mc -> {
            for (int n : new int[] { 12, 40, 97 })
                pick(mc, n);
            mouse(mc, 0, 0);
        });
        at(615, mc -> shot(mc, "15_emojis"));
        at(620, mc -> mc.setScreen(new GuiSpecialChars(new GuiChat("", false), "", GuiSpecialChars.Kind.CHARACTERS)));
        at(625, mc -> {
            for (int n : new int[] { 35, 29, 37 })
                pick(mc, n);
            mouse(mc, 0, 0);
        });
        at(645, mc -> shot(mc, "16_chars"));

        // config and colors
        at(650, mc -> {
            mc.setScreen(new GuiNewConfig(null));
            mouse(mc, 0, 0);
        });
        at(670, mc -> shot(mc, "17_config"));
        at(675, mc -> {
            TukMCReference.RED_INNER = 18;
            TukMCReference.GREEN_INNER = 30;
            TukMCReference.BLUE_INNER = 48;
            TukMCReference.RED_OUTER = 90;
            TukMCReference.GREEN_OUTER = 170;
            TukMCReference.BLUE_OUTER = 250;
            TukMC.applyColorSettings();
            mc.setScreen(new GuiColorConfig(null));
            mouse(mc, 0, 0);
        });
        at(695, mc -> shot(mc, "18_colors"));
        at(700, mc -> {
            mc.setScreen(null);
            mc.gui.getChat().clearMessages(false);
        });
        at(725, mc -> shot(mc, "19_hud_colors"));
        at(730, mc -> TukMC.loadColorSettings());
        at(740, Minecraft::stop);
    }

    /** Box colors, from 0-255 components. */
    private static void colors(int ri, int gi, int bi, int ro, int go, int bo) {
        TukMCReference.RED_INNER = ri;
        TukMCReference.GREEN_INNER = gi;
        TukMCReference.BLUE_INNER = bi;
        TukMCReference.RED_OUTER = ro;
        TukMCReference.GREEN_OUTER = go;
        TukMCReference.BLUE_OUTER = bo;
        TukMC.applyColorSettings();
    }

    /**
     * Frames for the README gif: the same still scene each time, only the HUD options, the colors and the held item
     * change. Nothing in the world moves (no clouds, no mobs, no effect that changes the view or the health).
     */
    private static void gif() {
        Map<String, Boolean> saved = new TreeMap<>();
        at(60, mc -> {
            Config.nodes.forEach((label, n) -> saved.put(label, n.isEnabled()));
            mc.options.cloudStatus().set(CloudStatus.OFF);
            mc.options.bobView().set(false);
            // the fancy vignette fades slowly and would change the whole picture between frames
            mc.options.graphicsMode().set(GraphicsStatus.FAST);
            // an enchanted item in hand would shimmer from frame to frame
            cmd(mc, "gamerule naturalRegeneration false",
                    "item replace entity @p hotbar.0 with diamond_sword[damage=600]");
            mc.gui.getChat().clearMessages(false);
            look(mc, yaw, pitch);
            TukMC.deaths = 3;
            TukMC.negativeMobKills = TukHud.getMobKills() - 41;
            TukMC.negativePKills = TukHud.getPlayerKills() - 2;
        });

        String[] effects = { "effect give @p haste 95 0", "effect give @p fire_resistance 300 0",
                "effect give @p resistance 160 1", "effect give @p luck 600 0" };
        String[] boss = { "bossbar add tukmc:readme \"Wither\"", "bossbar set tukmc:readme players @a",
                "bossbar set tukmc:readme value 64", "bossbar set tukmc:readme color purple" };
        String[] scores = { "scoreboard objectives add readme dummy \"Treasure\"",
                "scoreboard objectives setdisplay sidebar readme", "scoreboard players set @p readme 18",
                "scoreboard players set Alex readme 11", "scoreboard players set Steve readme 7" };
        int[] tick = { 90 };
        // each frame starts from the player's own settings and the default colors
        FrameSetup frame = (slot, colors, setup) -> {
            at(tick[0], mc -> {
                saved.forEach((label, on) -> node(label, on));
                if (colors == null)
                    colors(26, 26, 26, 150, 154, 165);
                else
                    colors(colors[0], colors[1], colors[2], colors[3], colors[4], colors[5]);
                cmd(mc, "effect clear @p", "bossbar remove tukmc:readme", "scoreboard objectives remove readme");
                mc.gui.overlayMessageTime = 0;
                mc.player.getInventory().selected = slot;
                setup.accept(mc);
            });
            String name = "gif_" + ((tick[0] - 90) / 25 + 1);
            at(tick[0] + 20, mc -> shot(mc, name));
            tick[0] += 25;
        };

        frame.add(0, null, mc -> {
        });
        frame.add(1, new int[] { 20, 28, 42, 100, 130, 170 }, mc -> {
            node(Config.NODE_ALT_STATUS, true);
            node(Config.NODE_STATBAR, true);
            node(Config.NODE_ITEMS_BACKGROUND, true);
            node(Config.NODE_DEGREES, true);
        });
        frame.add(3, new int[] { 22, 34, 24, 120, 160, 115 }, mc -> {
            node(Config.NODE_BOTTOM_ADORNMENTS, true);
            node(Config.NODE_REPLACE_PING_WITH_TIME, true);
            node(Config.NODE_24HR_CLOCK, true);
            cmd(mc, effects);
        });
        frame.add(0, new int[] { 40, 22, 32, 175, 115, 140 }, mc -> {
            node(Config.NODE_NUMERICAL_DAMAGE_DISPLAY, true);
            cmd(mc, boss);
            cmd(mc, scores);
            mc.gui.setNowPlaying(Component.literal("C418 - cat"));
        });
        frame.add(5, new int[] { 42, 33, 20, 185, 155, 100 }, mc -> {
            node(Config.NODE_CUSTOM_BARS, false);
            node(Config.NODE_LEFT_BAR, false);
            node(Config.NODE_RIGHT_BAR, false);
        });
        frame.add(2, new int[] { 18, 32, 34, 100, 150, 150 }, mc -> {
            node(Config.NODE_TOOLTIP_ALWAYS_ON, true);
            node(Config.NODE_ALT_STATUS, true);
            node(Config.NODE_REPLACE_PING_WITH_TIME, true);
            cmd(mc, scores);
        });
        frame.add(6, new int[] { 30, 26, 40, 140, 125, 170 }, mc -> {
            node(Config.NODE_BOTTOM_ADORNMENTS, true);
            node(Config.NODE_STATBAR, true);
            node(Config.NODE_ITEMS_BACKGROUND, true);
            cmd(mc, effects);
            cmd(mc, boss);
        });
        frame.add(7, new int[] { 32, 32, 26, 150, 150, 120 }, mc -> {
            node(Config.NODE_TOP_BAR, false);
            node(Config.NODE_LEFT_BAR, false);
            node(Config.NODE_NUMERICAL_DAMAGE_DISPLAY, true);
            node(Config.NODE_24HR_CLOCK, true);
            mc.gui.setNowPlaying(Component.literal("C418 - mall"));
        });

        at(tick[0], mc -> {
            saved.forEach((label, on) -> node(label, on));
            TukMC.loadColorSettings();
            mc.options.cloudStatus().set(CloudStatus.FANCY);
            mc.options.bobView().set(true);
            mc.options.graphicsMode().set(GraphicsStatus.FANCY);
        });
        at(tick[0] + 10, Minecraft::stop);
    }

    @FunctionalInterface
    private interface FrameSetup {
        void add(int slot, int[] colors, Consumer<Minecraft> setup);
    }

    private static void tick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (!started && mc.screen instanceof TitleScreen) {
            started = true;
            mc.createWorldOpenFlows().openWorld(WORLD, () -> mc.setScreen(new TitleScreen()));
            return;
        }
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null)
            return;
        t++;
        mc.getToasts().clear();
        Consumer<Minecraft> step = STEPS.get(t);
        if (step != null)
            step.accept(mc);
    }
}
