package dev.hangel.tukmc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.hangel.tukmc.client.TukHud;
import dev.hangel.tukmc.client.TukHudFabric;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/** Stands in for the NeoForge GUI layer replacements: each vanilla HUD piece hands over to TukHud. */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "renderCameraOverlays", at = @At("RETURN"))
    private void tukmc$adornments(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        TukHud.INSTANCE.renderAdornments(g);
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    private void tukmc$hotbar(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        TukHudFabric.renderHotbarAndDecorations((Gui) (Object) this, g, dt);
        ci.cancel();
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void tukmc$armor(GuiGraphics g, Player player, int i, int j, int k, int l, CallbackInfo ci) {
        if (TukHudFabric.hideArmorRow())
            ci.cancel();
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void tukmc$xpLevel(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        if (TukHudFabric.hideExperienceLevel())
            ci.cancel();
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void tukmc$effects(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        if (TukHud.INSTANCE.renderEffects(g)) {
            TukHud.INSTANCE.renderOverlayLayer(g, dt);
            ci.cancel();
        }
    }

    @Inject(method = "renderEffects", at = @At("RETURN"))
    private void tukmc$overlay(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        TukHud.INSTANCE.renderOverlayLayer(g, dt);
    }

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void tukmc$record(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        if (TukHud.INSTANCE.renderOverlayMessage(g, dt))
            ci.cancel();
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void tukmc$sidebar(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        TukHud.INSTANCE.renderScoreboard(g);
        ci.cancel();
    }

    @Inject(method = "renderTabList", at = @At("HEAD"), cancellable = true)
    private void tukmc$tabList(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        TukHud.INSTANCE.renderTabList(g);
        ci.cancel();
    }

    @Inject(method = "renderChat", at = @At("HEAD"), cancellable = true)
    private void tukmc$chat(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        if (TukHud.INSTANCE.renderChat(g))
            ci.cancel();
    }
}
