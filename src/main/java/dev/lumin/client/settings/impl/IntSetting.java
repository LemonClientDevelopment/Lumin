package dev.lumin.client.settings.impl;

import dev.lumin.client.settings.AbstractSetting;
import net.minecraft.util.Mth;

public class IntSetting extends AbstractSetting<Integer> {

    private final int min;
    private final int max;
    private final int step;

    public IntSetting(String englishName, String chineseName, int defaultValue, int min, int max, int step, Dependency dependency) {
        super(englishName, chineseName, dependency);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public IntSetting(String englishName, String chineseName, int defaultValue, int min, int max, int step) {
        this(englishName, chineseName, defaultValue, min, max, step, () -> true);
    }

    @Override
    public void setValue(Integer value) {
        super.setValue(Mth.clamp(value, min, max));
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getStep() {
        return step;
    }

}
