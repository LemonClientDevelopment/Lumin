package com.github.lumin.modules.impl.client;

import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
import com.github.lumin.settings.impl.ColorSetting;
import com.github.lumin.settings.impl.ModeSetting;

import java.awt.*;

public class Test2 extends Module {

    public static final Test2 INSTANCE = new Test2();

    public Test2() {
        super("Test2", "测试模块2", "Another test module", "Test2 description", Category.CLIENT);
    }

    public final ModeSetting modeSet = modeSetting("Mode Setting", "模式设置", "ModeA", new String[]{"ModeA", "ModeB", "ModeC"});
    public final ColorSetting colorSet = colorSetting("Color Setting", "颜色设置", new Color(255, 0, 0));

}
