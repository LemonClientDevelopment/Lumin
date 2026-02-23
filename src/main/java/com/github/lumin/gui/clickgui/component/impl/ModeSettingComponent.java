package com.github.lumin.gui.clickgui.component.impl;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.gui.Component;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.settings.impl.ModeSetting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;

import java.awt.*;

public class ModeSettingComponent extends Component {
    private final ModeSetting setting;
    private boolean opened;

    private final TextRenderer textRenderer = new TextRenderer();
    private final RectRenderer rectRenderer = new RectRenderer();

    public ModeSettingComponent(ModeSetting setting) {
        this.setting = setting;
    }

    @Override
    public void setScale(float scale) {
        super.setScale(scale);
        updateHeight();
    }

    @Override
    public boolean isVisible() {
        return setting.isAvailable();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTicks) {
        updateHeight();

        String name = InterFace.isEnglish() ? setting.getEnglishName() : setting.getChineseName();
        float fontScale = 0.8f * scale;
        float textHeight = textRenderer.getHeight(fontScale);
        float headerH = 12.0f * scale;
        float textY = getY() + (headerH - textHeight) / 2f;
        textRenderer.addText(name, getX(), textY, Color.WHITE, fontScale);

        String current = setting.getValue();
        float currentWidth = textRenderer.getWidth(current, fontScale);
        textRenderer.addText(current, getX() + getWidth() - currentWidth, textY, new Color(255, 255, 255, 200), fontScale);

        if (opened) {
            float optionY = getY() + 12.0f * scale;
            float optionH = 10.0f * scale;
            for (String mode : setting.getModes()) {
                boolean selected = mode.equalsIgnoreCase(setting.getValue());
                Color bg = selected ? new Color(255, 255, 255, 25) : new Color(0, 0, 0, 0);
                rectRenderer.addRect(getX(), optionY, getWidth(), optionH, bg);
                float optionTextY = optionY + (optionH - textHeight) / 2f;
                textRenderer.addText(mode, getX() + 2.0f * scale, optionTextY, Color.WHITE, fontScale);
                optionY += optionH;
            }
        }

        rectRenderer.drawAndClear();
        textRenderer.drawAndClear();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        if (event.button() != 0) return super.mouseClicked(event, focused);

        float mx = (float) event.x();
        float my = (float) event.y();

        float headerH = 12.0f * scale;
        if (isHovered(mx, my, headerH)) {
            opened = !opened;
            updateHeight();
            return true;
        }

        if (opened) {
            float optionY = getY() + headerH;
            float optionH = 10.0f * scale;
            for (String mode : setting.getModes()) {
                if (mx >= getX() && mx <= getX() + getWidth() && my >= optionY && my <= optionY + optionH) {
                    setting.setMode(mode);
                    opened = false;
                    updateHeight();
                    return true;
                }
                optionY += optionH;
            }
        }

        return super.mouseClicked(event, focused);
    }

    private void updateHeight() {
        float headerH = 12.0f * scale;
        if (!opened) {
            setHeight(headerH);
            return;
        }
        setHeight(headerH + setting.getModes().length * 10.0f * scale);
    }
}
