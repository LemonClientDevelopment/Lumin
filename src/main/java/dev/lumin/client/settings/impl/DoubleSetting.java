package dev.lumin.client.settings.impl;

import dev.lumin.client.settings.AbstractSetting;

public class DoubleSetting extends AbstractSetting<Double> {

    private final double min;
    private final double max;
    private final double step;

    public DoubleSetting(String englishName, String chineseName, double defaultValue, double min, double max, double step, Dependency dependency) {
        super(englishName, chineseName, dependency);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public DoubleSetting(String englishName, String chineseName, double defaultValue, double min, double max, double step) {
        this(englishName, chineseName, defaultValue, min, max, step, () -> true);
    }

    @Override
    public void setValue(Double value) {
        if (value < min) {
            super.setValue(min);
        } else if (value > max) {
            super.setValue(max);
        } else {
            super.setValue(value);
        }
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

}
