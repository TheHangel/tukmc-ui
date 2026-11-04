package dev.hangel.tukmc.render;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

/** Flat colored quads facing the camera, for the in-world TukMC boxes. */
final class WorldDraw {

    static final int FULL_BRIGHT = 0xF000F0;

    private WorldDraw() {
    }

    static void rect(VertexConsumer vc, Matrix4f m, float x1, float y1, float x2, float y2, float z, int color) {
        float r = (color >> 16 & 0xFF) / 255F, g = (color >> 8 & 0xFF) / 255F, b = (color & 0xFF) / 255F;
        float a = (color >>> 24) == 0 ? 1F : (color >>> 24) / 255F;
        vc.addVertex(m, x1, y2, z).setColor(r, g, b, a).setLight(FULL_BRIGHT);
        vc.addVertex(m, x2, y2, z).setColor(r, g, b, a).setLight(FULL_BRIGHT);
        vc.addVertex(m, x2, y1, z).setColor(r, g, b, a).setLight(FULL_BRIGHT);
        vc.addVertex(m, x1, y1, z).setColor(r, g, b, a).setLight(FULL_BRIGHT);
    }

    /** color1 at the bottom, color2 at the top. */
    static void gradient(VertexConsumer vc, Matrix4f m, float x1, float y1, float x2, float y2, float z, int color1,
            int color2) {
        float r1 = (color1 >> 16 & 0xFF) / 255F, g1 = (color1 >> 8 & 0xFF) / 255F, b1 = (color1 & 0xFF) / 255F;
        float r2 = (color2 >> 16 & 0xFF) / 255F, g2 = (color2 >> 8 & 0xFF) / 255F, b2 = (color2 & 0xFF) / 255F;
        vc.addVertex(m, x1, y2, z).setColor(r1, g1, b1, 1F).setLight(FULL_BRIGHT);
        vc.addVertex(m, x2, y2, z).setColor(r1, g1, b1, 1F).setLight(FULL_BRIGHT);
        vc.addVertex(m, x2, y1, z).setColor(r2, g2, b2, 1F).setLight(FULL_BRIGHT);
        vc.addVertex(m, x1, y1, z).setColor(r2, g2, b2, 1F).setLight(FULL_BRIGHT);
    }
}
