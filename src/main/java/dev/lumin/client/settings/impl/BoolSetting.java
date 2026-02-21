package dev.lumin.client.settings.impl;

import dev.lumin.client.settings.AbstractSetting;

public class BoolSetting extends AbstractSetting<Boolean> {

    public BoolSetting(String name, String chineseName, boolean defaultValue, Dependency dependency) {
        super(name, chineseName, dependency);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public BoolSetting(String name, String chineseName, boolean defaultValue) {
        this(name, chineseName, defaultValue, () -> true);
    }

}
