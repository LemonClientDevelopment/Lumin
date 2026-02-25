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

        List<Module> modules = Managers.MODULE.getModulesByCategory(category);
        for (Module module : modules) {
            moduleCards.add(new ModuleCard(module));
        }
    }

    @Override
    public void render(RendererSet set, int mouseX, int mouseY, float deltaTicks) {

        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float radius = guiScale * 8f;

        float width = this.width * guiScale;
        float height = this.height * guiScale;

        BlurShader.drawRoundedBlur(x, y, width, height, 0, radius, radius, 0, new Color(30, 30, 30, 245), InterFace.INSTANCE.blurStrength.getValue().floatValue(), 1.0f);

        float padding = 4 * guiScale;

        // Search Bar
        float searchHeight = 22 * guiScale;
        float searchWidth = width * 0.9f;
        float searchX = x + width - searchWidth - padding;
        float searchY = y + 1.5f * padding;

        boolean searchHovered = MouseUtils.isHovering(searchX, searchY, searchWidth, searchHeight, mouseX, mouseY);
        Color searchColor = searchFocused ? new Color(50, 50, 50, 200) : (searchHovered ? new Color(40, 40, 40, 200) : new Color(30, 30, 30, 200));

        set.bottomRoundRect().addRoundRect(searchX, searchY, searchWidth, searchHeight, 8f * guiScale, searchColor);

        // Draw Return Icon
        String returnIcon = "\uF00D";
        float iconScale = guiScale * 1.2f;
        float iconWidth = set.icons().getWidth(returnIcon, iconScale);
        float iconHeight = set.icons().getHeight(iconScale);
        float iconX = searchX - iconWidth - 3.5f * guiScale;
        float iconY = searchY + (searchHeight - iconHeight) / 2f - guiScale;
        set.icons().addText(returnIcon, iconX, iconY, iconScale, new Color(200, 200, 200));

        String displayText = searchText.isEmpty() && !searchFocused ? "Search..." : searchText;
        if (searchFocused && (System.currentTimeMillis() % 1000 > 500)) {
            displayText += "_";
        }

        Color textColor = searchText.isEmpty() && !searchFocused ? Color.GRAY : Color.WHITE;
        set.font().addText(displayText, searchX + 6 * guiScale, searchY + searchHeight / 2 - 7 * guiScale, guiScale * 0.9f, textColor);

        if (currentCategory != null && !moduleCards.isEmpty()) {
            for (ModuleCard bar : moduleCards) {
                if (!searchText.isEmpty() && !bar.module.getName().toLowerCase().startsWith(searchText.toLowerCase())) {
                    continue;
                }


            }
        }

    }

    private class ModuleCard {

        private final Module module;

        private ModuleCard(Module module) {
            this.module = module;
        }

        private void render() {

        }

    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        if (!MouseUtils.isHovering(x, y, width, height, event.x(), event.y())) {
            searchFocused = false;
            return false;
        }

        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float padding = 12 * guiScale;
        float searchHeight = 22 * guiScale;
        float searchWidth = width * 0.85f;
        float searchX = x + width - searchWidth - padding;
        float searchY = y + padding;

        if (MouseUtils.isHovering(searchX, searchY, searchWidth, searchHeight, event.x(), event.y())) {
            if (event.button() == 1) {
                searchText = "";
            }
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
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
