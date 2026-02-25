package com.github.lumin.graphics.renderers;

import com.github.lumin.graphics.LuminRenderSystem;
import com.github.lumin.graphics.text.ITextRenderer;
import com.github.lumin.graphics.text.ttf.TtfTextRenderer;

import java.awt.*;

public class TextRenderer implements IRenderer {

    private final ITextRenderer textRenderer;

    public TextRenderer(String fontPath, long bufferSize) {
        textRenderer = new TtfTextRenderer(fontPath, bufferSize);
    }

    public TextRenderer(String fontPath) {
        textRenderer = new TtfTextRenderer(fontPath, 2 * 1024 * 1024);
    }

    public TextRenderer(long bufferSize) {
        textRenderer = new TtfTextRenderer(bufferSize);
    }

    public TextRenderer() {
        textRenderer = new TtfTextRenderer();
    }

    public void addText(String text, float x, float y, float scale, Color color) {
        textRenderer.addText(text, x, y, color, scale);
    }

    public void addText(String text, float x, float y, Color color) {
        addText(text, x, y, 1.0f, color);
    }

    public float getHeight(float scale) {
        return textRenderer.getHeight(scale);
    }

    public float getWidth(String text, float scale) {
        return textRenderer.getWidth(text, scale);
    }

    @Override
    public void draw() {
        LuminRenderSystem.applyOrthoProjection();

        textRenderer.draw();
    }

    @Override
    public void clear() {
        textRenderer.clear();
    }

    @Override
    public void close() {
        textRenderer.close();
    }
}
