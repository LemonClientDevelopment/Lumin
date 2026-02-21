package dev.lumin.client.skija.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.lumin.client.modules.impl.client.InterFace;
import dev.lumin.client.skija.Skija;
import io.github.humbleui.skija.*;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontMetrics;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class providing helper methods for rendering with Skia.
 *
 * @author quantamyt
 * <p>
 * This code is released under the Creative Commons Attribution 4.0 International License (CC BY 4.0).
 * You are free to share and adapt this code, provided appropriate credit is given to the original author.
 * For more details, visit: <a href="https://creativecommons.org/licenses/by/4.0/deed.en">Creative Commons</a>
 */
public class SkijaHelper {

    private static final Map<String, MaskFilter> BLUR_MASK_CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, Image> textures = new HashMap<>();
    private static Minecraft mc = Minecraft.getInstance();

    /**
     * Retrieves the current {@link Canvas} instance used for drawing with Skia.
     *
     * @return the current {@link Canvas} instance.
     */
    private static Canvas getCanvas() {
        return Skija.canvas;
    }

    /**
     * Draws a string at the specified position using the given font and paint.
     *
     * @param x     The x-coordinate for the text.
     * @param y     The y-coordinate for the text.
     * @param text  The text to draw.
     * @param font  The font to use for drawing the text.
     * @param paint The paint used for coloring the text.
     * @return The height of the drawn text.
     */
    public static float drawString(String text, float x, float y, Font font, Paint paint) {
        float drawX = adjustTextX(x);
        float drawY = adjustTextY(y, 0);
        getCanvas().drawString(text, drawX, drawY, font, paint);
        return 0;
    }

    public static float drawStringBounds(String text, float x, float y, Font font, Paint paint) {
        Rect bounds = font.measureText(text);
        float drawX = adjustTextX(x);
        float drawY = adjustTextY(y, bounds.getHeight());
        getCanvas().drawString(text, drawX, drawY, font, paint);
        return bounds.getHeight();
    }

    public static float getTextHeight(Font font, String text) {
        return font.measureText(text).getHeight();
    }

    public static Image getMinecraftAsImage() {
        RenderTarget fb = mc.getMainRenderTarget();

        int tex = fb.getColorTextureId();
        int width = fb.viewWidth;
        int height = fb.viewHeight;
        Image img = textures.get(tex);
        if (img == null || img.getWidth() != width || img.getHeight() != height) {
            img = Image.adoptGLTextureFrom(Skija.context, fb.getColorTextureId(), GL11.GL_TEXTURE_2D, width, height, GL11.GL_RGBA8, SurfaceOrigin.BOTTOM_LEFT, ColorType.RGBA_8888);
            textures.put(tex, img);
        }
        return img;
    }

    /**
     * Draws a glowing string with an outer blur before rendering the crisp text.
     *
     * @param text       The text to draw.
     * @param x          The x-coordinate for the text.
     * @param y          The y-coordinate for the text.
     * @param font       The font to use when rendering the text.
     * @param textPaint  The paint used for the main text fill.
     * @param glowRadius The blur radius of the glow (higher values produce a softer glow).
     * @return The height of the drawn text.
     */
    public static float drawGlowingString(String text, float x, float y, Font font, Paint textPaint, float glowRadius) {
        Rect bounds = font.measureText(text);
        MaskFilter blurMask = MaskFilter.makeBlur(InterFace.filterBlurMode(), glowRadius);

        float drawX = adjustTextX(x);
        float drawY = adjustTextY(y, bounds.getHeight());
        Canvas canvas = getCanvas();
        canvas.drawString(text, drawX, drawY, font, textPaint
                .setMaskFilter(blurMask));
        return bounds.getHeight();
    }

    /**
     * Draws centered text at the specified position.
     *
     * @param x     The x-coordinate for the text center.
     * @param y     The y-coordinate for the text center.
     * @param text  The text to draw.
     * @param font  The font to use for drawing the text.
     * @param paint The paint used for coloring the text.
     * @return The height of the drawn text.
     */
    public static float drawCenteredString(String text, float x, float y, Font font, Paint paint) {
        Rect measure = font.measureText(text);
        return drawString(text, x - measure.getWidth() / 2f, y - measure.getHeight() / 2f, font, paint);
    }

