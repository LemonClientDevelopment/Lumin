package com.github.lumin.gui.clickgui.component.impl;

import com.github.lumin.gui.Component;
import com.github.lumin.settings.impl.StringSetting;
import com.github.lumin.utils.render.MouseUtils;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class StringSettingComponent extends Component {
    private final StringSetting setting;
    private boolean focused;

    public StringSettingComponent(StringSetting setting) {
        this.setting = setting;
    }

    public StringSetting getSetting() {
        return setting;
    }

    @Override
    public void render(RendererSet set, int mouseX, int mouseY, float partialTicks) {
        if (!setting.isAvailable()) return;

        boolean hovered = ColorSettingComponent.isMouseOutOfPicker(mouseX, mouseY) && MouseUtils.isHovering(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY);
        Color bg = focused ? new Color(255, 255, 255, 22) : (hovered ? new Color(255, 255, 255, 18) : new Color(255, 255, 255, 10));
        set.bottomRoundRect().addRoundRect(getX(), getY(), getWidth(), getHeight(), 6.0f * scale, bg);

        String name = setting.getDisplayName();
        float textScale = 0.85f * scale;
        float textY = getY() + (getHeight() - set.font().getHeight(textScale)) / 2.0f - 0.5f * scale;
        set.font().addText(name, getX() + 6.0f * scale, textY, textScale, Color.WHITE);

        String value = setting.getValue() == null ? "" : setting.getValue();
        if (focused && (System.currentTimeMillis() % 1000 > 500)) {
            value += "_";
        }
        float maxValueW = Math.max(0.0f, getWidth() * 0.55f);
        float valueW = Math.min(set.font().getWidth(value, textScale), maxValueW);
        float valueX = getX() + getWidth() - 6.0f * scale - valueW;
        set.font().addText(value, valueX, textY, textScale, new Color(200, 200, 200));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        if (event.button() == 0) {
            this.focused = isHovered((float) event.x(), (float) event.y());
        }
        return super.mouseClicked(event, focused);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!focused) return super.keyPressed(event);
        if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
            String v = setting.getValue();
            if (v != null && !v.isEmpty()) {
                setting.setValue(v.substring(0, v.length() - 1));
            }
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_ESCAPE) {
            focused = false;
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (!focused) return super.charTyped(input);
        String v = setting.getValue();
        if (v == null) v = "";
        setting.setValue(v + Character.toString(input.codepoint()));
        return true;
    }

}
