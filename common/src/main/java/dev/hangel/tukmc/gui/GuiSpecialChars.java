package dev.hangel.tukmc.gui;

import java.util.ArrayList;
import java.util.List;

import dev.hangel.tukmc.client.TukDraw;
import dev.hangel.tukmc.util.EmojiList;
import dev.hangel.tukmc.util.FormattingCode;
import dev.hangel.tukmc.util.TukMCReference;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Character picker (C button) and emoji picker (E button): same screen, different list. */
public class GuiSpecialChars extends Screen {

    public enum Kind {
        CHARACTERS("tukmc.picker.characters", TukMCReference.otherEmotes),
        EMOJIS("tukmc.picker.emojis", EmojiList.EMOJIS);

        /** Translation key prefix: .title and .to_add */
        private final String key;
        /** One entry per code point: emoji outside the BMP take two Java chars. */
        private final String[] entries;
        private int pageNumber = 1;

        Kind(String key, String all) {
            this.key = key;
            this.entries = all.codePoints().mapToObj(Character::toString).toArray(String[]::new);
        }
    }

    private static final int PER_PAGE = 14 * 8;

    private final Kind kind;
    private final int pageCount;
    private final List<String> addedChars = new ArrayList<>();
    public final GuiChat parentScreen;
    private final String inputFieldText;

    public GuiSpecialChars(GuiChat guiChat, String string, Kind kind) {
        super(Component.translatable(kind.key + ".title"));
        inputFieldText = string;
        parentScreen = guiChat;
        this.kind = kind;
        pageCount = (kind.entries.length + PER_PAGE - 1) / PER_PAGE;
    }

    @Override
    protected void init() {
        int n = (kind.pageNumber - 1) * PER_PAGE;
        for (int i = 1; i < 15; i++) {
            for (int j = 1; j < 9; j++) {
                if (n < kind.entries.length) {
                    String str = kind.entries[n];
                    addRenderableWidget(new GuiTukButton(n, width / 2 - 135 + 17 * i, height / 2 - 75 + 17 * j, 15, 15,
                            str, b -> {
                                if (addedChars.size() <= 11) {
                                    addedChars.add(str);
                                }
                            }));
                    n++;
                }
            }
        }
        addRenderableWidget(new GuiTukButton(14096, width / 2 - 120, height / 2 - 84, 15, 16, "<", b -> {
            kind.pageNumber = kind.pageNumber > 1 ? kind.pageNumber - 1 : pageCount;
            rebuildWidgets();
        }));
        addRenderableWidget(new GuiTukButton(14097, width / 2 + 105, height / 2 - 84, 15, 16, ">", b -> {
            kind.pageNumber = kind.pageNumber < pageCount ? kind.pageNumber + 1 : 1;
            rebuildWidgets();
        }));
        addRenderableWidget(new GuiTukButton(14098, width / 2 + 107, height / 2 - 100, 11, 11, "x", b -> onClose()));
        addRenderableWidget(new GuiTukButton(14099, width / 2 + 127, height / 2 + 15, 75, 20, I18n.get("tukmc.picker.backspace"), b -> {
            if (!addedChars.isEmpty()) {
                addedChars.remove(addedChars.size() - 1);
            }
        }));
        addRenderableWidget(new GuiTukButton(14100, width / 2 + 127, height / 2 - 15, 75, 20, I18n.get("tukmc.picker.insert"), b -> {
            parentScreen.initial = inputFieldText + String.join("", addedChars);
            minecraft.setScreen(parentScreen);
        }));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        String charStr = I18n.get(kind.key + ".to_add",
                addedChars.isEmpty() ? I18n.get("tukmc.picker.none") : String.join("", addedChars));
        TukDraw.drawDoubleOutlinedBox(g, width / 2 - 100, height / 2 - 86, 200, 20, TukMCReference.BOX_INNER_COLOR,
                TukMCReference.BOX_OUTLINE_COLOR);
        TukDraw.drawDoubleOutlinedBox(g, width / 2 - 120, height / 2 + 79, 240, 12, TukMCReference.BOX_INNER_COLOR,
                TukMCReference.BOX_OUTLINE_COLOR);
        g.drawCenteredString(font, FormattingCode.ITALICS
                + I18n.get("tukmc.picker.page", I18n.get(kind.key + ".title"), kind.pageNumber, pageCount), width / 2, height / 2 - 80, 0xFFFFFF);
        g.drawCenteredString(font, charStr, width / 2, height / 2 + 81, 0xFFFFFF);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void onClose() {
        parentScreen.initial = inputFieldText;
        minecraft.setScreen(parentScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