    /**
     * Creates a Paint object using various color formats.
     *
     * @param color The color to use (Color4f or Color).
     * @return A Paint object configured with the specified color.
     */
    public static Paint paintColor(Object color) {
        Paint paint = new Paint();
        if (color instanceof Color4f) {
            paint.setColor4f((Color4f) color);
        } else if (color instanceof Color c) {
            paint.setARGB(c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
        }
        return paint;
    }

    /**
     * Returns the width of the specified text when drawn with the given font.
     *
     * @param text The text to measure.
     * @param font The font to use for measuring.
     * @return The width of the text.
     */
    public static float getTextWidth(String text, Font font) {
        return font.measureTextWidth(text);
    }

    public static void drawCircle(float x, float y, float size, Paint paint) {
        getCanvas().drawCircle(x, y, size, paint);
    }

    /**
     * Draws a rounded rectangle at the specified position.
     *
     * @param x      The x-coordinate for the rectangle.
     * @param y      The y-coordinate for the rectangle.
     * @param w      The width of the rectangle.
     * @param h      The height of the rectangle.
     * @param radius The radius of the rounded corners.
     * @param paint  The paint used for coloring the rectangle.
     */
    public static void drawRoundRect(float x, float y, float w, float h, float radius, Paint paint) {
        getCanvas().drawRRect(RRect.makeXYWH(x, y, w, h, radius), paint);
    }

    public static void drawRoundRectBloom(float x, float y, float w, float h, float radius, Paint paint) {
        getCanvas().drawRRect(RRect.makeXYWH(x, y, w, h, radius), paint.setMaskFilter(MaskFilter.makeBlur(InterFace.filterBlurMode(), 8)));
    }

    /**
     * Creates an ARGB color from a Color object.
     *
     * @param color The Color object to convert.
     * @return The ARGB integer representation of the color.
     */
    private static int colorToArgb(Color color) {
        return (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }

    /**
     * Draws a gradient rounded rectangle.
     *
     * @param x          The x-coordinate for the rectangle.
     * @param y          The y-coordinate for the rectangle.
     * @param w          The width of the rectangle.
     * @param h          The height of the rectangle.
     * @param radius     The radius of the rounded corners.
     * @param startColor The starting color of the gradient.
     * @param endColor   The ending color of the gradient.
     */
    public static void drawGradientRRect(float x, float y, float w, float h, float radius, Color startColor, Color endColor) {
        int[] colors = new int[]{
                colorToArgb(startColor),
                colorToArgb(endColor)
        };

        float[] positions = {0.0f, 1.0f};

        Shader gradient = Shader.makeLinearGradient(x, y, x, y + h, colors, positions, GradientStyle.DEFAULT);
        Paint gradientPaint = new Paint().setShader(gradient);
        getCanvas().drawRRect(RRect.makeXYWH(x, y, w, h, radius), gradientPaint);
    }

    public static Rect rect(float x, float y, float w, float h) {
        return Rect.makeXYWH(x, y, w, h);
    }

    /**
     * Draws a gradient rounded rectangle with horizontal (left-to-right) gradient.
     *
     * @param x          The x-coordinate for the rectangle.
     * @param y          The y-coordinate for the rectangle.
     * @param w          The width of the rectangle.
     * @param h          The height of the rectangle.
     * @param radius     The radius of the rounded corners.
     * @param startColor The starting color of the gradient (left side).
     * @param endColor   The ending color of the gradient (right side).
     */
    public static void drawGradientRRect2(float x, float y, float w, float h, float radius, Color startColor, Color endColor) {
        int[] colors = new int[]{
                colorToArgb(startColor),
                colorToArgb(endColor)
        };

        float[] positions = {0.0f, 1.0f};

        Shader gradient = Shader.makeLinearGradient(x, y, x + w, y, colors, positions, GradientStyle.DEFAULT);
        Paint gradientPaint = new Paint().setShader(gradient);
        getCanvas().drawRRect(RRect.makeXYWH(x, y, w, h, radius), gradientPaint);
    }

    /**
     * 我操你妈 老子上去俩双线渐变干上去 这个和189一样原理 也就是抽了一根烟写出来的 呵呵呵
     */
    public static void drawGradientRRect3(float x, float y, float w, float h, float radius,
                                          Color bottomLeft, Color topLeft, Color bottomRight, Color topRight) {
        int[] topColors = {colorToArgb(topLeft), colorToArgb(topRight)};
        int[] bottomColors = {colorToArgb(bottomLeft), colorToArgb(bottomRight)};

        getCanvas().saveLayer(null, null);

        Shader verticalGradient = Shader.makeLinearGradient(x, y, x, y + h, topColors);
        Paint verticalPaint = new Paint();
        verticalPaint.setShader(verticalGradient);
        getCanvas().drawRRect(RRect.makeXYWH(x, y, w, h, radius), verticalPaint);

        int[] leftColors = {colorToArgb(topLeft), colorToArgb(bottomLeft)};

        Shader horizontalGradient = Shader.makeLinearGradient(x, y, x + w, y, leftColors);
        Paint horizontalPaint = new Paint();
        horizontalPaint.setShader(horizontalGradient);
        horizontalPaint.setBlendMode(BlendMode.MULTIPLY);

        getCanvas().drawRRect(RRect.makeXYWH(x, y, w, h, radius), horizontalPaint);

        getCanvas().restore();
    }

    /**
     * Draws a shadowed rectangle with configurable properties.
     *
     * @param x       The x-coordinate for the rectangle.
     * @param y       The y-coordinate for the rectangle.
     * @param w       The width of the rectangle.
     * @param h       The height of the rectangle.
     * @param radius  The radius of the rounded corners.
     * @param color   The color of the shadow.
     * @param blur    The blur radius of the shadow.
     * @param offsetX The x-offset for the shadow.
     * @param offsetY The y-offset for the shadow.
     */
    public static void drawShadow(float x, float y, float w, float h, float radius, Color color, float blur, float offsetX, float offsetY) {
        drawShadow(RRect.makeXYWH(x, y, w, h, radius), color, blur, offsetX, offsetY);
    }

    /**
     * Draws a shadowed rectangle with configurable properties.
     *
     * @param x       The x-coordinate for the rectangle.
     * @param y       The y-coordinate for the rectangle.
     * @param w       The width of the rectangle.
     * @param h       The height of the rectangle.
     * @param radius  The radius of the rounded corners.
     * @param color   The color of the shadow.
     * @param blur    The blur radius of the shadow.
     * @param offsetX The x-offset for the shadow.
     * @param offsetY The y-offset for the shadow.
     */
    public static void drawRectShadowNoClip(float x, float y, float w, float h, float radius, Color color, float blur, float offsetX, float offsetY) {
        drawShadow(RRect.makeXYWH(x, y, w, h, radius), color, blur, offsetX, offsetY);
    }

    /**
     * Draws a shadowed rectangle with configurable properties.
     *
     * @param x       The x-coordinate for the rectangle.
     * @param y       The y-coordinate for the rectangle.
     * @param w       The width of the rectangle.
     * @param h       The height of the rectangle.
     * @param color   The color of the shadow.
     * @param blur    The blur radius of the shadow.
     * @param offsetX The x-offset for the shadow.
     * @param offsetY The y-offset for the shadow.
     */
    public static void drawShadow(float x, float y, float w, float h, Color color, float blur, float offsetX, float offsetY) {
        drawShadow(Rect.makeXYWH(x, y, w, h), color, blur, offsetX, offsetY);
    }

    /**
     * Draws a shadowed rectangle with configurable properties.
     *
     * @param x       The x-coordinate for the rectangle.
     * @param y       The y-coordinate for the rectangle.
     * @param w       The width of the rectangle.
     * @param h       The height of the rectangle.
     * @param color   The color of the shadow.
     * @param blur    The blur radius of the shadow.
     * @param offsetX The x-offset for the shadow.
     * @param offsetY The y-offset for the shadow.
     */
    public static void drawRectShadowNoClip(float x, float y, float w, float h, Color color, float blur, float offsetX, float offsetY) {
        drawShadow(Rect.makeXYWH(x, y, w, h), color, blur, offsetX, offsetY);
    }

    private static float adjustTextX(float x) {
        return x - 2f;
    }

    private static float adjustTextY(float y, float height) {
        return (y - 1f) + height;
    }

    private static Paint createShadowPaint(Color color, float blurRadius) {
        Paint paint = paintColor(color);
        // TODO: 代办
        //paint.setMaskFilter(getBlurMaskFilter(InterFace.filterBlurMode(), blurRadius));
        return paint;
    }

    private static void drawShadow(Rect rect, Color color, float blurRadius, float offsetX, float offsetY) {
        Canvas canvas = getCanvas();
        Paint paint = createShadowPaint(color, blurRadius);
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.drawRect(rect, paint);
        canvas.restore();
    }

    private static void drawShadow(RRect rect, Color color, float blurRadius, float offsetX, float offsetY) {
        Canvas canvas = getCanvas();
        Paint paint = createShadowPaint(color, blurRadius);
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.drawRRect(rect, paint);
        canvas.restore();
    }

    private static MaskFilter getBlurMaskFilter(FilterBlurMode style, float radius) {
        float sigma = normalizeSigma(radius);
        float keySigma = Math.round(sigma * 100f) / 100f;
        String key = style.ordinal() + ":" + keySigma;
        return BLUR_MASK_CACHE.computeIfAbsent(key, ignored -> MaskFilter.makeBlur(style, keySigma, true));
    }

    private static float normalizeSigma(float radius) {
        if (radius <= 0f) {
            return 0.5f;
        }
        return Math.max(0.5f, radius);
    }

    /**
     * Draws a rectangle.
     *
     * @param x     The x-coordinate for the rectangle.
     * @param y     The y-coordinate for the rectangle.
     * @param w     The width of the rectangle.
     * @param h     The height of the rectangle.
     * @param paint The paint used for coloring the rectangle.
     */
    public static void drawRect(float x, float y, float w, float h, Paint paint) {
        getCanvas().drawRect(Rect.makeXYWH(x, y, w, h), paint);
    }

    /**
     * Draws a rounded blurred rectangle at the specified position.
     *
     * @param x      The x-coordinate for the rectangle.
     * @param y      The y-coordinate for the rectangle.
     * @param w      The width of the rectangle.
     * @param h      The height of the rectangle.
     * @param radius The radius of the rounded corners.
     */
    public static void drawRRectBlur(float x, float y, float w, float h, float radius, float blurRadius) {
        Canvas canvas = getCanvas();
        Image textureImage = getMinecraftAsImage();
        Paint paint = new Paint();
        paint.setImageFilter(ImageFilter.makeBlur(blurRadius, blurRadius, FilterTileMode.CLAMP));
        canvas.saveLayer(null, paint);
        canvas.drawRRect(RRect.makeXYWH(x, y, w, h, radius), new Paint());
        canvas.clipRRect(RRect.makeXYWH(x, y, w, h, radius), ClipMode.INTERSECT);
        canvas.drawImage(textureImage, 0, 0);
        canvas.restore();
    }

    public static void drawRRectWithBlur(float x, float y, float w, float h, float radius, float blurRadius, int color) {
        Canvas canvas = getCanvas();
        Image background = getMinecraftAsImage();
        ImageFilter blur = ImageFilter.makeBlur(blurRadius, blurRadius, FilterTileMode.CLAMP);
        Paint blurPaint = new Paint();
        blurPaint.setImageFilter(blur);
        canvas.saveLayer(null, blurPaint);
        RRect rect = RRect.makeXYWH(x, y, w, h, radius);
        canvas.clipRRect(rect, ClipMode.INTERSECT);
        canvas.drawImage(background, 0, 0);
        canvas.restore();
        Paint colorPaint = new Paint();
        colorPaint.setColor(color);
        colorPaint.setAlphaf((color >> 24 & 0xFF) / 255f);
        canvas.drawRRect(rect, colorPaint);
    }


    /**
     * Draws a blurred rectangle at the specified position.
     *
     * @param x The x-coordinate for the rectangle.
     * @param y The y-coordinate for the rectangle.
     * @param w The width of the rectangle.
     * @param h The height of the rectangle.
     */
    public static void drawRectBlur(float x, float y, float w, float h, float blurRadius) {
        Paint paint = new Paint();
        Image textureImage = getMinecraftAsImage();
        paint.setImageFilter(ImageFilter.makeBlur(blurRadius, blurRadius, FilterTileMode.REPEAT));
        getCanvas().save();
        getCanvas().clipRect(Rect.makeXYWH(x, y, w, h), ClipMode.INTERSECT);
        getCanvas().drawImage(textureImage, 0, 0, paint);
        getCanvas().restore();
    }

    public static float getFontHeight(Font font) {
        FontMetrics metrics = font.getMetrics();
        float ascent = Math.abs(metrics.getAscent());
        float descent = Math.abs(metrics.getDescent());
        float leading = metrics.getLeading();
        return ascent + descent + leading;
    }
}
