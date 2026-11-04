package dev.hangel.tukmc.gui;

import dev.hangel.tukmc.TukMC;
import dev.hangel.tukmc.client.TukDraw;
import dev.hangel.tukmc.client.TukHud;
import dev.hangel.tukmc.util.Config;
import dev.hangel.tukmc.util.Config.Node;
import dev.hangel.tukmc.util.FormattingCode;
import dev.hangel.tukmc.util.TukMCReference;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiNewConfig extends Screen {

    private int pageMin;
    private int pageMax;
    private static int pageNumber = 1;
    private final int pageCount = (int) (Math.ceil(Config.getSize() / 6) + 1);

    private final Screen parent;

    public GuiNewConfig(Screen parent) {
        super(Component.translatable("tukmc.config.title"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    protected void init() {
        pageMax = pageNumber * 6;
        pageMin = pageMax - 5;
        int i = 1;
        for (String s : Config.nodekeys) {
            if (i <= pageMax && i >= pageMin) {
                Node node = Config.nodes.get(s);
                addRenderableWidget(new GuiTukButton(i, width / 2 - 125, height / 2 - 70 + (i - pageMin + 1) * 21, 250,
                        19, node.getDisplayName(), b -> {
                            if (node == Config.nodes.get(Config.NODE_SMOOTH_TRANSITION))
                                TukHud.reset = true;
                            node.set(!node.isEnabled());
                            Config.saveNode(node);
                        }));
            }
            ++i;
        }
        addRenderableWidget(new GuiTukButton(101, width / 2 - 125 - 76, height / 2 - 85, 70, 20, I18n.get("tukmc.config.colors"),
                b -> minecraft.setScreen(new GuiColorConfig(this))));
        addRenderableWidget(new GuiTukButton(1341, width / 2 - 125 - 76, height / 2 - 62, 70, 20, I18n.get("tukmc.config.reset_stats"), b -> {
            TukMC.deaths = 0;
            TukMC.negativeMobKills = TukHud.getMobKills();
            TukMC.negativePKills = TukHud.getPlayerKills();
        }));
        addRenderableWidget(new GuiTukButton(1337, width / 2 - 125, height / 2 - 85, 20, 20, "<", b -> {
            if (pageNumber > 1) {
                pageNumber--;
                rebuildWidgets();
            }
        }));
        addRenderableWidget(new GuiTukButton(1338, width / 2 + 105, height / 2 - 85, 20, 20, ">", b -> {
            if (pageNumber < pageCount) {
                pageNumber++;
                rebuildWidgets();
            }
        }));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, FormattingCode.ITALICS + I18n.get("tukmc.config.page", pageNumber, pageCount),
                width / 2, height / 2 - 80, 0xFFFFFF);
        int i = 1;
        for (String s : Config.nodekeys) {
            if (i <= pageMax && i >= pageMin) {
                boolean enabled = Config.nodes.get(s).isEnabled();
                TukDraw.drawOutlinedBox(g, width / 2 + 113, height / 2 - 5 + (i - pageMin + 1) * 21 - 58, 5, 5,
                        enabled ? 0xFF00 : 0xFF0000, TukMCReference.BOX_OUTLINE_COLOR);
            }
            i++;
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // drawDefaultBackground(): the plain dark gradient, without the 1.21 blur
        if (minecraft.level != null) {
            renderTransparentBackground(g);
        } else {
            super.renderBackground(g, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
