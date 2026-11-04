package dev.hangel.tukmc.client;

import java.util.function.Consumer;

import dev.hangel.tukmc.TukMC;
import dev.hangel.tukmc.util.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** On NeoForge every vanilla HUD element is a named layer, so each TukHud piece replaces (or wraps) the matching one. */
public final class TukHudLayers {

    private TukHudLayers() {
    }

    public static void register(RegisterGuiLayersEvent event) {
        TukHud hud = TukHud.INSTANCE;
        event.wrapLayer(VanillaGuiLayers.HOTBAR, old -> (g, dt) -> {
            if (hud.visible())
                hud.renderHotbar(g, dt);
        });
        event.wrapLayer(VanillaGuiLayers.SELECTED_ITEM_NAME, old -> (g, dt) -> {
        });
        event.wrapLayer(VanillaGuiLayers.JUMP_METER, old -> (g, dt) -> {
            if (hud.visible() && hud.mc.player.jumpableVehicle() != null)
                hud.renderJumpBar(g);
        });
        event.wrapLayer(VanillaGuiLayers.EXPERIENCE_BAR, old -> customBars(old, (g, dt) -> {
            if (hud.mc.gameMode.hasExperience())
                hud.renderExperience(g);
        }));
        event.wrapLayer(VanillaGuiLayers.EXPERIENCE_LEVEL, old -> customBars(old, (g, dt) -> {
        }));
        event.wrapLayer(VanillaGuiLayers.PLAYER_HEALTH, old -> statusBar(old, hud::renderHealth));
        event.wrapLayer(VanillaGuiLayers.FOOD_LEVEL, old -> statusBar(old, hud::renderFood));
        event.wrapLayer(VanillaGuiLayers.AIR_LEVEL, old -> statusBar(old, hud::renderAir));
        event.wrapLayer(VanillaGuiLayers.ARMOR_LEVEL, old -> customBars(old, (g, dt) -> {
        }));
        event.wrapLayer(VanillaGuiLayers.VEHICLE_HEALTH, old -> customBars(old, (g, dt) -> {
        }));
        event.wrapLayer(VanillaGuiLayers.EFFECTS, old -> (g, dt) -> {
            if (!hud.renderEffects(g))
                old.render(g, dt);
        });
        event.wrapLayer(VanillaGuiLayers.BOSS_OVERLAY, old -> (g, dt) -> {
            if (!hud.renderBossBar(g))
                old.render(g, dt);
        });
        event.wrapLayer(VanillaGuiLayers.OVERLAY_MESSAGE, old -> (g, dt) -> {
            if (!hud.renderOverlayMessage(g, dt))
                old.render(g, dt);
        });
        event.wrapLayer(VanillaGuiLayers.SCOREBOARD_SIDEBAR, old -> (g, dt) -> hud.renderScoreboard(g));
        event.wrapLayer(VanillaGuiLayers.TAB_LIST, old -> (g, dt) -> hud.renderTabList(g));
        event.wrapLayer(VanillaGuiLayers.CHAT, old -> (g, dt) -> {
            if (!hud.renderChat(g))
                old.render(g, dt);
        });

        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, ResourceLocation.fromNamespaceAndPath(TukMC.MODID, "adornments"),
                (g, dt) -> hud.renderAdornments(g));
        event.registerAbove(VanillaGuiLayers.EFFECTS, ResourceLocation.fromNamespaceAndPath(TukMC.MODID, "hud"),
                hud::renderOverlayLayer);
    }

    private static LayeredDraw.Layer customBars(LayeredDraw.Layer old, LayeredDraw.Layer custom) {
        return (g, dt) -> {
            if (!Config.get(Config.NODE_CUSTOM_BARS)) {
                old.render(g, dt);
            } else if (TukHud.INSTANCE.canHurt()) {
                custom.render(g, dt);
            }
        };
    }

    /** Health, food and air: vanilla icons in "plain status" mode, TukMC bars otherwise. */
    private static LayeredDraw.Layer statusBar(LayeredDraw.Layer old, Consumer<GuiGraphics> custom) {
        return (g, dt) -> {
            if (!Config.get(Config.NODE_CUSTOM_BARS) || Config.get(Config.NODE_PLAIN_STATUS)) {
                old.render(g, dt);
            } else if (TukHud.INSTANCE.canHurt()) {
                custom.accept(g);
            }
        };
    }
}
