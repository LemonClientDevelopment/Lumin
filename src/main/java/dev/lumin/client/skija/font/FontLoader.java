package dev.lumin.client.skija.font;

import io.github.humbleui.skija.Font;

/**
 * FontLoader provides utility methods for retrieving commonly used fonts.
 * Each method returns a Font instance with the specified size.
 *
 * @author quantamyt
 * <p>
 * This code is released under the Creative Commons Attribution 4.0 International License (CC BY 4.0).
 * You are free to share and adapt this code, provided appropriate credit is given to the original author.
 * For more details, visit: <a href="https://creativecommons.org/licenses/by/4.0/deed.en">Creative Commons</a>
 */
public class FontLoader {

    /**
     * Returns the font with the specified size.
     *
     * @param size The size of the font.
     * @return The Greycliff regular font.
     */
    public static Font harmony(float size) {
        return FontManager.font("harmony.ttf", size);
    }

    public static Font greycliffRegular(float size) {
        return FontManager.font("regular.otf", size);
    }

    public static Font greycliffBold(float size) {
        return FontManager.font("regular_bold.otf", size);
    }

    public static Font greycliffMedium(float size) {
        return FontManager.font("regular_medium.otf", size);
    }

    public static Font greycliffSemi(float size) {
        return FontManager.font("regular_semi.otf", size);
    }

    public static Font icon(float size) {
        return FontManager.font("solid.ttf", size);
    }

    public static Font minecraftRegular(float size) {
        return FontManager.font("MinecraftRegular.ttf", size);
    }

    public static Font minecraftItalic(float size) {
        return FontManager.font("MinecraftItalic.otf", size);
    }

    public static Font minecraftBold(float size) {
        return FontManager.font("MinecraftBold.otf", size);
    }

    public static Font icons(float size) {
        return FontManager.font("icon.ttf", size);
    }
}
