package com.github.lumin.modules.impl.client;

import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
import com.github.lumin.settings.impl.ModeSetting;

public class InterFace extends Module {
    public static InterFace INSTANCE = new InterFace();

    public InterFace() {
        super("Interface", "界面", Category.CLIENT);
    }

    public final ModeSetting language = modeSetting("Language", "语言", "English", new String[]{"English", "Chinese"});

    public static boolean isEnglish() {
        return INSTANCE.language.is("English");
    }
}
