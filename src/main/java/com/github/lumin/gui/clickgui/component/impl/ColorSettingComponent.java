package com.github.lumin.gui.clickgui.component.impl;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.renderers.RoundRectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.gui.Component;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.settings.impl.ColorSetting;
import com.github.lumin.utils.render.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;

import java.awt.*;

public class ColorSettingComponent extends Component {
    private final ColorSetting setting;
    private boolean opened;
    private boolean dragging;
    private int draggingChannel = -1;

    private final TextRenderer textRenderer = new TextRenderer();
    private final RectRenderer rectRenderer = new RectRenderer();
    private final RoundRectRenderer roundRectRenderer = new RoundRectRenderer();

    public ColorSettingComponent(ColorSetting setting) {
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
        textRenderer.addText(name, getX(), getY() + 6.0f * scale, Color.WHITE, fontScale);

        Color c = setting.getValue() == null ? Color.WHITE : setting.getValue();
        float boxSize = 7.0f * scale;
        float boxX = getX() + getWidth() - boxSize;
        float boxY = getY() + (12.0f * scale - boxSize) / 2.0f;
        roundRectRenderer.addRoundRect(boxX, boxY, boxSize, boxSize, 2.0f * scale, c);

        if (opened) {
            int[] rgba = new int[]{c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()};
            String[] labels = new String[]{"R", "G", "B", "A"};
            Color[] trackColors = new Color[]{
                    new Color(255, 0, 0, 255),
                    new Color(0, 255, 0, 255),
                    new Color(0, 0, 255, 255),
                    new Color(255, 255, 255, 255)
            };

            float lineH = 10.0f * scale;
            float startY = getY() + 12.0f * scale;
            for (int i = 0; i < 4; i++) {
                float ly = startY + i * lineH;
                float labelW = 8.0f * scale;
                float barX = getX() + labelW;
                float barW = getWidth() - labelW;
                float barY = ly + 4.0f * scale;
                float barH = 3.0f * scale;

                float labelY = ly + (lineH - textHeight) / 2f;
                textRenderer.addText(labels[i], getX(), labelY, new Color(255, 255, 255, 220), fontScale);
                rectRenderer.addRect(barX, barY, barW, barH, new Color(255, 255, 255, 25));
                float pct = rgba[i] / 255.0f;
                rectRenderer.addRect(barX, barY, barW * pct, barH, ColorUtils.applyOpacity(trackColors[i], 0.8f));

                if (dragging && draggingChannel == i) {
                    int newValue = (int) Math.round(clamp01((mouseX - barX) / barW) * 255.0);
                    rgba[i] = clamp255(newValue);
                    setting.setValue(new Color(rgba[0], rgba[1], rgba[2], rgba[3]));
                }
            }
        }

        roundRectRenderer.drawAndClear();
        rectRenderer.drawAndClear();
        textRenderer.drawAndClear();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        if (event.button() != 0) return super.mouseClicked(event, focused);

        float boxSize = 7.0f * scale;
        float boxX = getX() + getWidth() - boxSize;
        float boxY = getY() + (12.0f * scale - boxSize) / 2.0f;
        boolean inPreview = event.x() >= boxX && event.x() <= boxX + boxSize && event.y() >= boxY && event.y() <= boxY + boxSize;
        if (inPreview) {
            opened = !opened;
            updateHeight();
            return true;
        }

        if (!opened) return super.mouseClicked(event, focused);

        float lineH = 10.0f * scale;
        float startY = getY() + 12.0f * scale;
        float labelW = 8.0f * scale;
        float barX = getX() + labelW;
        float barW = getWidth() - labelW;
        for (int i = 0; i < 4; i++) {
            float ly = startY + i * lineH;
            float barY = ly + 4.0f * scale;
            float barH = 3.0f * scale;
            if (event.x() >= barX && event.x() <= barX + barW && event.y() >= barY && event.y() <= barY + barH + 3.0f * scale) {
                dragging = true;
                draggingChannel = i;
                return true;
            }
        }

        return super.mouseClicked(event, focused);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && dragging) {
            dragging = false;
            draggingChannel = -1;
            return true;
        }
        return super.mouseReleased(event);
    }

    private void updateHeight() {
        float header = 12.0f * scale;
        if (!opened) {
            setHeight(header);
            return;
        }
        setHeight(header + 4 * 10.0f * scale);
    }

    private static float clamp01(float v) {
        return Math.min(1.0f, Math.max(0.0f, v));
    }

    private static int clamp255(int v) {
        return Math.min(255, Math.max(0, v));
    }
}
