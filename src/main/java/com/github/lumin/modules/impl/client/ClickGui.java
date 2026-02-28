package com.github.lumin.modules.impl.client;

import com.github.lumin.gui.clickgui.ClickGuiScreen;
import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
import com.github.lumin.settings.impl.BoolSetting;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;

public class ClickGui extends Module {

    public static final ClickGui INSTANCE = new ClickGui();

    public ClickGui() {
        super("ClickGui", "控制面板", "idk", "idk", Category.CLIENT);
        keyBind = GLFW_KEY_RIGHT_SHIFT;
    }

    @Override
    protected void onEnable() {
        if (nullCheck()) return;
        mc.setScreen(new ClickGuiScreen());
    }

    @Override
    protected void onDisable() {
        if (mc.screen instanceof ClickGuiScreen) {
            mc.setScreen(null);
        }
    }

}
