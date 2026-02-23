package com.github.lumin.graphics.text;

import java.awt.*;

public interface ITextRenderer {

    void addText(String text, float x, float y, Color color, float scale);

    void draw();

    void clear();

    void close();

    float getHeight(float scale);

    float getWidth(String text, float scale);

}
