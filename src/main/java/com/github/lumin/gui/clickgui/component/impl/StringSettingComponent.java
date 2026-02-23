package com.github.lumin.gui.clickgui.component.impl;

import com.github.lumin.gui.Component;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.settings.impl.StringSetting;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class StringSettingComponent extends Component {
    private final StringSetting setting;
    private boolean editing;
    private String editingValue = "";

    public StringSettingComponent(StringSetting setting) {
        this.setting = setting;
    }

    @Override
    public void setScale(float scale) {
        super.setScale(scale);
        setHeight(12.0f * scale);
    }

    @Override
    public boolean isVisible() {
        return setting.isAvailable();
    }

    @Override
    public void render(RendererSet set, int mouseX, int mouseY, float deltaTicks) {
        String name = InterFace.isEnglish() ? setting.getEnglishName() : setting.getChineseName();
        float fontScale = 0.8f * scale;
        float textHeight = set.font().getHeight(fontScale);
        float headerH = 12.0f * scale;
        float textY = getY() + (headerH - textHeight) / 2f;
        set.font().addText(name, getX(), textY, Color.WHITE, fontScale);

        float boxW = Math.min(60.0f * scale, getWidth() * 0.55f);
        float boxH = 8.0f * scale;
        float boxX = getX() + getWidth() - boxW;
        float boxY = getY() + (getHeight() - boxH) / 2.0f;

        Color base = InterFace.getMainColor();
        Color boxBg = editing ? new Color(base.getRed(), base.getGreen(), base.getBlue(), 120) : new Color(255, 255, 255, 30);
        set.topRoundRect().addRoundRect(boxX, boxY, boxW, boxH, 2.0f * scale, boxBg);

        String text = editing ? editingValue : setting.getValue();
        if (text == null) text = "";
        if (text.length() > 18) text = text.substring(0, 17) + ".";
        float textW = set.font().getWidth(text, fontScale);
        float valY = boxY + (boxH - textHeight) / 2f;
        set.font().addText(text, boxX + (boxW - textW) / 2.0f, valY, Color.WHITE, fontScale);

//        set.topRoundRect().drawAndClear();
//        set.middleRect().drawAndClear();
//        set.font().drawAndClear();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        if (event.button() != 0) return super.mouseClicked(event, focused);

        float boxW = Math.min(60.0f * scale, getWidth() * 0.55f);
        float boxH = 8.0f * scale;
        float boxX = getX() + getWidth() - boxW;
        float boxY = getY() + (getHeight() - boxH) / 2.0f;

        boolean inBox = event.x() >= boxX && event.x() <= boxX + boxW && event.y() >= boxY && event.y() <= boxY + boxH;
        if (inBox) {
            editing = !editing;
            if (editing) {
                editingValue = setting.getValue() == null ? "" : setting.getValue();
            }
            return true;
        }

        if (editing) {
            editing = false;
            return true;
        }

        return super.mouseClicked(event, focused);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!editing) return super.keyPressed(event);

        int key = event.key();
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            editing = false;
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER) {
            setting.setValue(editingValue);
            editing = false;
            return true;
        }
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (!editingValue.isEmpty()) {
                editingValue = editingValue.substring(0, editingValue.length() - 1);
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (!editing) return super.charTyped(input);
        if (!input.isAllowedChatCharacter()) return true;
        if (editingValue.length() >= 64) return true;
        editingValue += input.codepointAsString();
        return true;
    }
}
