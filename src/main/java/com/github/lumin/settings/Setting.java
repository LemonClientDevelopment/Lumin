package com.github.lumin.settings;

public class Setting<V> {

    protected final String chineseName;

    protected V value;
    protected V defaultValue;

    protected final Dependency dependency;

    public Setting(String chineseName, Dependency dependency) {
        this.chineseName = chineseName;
        this.dependency = dependency;
    }

    public Setting(String chineseName) {
        this(chineseName, () -> true);
    }

    public String getDisplayName() {
        return chineseName;
    }

    public String getChineseName() {
        return chineseName;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public void reset() {
        this.value = this.defaultValue;
    }

    public V getDefaultValue() {
        return defaultValue;
    }

    public boolean isAvailable() {
        return dependency != null && this.dependency.check();
    }

    @FunctionalInterface
    public interface Dependency {
        boolean check();
    }

    public Dependency getDependency() {
        return dependency;
    }

}