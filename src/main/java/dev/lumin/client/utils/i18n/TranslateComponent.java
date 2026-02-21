package dev.lumin.client.utils.i18n;

public record TranslateComponent(String prefix, String suffix) {

    public static String LUMIN_PREFIX = "lumin";

    public String getFullKey() {
        return LUMIN_PREFIX + "." + prefix + "." + suffix;
    }

}