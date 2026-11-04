package dev.hangel.tukmc.gui;

import static dev.hangel.tukmc.util.TukMCReference.BOX_INNER_COLOR;
import static dev.hangel.tukmc.util.TukMCReference.BOX_OUTLINE_COLOR;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.lwjgl.glfw.GLFW;

import dev.hangel.tukmc.TukMC;
import dev.hangel.tukmc.client.TukChatOverlay;
import dev.hangel.tukmc.client.TukDraw;
import dev.hangel.tukmc.handlers.KeyRegister;
import dev.hangel.tukmc.platform.Services;
import dev.hangel.tukmc.util.ColorCode;
import dev.hangel.tukmc.util.ColorUtil;
import dev.hangel.tukmc.util.IOUtils;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

/**
 * Port of the original TukMC 1.6.4 GuiChat: input box on top of the chat, the "LNWOCE" button row, link tooltips,
 * chat logs... It still extends the vanilla ChatScreen so command completion and chat signing keep working.
 */
public class GuiChat extends ChatScreen {

    public static final Pattern pattern = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,3})(/\\S*)?$");
    public static final String CHARS = "LNWOCE";
    static int Yoffset = 75;

    /** Command suggestions are anchored to the bottom of the screen by vanilla; we move them up to our input box. */
    private int suggestionShift;

    String username;
    String tooltip = "";
    boolean isBed;

    public GuiChat(String initial, boolean bed) {
        super(initial);
        isBed = bed;
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft.player != null) {
            username = ColorCode.BRIGHT_GREEN + "<" + minecraft.player.getName().getString() + "> ";
        } else {
            username = ColorCode.RED + "<UNKNOWN> ";
        }
        input.setX(font.width(username) + 2);
        input.setY(height - 170);
        input.setWidth(Math.min(360, width - input.getX() - 4));
        input.setHeight(10);
        suggestionShift = (height - 12) - (height - 172);

        for (int i = 0; i < CHARS.length(); i++) {
            String s = "" + CHARS.charAt(i);
            if (s.equalsIgnoreCase("L"))
                s = ColorCode.DARK_AQUA + "L";
            else if (s.equalsIgnoreCase("N"))
                s = ColorCode.GOLD + "N";
            else if (s.equalsIgnoreCase("W"))
                s = ColorCode.RED + "W";
            else if (s.equalsIgnoreCase("C"))
                s = ColorCode.INDIGO + "C";
            else if (s.equalsIgnoreCase("E"))
                s = ColorCode.YELLOW + "E";
            int id = i;
            addRenderableWidget(new GuiTukButton(i, 15 + i * 12, height - 112 - Yoffset, 9, 10, s,
                    b -> actionPerformed(id)));
        }
    }

    public void setBed() {
        isBed = true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // the chat lines themselves are drawn by the HUD chat layer (TukChatOverlay)
        for (Renderable r : renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
        TukDraw.drawDoubleOutlinedBox(g, 2, height - 112 - Yoffset, 10, 10, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
        g.drawString(font, ">", 5, height - 111 - Yoffset, 0x55FF55, true);
        int textWidth = font.width(username) + 9 + font.width(input.getValue());
        if (textWidth >= 428) {
            textWidth = 428;
        }
        TukDraw.drawDoubleOutlinedBox(g, 1, height - 170 - 2, textWidth, 11, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
        g.drawString(font, username, 3, height - 170, 0xFFFFFF, false);
        input.render(g, mouseX, mouseY, partialTick);

        for (var child : children()) {
            if (child instanceof GuiTukButton guibutton && guibutton.isMouseOver()) {
                tooltip = switch (guibutton.id) {
                    case 0 -> I18n.get(TukMC.closeOnFinish ? "tukmc.chat.tooltip.unlock" : "tukmc.chat.tooltip.lock");
                    case 1 -> I18n.get(TukMC.displayNotification ? "tukmc.chat.tooltip.notifications.off"
                            : "tukmc.chat.tooltip.notifications.on");
                    case 2 -> I18n.get("tukmc.chat.tooltip.wipe");
                    case 3 -> I18n.get("tukmc.chat.tooltip.save", ColorCode.RED + I18n.get("tukmc.chat.tooltip.save.shift"));
                    case 4 -> I18n.get("tukmc.chat.tooltip.characters");
                    case 5 -> I18n.get("tukmc.chat.tooltip.emojis");
                    default -> "";
                };
                if (!tooltip.isEmpty()) {
                    drawButtonTooltip(g, guibutton.id);
                }
            }
        }

        if (isBed) {
            String bed = I18n.get("tukmc.chat.bed");
            TukDraw.drawDoubleOutlinedBox(g, 260, height - 80, font.width(bed) + 10, 16, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
            g.drawString(font, bed, 265, height - 76, 0xFFFFFF, true);
        }

        if (KeyRegister.showTooltipKB.isDown() || isKeyDown(Services.PLATFORM.getBoundKey(KeyRegister.showTooltipKB).getValue())) {
            GuiMessage.Line line = TukChatOverlay.lineAt(mouseX, mouseY);
            if (line != null) {
                String time = I18n.get("tukmc.chat.received", TukChatOverlay.getTime(line)) + ColorCode.GREY
                        + TukChatOverlay.getElapsedTime(line);
                int timeWidth = font.width(time);
                g.pose().pushPose();
                g.pose().translate(0, 0, 300);
                TukDraw.drawDoubleOutlinedBox(g, mouseX + 2, mouseY - 16, timeWidth + 4, 12, BOX_INNER_COLOR,
                        BOX_OUTLINE_COLOR);
                g.drawString(font, time, mouseX + 4, mouseY - 14, 0xFFFFFF, true);
                g.pose().popPose();
            }
        }

        g.pose().pushPose();
        g.pose().translate(0.0F, -suggestionShift, 200.0F);
        commandSuggestions.render(g, mouseX, mouseY + suggestionShift);
        g.pose().popPose();

        Style style = TukChatOverlay.styleAt(mouseX, mouseY);
        if (style != null && style.getHoverEvent() != null) {
            g.renderComponentHoverEffect(font, style, mouseX, mouseY);
        }
    }

    private boolean isKeyDown(int key) {
        return key > 0 && GLFW.glfwGetKey(minecraft.getWindow().getWindow(), key) == GLFW.GLFW_PRESS;
    }

    private void drawButtonTooltip(GuiGraphics g, int id) {
        int lightInner = ColorUtil.brighter(ColorUtil.brighter(BOX_INNER_COLOR));
        String[] tokens = tooltip.split(";");
        int length = 12;
        for (String s : tokens) {
            length = Math.max(length, font.width(s));
        }
        g.pose().pushPose();
        g.pose().translate(0, 0, 300);
        if (id + 1 <= CHARS.length()) {
            TukDraw.drawDoubleOutlinedBox(g, 14, height - 114 - tokens.length * 12 - Yoffset, length + 6,
                    tokens.length * 12, lightInner, BOX_OUTLINE_COLOR);
            TukDraw.drawOutlinedBox(g, 15 + id * 12, height - 114 - Yoffset, 9, 1, lightInner, BOX_OUTLINE_COLOR);
            g.pose().pushPose();
            g.pose().scale(0.5F, 0.5F, 1F);
            TukDraw.drawSolidRect(g, 14 + id * 12, height - 115 - Yoffset, 26 + id * 12, height - 114 - Yoffset,
                    lightInner);
            TukDraw.drawSolidRect(g, 15 + id * 12, height - 115 - Yoffset, 24 + id * 12, height - 112 - Yoffset,
                    lightInner);
            g.pose().popPose();
        }
        int i = 0;
        for (String s : tokens) {
            g.drawString(font, s, 18, height - 112 - tokens.length * 12 + i * 12 - Yoffset, 0xFFFFFF, true);
            ++i;
        }
        g.pose().popPose();
    }

    @Override
    public void tick() {
        super.tick();
        if (isBed && minecraft.player != null && !minecraft.player.isSleeping()) {
            minecraft.setScreen(null);
        }
    }

    private void wakeEntity() {
        minecraft.player.connection
                .send(new ServerboundPlayerCommandPacket(minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && minecraft.player.isSleeping()) {
            wakeEntity();
            minecraft.setScreen(null);
            return true;
        }
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && TukMC.closeOnFinish
                && !commandSuggestions.keyPressed(keyCode, scanCode, modifiers)) {
            // "locked" chat: send and stay open (the original reopened the GUI right away)
            handleChatInput(input.getValue(), true);
            if (minecraft.screen == this) {
                input.setValue("");
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollY = Math.max(-1.0, Math.min(1.0, scrollY));
        if (commandSuggestions.mouseScrolled(scrollY)) {
            return true;
        }
        if (hasShiftDown()) {
            scrollY *= 7.0;
        }
        minecraft.gui.getChat().scrollChat((int) scrollY);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (commandSuggestions.mouseClicked((int) mouseX, (int) mouseY + suggestionShift, button)) {
            return true;
        }
        if (button == 0) {
            Style style = TukChatOverlay.styleAt(mouseX, mouseY);
            if (style != null && handleComponentClicked(style)) {
                initial = input.getValue();
                return true;
            }
        }
        for (var child : children()) {
            if (child instanceof GuiTukButton b && b.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return input.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean handleComponentClicked(Style style) {
        ClickEvent clickevent = style == null ? null : style.getClickEvent();
        if (clickevent != null && clickevent.getAction() == ClickEvent.Action.OPEN_URL && !hasShiftDown()
                && minecraft.options.chatLinks().get() && minecraft.options.chatLinksPrompt().get()) {
            initial = input.getValue();
            minecraft.setScreen(new GuiChatConfirmLink(this, clickevent.getValue()));
            return true;
        }
        return super.handleComponentClicked(style);
    }

    protected void actionPerformed(int id) {
        switch (id) {
            case 0 -> TukMC.setCloseOnFinish(!TukMC.closeOnFinish);
            case 1 -> TukMC.setDisplayNotification(!TukMC.displayNotification);
            case 2 -> minecraft.gui.getChat().clearMessages(false);
            case 3 -> {
                if (hasShiftDown())
                    saveChatLog();
            }
            case 4 -> minecraft.setScreen(new GuiSpecialChars(this, input.getValue(), GuiSpecialChars.Kind.CHARACTERS));
            case 5 -> minecraft.setScreen(new GuiSpecialChars(this, input.getValue(), GuiSpecialChars.Kind.EMOJIS));
            default -> {
            }
        }
    }

    private void saveChatLog() {
        File subFolder = new File(IOUtils.getTukMCFolder(), "chatlogs");
        if (!subFolder.exists()) {
            subFolder.mkdirs();
        }
        File log = new File(subFolder, new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(log))) {
            List<GuiMessage.Line> chatLines = minecraft.gui.getChat().trimmedMessages;
            for (int i = chatLines.size() - 1; i >= 0; i--) {
                GuiMessage.Line line = chatLines.get(i);
                String lineString = "[" + TukChatOverlay.getTime(line) + " ("
                        + TukChatOverlay.getMillisOfCreation(line) + ")" + "] "
                        + TukChatOverlay.toPlainString(line.content()) + "\n";
                writer.write(ChatFormatting.stripFormatting(lineString));
            }
            // like vanilla's "Saved screenshot as ...": underlined file name that opens the file when clicked
            Component name = Component.literal(log.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle(
                    style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, log.getAbsolutePath())));
            minecraft.gui.getChat().addMessage(Component.translatable("tukmc.chat.saved", name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getInputText() {
        return input.getValue();
    }
}
