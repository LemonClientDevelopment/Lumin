package com.github.lumin.gui.clickgui.panel.views;

import com.github.lumin.graphics.renderers.RoundRectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.graphics.text.StaticFontLoader;
import com.github.lumin.gui.IComponent.RendererSet;
import com.github.lumin.modules.Module;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.utils.render.MouseUtils;
import com.github.lumin.utils.render.animation.Animation;
import com.github.lumin.utils.render.animation.Easing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleListView {

    // 16:9
    private static final float CARD_ASPECT_WIDTH = 16.0f;
    private static final float CARD_ASPECT_HEIGHT = 9.0f;

    private final Minecraft mc;
    private final RoundRectRenderer listRoundRect = new RoundRectRenderer();
    private final TextRenderer listFont = new TextRenderer();

    private final List<ModuleCard> moduleCards = new ArrayList<>();

    private String searchText = "";
    private boolean searchFocused = false;

    private float scrollOffset = 0.0f;
    private float scrollTarget = 0.0f;
    private float maxScroll = 0.0f;

    private boolean draggingScrollbar = false;
    private float scrollbarDragStartMouseY = 0.0f;
    private float scrollbarDragStartScroll = 0.0f;

    private float lastSearchBoxX, lastSearchBoxY, lastSearchBoxW, lastSearchBoxH;
    private float lastListX, lastListY, lastListW, lastListH;
    private float lastScrollbarX, lastScrollbarY, lastScrollbarW, lastScrollbarH;
    private float lastThumbY, lastThumbH;

    private Module requestedSettingsModule = null;

    public ModuleListView(Minecraft mc) {
        this.mc = mc;
    }

    public void setModules(List<Module> modules) {
        moduleCards.clear();
        for (Module module : modules) {
            moduleCards.add(new ModuleCard(module));
        }
        searchText = "";
        searchFocused = false;
        scrollOffset = 0.0f;
        scrollTarget = 0.0f;
        maxScroll = 0.0f;
        draggingScrollbar = false;
        requestedSettingsModule = null;
    }

    public Module consumeRequestedSettingsModule() {
        Module m = requestedSettingsModule;
        requestedSettingsModule = null;
        return m;
    }

    public void render(RendererSet set, float x, float y, float width, float height, int mouseX, int mouseY, float deltaTicks) {
        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();

        float panelWidth = width * guiScale;
        float panelHeight = height * guiScale;

        float padding = 8 * guiScale;
        float spacing = 4 * guiScale;

        float searchHeight = 24 * guiScale;
        float availableWidth = panelWidth - padding * 2 - spacing;
        float iconBoxWidth = availableWidth * 0.1f;
        float searchBoxWidth = availableWidth * 0.9f;

        float iconBoxX = x + padding;
        float searchBoxX = iconBoxX + iconBoxWidth + spacing;
        float boxY = y + padding;

        boolean iconBoxHovered = MouseUtils.isHovering(iconBoxX, boxY, iconBoxWidth, searchHeight, mouseX, mouseY);
        Color iconBoxColor = iconBoxHovered ? new Color(40, 40, 40, 200) : new Color(30, 30, 30, 200);
        set.bottomRoundRect().addRoundRect(iconBoxX, boxY, iconBoxWidth, searchHeight, 8f * guiScale, iconBoxColor);

        String returnIcon = "\uF00D";
        float iconScale = guiScale * 1.2f;
        float iconW = set.font().getWidth(returnIcon, iconScale, StaticFontLoader.ICONS);
        float iconH = set.font().getHeight(iconScale, StaticFontLoader.ICONS);
        float iconX = iconBoxX + (iconBoxWidth - iconW) / 2f;
        float iconY = boxY + (searchHeight - iconH) / 2f - guiScale;
        set.font().addText(returnIcon, iconX, iconY, iconScale, new Color(200, 200, 200), StaticFontLoader.ICONS);

        boolean searchHovered = MouseUtils.isHovering(searchBoxX, boxY, searchBoxWidth, searchHeight, mouseX, mouseY);
        Color searchColor = searchFocused ? new Color(50, 50, 50, 200) : (searchHovered ? new Color(40, 40, 40, 200) : new Color(30, 30, 30, 200));
        set.bottomRoundRect().addRoundRect(searchBoxX, boxY, searchBoxWidth, searchHeight, 8f * guiScale, searchColor);

        String displayText = searchText.isEmpty() && !searchFocused ? InterFace.isEnglish() ? "Search..." : "搜索..." : searchText;
        if (searchFocused && (System.currentTimeMillis() % 1000 > 500)) {
            displayText += "_";
        }

        Color textColor = searchText.isEmpty() && !searchFocused ? Color.GRAY : Color.WHITE;
        set.font().addText(displayText, searchBoxX + 6 * guiScale, boxY + searchHeight / 2 - 7 * guiScale, guiScale * 0.9f, textColor);

        lastSearchBoxX = searchBoxX;
        lastSearchBoxY = boxY;
        lastSearchBoxW = searchBoxWidth;
        lastSearchBoxH = searchHeight;

        float listStartY = boxY + searchHeight + padding;
        float listBottom = y + panelHeight - padding;
        float listH = Math.max(0.0f, listBottom - listStartY);

        float scrollbarW = 4.0f * guiScale;
        float scrollbarGap = 4.0f * guiScale;
        float listAreaX = x + padding;
        float listAreaW = Math.max(0.0f, panelWidth - padding * 2 - scrollbarGap - scrollbarW);

        lastListX = listAreaX;
        lastListY = listStartY;
        lastListW = listAreaW;
        lastListH = listH;

        float scrollbarX = listAreaX + listAreaW + scrollbarGap;

        lastScrollbarX = scrollbarX;
        lastScrollbarY = listStartY;
        lastScrollbarW = scrollbarW;
        lastScrollbarH = listH;

        List<ModuleCard> visibleCards = new ArrayList<>();
        for (ModuleCard card : moduleCards) {
            if (!searchText.isEmpty() && !card.module.getName().toLowerCase().startsWith(searchText.toLowerCase())) {
                card.width = 0;
                card.height = 0;
                continue;
            }
            visibleCards.add(card);
        }

        if (visibleCards.isEmpty() || listH <= 0.0f || listAreaW <= 0.0f) {
            maxScroll = 0.0f;
            scrollOffset = 0.0f;
            scrollTarget = 0.0f;
            draggingScrollbar = false;
            lastThumbY = 0.0f;
            lastThumbH = 0.0f;
            return;
        }

        float itemGap = 8 * guiScale;
        float minCardWidth = 120 * guiScale;
        int columns = Math.max(3, (int) Math.floor((listAreaW + itemGap) / (minCardWidth + itemGap)));

        float cardWidth = (listAreaW - itemGap * (columns - 1)) / columns;
        float cardHeight = cardWidth * (CARD_ASPECT_HEIGHT / CARD_ASPECT_WIDTH);

        int totalRows = (int) Math.ceil(visibleCards.size() / (double) columns);
        float contentH = totalRows <= 0 ? 0.0f : totalRows * cardHeight + Math.max(0, totalRows - 1) * itemGap;

        maxScroll = Math.max(0.0f, contentH - listH);
        scrollTarget = Math.max(0.0f, Math.min(scrollTarget, maxScroll));
        scrollOffset = scrollOffset + (scrollTarget - scrollOffset) * 0.35f;
        scrollOffset = Math.max(0.0f, Math.min(scrollOffset, maxScroll));

        float thumbH = maxScroll <= 0.0f ? listH : Math.max(12.0f * guiScale, listH * (listH / contentH));
        float thumbTravel = Math.max(0.0f, listH - thumbH);
        float thumbY = maxScroll <= 0.0f ? listStartY : listStartY + (scrollOffset / maxScroll) * thumbTravel;

        lastThumbY = thumbY;
        lastThumbH = thumbH;

        if (draggingScrollbar && maxScroll > 0.0f && thumbTravel > 0.0f) {
            float mouseDelta = mouseY - scrollbarDragStartMouseY;
            float scrollDelta = (mouseDelta / thumbTravel) * maxScroll;
            scrollTarget = Math.max(0.0f, Math.min(scrollbarDragStartScroll + scrollDelta, maxScroll));
        }

        float pxScale = (float) mc.getWindow().getGuiScale();
        int fbW = mc.getWindow().getWidth();
        int fbH = mc.getWindow().getHeight();
        int guiH = mc.getWindow().getGuiScaledHeight();

        int scX = (int) Math.floor(listAreaX * pxScale);
        int scY = (int) Math.floor((guiH - (listStartY + listH)) * pxScale);
        int scW = (int) Math.ceil(listAreaW * pxScale);
        int scH = (int) Math.ceil(listH * pxScale);

        scX = Math.max(0, Math.min(scX, fbW));
        scY = Math.max(0, Math.min(scY, fbH));
        scW = Math.max(0, Math.min(scW, fbW - scX));
        scH = Math.max(0, Math.min(scH, fbH - scY));

        RendererSet listSet = new RendererSet(listRoundRect, set.topRoundRect(), set.texture(), listFont, null, null, null, null);
        listRoundRect.setScissor(scX, scY, scW, scH);
        listFont.setScissor(scX, scY, scW, scH);

        int visibleIndex = 0;
        for (ModuleCard card : visibleCards) {
            int row = visibleIndex / columns;
            int col = visibleIndex % columns;

            float cardX = listAreaX + col * (cardWidth + itemGap);
            float cardY = listStartY + row * (cardHeight + itemGap) - scrollOffset;

            card.x = cardX;
            card.y = cardY;
            card.width = cardWidth;
            card.height = cardHeight;

            if (cardY + cardHeight < listStartY || cardY > listBottom) {
                visibleIndex++;
                continue;
            }

            card.render(listSet, mouseX, mouseY, guiScale);
            visibleIndex++;
        }

        listRoundRect.drawAndClear();
        listFont.drawAndClear();
        listRoundRect.clearScissor();
        listFont.clearScissor();

        if (maxScroll > 0.0f) {
            boolean scrollbarHovered = MouseUtils.isHovering(scrollbarX, listStartY, scrollbarW, listH, mouseX, mouseY);
            boolean thumbHovered = MouseUtils.isHovering(scrollbarX, thumbY, scrollbarW, thumbH, mouseX, mouseY);

            Color trackColor = scrollbarHovered ? new Color(255, 255, 255, 28) : new Color(255, 255, 255, 18);
            Color thumbColor = draggingScrollbar ? new Color(255, 255, 255, 90) : (thumbHovered ? new Color(255, 255, 255, 75) : new Color(255, 255, 255, 55));

            set.bottomRoundRect().addRoundRect(scrollbarX, listStartY, scrollbarW, listH, scrollbarW / 2.0f, trackColor);
            set.bottomRoundRect().addRoundRect(scrollbarX, thumbY, scrollbarW, thumbH, scrollbarW / 2.0f, thumbColor);
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean focused, float x, float y, float width, float height) {
        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float panelWidth = width * guiScale;
        float panelHeight = height * guiScale;

        if (!MouseUtils.isHovering(x, y, panelWidth, panelHeight, event.x(), event.y())) {
            return false;
        }

        if (MouseUtils.isHovering(lastSearchBoxX, lastSearchBoxY, lastSearchBoxW, lastSearchBoxH, event.x(), event.y())) {
            if (event.button() == 1) {
                searchText = "";
                scrollTarget = 0.0f;
            }
            searchFocused = true;
            return true;
        }

        searchFocused = false;

        if (event.button() == 0 && maxScroll > 0.0f && MouseUtils.isHovering(lastScrollbarX, lastScrollbarY, lastScrollbarW, lastScrollbarH, event.x(), event.y())) {
            float thumbTravel = Math.max(0.0f, lastScrollbarH - lastThumbH);
            if (thumbTravel > 0.0f) {
                if (MouseUtils.isHovering(lastScrollbarX, lastThumbY, lastScrollbarW, lastThumbH, event.x(), event.y())) {
                    draggingScrollbar = true;
                    scrollbarDragStartMouseY = (float) event.y();
                    scrollbarDragStartScroll = scrollTarget;
                    return true;
                }
                float clickY = (float) event.y();
                float ratio = (clickY - lastScrollbarY - lastThumbH / 2.0f) / thumbTravel;
                ratio = Math.max(0.0f, Math.min(1.0f, ratio));
                scrollTarget = ratio * maxScroll;
                draggingScrollbar = true;
                scrollbarDragStartMouseY = (float) event.y();
                scrollbarDragStartScroll = scrollTarget;
                return true;
            }
        }

        if (event.button() == 0 && !moduleCards.isEmpty()) {
            for (ModuleCard card : moduleCards) {
                if (card.width <= 0 || card.height <= 0) continue;
                if (MouseUtils.isHovering(card.x, card.y, card.width, card.height, event.x(), event.y())) {
                    card.module.toggle();
                    return true;
                }
            }
        }

        if (event.button() == 1 && !moduleCards.isEmpty()) {
            for (ModuleCard card : moduleCards) {
                if (card.width <= 0 || card.height <= 0) continue;
                if (MouseUtils.isHovering(card.x, card.y, card.width, card.height, event.x(), event.y())) {
                    requestedSettingsModule = card.module;
                    draggingScrollbar = false;
                    return true;
                }
            }
        }

        return true;
    }

    public boolean mouseReleased(MouseButtonEvent event, float x, float y, float width, float height) {
        draggingScrollbar = false;
        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float panelWidth = width * guiScale;
        float panelHeight = height * guiScale;
        return MouseUtils.isHovering(x, y, panelWidth, panelHeight, event.x(), event.y());
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (maxScroll <= 0.0f) return false;
        if (!MouseUtils.isHovering(lastListX, lastListY, lastListW + lastScrollbarW, lastListH, mouseX, mouseY))
            return false;
        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float step = 24.0f * guiScale;
        scrollTarget = Math.max(0.0f, Math.min(scrollTarget - (float) scrollY * step, maxScroll));
        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        if (searchFocused) {
            if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchText.isEmpty()) {
                    searchText = searchText.substring(0, searchText.length() - 1);
                    scrollTarget = 0.0f;
                }
                return true;
            }
            if (event.key() == GLFW.GLFW_KEY_ESCAPE || event.key() == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
        }
        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        if (!searchFocused) return false;
        String str = Character.toString(event.codepoint());
        searchText += str;
        scrollTarget = 0.0f;
        return true;
    }

    public void clickOutside() {
        searchFocused = false;
        draggingScrollbar = false;
    }

    private static final class ModuleCard {
        float x;
        float y;
        float width;
        float height;

        final Module module;
        private final Animation hoverAnimation = new Animation(Easing.EASE_OUT_QUAD, 120L);
        private final Animation enabledAnimation = new Animation(Easing.EASE_OUT_QUAD, 160L);

        private ModuleCard(Module module) {
            this.module = module;
            enabledAnimation.setStartValue(module.isEnabled() ? 1.0f : 0.0f);
        }

        private void render(RendererSet set, int mouseX, int mouseY, float guiScale) {
            if (width <= 0 || height <= 0) return;

            boolean hovered = MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);

            hoverAnimation.run(hovered ? 1.0f : 0.0f);
            enabledAnimation.run(module.isEnabled() ? 1.0f : 0.0f);
            float ht = clamp01(hoverAnimation.getValue());
            float et = clamp01(enabledAnimation.getValue());

            Color offColor = new Color(40, 40, 40, 130);
            Color onColor = new Color(148, 148, 148, 130);
            Color base = lerpColor(offColor, onColor, et);
            int alphaBump = (int) (24.0f * ht);
            Color bgColor = new Color(base.getRed(), base.getGreen(), base.getBlue(), clamp255(base.getAlpha() + alphaBump));

            float scale = 1.0f + 0.02f * ht;
            float rw = width * scale;
            float rh = height * scale;
            float rx = x - (rw - width) / 2.0f;
            float ry = y - (rh - height) / 2.0f;

            set.bottomRoundRect().addRoundRect(rx, ry, rw, rh, 10f * guiScale, bgColor);

            String moduleName = module.getName();
            String moduleDescription = module.getDescription();

            float nameScale = 1.1f * guiScale;
            float maxNameWidth = rw - 14 * guiScale;
            float nameWidth = set.font().getWidth(moduleName, nameScale);
            if (nameWidth > maxNameWidth && nameWidth > 0) {
                nameScale *= maxNameWidth / nameWidth;
                nameWidth = maxNameWidth;
            }

            float descriptionScale = 0.62f * guiScale;
            float maxDescriptionWidth = rw - 16 * guiScale;
            float descriptionWidth = set.font().getWidth(moduleDescription, descriptionScale);
            if (descriptionWidth > maxDescriptionWidth && descriptionWidth > 0) {
                descriptionScale *= maxDescriptionWidth / descriptionWidth;
                descriptionWidth = maxDescriptionWidth;
            }

            float nameHeight = set.font().getHeight(nameScale);
            float descriptionHeight = set.font().getHeight(descriptionScale);
            float textGap = 3 * guiScale;

            float blockHeight = nameHeight + textGap + descriptionHeight;
            float startY = ry + (rh - blockHeight) / 2f;

            float nameX = rx + (rw - nameWidth) / 2f;
            float nameY = startY - 0.6f * guiScale;

            float descriptionX = rx + (rw - descriptionWidth) / 2f;
            float descriptionY = startY + nameHeight + textGap - 0.2f * guiScale;

            set.font().addText(moduleName, nameX, nameY, nameScale, Color.WHITE);
            set.font().addText(moduleDescription, descriptionX, descriptionY, descriptionScale, new Color(200, 200, 200));
        }

        private static float clamp01(float v) {
            if (v < 0.0f) return 0.0f;
            if (v > 1.0f) return 1.0f;
            return v;
        }

        private static int clamp255(int v) {
            return Math.max(0, Math.min(255, v));
        }

        private static Color lerpColor(Color a, Color b, float t) {
            t = clamp01(t);
            int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
            int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
            int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
            int al = (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
            return new Color(clamp255(r), clamp255(g), clamp255(bl), clamp255(al));
        }
    }

}
