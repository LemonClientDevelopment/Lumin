package com.github.lumin.settings.impl;

import com.github.lumin.settings.AbstractSetting;
import net.minecraft.util.Mth;

public class IntSetting extends AbstractSetting<Integer> {

    private final int min;
    private final int max;
    private final int step;

    private final boolean percentageMode;

    public IntSetting(String englishName, String chineseName, int defaultValue, int min, int max, int step) {
        this(englishName, chineseName, defaultValue, min, max, step, () -> true, false);
    }

    public IntSetting(String englishName, String chineseName, int defaultValue, int min, int max, int step, boolean percentageMode) {
        this(englishName, chineseName, defaultValue, min, max, step, () -> true, percentageMode);
    }

    public IntSetting(String englishName, String chineseName, int defaultValue, int min, int max, int step, Dependency dependency, boolean percentageMode) {
        super(englishName, chineseName, dependency);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.step = step;
        this.percentageMode = percentageMode;
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

    public boolean isPercentageMode() {
        return percentageMode;
    }

}
