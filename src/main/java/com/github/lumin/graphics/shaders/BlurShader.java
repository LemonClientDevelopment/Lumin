package com.github.lumin.graphics.shaders;

import com.github.lumin.graphics.shaders.programs.BlurProgram;

import java.awt.*;

public class BlurShader {
    private static final BlurProgram BLUR_PROGRAM = new BlurProgram();

    public static void drawQuadBlur(float x, float y, float width, float height, float blurStrength) {
        drawQuadBlur(x, y, width, height, blurStrength, 1.0f);
    }

    public static void drawQuadBlur(float x, float y, float width, float height, float blurStrength, float blurOpacity) {
        drawRoundedBlur(x, y, width, height, 0.0f, new Color(0, 0, 0, 0), blurStrength, blurOpacity);
    }

    public static void drawRoundedBlur(float x, float y, float width, float height, float radius, float blurStrength) {
        drawRoundedBlur(x, y, width, height, radius, new Color(0, 0, 0, 0), blurStrength, 1.0f);
    }

    public static void drawRoundedBlur(float x, float y, float width, float height, float radius, float blurStrength, float blurOpacity) {
        drawRoundedBlur(x, y, width, height, radius, new Color(0, 0, 0, 0), blurStrength, blurOpacity);
    }

    public static void drawRoundedBlur(float x, float y, float width, float height, float radius, Color c1, float blurStrenth, float blurOpacity) {
        blurOpacity = Math.max(0f, Math.min(1f, blurOpacity));
        BLUR_PROGRAM.render(x, y, width, height, radius, c1, blurStrenth, blurOpacity);
    }
}