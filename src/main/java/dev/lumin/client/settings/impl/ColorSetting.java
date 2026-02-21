package dev.lumin.client.settings.impl;

import dev.lumin.client.settings.AbstractSetting;

import java.awt.*;

public class ColorSetting extends AbstractSetting<Color> {
    public ColorSetting(String englishName, String chineseName, Color defaultValue, Dependency dependency) {
        super(englishName, chineseName, dependency);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public ColorSetting(String englishName, String chineseName, Color defaultValue) {
        this(englishName, chineseName, defaultValue, () -> true);
    }
}
