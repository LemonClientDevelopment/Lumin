package com.github.lumin.gui.clickgui.panel;

import com.github.lumin.graphics.renderers.RoundRectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.graphics.shaders.BlurShader;
import com.github.lumin.gui.IComponent;
import com.github.lumin.gui.clickgui.component.ModuleComponent;
import com.github.lumin.managers.Managers;
import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.utils.render.MouseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ContentPanel implements IComponent {

    private static final float CARD_ASPECT_WIDTH = 16.0f;
    private static final float CARD_ASPECT_HEIGHT = 9.0f;

    private final Minecraft mc = Minecraft.getInstance();

    private final RoundRectRenderer listRoundRect = new RoundRectRenderer();
    private final TextRenderer listFont = new TextRenderer();

    private float x, y, width, height;
    private Category currentCategory;
    private final List<ModuleCard> moduleCards = new ArrayList<>();

    private String searchText = "";
    private boolean searchFocused = false;

    private boolean inSettingsView = false;
    private ModuleComponent settingsComponent = null;

    private float scrollOffset = 0.0f;
    private float scrollTarget = 0.0f;
    private float maxScroll = 0.0f;

    private boolean draggingScrollbar = false;
    private float scrollbarDragStartMouseY = 0.0f;
    private float scrollbarDragStartScroll = 0.0f;

    private float lastIconBoxX, lastIconBoxY, lastIconBoxW, lastIconBoxH;
    private float lastSearchBoxX, lastSearchBoxY, lastSearchBoxW, lastSearchBoxH;
    private float lastListX, lastListY, lastListW, lastListH;
    private float lastScrollbarX, lastScrollbarY, lastScrollbarW, lastScrollbarH;
    private float lastThumbY, lastThumbH;

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setCurrentCategory(Category category) {
        if (this.currentCategory == category) return;
        this.currentCategory = category;
        this.moduleCards.clear();
        this.searchText = "";
        this.searchFocused = false;
        this.inSettingsView = false;
        this.settingsComponent = null;
        this.scrollOffset = 0.0f;
        this.scrollTarget = 0.0f;
        this.maxScroll = 0.0f;
        this.draggingScrollbar = false;

        List<Module> modules = Managers.MODULE.getModules();
        for (Module module : modules) {
            if (module.category == category) {
                moduleCards.add(new ModuleCard(module));
            }
        }
    }

    @Override
    public void render(RendererSet set, int mouseX, int mouseY, float deltaTicks) {

        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float radius = guiScale * 8f;

        float panelWidth = this.width * guiScale;
        float panelHeight = this.height * guiScale;

        BlurShader.drawRoundedBlur(x, y, panelWidth, panelHeight, 0, radius, radius, 0, new Color(30, 30, 30, 245), InterFace.INSTANCE.blurStrength.getValue().floatValue(), 1.0f);

        float padding = 8 * guiScale;
        float spacing = 4 * guiScale;

        float searchHeight = 24 * guiScale;
        float availableWidth = panelWidth - padding * 2 - spacing;
        float iconBoxWidth = availableWidth * 0.1f;
        float searchBoxWidth = availableWidth * 0.9f;

        float iconBoxX = x + padding;
        float searchBoxX = iconBoxX + iconBoxWidth + spacing;
        float boxY = y + padding;

        // Render Icon Box
        boolean iconBoxHovered = MouseUtils.isHovering(iconBoxX, boxY, iconBoxWidth, searchHeight, mouseX, mouseY);
        Color iconBoxColor = iconBoxHovered ? new Color(40, 40, 40, 200) : new Color(30, 30, 30, 200);
        set.bottomRoundRect().addRoundRect(iconBoxX, boxY, iconBoxWidth, searchHeight, 8f * guiScale, iconBoxColor);

        // Render Icon
        String returnIcon = "\uF00D";
        float iconScale = guiScale * 1.2f;
        float iconW = set.font().getWidth(returnIcon, iconScale);
        float iconH = set.font().getHeight(iconScale);
        float iconX = iconBoxX + (iconBoxWidth - iconW) / 2f;
        float iconY = boxY + (searchHeight - iconH) / 2f - guiScale;
        set.font().addText(returnIcon, iconX, iconY, iconScale, new Color(200, 200, 200));

        lastIconBoxX = iconBoxX;
        lastIconBoxY = boxY;
        lastIconBoxW = iconBoxWidth;
        lastIconBoxH = searchHeight;

        // Render Search Box
        boolean searchHovered = MouseUtils.isHovering(searchBoxX, boxY, searchBoxWidth, searchHeight, mouseX, mouseY);
        Color searchColor = searchFocused ? new Color(50, 50, 50, 200) : (searchHovered ? new Color(40, 40, 40, 200) : new Color(30, 30, 30, 200));
        set.bottomRoundRect().addRoundRect(searchBoxX, boxY, searchBoxWidth, searchHeight, 8f * guiScale, searchColor);

        // Render Text
        String displayText;
        if (inSettingsView && settingsComponent != null) {
            displayText = settingsComponent.getModule().getName();
        } else {
            displayText = searchText.isEmpty() && !searchFocused ? "Search..." : searchText;
            if (searchFocused && (System.currentTimeMillis() % 1000 > 500)) {
                displayText += "_";
            }
        }

        Color textColor = (inSettingsView && settingsComponent != null) ? Color.WHITE : (searchText.isEmpty() && !searchFocused ? Color.GRAY : Color.WHITE);
        set.font().addText(displayText, searchBoxX + 6 * guiScale, boxY + searchHeight / 2 - 7 * guiScale, guiScale * 0.9f, textColor);

        lastSearchBoxX = searchBoxX;
        lastSearchBoxY = boxY;
        lastSearchBoxW = searchBoxWidth;
        lastSearchBoxH = searchHeight;

        float listStartY = boxY + searchHeight + padding;
        float listBottom = y + panelHeight - padding;
        float listH = Math.max(0.0f, listBottom - listStartY);

        if (inSettingsView && settingsComponent != null) {
            float areaX = x + padding;
            float areaY = listStartY;
            float areaW = Math.max(0.0f, panelWidth - padding * 2);
            float areaH = Math.max(0.0f, listBottom - listStartY);

            settingsComponent.setX(areaX);
            settingsComponent.setY(areaY);
            settingsComponent.setWidth(areaW);
            settingsComponent.setHeight(areaH);
            settingsComponent.render(set, mouseX, mouseY, deltaTicks);
            return;
        }

        float scrollbarW = 4.0f * guiScale;
        float scrollbarGap = 4.0f * guiScale;
        float listAreaX = x + padding;
        float listAreaW = Math.max(0.0f, panelWidth - padding * 2 - scrollbarGap - scrollbarW);

        lastListX = listAreaX;
        lastListY = listStartY;
        lastListW = listAreaW;
        lastListH = listH;

        float scrollbarX = listAreaX + listAreaW + scrollbarGap;
        float scrollbarY = listStartY;
        float scrollbarH = listH;

        lastScrollbarX = scrollbarX;
        lastScrollbarY = scrollbarY;
        lastScrollbarW = scrollbarW;
        lastScrollbarH = scrollbarH;

        List<ModuleCard> visibleCards = new ArrayList<>();
        for (ModuleCard card : moduleCards) {
            if (!searchText.isEmpty() && !card.module.getName().toLowerCase().contains(searchText.toLowerCase())) {
                card.width = 0;
                card.height = 0;
                continue;
            }
            visibleCards.add(card);
        }

        if (currentCategory == null || visibleCards.isEmpty() || listH <= 0.0f || listAreaW <= 0.0f) {
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

        float thumbH = maxScroll <= 0.0f ? scrollbarH : Math.max(12.0f * guiScale, scrollbarH * (listH / contentH));
        float thumbTravel = Math.max(0.0f, scrollbarH - thumbH);
        float thumbY = maxScroll <= 0.0f ? scrollbarY : scrollbarY + (scrollOffset / maxScroll) * thumbTravel;

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
            boolean scrollbarHovered = MouseUtils.isHovering(scrollbarX, scrollbarY, scrollbarW, scrollbarH, mouseX, mouseY);
            boolean thumbHovered = MouseUtils.isHovering(scrollbarX, thumbY, scrollbarW, thumbH, mouseX, mouseY);

            Color trackColor = scrollbarHovered ? new Color(255, 255, 255, 28) : new Color(255, 255, 255, 18);
            Color thumbColor = draggingScrollbar ? new Color(255, 255, 255, 90) : (thumbHovered ? new Color(255, 255, 255, 75) : new Color(255, 255, 255, 55));

            set.bottomRoundRect().addRoundRect(scrollbarX, listStartY, scrollbarW, listH, scrollbarW / 2.0f, trackColor);
            set.bottomRoundRect().addRoundRect(scrollbarX, thumbY, scrollbarW, thumbH, scrollbarW / 2.0f, thumbColor);
        }

    }

    private class ModuleCard {

        private final Module module;
        private float x;
        private float y;
        private float width;
        private float height;

        private ModuleCard(Module module) {
            this.module = module;
        }

        private void render(RendererSet set, int mouseX, int mouseY, float guiScale) {
            if (width <= 0 || height <= 0) return;

            boolean hovered = MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);

            Color bgColor = module.isEnabled() ? new Color(55, 180, 90, 130) : new Color(40, 40, 40, 130);
            if (hovered) {
                bgColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), Math.min(255, bgColor.getAlpha() + 30));
            }

            set.bottomRoundRect().addRoundRect(x, y, width, height, 10f * guiScale, bgColor);

            String moduleName = module.getName();
            String moduleDescription = module.getDescription();

            float nameScale = 1.1f * guiScale;
            float maxNameWidth = width - 14 * guiScale;
            float nameWidth = set.font().getWidth(moduleName, nameScale);
            if (nameWidth > maxNameWidth && nameWidth > 0) {
                nameScale *= maxNameWidth / nameWidth;
                nameWidth = maxNameWidth;
            }

            float descriptionScale = 0.62f * guiScale;
            float maxDescriptionWidth = width - 16 * guiScale;
            float descriptionWidth = set.font().getWidth(moduleDescription, descriptionScale);
            if (descriptionWidth > maxDescriptionWidth && descriptionWidth > 0) {
                descriptionScale *= maxDescriptionWidth / descriptionWidth;
                descriptionWidth = maxDescriptionWidth;
            }

            float nameHeight = set.font().getHeight(nameScale);
            float descriptionHeight = set.font().getHeight(descriptionScale);
            float textGap = 3 * guiScale;

            float blockHeight = nameHeight + textGap + descriptionHeight;
            float startY = y + (height - blockHeight) / 2f;

            float nameX = x + (width - nameWidth) / 2f;
            float nameY = startY - 0.6f * guiScale;

            float descriptionX = x + (width - descriptionWidth) / 2f;
            float descriptionY = startY + nameHeight + textGap - 0.2f * guiScale;

            set.font().addText(moduleName, nameX, nameY, nameScale, Color.WHITE);
            set.font().addText(moduleDescription, descriptionX, descriptionY, descriptionScale, new Color(200, 200, 200));
        }

    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float panelWidth = this.width * guiScale;
        float panelHeight = this.height * guiScale;
        if (!MouseUtils.isHovering(x, y, panelWidth, panelHeight, event.x(), event.y())) {
            searchFocused = false;
            draggingScrollbar = false;
            return false;
        }

        if (event.button() == 0 && MouseUtils.isHovering(lastIconBoxX, lastIconBoxY, lastIconBoxW, lastIconBoxH, event.x(), event.y())) {
            if (inSettingsView) {
                inSettingsView = false;
                settingsComponent = null;
                searchFocused = false;
                return true;
            }
        }

        if (inSettingsView) {
            return settingsComponent != null && settingsComponent.mouseClicked(event, focused);
        }

        if (MouseUtils.isHovering(lastSearchBoxX, lastSearchBoxY, lastSearchBoxW, lastSearchBoxH, event.x(), event.y())) {
            if (event.button() == 1) {
                searchText = "";
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

        if (event.button() == 0 && currentCategory != null && !moduleCards.isEmpty()) {
            for (ModuleCard card : moduleCards) {
                if (card.width <= 0 || card.height <= 0) continue;
                if (MouseUtils.isHovering(card.x, card.y, card.width, card.height, event.x(), event.y())) {
                    card.module.toggle();
                    return true;
                }
            }
        }

        if (event.button() == 1 && currentCategory != null && !moduleCards.isEmpty()) {
            for (ModuleCard card : moduleCards) {
                if (card.width <= 0 || card.height <= 0) continue;
                if (MouseUtils.isHovering(card.x, card.y, card.width, card.height, event.x(), event.y())) {
                    inSettingsView = true;
                    settingsComponent = new ModuleComponent(card.module);
                    searchFocused = false;
                    draggingScrollbar = false;
                    return true;
                }
            }
        }

        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScrollbar = false;
        if (inSettingsView) {
            return settingsComponent != null && settingsComponent.mouseReleased(event);
        }
        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float panelWidth = this.width * guiScale;
        float panelHeight = this.height * guiScale;
        return MouseUtils.isHovering(x, y, panelWidth, panelHeight, event.x(), event.y());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (inSettingsView) return false;
        if (maxScroll <= 0.0f) return false;
        if (!MouseUtils.isHovering(lastListX, lastListY, lastListW + lastScrollbarW, lastListH, mouseX, mouseY))
            return false;
        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float step = 24.0f * guiScale;
        scrollTarget = Math.max(0.0f, Math.min(scrollTarget - (float) scrollY * step, maxScroll));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (inSettingsView) {
            return settingsComponent != null && settingsComponent.keyPressed(event);
        }
        if (searchFocused) {
            if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchText.isEmpty()) {
                    searchText = searchText.substring(0, searchText.length() - 1);
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

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (inSettingsView) {
            return settingsComponent != null && settingsComponent.charTyped(event);
        }
        if (searchFocused) {
            String str = Character.toString(event.codepoint());
            searchText += str;
            return true;
        }
        return false;
    }
}