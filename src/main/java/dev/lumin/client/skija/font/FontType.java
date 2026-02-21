package dev.lumin.client.skija.font;

import lombok.Getter;

/**
 * FontType is an enumeration representing the different types of font formats supported.
 * It provides a way to map file extensions to their respective font types.
 *
 * @author quantamyt
 * <p>
 * This code is released under the Creative Commons Attribution 4.0 International License (CC BY 4.0).
 * You are free to share and adapt this code, provided appropriate credit is given to the original author.
 * For more details, visit: <a href="https://creativecommons.org/licenses/by/4.0/deed.en">Creative Commons</a>
 */
@Getter
public enum FontType {
    TTF("ttf"),
    OTF("otf");

    private final String string;

    FontType(String string) {
        this.string = string;
    }

    /**
     * Returns the FontType associated with the given file extension.
     *
     * @param extension The file extension of the font.
     * @return The corresponding FontType.
     * @throws IllegalArgumentException If the font type is unsupported.
     */
    public static FontType fromString(String extension) {
        // Normalize the extension to lower case to handle different casing
        String normalizedExtension = extension.toLowerCase();
        return switch (normalizedExtension) {
            case "ttf" -> TTF;
            case "otf" -> OTF;
            default -> throw new IllegalArgumentException("Unsupported font type: " + extension);
        };
    }

    @Override
    public String toString() {
        return string;
    }
}
