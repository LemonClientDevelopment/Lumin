package com.github.lumin.gui.clickgui.component.impl;

import com.github.lumin.gui.Component;
import com.github.lumin.settings.impl.DoubleSetting;
import com.github.lumin.utils.render.MouseUtils;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class DoubleSettingComponent extends Component {
    private final DoubleSetting setting;
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

    public DoubleSettingComponent(DoubleSetting setting) {
        this.setting = setting;
    }

    public DoubleSetting getSetting() {
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
        if (editing) {
            if (setting.isPercentageMode()) {
                valueStr = editText + "%";
                if (System.currentTimeMillis() % 1000 > 500) valueStr += "_";
            } else {
                valueStr = editText;
                if (System.currentTimeMillis() % 1000 > 500) valueStr += "_";
            }
        } else if (setting.isPercentageMode()) {
            double min = setting.getMin();
            double max = setting.getMax();
            double v = setting.getValue();
            int percent = 0;
            if (max != min) {
                percent = (int) Math.round(((v - min) / (max - min)) * 100.0);
                percent = Math.max(0, Math.min(100, percent));
            }
            valueStr = percent + "%";
        } else {
            valueStr = String.format("%.1f", setting.getValue());
        }

        float valueInnerPad = 4.0f * scale;
        String minStr = String.format("%.1f", setting.getMin());
        String maxStr = String.format("%.1f", setting.getMax());
        float valueMinW = set.font().getWidth(minStr, textScale);
        float valueMaxW = set.font().getWidth(maxStr, textScale);
        float valuePercentW = set.font().getWidth("100%", textScale);
        float valueBoxW = Math.max(valueMinW, Math.max(valueMaxW, valuePercentW)) + valueInnerPad * 2.0f;
        float valueBoxH = Math.max(0.0f, getHeight() - 4.0f * scale);
        float valueBoxX = getX() + getWidth() - padding - valueBoxW;
        float valueBoxY = getY() + (getHeight() - valueBoxH) / 2.0f;
        lastValueBoxX = valueBoxX;
        lastValueBoxY = valueBoxY;
        lastValueBoxW = valueBoxW;
        lastValueBoxH = valueBoxH;

        Color valueBg = editing ? new Color(255, 255, 255, 22) : new Color(255, 255, 255, 12);
        set.bottomRoundRect().addRoundRect(valueBoxX, valueBoxY, valueBoxW, valueBoxH, 6.0f * scale, valueBg);

        float valueW = Math.min(set.font().getWidth(valueStr, textScale), Math.max(0.0f, valueBoxW - valueInnerPad * 2.0f));
        float valueX = valueBoxX + (valueBoxW - valueW) / 2.0f;
        float valueTextY = valueBoxY + (valueBoxH - set.font().getHeight(textScale)) / 2.0f - 0.5f * scale;
        set.font().addText(valueStr, valueX, valueTextY, textScale, new Color(200, 200, 200));

        float sliderWidth = 60.0f * scale;
        float sliderHeight = 3.0f * scale;
        float sliderX = valueBoxX - padding - sliderWidth;
        float sliderY = getY() + (getHeight() - sliderHeight) / 2.0f;
        lastSliderX = sliderX;
        lastSliderW = sliderWidth;
        lastSliderHitH = Math.max(12.0f * scale, sliderHeight);
        lastSliderHitY = getY() + (getHeight() - lastSliderHitH) / 2.0f;

        if (!editing && dragging) {
            float mouseRelX = mouseX - sliderX;
            float percent = Math.max(0.0f, Math.min(1.0f, mouseRelX / sliderWidth));
            double range = setting.getMax() - setting.getMin();
            double newVal = setting.getMin() + (range * percent);

            if (setting.getStep() > 0) {
                double step = setting.getStep();
                double stepped = newVal - setting.getMin();
                stepped = Math.round(stepped / step) * step;
                newVal = setting.getMin() + stepped;
            }
            setting.setValue(newVal);
        }

        set.bottomRoundRect().addRoundRect(sliderX, sliderY, sliderWidth, sliderHeight, sliderHeight / 2.0f, new Color(60, 60, 60));

        float currentPercent = 0.0f;
        if (setting.getMax() != setting.getMin()) {
            currentPercent = (float) ((setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin()));
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
        if (event.button() == 0) {
            if (editing && !MouseUtils.isHovering(lastValueBoxX, lastValueBoxY, lastValueBoxW, lastValueBoxH, event.x(), event.y())) {
                applyEditText();
                editing = false;
            }

            if (MouseUtils.isHovering(lastSliderX, lastSliderHitY, lastSliderW, lastSliderHitH, event.x(), event.y())) {
                if (editing) return true;
                dragging = true;

                float mouseRelX = (float) event.x() - lastSliderX;
                float percent = Math.max(0.0f, Math.min(1.0f, mouseRelX / lastSliderW));
                double range = setting.getMax() - setting.getMin();
                double newVal = setting.getMin() + (range * percent);

                if (setting.getStep() > 0) {
                    double step = setting.getStep();
                    double stepped = newVal - setting.getMin();
                    stepped = Math.round(stepped / step) * step;
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
                    double min = setting.getMin();
                    double max = setting.getMax();
                    double v = setting.getValue();
                    int percent = 0;
                    if (max != min) {
                        percent = (int) Math.round(((v - min) / (max - min)) * 100.0);
                        percent = Math.max(0, Math.min(100, percent));
                    }
                    editText = String.valueOf(percent);
                } else {
                    editText = String.format("%.1f", setting.getValue());
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
        if (c == '.' && !editText.contains(".")) {
            editText += ".";
            return true;
        }
        if (c == '%' && setting.isPercentageMode()) {
            return true;
        }
        return super.charTyped(input);
    }

    private void applyEditText() {
        String raw = editText == null ? "" : editText.trim();
        if (raw.isEmpty() || raw.equals("-") || raw.equals(".")) return;
        try {
            if (setting.isPercentageMode()) {
                raw = raw.replace("%", "");
                double p = Double.parseDouble(raw);
                p = Math.max(0.0, Math.min(100.0, p));
                double min = setting.getMin();
                double max = setting.getMax();
                double newVal = min;
                if (max != min) {
                    newVal = min + ((max - min) * (p / 100.0));
                }
                if (setting.getStep() > 0) {
                    double step = setting.getStep();
                    double stepped = newVal - min;
                    stepped = Math.round(stepped / step) * step;
                    newVal = min + stepped;
                }
                setting.setValue(newVal);
            } else {
                double newVal = Double.parseDouble(raw);
                if (setting.getStep() > 0) {
                    double step = setting.getStep();
                    double min = setting.getMin();
                    double stepped = newVal - min;
                    stepped = Math.round(stepped / step) * step;
                    newVal = min + stepped;
                }
                setting.setValue(newVal);
            }
        } catch (NumberFormatException ignored) {
        }
    }
}
