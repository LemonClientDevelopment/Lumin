package com.github.lumin.gui.clickgui.panel;

import com.github.lumin.graphics.shaders.BlurShader;
import com.github.lumin.gui.IComponent;
import com.github.lumin.managers.Managers;
import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.utils.render.MouseUtils;
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

    private float x, y, width, height;
    private Category currentCategory;
    private final List<ModuleCard> moduleCards = new ArrayList<>();

    private String searchText = "";
    private boolean searchFocused = false;

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
        float iconW = set.icons().getWidth(returnIcon, iconScale);
        float iconH = set.icons().getHeight(iconScale);
        float iconX = iconBoxX + (iconBoxWidth - iconW) / 2f;
        float iconY = boxY + (searchHeight - iconH) / 2f - guiScale;
        set.icons().addText(returnIcon, iconX, iconY, iconScale, new Color(200, 200, 200));

        // Render Search Box
        boolean searchHovered = MouseUtils.isHovering(searchBoxX, boxY, searchBoxWidth, searchHeight, mouseX, mouseY);
        Color searchColor = searchFocused ? new Color(50, 50, 50, 200) : (searchHovered ? new Color(40, 40, 40, 200) : new Color(30, 30, 30, 200));
        set.bottomRoundRect().addRoundRect(searchBoxX, boxY, searchBoxWidth, searchHeight, 8f * guiScale, searchColor);

        // Render Text
        String displayText = searchText.isEmpty() && !searchFocused ? "Search..." : searchText;
        if (searchFocused && (System.currentTimeMillis() % 1000 > 500)) {
            displayText += "_";
        }

        Color textColor = searchText.isEmpty() && !searchFocused ? Color.GRAY : Color.WHITE;
        set.font().addText(displayText, searchBoxX + 6 * guiScale, boxY + searchHeight / 2 - 7 * guiScale, guiScale * 0.9f, textColor);

        if (currentCategory != null && !moduleCards.isEmpty()) {
            float listStartY = boxY + searchHeight + padding;
            float listBottom = y + panelHeight - padding;

            float itemGap = 8 * guiScale;
            float itemAreaX = x + padding;
            float itemAreaWidth = panelWidth - padding * 2;

            float minCardWidth = 120 * guiScale;
            int columns = Math.max(3, (int) Math.floor((itemAreaWidth + itemGap) / (minCardWidth + itemGap)));

            float cardWidth = (itemAreaWidth - itemGap * (columns - 1)) / columns;
            float cardHeight = cardWidth * (CARD_ASPECT_HEIGHT / CARD_ASPECT_WIDTH);

            int visibleIndex = 0;

            for (ModuleCard card : moduleCards) {
                if (!searchText.isEmpty() && !card.module.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    card.width = 0;
                    card.height = 0;
                    continue;
                }

                int row = visibleIndex / columns;
                int col = visibleIndex % columns;

                card.x = itemAreaX + col * (cardWidth + itemGap);
                card.y = listStartY + row * (cardHeight + itemGap);
                card.width = cardWidth;
                card.height = cardHeight;

                if (card.y + card.height > listBottom) {
                    card.width = 0;
                    card.height = 0;
                    continue;
                }

                card.render(set, mouseX, mouseY, guiScale);
                visibleIndex++;
            }
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
        if (!MouseUtils.isHovering(x, y, width, height, event.x(), event.y())) {
            searchFocused = false;
            return false;
        }

        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float panelWidth = this.width * guiScale;
        float panelHeight = this.height * guiScale;
        float padding = 8 * guiScale;
        float spacing = 4 * guiScale;
        float searchHeight = 24 * guiScale;

        float availableWidth = panelWidth - padding * 2 - spacing;
        float iconBoxWidth = availableWidth * 0.1f;
        float searchBoxWidth = availableWidth * 0.9f;

        float iconBoxX = x + padding;
        float searchBoxX = iconBoxX + iconBoxWidth + spacing;
        float boxY = y + padding;

        if (MouseUtils.isHovering(searchBoxX, boxY, searchBoxWidth, searchHeight, event.x(), event.y())) {
            if (event.button() == 1) {
                searchText = "";
            }
            searchFocused = true;
            return true;
        }

        searchFocused = false;

        if (event.button() == 0 && currentCategory != null && !moduleCards.isEmpty()) {
            float listStartY = boxY + searchHeight + padding;
            float listBottom = y + panelHeight - padding;

            float itemGap = 8 * guiScale;
            float itemAreaX = x + padding;
            float itemAreaWidth = panelWidth - padding * 2;

            float minCardWidth = 120 * guiScale;
            int columns = Math.max(3, (int) Math.floor((itemAreaWidth + itemGap) / (minCardWidth + itemGap)));

            float cardWidth = (itemAreaWidth - itemGap * (columns - 1)) / columns;
            float cardHeight = cardWidth * (CARD_ASPECT_HEIGHT / CARD_ASPECT_WIDTH);

            int visibleIndex = 0;
            for (ModuleCard card : moduleCards) {
                if (!searchText.isEmpty() && !card.module.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    continue;
                }

                int row = visibleIndex / columns;
                int col = visibleIndex % columns;

                float cardX = itemAreaX + col * (cardWidth + itemGap);
                float cardY = listStartY + row * (cardHeight + itemGap);

                if (cardY + cardHeight > listBottom) {
                    continue;
                }

                if (MouseUtils.isHovering(cardX, cardY, cardWidth, cardHeight, event.x(), event.y())) {
                    card.module.toggle();
                    return true;
                }

                visibleIndex++;
            }
        }

        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return MouseUtils.isHovering(x, y, width, height, event.x(), event.y());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
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
        if (searchFocused) {
            String str = Character.toString(event.codepoint());
            searchText += str;
            return true;
        }
        return false;
    }
}
