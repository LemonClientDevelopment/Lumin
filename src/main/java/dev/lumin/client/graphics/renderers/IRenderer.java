package dev.lumin.client.graphics.renderers;

public interface IRenderer {

    void draw();

    void clear();

    default void drawAndClear() {
        draw();
        clear();
    }

}
