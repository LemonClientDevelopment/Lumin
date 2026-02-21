package dev.lumin.client.skija.font;


import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Typeface;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * FontManager is responsible for managing fonts and their associated typefaces.
 * It provides methods to retrieve fonts from cached data or load them from resources.
 *
 * @author quantamyt
 * <p>
 * This code is released under the Creative Commons Attribution 4.0 International License (CC BY 4.0).
 * You are free to share and adapt this code, provided appropriate credit is given to the original author.
 * For more details, visit: <a href="https://creativecommons.org/licenses/by/4.0/deed.en">Creative Commons</a>
 */
public class FontManager {
    private static final Map<String, Font> fontCache = new HashMap<>();
    private static final Map<String, Typeface> typefaceCache = new HashMap<>();

    /**
     * Retrieves the Typeface for the specified font.
     *
     * @param font The font name.
     * @param type The font type.
     * @return The Typeface for the specified font.
     * @throws IllegalArgumentException If the font could not be found.
     */
    private static Typeface getTypeface(String font, FontType type) {
        return typefaceCache.computeIfAbsent(font, k -> loadTypeface(k, type));
    }

    /**
     * Loads a Typeface from the specified font resource.
     *
     * @param font The font name.
     * @param type The font type.
     * @return The loaded Typeface.
     * @throws IllegalArgumentException If the font data could not be found.
     */
    private static Typeface loadTypeface(String font, FontType type) {
        Optional<Data> fontDataOptional = ResourceUtil.convertToData("/assets/alisa/fonts/ui/" + font);
        return fontDataOptional
                .map(Typeface::makeFromData)
                .orElseThrow(() -> new IllegalArgumentException("Font not found: " + font));
    }

    /**
     * Retrieves a Font instance for the specified font name and size.
     *
     * @param font     The font name.
     * @param size     The size of the font.
     * @param fontType The type of the font.
     * @return The Font instance.
     */
    public static Font font(String font, float size, FontType fontType) {
        String key = font + "-" + size;
        return fontCache.computeIfAbsent(key, k -> new Font(getTypeface(font, fontType), size));
    }

    /**
     * Retrieves a Font instance for the specified font name and size,
     * automatically detecting the font type.
     *
     * @param font The font name.
     * @param size The size of the font.
     * @return The Font instance.
     */
    public static Font font(String font, float size) {
        return font(font, size, getFontType(font)); // Automatically detect font type
    }

    /**
     * Automatically determines the FontType based on the file extension of the font.
     *
     * @param font The font name.
     * @return The determined FontType.
     */
    private static FontType getFontType(String font) {
        String fileExtension = font.substring(font.lastIndexOf('.') + 1).toLowerCase();
        return FontType.fromString(fileExtension);
    }
}
