package dev.hangel.tukmc.render;

import java.math.BigDecimal;
import java.math.MathContext;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.hangel.tukmc.util.Config;
import dev.hangel.tukmc.util.TukMCReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class MobHealthBars {

    public static final MobHealthBars INSTANCE = new MobHealthBars();

    /** Once an entity is rendered (RenderLivingEvent.Post on NeoForge, LivingEntityRendererMixin on Fabric). */
    public void onRenderLiving(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || !Config.get(Config.NODE_HEALTH_BARS))
            return;
        Entity viewPoint = mc.getCameraEntity();
        if (viewPoint == null || !entity.isAlive())
            return;
        renderHealthBar(entity, viewPoint, poseStack, buffer);
    }

    public void renderHealthBar(LivingEntity entity, Entity viewPoint, PoseStack ps, MultiBufferSource buf) {
        float distance = entity.distanceTo(viewPoint);
        if (distance > 20 || !entity.hasLineOfSight(viewPoint) || entity == viewPoint || entity.hasPassenger(viewPoint))
            return;

        Minecraft mc = Minecraft.getInstance();
        float scale = 0.026666672F;
        float maxHealth = new BigDecimal(entity.getMaxHealth()).round(new MathContext(2)).floatValue();
        float health = new BigDecimal(entity.getHealth()).round(new MathContext(2)).floatValue();

        if (!Config.get(Config.NODE_HEALTHBAR_NO_TEXT))
            renderLabel(ps, buf, entity, viewPoint,
                    String.format("%s/%s (%s", health, maxHealth, maxHealth == 0 ? 0 : (int) (health * 100 / maxHealth))
                            + "%)",
                    entity.getBbHeight() + 1, 20);

        ps.pushPose();
        ps.translate(0, entity.getBbHeight() + 0.7, 0);
        ps.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        ps.scale(scale, -scale, scale);
        Matrix4f m = ps.last().pose();
        VertexConsumer vc = buf.getBuffer(RenderType.textBackgroundSeeThrough());

        // drawDoubleOutlinedBox(-max/2, -1, 3, max, 2): coordinates doubled, no half scaling here.
        // Translucent quads are drawn sorted by distance, so stacked layers could end up in any order (the outline
        // hiding the health): every piece is drawn side by side instead, without overlaps.
        int x = -(int) maxHealth / 2, y = -1, w = (int) maxHealth, h = 2;
        int left = x * 2, top = y * 2, right = (x + w) * 2, bottom = (y + h) * 2;
        frame(vc, m, left - 2, top - 2, right + 2, bottom + 2, TukMCReference.BOX_INNER_COLOR);
        frame(vc, m, left - 1, top - 1, right + 1, bottom + 1, TukMCReference.BOX_OUTLINE_COLOR);
        int gx = -(int) maxHealth + (maxHealth % 2 == 1 ? 1 : 0);
        int filled = health > 0 ? Math.min(right, gx + (int) health * 2) : left;
        if (filled > left) {
            if (gx > left)
                WorldDraw.rect(vc, m, left, top, gx, bottom, 0, TukMCReference.BOX_INNER_COLOR);
            WorldDraw.gradient(vc, m, Math.max(left, gx), top, filled, bottom, 0, 0x44 << 16, 0xFF << 16);
        }
        if (filled < right)
            WorldDraw.rect(vc, m, Math.max(left, filled), top, right, bottom, 0, TukMCReference.BOX_INNER_COLOR);
        ps.popPose();
    }

    /** A one unit wide border inside the given rectangle. */
    private static void frame(VertexConsumer vc, Matrix4f m, int x1, int y1, int x2, int y2, int color) {
        WorldDraw.rect(vc, m, x1, y1, x2, y1 + 1, 0, color);
        WorldDraw.rect(vc, m, x1, y2 - 1, x2, y2, 0, color);
        WorldDraw.rect(vc, m, x1, y1 + 1, x1 + 1, y2 - 1, 0, color);
        WorldDraw.rect(vc, m, x2 - 1, y1 + 1, x2, y2 - 1, 0, color);
    }

    public static void renderLabel(PoseStack ps, MultiBufferSource buf, Entity entity, Entity viewer, String text,
            double yOffset, int range) {
        if (entity.distanceToSqr(viewer) > range * range)
            return;
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        float s = 0.016666668F * 1.6F;
        ps.pushPose();
        ps.translate(0, yOffset, 0);
        ps.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        ps.scale(s, -s, s);
        Matrix4f m = ps.last().pose();
        float x = -font.width(text) / 2F;
        font.drawInBatch(text, x, 0, 553648127, false, m, buf, Font.DisplayMode.SEE_THROUGH, 0x40000000,
                WorldDraw.FULL_BRIGHT);
        font.drawInBatch(text, x, 0, -1, false, m, buf, Font.DisplayMode.NORMAL, 0, WorldDraw.FULL_BRIGHT);
        ps.popPose();
    }
}
