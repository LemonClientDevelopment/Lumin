package dev.lumin.client.settings;

public abstract class AbstractSetting<V> {

    protected final String englishName;
    protected final String chineseName;

    protected V value;
    protected V defaultValue;

    protected final Dependency dependency;

    public AbstractSetting(String englishName, String chineseName, Dependency dependency) {
        this.englishName = englishName;
        this.chineseName = chineseName;
        this.dependency = dependency;
    }

    public AbstractSetting(String englishName, String chineseName) {
        this(englishName, chineseName, () -> true);
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getChineseName() {
        return chineseName;
    }

    public String getDisplayName() {
        return englishName;
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
