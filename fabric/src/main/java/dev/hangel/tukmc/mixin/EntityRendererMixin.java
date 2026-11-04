package dev.hangel.tukmc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.hangel.tukmc.render.RenderPlayerTuk;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
    private void tukmc$nameplate(Entity entity, Component content, PoseStack poseStack, MultiBufferSource buffer,
            int light, float partialTick, CallbackInfo ci) {
        if (RenderPlayerTuk.INSTANCE.onNameTag(entity, content, poseStack, buffer, light))
            ci.cancel();
    }
}
