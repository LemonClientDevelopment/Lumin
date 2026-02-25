package com.github.lumin.modules.impl.client;

import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
import com.github.lumin.settings.impl.BoolSetting;
import com.github.lumin.settings.impl.ColorSetting;
import com.github.lumin.settings.impl.DoubleSetting;
import com.github.lumin.settings.impl.ModeSetting;

import java.awt.*;

public class InterFace extends Module {
    public static InterFace INSTANCE = new InterFace();

    public InterFace() {
        super("Interface", "界面", "idk", "idk", Category.CLIENT);
    }

    public final ModeSetting language = modeSetting("Language", "语言", "English", new String[]{"English", "Chinese"});

    public final DoubleSetting scale = doubleSetting("Gui Scale", "界面缩放", 1.0, 0.5, 2.0, 0.05);

    public final ColorSetting mainColor = colorSetting("Main Color", "主色调", new Color(255, 183, 197, 255));
    public final ColorSetting secondColor = colorSetting("Second Color", "次色调", new Color(255, 133, 161, 255));
    public final ColorSetting backgroundColor = colorSetting("Background Color", "背景颜色", new Color(28, 28, 28, 120));
    public final ColorSetting expandedBackgroundColor = colorSetting("Expanded Background", "展开背景颜色", new Color(20, 20, 20, 120));

    public final BoolSetting backgroundBlur = boolSetting("Background Blur", "背景模糊", true);
    public final DoubleSetting blurStrength = doubleSetting("Blur Strength", "模糊强度", 8.0, 1.0, 20.0, 0.5, backgroundBlur::getValue);
    public final ModeSetting blurMode = modeSetting("Blur Mode", "模糊方式", "OnlyCategory", new String[]{"FullScreen", "OnlyCategory"}, backgroundBlur::getValue);

    public static boolean isEnglish() {
        return INSTANCE.language.is("English");
    }

    public static Color getMainColor() {
        return INSTANCE.mainColor.getValue();
    }

    public static Color getSecondColor() {
        return INSTANCE.secondColor.getValue();
    }
}
