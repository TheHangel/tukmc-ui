package dev.hangel.tukmc.gui;

import dev.hangel.tukmc.util.ColorCode;
import dev.hangel.tukmc.util.WebsiteVisits;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.Component;

public class GuiChatConfirmLink extends ConfirmLinkScreen {

    private final int times;
    /** Top of the button row, which vanilla places under the (possibly wrapped) message. */
    private int buttonsY;

    public GuiChatConfirmLink(GuiChat chatGui, String url) {
        super(open -> {
            Minecraft mc = Minecraft.getInstance();
            if (open) {
                WebsiteVisits.add(url);
                try {
                    Util.getPlatform().openUri(Util.parseAndValidateUntrustedUri(url));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mc.setScreen(chatGui);
        }, Component.translatable("chat.link.confirm"), Component.literal(ColorCode.BRIGHT_GREEN + url), url,
                net.minecraft.network.chat.CommonComponents.GUI_CANCEL, false);
        this.times = WebsiteVisits.get(url);
    }

    @Override
    protected void addButtons(int y) {
        buttonsY = y;
        addRenderableWidget(new GuiTukButton(0, width / 2 - 155, y, 100, 20, yesButton.getString(),
                b -> callback.accept(true)));
        addRenderableWidget(new GuiTukButton(2, width / 2 - 155 + 105, y, 100, 20,
                Component.translatable("chat.copy").getString(), b -> {
                    copyToClipboard();
                    callback.accept(false);
                }));
        addRenderableWidget(new GuiTukButton(1, width / 2 - 155 + 210, y, 100, 20, noButton.getString(),
                b -> callback.accept(false)));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, I18n.get("tukmc.link.extra_info"), width / 2, buttonsY + 30, 0xFFFFFF);
        g.drawCenteredString(font,
                times == 0 ? ColorCode.RED + I18n.get("tukmc.link.never")
                        : ColorCode.BRIGHT_GREEN + I18n.get("tukmc.link.visits", times),
                width / 2, buttonsY + 45, 0xFFFFFF);
    }
}
