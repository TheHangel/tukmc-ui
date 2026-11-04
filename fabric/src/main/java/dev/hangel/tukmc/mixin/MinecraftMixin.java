package dev.hangel.tukmc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import dev.hangel.tukmc.handlers.TickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    /** Equivalent of NeoForge's ScreenEvent.Opening: the vanilla chat screen is swapped for TukMC's as it opens. */
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen tukmc$swapChat(Screen screen) {
        return TickHandler.onScreenOpening(screen);
    }
}
