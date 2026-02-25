package com.github.lumin.gui.clickgui.component;

import com.github.lumin.gui.Component;
import com.github.lumin.gui.IComponent;
import com.github.lumin.gui.clickgui.component.impl.*;
import com.github.lumin.modules.Module;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.settings.AbstractSetting;
import com.github.lumin.settings.impl.*;
import com.github.lumin.utils.render.ColorUtils;
import com.github.lumin.utils.render.MouseUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ModuleComponent implements IComponent {

    private static final int MODULE_HEIGHT = 18;

    private float x, y, width, height = MODULE_HEIGHT;
    private float scale = 1.0f;
    private final Module module;
    private boolean opened = false;
    private boolean listening = false;
    private final CopyOnWriteArrayList<Component> settings = new CopyOnWriteArrayList<>();

    public ModuleComponent(Module module) {
        this.module = module;

        for (AbstractSetting<?> setting : module.getSettings()) {
            if (setting instanceof BoolSetting boolValue) {
                settings.add(new BoolSettingComponent(boolValue));
            } else if (setting instanceof IntSetting intSetting) {
                settings.add(new IntSettingComponent(intSetting));
            } else if (setting instanceof DoubleSetting doubleSetting) {
                settings.add(new DoubleSettingComponent(doubleSetting));
            } else if (setting instanceof ModeSetting modeSetting) {
                settings.add(new ModeSettingComponent(modeSetting));
            } else if (setting instanceof ColorSetting colorSetting) {
                settings.add(new ColorSettingComponent(colorSetting));
            } else if (setting instanceof StringSetting stringSetting) {
                settings.add(new StringSettingComponent(stringSetting));
            }
        }
    }

    @Override
    public void render(RendererSet set, int mouseX, int mouseY, float partialTicks) {
        float scaledHeight = MODULE_HEIGHT * scale;
        float yOffset = scaledHeight;

        boolean hasVisibleSettings = false;
        for (Component component : settings) {
            if (!component.isVisible()) continue;
            hasVisibleSettings = true;
            component.setScale(scale);
            if (opened) {
                yOffset += component.getHeight() * 1.0f/*animation*/;
            }
        }

        if (hasVisibleSettings && opened/*后面改成动画检测，不再检测opened*/) {
            yOffset += 3 * scale * 1.0f/*animation因子*/;
        }

        this.height = yOffset;

        if (module.isEnabled()) {
            set.middleRect().addHorizontalGradient(x, y, width, scaledHeight, InterFace.getMainColor(), InterFace.getSecondColor());
        }

        set.middleRect().addRect(x, y, width, scaledHeight, InterFace.INSTANCE.backgroundColor.getValue());

        if (/*后面改成动画检测，不再检测opened*/opened && hasVisibleSettings) {
            float expandedHeight = yOffset - scaledHeight;
            set.middleRect().addRect(x, y + scaledHeight, width, expandedHeight, InterFace.INSTANCE.expandedBackgroundColor.getValue());
        }
//        set.middleRect().drawAndClear();

        float nameScale = 0.85f * scale;
        float textHeight = set.font().getHeight(nameScale);
        float textY = y + (MODULE_HEIGHT * scale - textHeight) / 2f - scale;
        set.font().addText(module.getDisplayName(), x + 4 * scale, textY, nameScale, Color.WHITE);
//        set.font().drawAndClear();

        float boxWidth = 25 * scale;
        float boxHeight = 12 * scale;
        float boxX = x + width - boxWidth - 4 * scale;
        float boxY = y + (scaledHeight - boxHeight) / 2;

        int keyCode = module.keyBind;
        boolean hasKey = keyCode != 0 && keyCode != GLFW.GLFW_KEY_UNKNOWN;
        boolean isHold = module.getBindMode() == Module.BindMode.Hold;

        Color themeColor = InterFace.getMainColor();
        Color bgColor;
        Color borderColor;

        if (listening) {
            bgColor = new Color(255, 100, 100, 180);
            borderColor = new Color(255, 150, 150, 220);
        } else {
            bgColor = ColorUtils.applyOpacity(themeColor, hasKey ? 0.6f : 0.3f);
            borderColor = ColorUtils.applyOpacity(themeColor, hasKey ? 0.9f : 0.5f);
        }

        set.topRoundRect().addRoundRect(boxX, boxY, boxWidth, boxHeight, 2.5f * scale, bgColor);
        //drawRoundRectOutline(boxX, boxY, boxWidth, boxHeight, 2 * scale, 0.5f * scale, borderColor);
//        set.topRoundRect().drawAndClear();

        float fontScale = 0.65f * scale;
        String displayText = listening ? "..." : (hasKey ? getKeyName(keyCode) : "");
        float maxTextWidth = boxWidth - 4 * scale;
        float textWidth = set.font().getWidth(displayText, fontScale);
        if (textWidth > maxTextWidth) {
            fontScale = fontScale * (maxTextWidth / textWidth);
            textWidth = maxTextWidth;
        }
        float textX = boxX + (boxWidth - textWidth) / 2;
        float bindTextHeight = set.font().getHeight(fontScale);
        float bindTextY = boxY + (boxHeight - bindTextHeight) / 2f - scale;
        if (!displayText.isEmpty()) {
            set.font().addText(displayText, textX, bindTextY, fontScale, Color.WHITE);
        }
//        set.font().drawAndClear();

        if (isHold && !listening) {
            float lineWidth = hasKey ? textWidth + scale : 6 * scale;
            float lineX = hasKey ? textX - 0.5f * scale : boxX + (boxWidth - lineWidth) / 2;
            float lineY = boxY + boxHeight - scale;
            set.middleRect().addRect(lineX, lineY, lineWidth, 0.5f * scale, Color.WHITE);
//            set.middleRect().drawAndClear();
        }

        if (opened) {
            float componentYOffset = scaledHeight;
            for (Component component : settings) {
                if (!component.isVisible()) continue;
                component.setX(x + 4 * scale);
                component.setY(y + 10 * scale + componentYOffset);
                component.setWidth(width - 8 * scale);
                component.render(set, mouseX, mouseY, partialTicks);
                componentYOffset += component.getHeight();
            }
        }

//        IComponent.super.render(set, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        boolean handled = false;
        if (isBindBoxHovered(event)) {
            if (event.button() == 0) {
                listening = !listening;
            } else if (event.button() == 2) {
                module.setBindMode(module.getBindMode() == Module.BindMode.Toggle ? Module.BindMode.Hold : Module.BindMode.Toggle);
            }
            handled = true;
        } else if (listening) {
            listening = false;
            handled = true;
        }

        if (isHovered((int) event.x(), (int) event.y()) && !isBindBoxHovered(event)) {
            switch (event.button()) {
                case 0 -> module.toggle();
                case 1 -> opened = !opened;
            }
            handled = true;
        }
        if (opened && !isHovered((int) event.x(), (int) event.y())) {
            for (Component setting : settings) {
                if (setting.mouseClicked(event, focused)) {
                    handled = true;
                }
            }
        }
        return handled || IComponent.super.mouseClicked(event, focused);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean handled = false;
        if (opened && !isHovered((int) event.x(), (int) event.y())) {
            for (Component setting : settings) {
                if (setting.mouseReleased(event)) {
                    handled = true;
                }
            }
        }
        return handled || IComponent.super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listening) {
            int key = event.key();
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                module.keyBind = 0;
            } else {
                module.keyBind = key;
            }
            listening = false;
            return true;
        }

        boolean handled = false;
        if (opened) {
            for (Component setting : settings) {
                if (setting.keyPressed(event)) {
                    handled = true;
                }
            }
        }
        return handled || IComponent.super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        boolean handled = false;
        if (opened) {
            for (Component setting : settings) {
                if (setting.charTyped(input)) {
                    handled = true;
                }
            }
        }
        return handled || IComponent.super.charTyped(input);
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Module getModule() {
        return module;
    }

    public boolean isOpened() {
        return opened;
    }

    public CopyOnWriteArrayList<Component> getSettings() {
        return settings;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return MouseUtils.isHovering(x, y, width, MODULE_HEIGHT * scale, mouseX, mouseY);
    }

    private boolean isBindBoxHovered(MouseButtonEvent event) {
        float scaledHeight = MODULE_HEIGHT * scale;
        float boxWidth = 18 * scale;
        float boxHeight = 8 * scale;
        float boxX = x + width - boxWidth - 4 * scale;
        float boxY = y + (scaledHeight - boxHeight) / 2;
        return MouseUtils.isHovering(boxX, boxY, boxWidth, boxHeight, event.x(), event.y());
    }

    private String getKeyName(int keyCode) {
        if (keyCode < 0) {
            return "M" + (-keyCode);
        }
        try {
            InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(keyCode);
            String name = key.getDisplayName().getString();
            if (name.length() > 6) {
                name = name.substring(0, 5) + ".";
            }
            return name.toUpperCase();
        } catch (Exception e) {
            return "?";
        }
    }

}
