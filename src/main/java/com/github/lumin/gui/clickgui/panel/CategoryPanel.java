package com.github.lumin.gui.clickgui.panel;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.graphics.renderers.TextureRenderer;
import com.github.lumin.gui.clickgui.componet.ModuleComponet;
import com.github.lumin.managers.Managers;
import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
import com.github.lumin.modules.impl.client.InterFace;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {
    public static final float PANEL_WIDTH = 119.0f;
    public static final float PANEL_HEIGHT = 169.0f;

    private static final float BORDER = 9.5f;
    private static final float CONTENT_WIDTH = 100.0f;
    private static final float HEADER_HEIGHT = 25.0f;
    private static final float LIST_HEIGHT = 125.0f;
    private static final float ROW_HEIGHT = 15.0f;
    private static final float SCROLL_STEP = 18.5f;

    private float x;
    private float y;
    private final Category category;

    private boolean dragging;
    private float dragMouseX;
    private float dragMouseY;
    private float dragPanelX;
    private float dragPanelY;

    private float scroll;
    private Module selectedModule;

    private final List<ModuleComponet> modules = new ArrayList<>();

    public CategoryPanel(float x, float y, Category category) {
        this.x = x;
        this.y = y;
        this.category = category;
        rebuildModules();
    }

    public enum ClickResult {
        NONE,
        TOGGLE_HANDLED,
        OPEN_SETTINGS
    }

    public Module getSelectedModule() {
        return selectedModule;
    }

    private void rebuildModules() {
        modules.clear();
        for (Module module : Managers.MODULE.getModules()) {
            if (module.category != category) continue;
            modules.add(new ModuleComponet(module));
        }
    }

    public void render(int mouseX, int mouseY, float partialTick, TextureRenderer textureRenderer, RectRenderer rectRenderer, TextRenderer textRenderer) {
        if (dragging) {
            x = dragPanelX + (float) mouseX - dragMouseX;
            y = dragPanelY + (float) mouseY - dragMouseY;
        }

        rebuildModules();

        textureRenderer.addTexture("jello/jellopanel.png", x - BORDER, y - BORDER, PANEL_WIDTH, PANEL_HEIGHT);

        String header = category.getName();
        float headerScale = 0.8f;
        float headerW = textRenderer.getWidth(header, headerScale);
        float headerH = textRenderer.getHeight(headerScale);
        float headerX = x + CONTENT_WIDTH / 2.0f - headerW / 2.0f;
        float headerY = y + (HEADER_HEIGHT - headerH) / 2.0f;
        textRenderer.addText(header, headerX, headerY, Color.WHITE, headerScale);

        float listX = x;
        float listY = y + HEADER_HEIGHT;
        float listBottom = listY + LIST_HEIGHT;

        for (int i = 0; i < modules.size(); i++) {
            ModuleComponet comp = modules.get(i);
            float rowY = listY + i * ROW_HEIGHT - scroll;
            if (rowY + ROW_HEIGHT < listY || rowY > listBottom) continue;

            boolean hovered = isHovering(listX, rowY, CONTENT_WIDTH, ROW_HEIGHT, mouseX, mouseY);

            Color bg;
            if (comp.getModule().isEnabled()) {
                bg = new Color(42, 165, 255, hovered ? 230 : 200);
            } else {
                bg = new Color(60, 60, 60, hovered ? 180 : 120);
            }

            rectRenderer.addRect(listX, rowY, CONTENT_WIDTH, ROW_HEIGHT, bg);

            String name = InterFace.isEnglish() ? comp.getModule().englishName : comp.getModule().chineseName;
            float rowScale = 0.7f;
            float textW = textRenderer.getWidth(name, rowScale);
            float textH = textRenderer.getHeight(rowScale);
            float textX = listX + CONTENT_WIDTH / 2.0f - textW / 2.0f;
            float textY = rowY + (ROW_HEIGHT - textH) / 2.0f;
            textRenderer.addText(name, textX, textY, new Color(240, 240, 240), rowScale);
        }
    }

    public ClickResult mouseClicked(float mouseX, float mouseY, int button) {
        selectedModule = null;

        if (button == 0 && isHovering(x, y, CONTENT_WIDTH, 30.0f, mouseX, mouseY)) {
            dragging = true;
            dragMouseX = mouseX;
            dragMouseY = mouseY;
            dragPanelX = x;
            dragPanelY = y;
            return ClickResult.TOGGLE_HANDLED;
        }

        float listX = x;
        float listY = y + HEADER_HEIGHT;
        float listBottom = listY + LIST_HEIGHT;

        if (!isHovering(listX, listY, CONTENT_WIDTH, LIST_HEIGHT, mouseX, mouseY)) {
            return ClickResult.NONE;
        }

        float localY = mouseY - listY + scroll;
        int index = (int) (localY / ROW_HEIGHT);
        if (index < 0 || index >= modules.size()) return ClickResult.NONE;

        float rowY = listY + index * ROW_HEIGHT - scroll;
        if (rowY < listY || rowY + ROW_HEIGHT > listBottom) return ClickResult.NONE;

        Module module = modules.get(index).getModule();
        if (button == 0) {
            module.toggle();
            return ClickResult.TOGGLE_HANDLED;
        }
        if (button == 1) {
            selectedModule = module;
            return ClickResult.OPEN_SETTINGS;
        }

        return ClickResult.NONE;
    }

    public void mouseReleased(float mouseX, float mouseY, int button) {
        dragging = false;
    }

    public boolean mouseScrolled(float mouseX, float mouseY, float scrollY) {
        float listX = x;
        float listY = y + HEADER_HEIGHT;

        if (!isHovering(listX, listY, CONTENT_WIDTH, LIST_HEIGHT, mouseX, mouseY)) {
            return false;
        }

        float contentHeight = Math.max(0.0f, modules.size() * ROW_HEIGHT - LIST_HEIGHT);
        if (contentHeight <= 0.0f) return false;

        if (scrollY < 0.0f) {
            scroll += SCROLL_STEP;
        } else if (scrollY > 0.0f) {
            scroll -= SCROLL_STEP;
        }

        if (scroll < 0.0f) scroll = 0.0f;
        if (scroll > contentHeight) scroll = contentHeight;
        return true;
    }

    private boolean isHovering(float x, float y, float w, float h, float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private boolean isHovering(float x, float y, float w, float h, int mouseX, int mouseY) {
        return isHovering(x, y, w, h, (float) mouseX, (float) mouseY);
    }
}
