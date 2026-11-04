package dev.hangel.tukmc.client;

import dev.hangel.tukmc.util.Config;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Fabric 1.21.1 has no GUI layer API: GuiMixin intercepts each vanilla Gui render method instead, applying the same
 * replace/fallback rules as the NeoForge layers (TukHudLayers).
 */
public final class TukHudFabric {

    private TukHudFabric() {
    }

    /** Replaces Gui#renderHotbarAndDecorations (hotbar, jump meter, xp bar, health/armor/food/air, vehicle, item name). */
    public static void renderHotbarAndDecorations(Gui gui, GuiGraphics g, DeltaTracker dt) {
        TukHud hud = TukHud.INSTANCE;
        if (!hud.visible())
            return;
        hud.renderHotbar(g, dt);

        boolean custom = Config.get(Config.NODE_CUSTOM_BARS);
        if (hud.mc.player.jumpableVehicle() != null) {
            hud.renderJumpBar(g);
        }
        if (!custom) {
            if (hud.mc.player.jumpableVehicle() == null && gui.isExperienceBarVisible())
                gui.renderExperienceBar(g, g.guiWidth() / 2 - 91);
        } else if (hud.canHurt() && hud.mc.gameMode.hasExperience()) {
            hud.renderExperience(g);
        }

        if (hud.canHurt()) {
            if (!custom || Config.get(Config.NODE_PLAIN_STATUS)) {
                // vanilla hearts/food/air; GuiMixin hides the armor row while custom bars are on
                gui.renderPlayerHealth(g);
            } else {
                hud.renderHealth(g);
                hud.renderFood(g);
                hud.renderAir(g);
            }
        }
        if (!custom) {
            gui.renderVehicleHealth(g);
        }
        // selected item name: never shown (TukMC has its own tooltip)
        if (hud.mc.player.isSpectator()) {
            gui.getSpectatorGui().renderTooltip(g);
        }
    }

    public static boolean hideExperienceLevel() {
        return Config.get(Config.NODE_CUSTOM_BARS);
    }

    public static boolean hideArmorRow() {
        return Config.get(Config.NODE_CUSTOM_BARS);
    }
}
