package dev.lumin.client.settings.impl;

import dev.lumin.client.settings.AbstractSetting;

public class StringSetting extends AbstractSetting<String> {

    public StringSetting(String englishName, String chineseName, String defaultValue, Dependency dependency) {
        super(englishName, chineseName, dependency);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public StringSetting(String englishName, String chineseName, String defaultValue) {
        this(englishName, chineseName, defaultValue, () -> true);
    }

}
