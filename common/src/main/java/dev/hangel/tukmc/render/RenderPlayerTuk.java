package dev.hangel.tukmc.render;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.hangel.tukmc.util.Config;
import dev.hangel.tukmc.util.TukMCReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** The TukMC nameplate: boxed name, distance and ping bars above other players. */
public class RenderPlayerTuk {

    public static final RenderPlayerTuk INSTANCE = new RenderPlayerTuk();

    /** Before vanilla draws a name tag. @return true if TukMC drew it instead (vanilla is skipped). */
    public boolean onNameTag(Entity entity, Component content, PoseStack poseStack, MultiBufferSource buffer, int light) {
        Minecraft mc = Minecraft.getInstance();
        if (Config.get(Config.NODE_DEFAULT_NAMEPLATE) || !(entity instanceof Player player) || player == mc.player)
            return false;
        if (player.isInvisibleTo(mc.player) || player.distanceToSqr(mc.player) > 64 * 64)
            return false;
        if (!mc.options.hideGui)
            renderLivingLabel(player, content.getString(), poseStack, buffer, light);
        return true;
    }

    private void renderLivingLabel(Player entity, String par2Str, PoseStack ps, MultiBufferSource buf, int light) {
        Minecraft mc = Minecraft.getInstance();
        boolean name = false;
        float var9 = 0.016666668F * 1.6F;
        String var13 = par2Str;
        if (par2Str.equalsIgnoreCase(entity.getName().getString())) {
            var13 = par2Str + " - " + Math.round(entity.distanceTo(mc.player)) + "m";
            name = true;
        }

        PlayerInfo var46 = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(entity.getUUID());
        String ping = "icon/ping_1";
        if (var46 != null && name) {
            int t = var46.getLatency();
            ping = t < 0 ? "icon/ping_unknown" : t < 150 ? "icon/ping_5" : t < 300 ? "icon/ping_4"
                    : t < 600 ? "icon/ping_3" : t < 1000 ? "icon/ping_2" : "icon/ping_1";
        }

        boolean seeThrough = !entity.isDiscrete();
        boolean sleeping = entity.isSleeping();
        Font font = mc.font;
        ps.pushPose();
        ps.translate(0, entity.getBbHeight() + 0.45, 0);
        ps.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        ps.scale(var9, -var9, var9);
        Matrix4f m = ps.last().pose();

        int var16 = font.width(var13) / 2;
        int bx = -var16 - 7, by = sleeping ? 50 : -1, bw = var16 * 2 + 18, bh = 10;
        VertexConsumer vc = buf.getBuffer(seeThrough ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
        // drawDoubleOutlinedBox at half-pixel outlines, pushed slightly behind the text like vanilla's backdrop
        WorldDraw.rect(vc, m, bx - 1, by - 1, bx + bw + 1, by + bh + 1, 0.03F, TukMCReference.BOX_INNER_COLOR);
        WorldDraw.rect(vc, m, bx - 0.5F, by - 0.5F, bx + bw + 0.5F, by + bh + 0.5F, 0.02F, TukMCReference.BOX_OUTLINE_COLOR);
        WorldDraw.rect(vc, m, bx, by, bx + bw, by + bh, 0.01F, TukMCReference.BOX_INNER_COLOR);

        font.drawInBatch(var13, -font.width(var13) / 2F - 6, sleeping ? 51 : 0, 0xFFFFFF, true, m, buf,
                seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, WorldDraw.FULL_BRIGHT);

        if (name) {
            TextureAtlasSprite sprite = mc.getGuiSprites().getSprite(ResourceLocation.withDefaultNamespace(ping));
            VertexConsumer tex = buf.getBuffer(seeThrough ? RenderType.textSeeThrough(sprite.atlasLocation())
                    : RenderType.text(sprite.atlasLocation()));
            float x1 = var16 - 1, y1 = sleeping ? 51 : 0, x2 = x1 + 10, y2 = y1 + 8;
            tex.addVertex(m, x1, y2, 0).setColor(-1).setUv(sprite.getU0(), sprite.getV1()).setLight(WorldDraw.FULL_BRIGHT);
            tex.addVertex(m, x2, y2, 0).setColor(-1).setUv(sprite.getU1(), sprite.getV1()).setLight(WorldDraw.FULL_BRIGHT);
            tex.addVertex(m, x2, y1, 0).setColor(-1).setUv(sprite.getU1(), sprite.getV0()).setLight(WorldDraw.FULL_BRIGHT);
            tex.addVertex(m, x1, y1, 0).setColor(-1).setUv(sprite.getU0(), sprite.getV0()).setLight(WorldDraw.FULL_BRIGHT);
        }
        ps.popPose();
    }
}
