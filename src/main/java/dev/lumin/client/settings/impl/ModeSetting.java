package dev.lumin.client.settings.impl;

import dev.lumin.client.settings.AbstractSetting;

public class ModeSetting extends AbstractSetting<String> {

    private final String[] modes;

    public ModeSetting(String englishName, String chineseName, String defaultValue, String[] modes, Dependency dependency) {
        super(englishName, chineseName, dependency);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.modes = modes;
    }

    public ModeSetting(String englishName, String chineseName, String defaultValue, String[] modes) {
        this(englishName, chineseName, defaultValue, modes, () -> true);
    }

    public boolean is(String string) {
        return this.getValue().equalsIgnoreCase(string);
    }

    public void setMode(String mode) {
        String[] arrV = this.modes;
        int n = arrV.length;
        int n2 = 0;
        while (n2 < n) {
            String e = arrV[n2];
            if (e == null)
                return;
            if (e.equalsIgnoreCase(mode)) {
                this.setValue(e);
            }
            ++n2;
        }
    }

}
