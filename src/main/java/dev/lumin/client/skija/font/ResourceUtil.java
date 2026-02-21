package dev.lumin.client.skija.font;

import io.github.humbleui.skija.Data;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling resource loading.
 *
 * @author quantamyt
 * <p>
 * This code is released under the Creative Commons Attribution 4.0 International License (CC BY 4.0).
 * You are free to share and adapt this code, provided appropriate credit is given to the original author.
 * For more details, visit: <a href="https://creativecommons.org/licenses/by/4.0/deed.en">Creative Commons</a>
 */
public class ResourceUtil {
    private static final Logger LOGGER = Logger.getLogger(ResourceUtil.class.getName());

    /**
     * Reads the bytes of a resource from the given path.
     *
     * @param path the path of the resource
     * @return byte array of the resource
     */
    public static Optional<byte[]> convertToBytes(String path) {
        try (InputStream inputStream = getResourceAsStream(path)) {
            return Optional.of(inputStream.readAllBytes());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read bytes from resource: " + path, e);
            return Optional.empty();
        }
    }

    /**
     * Converts a resource to a Data object.
     *
     * @param path the path of the resource
     * @return Data object or empty if resource not found
     */
    public static Optional<Data> convertToData(String path) {
        return convertToBytes(path).map(Data::makeFromBytes);
    }

    /**
     * Creates an ImageInputStream from the specified resource name.
     *
     * @param resourceName the name of the resource
     * @return ImageInputStream or empty if resource not found
     */
    public static Optional<ImageInputStream> convertToImageInputStream(String resourceName) {
        try (InputStream stream = getResourceAsStream(resourceName)) {
            return Optional.of(ImageIO.createImageInputStream(stream));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create ImageInputStream for resource: " + resourceName, e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves an InputStream for a given resource path.
     *
     * @param path the path of the resource
     * @return InputStream for the resource
     * @throws IllegalArgumentException if the resource cannot be found
     */
    public static InputStream getResourceAsStream(String path) {
        InputStream inputStream = ResourceUtil.class.getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + path);
        }
        return inputStream;
    }

    /**
     * Retrieves an InputStream from a file path.
     *
     * @param filePath the file path
     * @return InputStream for the file
     * @throws IOException if an I/O error occurs
     */
    public static InputStream getFileAsStream(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.newInputStream(path);
    }
}
