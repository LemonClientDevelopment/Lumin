package com.github.lumin.gui.clickgui;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.graphics.renderers.TextureRenderer;
import com.github.lumin.gui.clickgui.panel.CategoryPanel;
import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
import com.github.lumin.modules.impl.client.ClickGui;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.settings.AbstractSetting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private final List<CategoryPanel> panels = new ArrayList<>();
    private Module selectedModule;
    private boolean showSettings;

    private TextureRenderer textureRenderer;
    private RectRenderer rectRenderer;
    private TextRenderer textRenderer;

    public ClickGuiScreen() {
        super(Component.literal("ClickGui"));
    }

    @Override
    protected void init() {
        if (!panels.isEmpty()) return;

        float x = 50.0f;
        float y = 50.0f;
        for (Category category : Category.values()) {
            panels.add(new CategoryPanel(x, y, category));
            x += CategoryPanel.PANEL_WIDTH + 5.0f;
        }

        textureRenderer = new TextureRenderer();
        rectRenderer = new RectRenderer();
        textRenderer = new TextRenderer();
    }

    @Override
    public void removed() {
        if (ClickGui.INSTANCE.isEnabled()) {
            ClickGui.INSTANCE.setEnabled(false);
        }

        if (textureRenderer != null) textureRenderer.close();
        if (rectRenderer != null) rectRenderer.close();
        if (textRenderer != null) textRenderer.close();
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (CategoryPanel panel : panels) {
            panel.render(mouseX, mouseY, partialTick, textureRenderer, rectRenderer, textRenderer);
        }

        if (showSettings && selectedModule != null) {
            renderSettings();
        }

        textureRenderer.drawAndClear();
        rectRenderer.drawAndClear();
        textRenderer.drawAndClear();
    }

    private void renderSettings() {
        float w = 240.0f;
        float h = 160.0f;
        float x = (float) (this.width / 2) - w / 2.0f;
        float y = (float) (this.height / 2) - h / 2.0f;

        rectRenderer.addRect(x, y, w, h, new Color(0, 0, 0, 140));

        String title = getModuleName(selectedModule);
        float titleScale = 0.85f;
        float titleW = textRenderer.getWidth(title, titleScale);
        float titleH = textRenderer.getHeight(titleScale);
        float titleX = x + w / 2.0f - titleW / 2.0f;
        float titleY = y + 10.0f;
        textRenderer.addText(title, titleX, titleY, Color.WHITE, titleScale);

        float lineY = y + 10.0f + titleH + 8.0f;
        var settings = selectedModule.getSettings().stream().filter(AbstractSetting::isAvailable).sorted(Comparator.comparing(AbstractSetting::getEnglishName)).toList();
        for (var setting : settings) {
            float lineScale = 0.72f;
            float lineH = textRenderer.getHeight(lineScale);
            if (lineY > y + h - lineH - 6.0f) break;
            String name = InterFace.isEnglish() ? setting.getEnglishName() : setting.getChineseName();
            String value = String.valueOf(setting.getValue());
            textRenderer.addText(name + ": " + value, x + 10.0f, lineY, new Color(220, 220, 220), lineScale);
            lineY += lineH + 4.0f;
        }
    }

    private String getModuleName(Module module) {
        if (InterFace.isEnglish()) {
            return module.englishName;
        } else {
            return module.chineseName;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        if (showSettings && selectedModule != null) {
            if (button == 0) {
                float w = 240.0f;
                float h = 160.0f;
                float x = (float) (this.width / 2) - w / 2.0f;
                float y = (float) (this.height / 2) - h / 2.0f;
                if (mouseX < x || mouseX > x + w || mouseY < y || mouseY > y + h) {
                    showSettings = false;
                    selectedModule = null;
                    return true;
                }
            }
        }

        for (CategoryPanel panel : panels) {
            CategoryPanel.ClickResult result = panel.mouseClicked((float) mouseX, (float) mouseY, button);
            if (result == CategoryPanel.ClickResult.TOGGLE_HANDLED) {
                return true;
            }
            if (result == CategoryPanel.ClickResult.OPEN_SETTINGS && panel.getSelectedModule() != null) {
                selectedModule = panel.getSelectedModule();
                showSettings = true;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        float mouseX = (float) event.x();
        float mouseY = (float) event.y();
        int button = event.button();

        for (CategoryPanel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, button);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseScrolled((float) mouseX, (float) mouseY, (float) scrollY)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            if (showSettings) {
                showSettings = false;
                selectedModule = null;
                return true;
            }
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        if (ClickGui.INSTANCE.isEnabled()) {
            ClickGui.INSTANCE.setEnabled(false);
        } else {
            super.onClose();
        }
    }

    @Override
    public void renderBackground(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }
}
