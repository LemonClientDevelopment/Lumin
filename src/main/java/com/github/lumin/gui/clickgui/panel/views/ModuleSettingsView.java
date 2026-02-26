package com.github.lumin.gui.clickgui.panel.views;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.renderers.RoundRectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.graphics.text.StaticFontLoader;
import com.github.lumin.gui.IComponent.RendererSet;
import com.github.lumin.gui.clickgui.component.ModuleComponent;
import com.github.lumin.gui.clickgui.component.impl.ColorSettingComponent;
import com.github.lumin.modules.Module;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.utils.render.MouseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ModuleSettingsView {
    private final Minecraft mc;
    private final RoundRectRenderer settingsRoundRect = new RoundRectRenderer();
    private final RectRenderer settingsRect = new RectRenderer();
    private final TextRenderer settingsFont = new TextRenderer();

    private final RoundRectRenderer pickingRound = new RoundRectRenderer();
    private final RectRenderer pickingRect = new RectRenderer();
    private final RoundRectRenderer pickerRound = new RoundRectRenderer();
    private final TextRenderer pickingText = new TextRenderer();

    private ModuleComponent settingsComponent = null;
    private String searchText = "";
    private boolean searchFocused = false;

    private float settingsScrollOffset = 0.0f;
    private float settingsScrollTarget = 0.0f;
    private float settingsMaxScroll = 0.0f;

    private boolean draggingSettingsScrollbar = false;
    private float settingsScrollbarDragStartMouseY = 0.0f;
    private float settingsScrollbarDragStartScroll = 0.0f;

    private float lastIconBoxX, lastIconBoxY, lastIconBoxW, lastIconBoxH;
    private float lastSearchBoxX, lastSearchBoxY, lastSearchBoxW, lastSearchBoxH;
    private float lastSettingsX, lastSettingsY, lastSettingsW, lastSettingsH;
    private float lastSettingsScrollbarX, lastSettingsScrollbarY, lastSettingsScrollbarW, lastSettingsScrollbarH;
    private float lastSettingsThumbY, lastSettingsThumbH;

    private boolean exitRequested = false;

    public ModuleSettingsView(Minecraft mc) {
        this.mc = mc;
    }

    public boolean isActive() {
        return settingsComponent != null;
    }

    public void setModule(Module module) {
        ColorSettingComponent.closeActivePicker();
        settingsComponent = new ModuleComponent(module);
        searchText = "";
        searchFocused = false;
        settingsScrollOffset = 0.0f;
        settingsScrollTarget = 0.0f;
        settingsMaxScroll = 0.0f;
        draggingSettingsScrollbar = false;
        exitRequested = false;
    }

    public void clearModule() {
        ColorSettingComponent.closeActivePicker();
        settingsComponent = null;
        searchText = "";
        searchFocused = false;
        settingsScrollOffset = 0.0f;
        settingsScrollTarget = 0.0f;
        settingsMaxScroll = 0.0f;
        draggingSettingsScrollbar = false;
        exitRequested = false;
    }

    public boolean consumeExitRequest() {
        boolean v = exitRequested;
        exitRequested = false;
        return v;
    }

    public void render(RendererSet set, float x, float y, float width, float height, int mouseX, int mouseY, float deltaTicks) {
        if (settingsComponent == null) return;

        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float panelWidth = width * guiScale;
        float panelHeight = height * guiScale;

        float padding = 8 * guiScale;
        float spacing = 4 * guiScale;

        float searchHeight = 24 * guiScale;
        float availableWidth = panelWidth - padding * 2 - spacing;
        float iconBoxWidth = availableWidth * 0.1f;
        float titleBoxWidth = availableWidth * 0.9f;

        float iconBoxX = x + padding;
        float titleBoxX = iconBoxX + iconBoxWidth + spacing;
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

        lastIconBoxX = iconBoxX;
        lastIconBoxY = boxY;
        lastIconBoxW = iconBoxWidth;
        lastIconBoxH = searchHeight;

        boolean titleHovered = MouseUtils.isHovering(titleBoxX, boxY, titleBoxWidth, searchHeight, mouseX, mouseY);
        Color titleColor = searchFocused ? new Color(50, 50, 50, 200) : (titleHovered ? new Color(40, 40, 40, 200) : new Color(30, 30, 30, 200));
        set.bottomRoundRect().addRoundRect(titleBoxX, boxY, titleBoxWidth, searchHeight, 8f * guiScale, titleColor);

        String displayText = searchText.isEmpty() && !searchFocused ? "Search..." : searchText;
        if (searchFocused && (System.currentTimeMillis() % 1000 > 500)) {
            displayText += "_";
        }
        Color textColor = searchText.isEmpty() && !searchFocused ? Color.GRAY : Color.WHITE;
        set.font().addText(displayText, titleBoxX + 6 * guiScale, boxY + searchHeight / 2 - 7 * guiScale, guiScale * 0.9f, textColor);

        lastSearchBoxX = titleBoxX;
        lastSearchBoxY = boxY;
        lastSearchBoxW = titleBoxWidth;
        lastSearchBoxH = searchHeight;

        float areaX = x + padding;
        float areaY = boxY + searchHeight + padding;
        float areaW = Math.max(0.0f, panelWidth - padding * 2);
        float areaH = Math.max(0.0f, (y + panelHeight - padding) - areaY);

        float scrollbarW = 4.0f * guiScale;
        float scrollbarGap = 4.0f * guiScale;
        float contentW = Math.max(0.0f, areaW - scrollbarGap - scrollbarW);

        float titleScale = 1.15f * guiScale;
        float rowH = 18.0f * guiScale;
        float rowGap = 4.0f * guiScale;
        float innerPadding = 8.0f * guiScale;

        settingsComponent.setFilterText(searchText);
        int itemCount = settingsComponent.getFilteredVisibleCount();

        float titleH = set.font().getHeight(titleScale);
        float contentH = innerPadding + titleH + 6.0f * guiScale;
        if (itemCount > 0) {
            contentH += itemCount * rowH + Math.max(0, itemCount - 1) * rowGap;
        }
        contentH += innerPadding;

        settingsMaxScroll = Math.max(0.0f, contentH - areaH);
        settingsScrollTarget = Math.max(0.0f, Math.min(settingsScrollTarget, settingsMaxScroll));
        settingsScrollOffset = settingsScrollOffset + (settingsScrollTarget - settingsScrollOffset) * 0.35f;
        settingsScrollOffset = Math.max(0.0f, Math.min(settingsScrollOffset, settingsMaxScroll));

        float scrollbarX = areaX + contentW + scrollbarGap;

        float thumbH = settingsMaxScroll <= 0.0f ? areaH : Math.max(12.0f * guiScale, areaH * (areaH / contentH));
        float thumbTravel = Math.max(0.0f, areaH - thumbH);
        float thumbY = settingsMaxScroll <= 0.0f ? areaY : areaY + (settingsScrollOffset / settingsMaxScroll) * thumbTravel;

        lastSettingsX = areaX;
        lastSettingsY = areaY;
        lastSettingsW = contentW;
        lastSettingsH = areaH;

        lastSettingsScrollbarX = scrollbarX;
        lastSettingsScrollbarY = areaY;
        lastSettingsScrollbarW = scrollbarW;
        lastSettingsScrollbarH = areaH;

        lastSettingsThumbY = thumbY;
        lastSettingsThumbH = thumbH;

        if (draggingSettingsScrollbar && settingsMaxScroll > 0.0f && thumbTravel > 0.0f) {
            float mouseDelta = mouseY - settingsScrollbarDragStartMouseY;
            float scrollDelta = (mouseDelta / thumbTravel) * settingsMaxScroll;
            settingsScrollTarget = Math.max(0.0f, Math.min(settingsScrollbarDragStartScroll + scrollDelta, settingsMaxScroll));
        }

        float pxScale = (float) mc.getWindow().getGuiScale();
        int fbW = mc.getWindow().getWidth();
        int fbH = mc.getWindow().getHeight();
        int guiH = mc.getWindow().getGuiScaledHeight();

        int scX = Mth.floor(areaX * pxScale);
        int scY = Mth.floor((guiH - (areaY + areaH)) * pxScale);
        int scW = Mth.ceil(contentW * pxScale);
        int scH = Mth.ceil(areaH * pxScale);

        scX = Mth.clamp(scX, 0, fbW);
        scY = Mth.clamp(scY, 0, fbH);
        scW = Mth.clamp(scW, 0, fbW - scX);
        scH = Mth.clamp(scH, 0, fbH - scY);

        RendererSet settingsSet = new RendererSet(settingsRoundRect, set.topRoundRect(), set.texture(), settingsFont, pickingRound, pickingRect, pickerRound, pickingText);

        settingsRoundRect.setScissor(scX, scY, scW, scH);
        settingsRect.setScissor(scX, scY, scW, scH);
        settingsFont.setScissor(scX, scY, scW, scH);

        settingsComponent.setX(areaX);
        settingsComponent.setY(areaY - settingsScrollOffset);
        settingsComponent.setWidth(contentW);
        settingsComponent.setHeight(contentH);
        settingsComponent.render(settingsSet, mouseX, mouseY, deltaTicks);

        settingsRoundRect.drawAndClear();
        settingsRect.drawAndClear();
        settingsFont.drawAndClear();
        settingsRoundRect.clearScissor();
        settingsRect.clearScissor();
        settingsFont.clearScissor();

        settingsComponent.renderOverlayBlurs(mouseX, mouseY, deltaTicks);
        settingsComponent.renderOverlays(settingsSet, mouseX, mouseY, deltaTicks);

        pickingRound.drawAndClear();
        pickingRect.drawAndClear();
        pickerRound.drawAndClear();
        pickingText.drawAndClear();

        if (settingsMaxScroll > 0.0f) {
            boolean scrollbarHovered = MouseUtils.isHovering(scrollbarX, areaY, scrollbarW, areaH, mouseX, mouseY);
            boolean thumbHovered = MouseUtils.isHovering(scrollbarX, thumbY, scrollbarW, thumbH, mouseX, mouseY);

            Color trackColor = scrollbarHovered ? new Color(255, 255, 255, 28) : new Color(255, 255, 255, 18);
            Color thumbColor = draggingSettingsScrollbar ? new Color(255, 255, 255, 90) : (thumbHovered ? new Color(255, 255, 255, 75) : new Color(255, 255, 255, 55));

            set.bottomRoundRect().addRoundRect(scrollbarX, areaY, scrollbarW, areaH, scrollbarW / 2.0f, trackColor);
            set.bottomRoundRect().addRoundRect(scrollbarX, thumbY, scrollbarW, thumbH, scrollbarW / 2.0f, thumbColor);
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean focused, float x, float y, float width, float height) {
        if (settingsComponent == null) return false;

        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float panelWidth = width * guiScale;
        float panelHeight = height * guiScale;
        if (!MouseUtils.isHovering(x, y, panelWidth, panelHeight, event.x(), event.y())) {
            return false;
        }

        if (event.button() == 0 && MouseUtils.isHovering(lastIconBoxX, lastIconBoxY, lastIconBoxW, lastIconBoxH, event.x(), event.y())) {
            exitRequested = true;
            return true;
        }

        if (MouseUtils.isHovering(lastSearchBoxX, lastSearchBoxY, lastSearchBoxW, lastSearchBoxH, event.x(), event.y())) {
            if (event.button() == 1) {
                searchText = "";
                settingsScrollTarget = 0.0f;
            }
            searchFocused = true;
            return true;
        }

        searchFocused = false;

        if (event.button() == 0 && settingsMaxScroll > 0.0f && MouseUtils.isHovering(lastSettingsScrollbarX, lastSettingsScrollbarY, lastSettingsScrollbarW, lastSettingsScrollbarH, event.x(), event.y())) {
            float thumbTravel = Math.max(0.0f, lastSettingsScrollbarH - lastSettingsThumbH);
            if (thumbTravel > 0.0f) {
                if (MouseUtils.isHovering(lastSettingsScrollbarX, lastSettingsThumbY, lastSettingsScrollbarW, lastSettingsThumbH, event.x(), event.y())) {
                    draggingSettingsScrollbar = true;
                    settingsScrollbarDragStartMouseY = (float) event.y();
                    settingsScrollbarDragStartScroll = settingsScrollTarget;
                    return true;
                }
                float clickY = (float) event.y();
                float ratio = (clickY - lastSettingsScrollbarY - lastSettingsThumbH / 2.0f) / thumbTravel;
                ratio = Mth.clamp(ratio, 0.0f, 1.0f);
                settingsScrollTarget = ratio * settingsMaxScroll;
                draggingSettingsScrollbar = true;
                settingsScrollbarDragStartMouseY = (float) event.y();
                settingsScrollbarDragStartScroll = settingsScrollTarget;
                return true;
            }
        }

        return settingsComponent.mouseClicked(event, focused);
    }

    public boolean mouseReleased(MouseButtonEvent event, float x, float y, float width, float height) {
        draggingSettingsScrollbar = false;
        if (settingsComponent == null) return false;
        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float panelWidth = width * guiScale;
        float panelHeight = height * guiScale;
        return MouseUtils.isHovering(x, y, panelWidth, panelHeight, event.x(), event.y()) && settingsComponent.mouseReleased(event);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (settingsComponent == null) return false;
        if (settingsMaxScroll <= 0.0f) return false;
        if (!MouseUtils.isHovering(lastSettingsX, lastSettingsY, lastSettingsW + lastSettingsScrollbarW, lastSettingsH, mouseX, mouseY))
            return false;
        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float step = 24.0f * guiScale;
        settingsScrollTarget = Math.max(0.0f, Math.min(settingsScrollTarget - (float) scrollY * step, settingsMaxScroll));
        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        if (settingsComponent == null) return false;
        if (searchFocused) {
            if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchText.isEmpty()) {
                    searchText = searchText.substring(0, searchText.length() - 1);
                    settingsScrollTarget = 0.0f;
                }
                return true;
            }
            if (event.key() == GLFW.GLFW_KEY_ESCAPE || event.key() == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
        }
        return settingsComponent.keyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        if (settingsComponent == null) return false;
        if (searchFocused) {
            String str = Character.toString(event.codepoint());
            searchText += str;
            settingsScrollTarget = 0.0f;
            return true;
        }
        return settingsComponent.charTyped(event);
    }

    public void clickOutside() {
        draggingSettingsScrollbar = false;
        searchFocused = false;
    }

}
