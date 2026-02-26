package com.github.lumin.gui.clickgui.component.impl;

import com.github.lumin.gui.Component;
import com.github.lumin.settings.impl.IntSetting;
import com.github.lumin.utils.render.MouseUtils;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class IntSettingComponent extends Component {
    private final IntSetting setting;
    private boolean dragging;
    private boolean editing;
    private String editText = "";
    private float lastValueBoxX;
    private float lastValueBoxY;
    private float lastValueBoxW;
    private float lastValueBoxH;
    private float lastSliderX;
    private float lastSliderW;
    private float lastSliderHitY;
    private float lastSliderHitH;

    public IntSettingComponent(IntSetting setting) {
        this.setting = setting;
    }

    public IntSetting getSetting() {
        return setting;
    }

    @Override
    public void render(RendererSet set, int mouseX, int mouseY, float partialTicks) {
        if (!setting.isAvailable()) return;

        boolean hovered = MouseUtils.isHovering(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY);
        Color bg = hovered ? new Color(255, 255, 255, 18) : new Color(255, 255, 255, 10);
        set.bottomRoundRect().addRoundRect(getX(), getY(), getWidth(), getHeight(), 6.0f * scale, bg);

        float padding = 6.0f * scale;
        float textScale = 0.85f * scale;
        float textY = getY() + (getHeight() - set.font().getHeight(textScale)) / 2.0f - 0.5f * scale;

        String name = setting.getDisplayName();
        set.font().addText(name, getX() + padding, textY, textScale, Color.WHITE);

        String valueStr;
        String valueMeasureStr;
        if (editing) {
            if (setting.isPercentageMode()) {
                valueMeasureStr = editText + "%";
                valueStr = valueMeasureStr;
                if (System.currentTimeMillis() % 1000 > 500) valueStr += "_";
            } else {
                valueMeasureStr = editText;
                valueStr = editText;
                if (System.currentTimeMillis() % 1000 > 500) valueStr += "_";
            }
        } else if (setting.isPercentageMode()) {
            int min = setting.getMin();
            int max = setting.getMax();
            int v = setting.getValue();
            int percent = 0;
            if (max != min) {
                percent = Math.round(((float) (v - min) / (float) (max - min)) * 100.0f);
                percent = Mth.clamp(percent, 0, 100);
            }
            valueStr = percent + "%";
            valueMeasureStr = valueStr;
        } else {
            valueStr = String.valueOf(setting.getValue());
            valueMeasureStr = valueStr;
        }

        float valueInnerPad = 4.0f * scale;
        float valueMinW = set.font().getWidth(String.valueOf(setting.getMin()), textScale);
        float valueMaxW = set.font().getWidth(String.valueOf(setting.getMax()), textScale);
        float valuePercentW = set.font().getWidth("100%", textScale);
        float valueBoxW = Math.max(valueMinW, Math.max(valueMaxW, valuePercentW)) + valueInnerPad * 2.0f + 8.0f * scale;
        float valueBoxH = Math.max(0.0f, getHeight() - 4.0f * scale);
        float valueBoxX = getX() + getWidth() - padding - valueBoxW;
        float valueBoxY = getY() + (getHeight() - valueBoxH) / 2.0f;
        lastValueBoxX = valueBoxX;
        lastValueBoxY = valueBoxY;
        lastValueBoxW = valueBoxW;
        lastValueBoxH = valueBoxH;

        Color valueBg = editing ? new Color(255, 255, 255, 22) : new Color(255, 255, 255, 12);
        set.bottomRoundRect().addRoundRect(valueBoxX, valueBoxY, valueBoxW, valueBoxH, 6.0f * scale, valueBg);

        float valueMaxTextW = Math.max(0.0f, valueBoxW - valueInnerPad * 2.0f);
        float valueW = Mth.clamp(set.font().getWidth(valueMeasureStr, textScale), 0.0f, valueMaxTextW);
        float valueX = valueBoxX + (valueBoxW - valueW) / 2.0f;
        float valueTextY = valueBoxY + (valueBoxH - set.font().getHeight(textScale)) / 2.0f - 0.5f * scale;
        set.font().addText(valueStr, valueX, valueTextY, textScale, new Color(200, 200, 200));

        float sliderWidth = 68.0f * scale;
        float sliderHeight = 3.0f * scale;
        float sliderX = valueBoxX - padding - sliderWidth;
        float sliderY = getY() + (getHeight() - sliderHeight) / 2.0f;
        lastSliderX = sliderX;
        lastSliderW = sliderWidth;
        lastSliderHitH = Math.max(12.0f * scale, sliderHeight);
        lastSliderHitY = getY() + (getHeight() - lastSliderHitH) / 2.0f;

        if (!editing && dragging) {
            float mouseRelX = mouseX - sliderX;
            float percent = Mth.clamp(mouseRelX / sliderWidth, 0.0f, 1.0f);
            int range = setting.getMax() - setting.getMin();
            int newVal = setting.getMin() + (int) (range * percent);

            if (setting.getStep() > 1) {
                int step = setting.getStep();
                int stepped = newVal - setting.getMin();
                stepped = Math.round((float) stepped / step) * step;
                newVal = setting.getMin() + stepped;
            }
            setting.setValue(newVal);
        }

        set.bottomRoundRect().addRoundRect(sliderX, sliderY, sliderWidth, sliderHeight, sliderHeight / 2.0f, new Color(60, 60, 60));

        float currentPercent = 0.0f;
        if (setting.getMax() != setting.getMin()) {
            currentPercent = (float) (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        }
        float filledW = sliderWidth * currentPercent;
        filledW = Mth.clamp(filledW, 0.0f, sliderWidth);

        if (filledW > 0) {
            set.bottomRoundRect().addRoundRect(sliderX, sliderY, filledW, sliderHeight, sliderHeight / 2.0f, new Color(55, 180, 90));
        }

        float knobSize = 8.0f * scale;
        float knobX = sliderX + filledW - knobSize / 2.0f;
        float knobY = getY() + (getHeight() - knobSize) / 2.0f;
        set.bottomRoundRect().addRoundRect(knobX, knobY, knobSize, knobSize, knobSize / 2.0f, Color.WHITE);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        if (editing && !MouseUtils.isHovering(lastValueBoxX, lastValueBoxY, lastValueBoxW, lastValueBoxH, event.x(), event.y())) {
            applyEditText();
            editing = false;
            dragging = false;
            return true;
        }

        if (event.button() == 0) {
            if (MouseUtils.isHovering(lastSliderX, lastSliderHitY, lastSliderW, lastSliderHitH, event.x(), event.y())) {
                if (editing) return true;
                dragging = true;

                float mouseRelX = (float) event.x() - lastSliderX;
                float percent = Mth.clamp(mouseRelX / lastSliderW, 0.0f, 1.0f);
                int range = setting.getMax() - setting.getMin();
                int newVal = setting.getMin() + (int) (range * percent);

                if (setting.getStep() > 1) {
                    int step = setting.getStep();
                    int stepped = newVal - setting.getMin();
                    stepped = Math.round((float) stepped / step) * step;
                    newVal = setting.getMin() + stepped;
                }
                setting.setValue(newVal);
                return true;
            }
        }
        if (event.button() == 1) {
            if (MouseUtils.isHovering(lastValueBoxX, lastValueBoxY, lastValueBoxW, lastValueBoxH, event.x(), event.y())) {
                dragging = false;
                editing = true;
                if (setting.isPercentageMode()) {
                    int min = setting.getMin();
                    int max = setting.getMax();
                    int v = setting.getValue();
                    int percent = 0;
                    if (max != min) {
                        percent = Math.round(((float) (v - min) / (float) (max - min)) * 100.0f);
                        percent = Mth.clamp(percent, 0, 100);
                    }
                    editText = String.valueOf(percent);
                } else {
                    editText = String.valueOf(setting.getValue());
                }
                return true;
            }
            if (editing) {
                applyEditText();
                editing = false;
                return true;
            }
        }
        return super.mouseClicked(event, focused);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!editing) return super.keyPressed(event);
        if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
            if (!editText.isEmpty()) {
                editText = editText.substring(0, editText.length() - 1);
            }
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_ENTER) {
            applyEditText();
            editing = false;
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            editing = false;
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (!editing) return super.charTyped(input);
        char c = (char) input.codepoint();
        if (Character.isDigit(c)) {
            editText += c;
            return true;
        }
        if (c == '-' && editText.isEmpty()) {
            editText = "-";
            return true;
        }
        if (c == '%' && setting.isPercentageMode()) {
            return true;
        }
        return super.charTyped(input);
    }

    private void applyEditText() {
        String raw = editText == null ? "" : editText.trim();
        if (raw.isEmpty() || raw.equals("-")) return;
        try {
            if (setting.isPercentageMode()) {
                raw = raw.replace("%", "");
                float p = Float.parseFloat(raw);
                p = Mth.clamp(p, 0.0f, 100.0f);
                int min = setting.getMin();
                int max = setting.getMax();
                int newVal = min;
                if (max != min) {
                    newVal = min + Math.round(((max - min) * (p / 100.0f)));
                }
                if (setting.getStep() > 1) {
                    int step = setting.getStep();
                    int stepped = newVal - min;
                    stepped = Math.round((float) stepped / step) * step;
                    newVal = min + stepped;
                }
                setting.setValue(newVal);
            } else {
                int newVal = Integer.parseInt(raw);
                if (setting.getStep() > 1) {
                    int step = setting.getStep();
                    int min = setting.getMin();
                    int stepped = newVal - min;
                    stepped = Math.round((float) stepped / step) * step;
                    newVal = min + stepped;
                }
                setting.setValue(newVal);
            }
        } catch (NumberFormatException ignored) {
        }
    }
}
