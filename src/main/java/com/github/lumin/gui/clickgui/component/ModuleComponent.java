package com.github.lumin.gui.clickgui.component;

import com.github.lumin.gui.Component;
import com.github.lumin.gui.IComponent;
import com.github.lumin.gui.clickgui.component.impl.*;
import com.github.lumin.modules.Module;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.settings.AbstractSetting;
import com.github.lumin.settings.impl.*;
import com.github.lumin.utils.render.MouseUtils;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ModuleComponent implements IComponent {

    private final Module module;
    private float x, y, width, height;
    private final List<Component> settings = new CopyOnWriteArrayList<>();
    private String filterTextLower = "";
    private float lastBindBoxX, lastBindBoxY, lastBindBoxW, lastBindBoxH;
    private float lastBindModeX, lastBindModeY, lastBindModeW, lastBindModeH;
    private boolean bindingKey;
    private float lastBindListenX, lastBindListenY, lastBindListenW, lastBindListenH;

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
        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float padding = 8.0f * guiScale;
        float rowH = 18.0f * guiScale;
        float rowGap = 4.0f * guiScale;
        float radius = 8.0f * guiScale;

        set.bottomRoundRect().addRoundRect(x, y, width, height, radius, new Color(25, 25, 25, 140));

        float titleScale = 1.15f * guiScale;
        float titleY = y + padding - guiScale;
        set.font().addText(module.getName(), x + padding, titleY, titleScale, Color.WHITE);

        float titleH = set.font().getHeight(titleScale);
        float headerY = titleY - guiScale;
        float headerH = titleH + 4.0f * guiScale;

        String bindText = getKeyBindText();
        float bindTextScale = 0.85f * guiScale;
        float bindTextW = set.font().getWidth(bindText, bindTextScale);

        float bindPad = 6.0f * guiScale;
        float bindBoxW = Math.max(40.0f * guiScale, bindTextW + bindPad * 2.0f);

        String mode0 = InterFace.isEnglish() ? "Toggle" : "切换";
        String mode1 = InterFace.isEnglish() ? "Hold" : "按住";
        float modeTextScale = 0.80f * guiScale;
        float modePad = 7.0f * guiScale;
        float segW = Math.max(set.font().getWidth(mode0, modeTextScale), set.font().getWidth(mode1, modeTextScale)) + modePad * 2.0f;
        float modeW = segW * 2.0f;

        float gap = 6.0f * guiScale;
        float totalW = bindBoxW + gap + modeW;
        float rightX = x + width - padding - totalW;

        lastBindBoxX = rightX;
        lastBindBoxY = headerY;
        lastBindBoxW = bindBoxW;
        lastBindBoxH = headerH;

        float bindRadius = Math.min(6.0f * guiScale, headerH / 2.0f);
        set.bottomRoundRect().addRoundRect(rightX, headerY, bindBoxW, headerH, bindRadius, new Color(0, 0, 0, 70));

        float bindTextX = rightX + (bindBoxW - bindTextW) / 2.0f;
        float bindTextY = headerY + (headerH - set.font().getHeight(bindTextScale)) / 2.0f - guiScale;
        if (!bindingKey) {
            set.font().addText(bindText, bindTextX, bindTextY, bindTextScale, new Color(200, 200, 200));
        }

        lastBindListenX = rightX;
        lastBindListenY = headerY;
        lastBindListenW = bindBoxW;
        lastBindListenH = headerH;

        if (bindingKey) {
            Color listenBg = new Color(255, 255, 255, 22);
            set.bottomRoundRect().addRoundRect(lastBindListenX, lastBindListenY, lastBindListenW, lastBindListenH, bindRadius, listenBg);
            String listenText = InterFace.isEnglish() ? "Listening..." : "按下按键...";

            float maxListenW = lastBindListenW - 4.0f * guiScale;
            float listenScale = 0.80f * guiScale;
            float listenW = set.font().getWidth(listenText, listenScale);

            if (listenW > maxListenW) {
                listenScale = listenScale * (maxListenW / listenW);
                listenW = maxListenW;
            }

            float listenX = lastBindListenX + (lastBindListenW - listenW) / 2.0f;
            float listenY = lastBindListenY + (lastBindListenH - set.font().getHeight(listenScale)) / 2.0f - guiScale;
            set.font().addText(listenText, listenX, listenY, listenScale, new Color(200, 200, 200));

            if (System.currentTimeMillis() % 1000 > 500) {
                float barH = 1.0f * guiScale;
                float barY = listenY + set.font().getHeight(listenScale) + 1.5f * guiScale;
                set.bottomRoundRect().addRoundRect(listenX, barY, listenW, barH, 0.0f, new Color(200, 200, 200));
            }
        }

        float modeX = rightX + bindBoxW + gap;

        lastBindModeX = modeX;
        lastBindModeY = headerY;
        lastBindModeW = modeW;
        lastBindModeH = headerH;

        float modeRadius = Math.min(6.0f * guiScale, headerH / 2.0f);
        set.bottomRoundRect().addRoundRect(modeX, headerY, modeW, headerH, modeRadius, new Color(0, 0, 0, 70));

        int selectedIndex = module.getBindMode() == Module.BindMode.Hold ? 1 : 0;
        java.awt.Color selectedBg = new java.awt.Color(255, 255, 255, 26);
        if (selectedIndex == 0) {
            set.bottomRoundRect().addRoundRect(modeX, headerY, segW, headerH, modeRadius, 0.0f, 0.0f, modeRadius, selectedBg);
        } else {
            set.bottomRoundRect().addRoundRect(modeX + segW, headerY, segW, headerH, 0.0f, modeRadius, modeRadius, 0.0f, selectedBg);
        }

        set.bottomRoundRect().addRoundRect(modeX + segW, headerY + 2.0f * guiScale, 1.0f * guiScale, headerH - 4.0f * guiScale, 0.0f, new Color(255, 255, 255, 14));

        float modeTextY = headerY + (headerH - set.font().getHeight(modeTextScale)) / 2.0f - guiScale;
        float mode0W = set.font().getWidth(mode0, modeTextScale);
        float mode1W = set.font().getWidth(mode1, modeTextScale);
        set.font().addText(mode0, modeX + (segW - mode0W) / 2.0f, modeTextY, modeTextScale, selectedIndex == 0 ? Color.WHITE : new Color(200, 200, 200));
        set.font().addText(mode1, modeX + segW + (segW - mode1W) / 2.0f, modeTextY, modeTextScale, selectedIndex == 1 ? Color.WHITE : new Color(200, 200, 200));

        float cursorY = y + padding + set.font().getHeight(titleScale) + 6.0f * guiScale;
        float itemX = x + padding;
        float itemW = Math.max(0.0f, width - padding * 2);

        for (Component setting : settings) {
            if (!isSettingVisible(setting)) continue;
            setting.setScale(guiScale);
            setting.setX(itemX);
            setting.setY(cursorY);
            setting.setWidth(itemW);
            setting.setHeight(rowH);
            setting.render(set, mouseX, mouseY, partialTicks);
            cursorY += rowH + rowGap;
        }
    }

    public void renderOverlays(RendererSet set, int mouseX, int mouseY, float partialTicks) {
        for (Component setting : settings) {
            if (!isSettingVisible(setting)) continue;
            if (setting instanceof ColorSettingComponent c && c.isOpened()) {
                c.renderOverlay(set, mouseX, mouseY, partialTicks);
            }
        }
    }

    public void renderOverlayBlurs(int mouseX, int mouseY, float partialTicks) {
        for (Component setting : settings) {
            if (!isSettingVisible(setting)) continue;
            if (setting instanceof ColorSettingComponent c && c.isOpened()) {
                c.renderOverlayBlur(mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        boolean handled = false;
        for (Component setting : settings) {
            if (!isSettingVisible(setting)) continue;
            if (setting instanceof ColorSettingComponent c && c.isOpened()) {
                if (c.mouseClicked(event, focused)) {
                    return true;
                }
            }
        }
        if (event.button() == 0) {
            if (MouseUtils.isHovering(lastBindModeX, lastBindModeY, lastBindModeW, lastBindModeH, event.x(), event.y())) {
                float segW = lastBindModeW / 2.0f;
                if (segW > 0.0f) {
                    int index = (int) ((event.x() - lastBindModeX) / segW);
                    index = Math.max(0, Math.min(index, 1));
                    module.setBindMode(index == 0 ? Module.BindMode.Toggle : Module.BindMode.Hold);
                }
                return true;
            }
            if (MouseUtils.isHovering(lastBindBoxX, lastBindBoxY, lastBindBoxW, lastBindBoxH, event.x(), event.y())) {
                bindingKey = true;
                return true;
            }
            if (bindingKey && !MouseUtils.isHovering(lastBindListenX, lastBindListenY, lastBindListenW, lastBindListenH, event.x(), event.y())) {
                bindingKey = false;
            }
        }
        if (event.button() == 1 && bindingKey) {
            bindingKey = false;
            return true;
        }
        if (isHovered((int) event.x(), (int) event.y())) {
            for (Component setting : settings) {
                if (!isSettingVisible(setting)) continue;
                if (setting.mouseClicked(event, focused)) {
                    handled = true;
                }
            }
        }
        return handled || IComponent.super.mouseClicked(event, focused);
    }

    private String getKeyBindText() {
        int keyBind = module.keyBind;
        if (keyBind <= 0) return InterFace.isEnglish() ? "None" : "无";
        int scancode = GLFW.glfwGetKeyScancode(keyBind);
        String name = GLFW.glfwGetKeyName(keyBind, scancode);
        if (name != null && !name.isEmpty()) {
            if (name.length() == 1) return name.toUpperCase();
            return name;
        }
        return switch (keyBind) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_LEFT_SUPER -> "LSUPER";
            case GLFW.GLFW_KEY_RIGHT_SUPER -> "RSUPER";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            case GLFW.GLFW_KEY_INSERT -> "INS";
            case GLFW.GLFW_KEY_DELETE -> "DEL";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_PAGE_UP -> "PGUP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PGDN";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
            case GLFW.GLFW_KEY_NUM_LOCK -> "NUM";
            case GLFW.GLFW_KEY_SCROLL_LOCK -> "SCRL";
            case GLFW.GLFW_KEY_PAUSE -> "PAUSE";
            case GLFW.GLFW_KEY_PRINT_SCREEN -> "PRTSCR";
            default -> {
                if (keyBind >= GLFW.GLFW_KEY_F1 && keyBind <= GLFW.GLFW_KEY_F25) {
                    yield "F" + (keyBind - GLFW.GLFW_KEY_F1 + 1);
                }
                yield "KEY_" + keyBind;
            }
        };
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean handled = false;
        for (Component setting : settings) {
            if (!isSettingVisible(setting)) continue;
            if (setting instanceof ColorSettingComponent c && c.isOpened()) {
                if (c.mouseReleased(event)) {
                    return true;
                }
            }
        }
        if (isHovered((int) event.x(), (int) event.y())) {
            for (Component setting : settings) {
                if (!isSettingVisible(setting)) continue;
                if (setting.mouseReleased(event)) {
                    handled = true;
                }
            }
        }
        return handled || IComponent.super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        boolean handled = false;
        for (Component setting : settings) {
            if (!isSettingVisible(setting)) continue;
            if (setting instanceof ColorSettingComponent c && c.isOpened()) {
                if (c.keyPressed(event)) {
                    return true;
                }
            }
        }
        if (bindingKey) {
            int key = event.key();
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                bindingKey = false;
                return true;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE) {
                module.keyBind = 0;
                bindingKey = false;
                return true;
            }
            if (key != GLFW.GLFW_KEY_UNKNOWN) {
                module.keyBind = key;
                bindingKey = false;
                return true;
            }
        }
        for (Component setting : settings) {
            if (!isSettingVisible(setting)) continue;
            if (setting.keyPressed(event)) {
                handled = true;
            }
        }
        return handled || IComponent.super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        boolean handled = false;
        for (Component setting : settings) {
            if (!isSettingVisible(setting)) continue;
            if (setting instanceof ColorSettingComponent c && c.isOpened()) {
                if (c.charTyped(input)) {
                    return true;
                }
            }
        }
        if (bindingKey) {
            return true;
        }
        for (Component setting : settings) {
            if (!isSettingVisible(setting)) continue;
            if (setting.charTyped(input)) {
                handled = true;
            }
        }
        return handled || IComponent.super.charTyped(input);
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

    public List<Component> getSettings() {
        return settings;
    }

    public void setFilterText(String text) {
        if (text == null || text.isEmpty()) {
            filterTextLower = "";
            return;
        }
        filterTextLower = text.toLowerCase();
    }

    public int getFilteredVisibleCount() {
        int count = 0;
        for (Component setting : settings) {
            if (isSettingVisible(setting)) count++;
        }
        return count;
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

    public boolean isHovered(int mouseX, int mouseY) {
        return MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);
    }

    private boolean isSettingVisible(Component component) {
        if (!component.isVisible()) return false;
        if (!filterTextLower.isEmpty()) {
            String name = getSettingDisplayName(component);
            if (!name.toLowerCase().startsWith(filterTextLower.toLowerCase())) return false;
        }
        return isSettingAvailable(component);
    }

    private boolean isSettingAvailable(Component component) {
        if (component instanceof BoolSettingComponent c) return c.getSetting().isAvailable();
        if (component instanceof IntSettingComponent c) return c.getSetting().isAvailable();
        if (component instanceof DoubleSettingComponent c) return c.getSetting().isAvailable();
        if (component instanceof ModeSettingComponent c) return c.getSetting().isAvailable();
        if (component instanceof ColorSettingComponent c) return c.getSetting().isAvailable();
        if (component instanceof StringSettingComponent c) return c.getSetting().isAvailable();
        return true;
    }

    private String getSettingDisplayName(Component component) {
        if (component instanceof BoolSettingComponent c) return c.getSetting().getDisplayName();
        if (component instanceof IntSettingComponent c) return c.getSetting().getDisplayName();
        if (component instanceof DoubleSettingComponent c) return c.getSetting().getDisplayName();
        if (component instanceof ModeSettingComponent c) return c.getSetting().getDisplayName();
        if (component instanceof ColorSettingComponent c) return c.getSetting().getDisplayName();
        if (component instanceof StringSettingComponent c) return c.getSetting().getDisplayName();
        return null;
    }

}
