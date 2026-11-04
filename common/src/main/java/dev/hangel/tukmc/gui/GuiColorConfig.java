package dev.hangel.tukmc.gui;

import dev.hangel.tukmc.TukMC;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiColorConfig extends Screen {

    private final Screen parentScreen;
    public boolean autoApply = true;

    public GuiColorConfig(Screen parent) {
        super(Component.translatable("tukmc.colors.title"));
        parentScreen = parent;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        String applystr = I18n.get(autoApply ? "tukmc.colors.autoapply.on" : "tukmc.colors.autoapply.off");
        g.drawString(font, applystr, width / 2 - 172 + font.width(applystr) / 2, height / 2 + 50, 14737632, true);
        if (autoApply) {
            TukMC.applyColorSettings();
        }
    }

    @Override
    protected void init() {
        addRenderableWidget(new GuiMenuSlider(0, width / 2 - 151, height / 2 - 22));
        addRenderableWidget(new GuiMenuSlider(1, width / 2 - 151, height / 2));
        addRenderableWidget(new GuiMenuSlider(2, width / 2 - 151, height / 2 + 22));
        addRenderableWidget(new GuiMenuSlider(3, width / 2 + 1, height / 2 - 22));
        addRenderableWidget(new GuiMenuSlider(4, width / 2 + 1, height / 2));
        addRenderableWidget(new GuiMenuSlider(5, width / 2 + 1, height / 2 + 22));
        addRenderableWidget(new GuiTukButton(10, width / 2 - 151, height / 2 - 44, 74, 20, I18n.get("tukmc.colors.save"), b -> {
            TukMC.saveColorSettings();
            minecraft.setScreen(parentScreen);
        }));
        addRenderableWidget(new GuiTukButton(12, width / 2 - 75, height / 2 - 44, 74, 20, I18n.get("tukmc.colors.apply"),
                b -> TukMC.saveColorSettings()));
        addRenderableWidget(new GuiTukButton(11, width / 2 + 1, height / 2 - 44, 150, 20, I18n.get("tukmc.colors.defaults"), b -> {
            TukMC.defaultColorSettings();
            minecraft.setScreen(parentScreen);
        }));
        addRenderableWidget(new GuiTukButton(13, width / 2 - 151, height / 2 + 44, 150, 20, "",
                b -> autoApply = !autoApply));
        addRenderableWidget(new GuiTukButton(14, width / 2 + 1, height / 2 + 44, 150, 20, I18n.get("tukmc.colors.exit"), b -> onClose()));
    }

    @Override
    public void onClose() {
        if (autoApply)
            TukMC.saveColorSettings();
        else
            TukMC.loadColorSettings();
        minecraft.setScreen(parentScreen);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // 1.6.4 screens drew straight over the world, no blur/dim
    }
}
