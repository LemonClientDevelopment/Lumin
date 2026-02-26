package com.github.lumin.gui.clickgui.component.impl;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.renderers.RoundRectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.gui.Component;
import com.github.lumin.settings.impl.ColorSetting;
import com.github.lumin.utils.render.MouseUtils;

import java.awt.*;

public class ColorSettingComponent extends Component {
    private final ColorSetting setting;


    public ColorSettingComponent(ColorSetting setting) {
        this.setting = setting;
    }

    public ColorSetting getSetting() {
        return setting;
    }

    @Override
    public void render(RendererSet set, int mouseX, int mouseY, float partialTicks) {
        if (!setting.isAvailable()) return;

        boolean hovered = MouseUtils.isHovering(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY);
        Color bg = hovered ? new Color(255, 255, 255, 18) : new Color(255, 255, 255, 10);
        set.bottomRoundRect().addRoundRect(getX(), getY(), getWidth(), getHeight(), 6.0f * scale, bg);

        String name = setting.getDisplayName();
        Color value = setting.getValue();
        String hex = value == null ? "null" : String.format("#%02X%02X%02X", value.getRed(), value.getGreen(), value.getBlue());

        float textScale = 0.85f * scale;
        float textY = getY() + (getHeight() - set.font().getHeight(textScale)) / 2.0f - 0.5f * scale;
        set.font().addText(name, getX() + 6.0f * scale, textY, textScale, Color.WHITE);

        float sw = 10.0f * scale;
        float sx = getX() + getWidth() - 6.0f * scale - sw;
        float sy = getY() + (getHeight() - sw) / 2.0f;

        set.bottomRoundRect().addRoundRect(sx, sy, sw, sw, 3.0f * scale, value);


        float hexW = set.font().getWidth(hex, textScale);
        set.font().addText(hex, sx - 6.0f * scale - hexW, textY, textScale, new Color(200, 200, 200));
    }

    private static class PickingPanel {

        private final RoundRectRenderer roundRectRenderer = new RoundRectRenderer();
        private final RectRenderer rectRenderer = new RectRenderer();
        private final TextRenderer textRenderer = new TextRenderer();

        private final ColorSetting setting;

        private PickingPanel(ColorSetting setting) {
            this.setting = setting;
        }

    }
}
