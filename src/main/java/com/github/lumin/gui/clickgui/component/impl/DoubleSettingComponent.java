package com.github.lumin.gui.clickgui.component.impl;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.gui.Component;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.settings.impl.DoubleSetting;
import com.github.lumin.utils.render.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;

import java.awt.*;

public class DoubleSettingComponent extends Component {
    private final DoubleSetting setting;
    private boolean dragging;

    private final TextRenderer textRenderer = new TextRenderer();
    private final RectRenderer rectRenderer = new RectRenderer();

    public DoubleSettingComponent(DoubleSetting setting) {
        this.setting = setting;
    }

    @Override
    public void setScale(float scale) {
        super.setScale(scale);
        setHeight(16.0f * scale);
    }

    @Override
    public boolean isVisible() {
        return setting.isAvailable();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTicks) {
        if (dragging) {
            setValueFromMouse(mouseX);
        }

        String name = InterFace.isEnglish() ? setting.getEnglishName() : setting.getChineseName();
        String valueText = String.format("%.2f", setting.getValue());

        float fontScale = 0.8f * scale;
        float textHeight = textRenderer.getHeight(fontScale);
        float textTopArea = 10.5f * scale;
        float textY = getY() + (textTopArea - textHeight) / 2f;
        textRenderer.addText(name, getX(), textY, Color.WHITE, fontScale);
        float valueWidth = textRenderer.getWidth(valueText, fontScale);
        textRenderer.addText(valueText, getX() + getWidth() - valueWidth, textY, new Color(255, 255, 255, 200), fontScale);

        float barX = getX();
        float barY = getY() + 10.5f * scale;
        float barW = getWidth();
        float barH = 3.0f * scale;

        float pct = (float) ((setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin()));
        pct = Math.min(1.0f, Math.max(0.0f, pct));

        Color base = InterFace.getMainColor();
        rectRenderer.addRect(barX, barY, barW, barH, new Color(255, 255, 255, 70));
        rectRenderer.addRect(barX, barY, barW * pct, barH, ColorUtils.applyOpacity(base, 0.8f));

        rectRenderer.drawAndClear();
        textRenderer.drawAndClear();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        if (event.button() == 0 && isHovered((float) event.x(), (float) event.y())) {
            dragging = true;
            setValueFromMouse((int) event.x());
            return true;
        }
        return super.mouseClicked(event, focused);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && dragging) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    private void setValueFromMouse(int mouseX) {
        float pct = (mouseX - getX()) / getWidth();
        pct = Math.min(1.0f, Math.max(0.0f, pct));

        double range = setting.getMax() - setting.getMin();
        double raw = setting.getMin() + range * pct;
        double step = setting.getStep();

        double value;
        if (step > 0) {
            double steps = Math.round((raw - setting.getMin()) / step);
            value = setting.getMin() + steps * step;
        } else {
            value = raw;
        }
        setting.setValue(value);
    }
}
