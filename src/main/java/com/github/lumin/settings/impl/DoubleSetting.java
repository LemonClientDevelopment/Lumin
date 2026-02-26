package com.github.lumin.settings.impl;

import com.github.lumin.settings.AbstractSetting;
import net.minecraft.util.Mth;

public class DoubleSetting extends AbstractSetting<Double> {

    private final double min;
    private final double max;
    private final double step;

    private final boolean percentageMode;

    public DoubleSetting(String englishName, String chineseName, double defaultValue, double min, double max, double step) {
        this(englishName, chineseName, defaultValue, min, max, step, () -> true, false);
    }

    public DoubleSetting(String englishName, String chineseName, double defaultValue, double min, double max, double step, boolean percentageMode) {
        this(englishName, chineseName, defaultValue, min, max, step, () -> true, percentageMode);
    }

    public DoubleSetting(String englishName, String chineseName, double defaultValue, double min, double max, double step, Dependency dependency, boolean percentageMode) {
        super(englishName, chineseName, dependency);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.step = step;
        this.percentageMode = percentageMode;
    }

    @Override
    public void setValue(Double value) {
        super.setValue(Mth.clamp(value, min, max));
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public boolean isPercentageMode() {
        return percentageMode;
    }

}
