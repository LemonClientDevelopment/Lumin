package com.github.lumin.gui;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.renderers.RoundRectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.graphics.renderers.TextureRenderer;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public interface IComponent {
    default void render(RendererSet set, int mouseX, int mouseY, float deltaTicks) {
    }

    default boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        return false;
    }

    default boolean mouseReleased(MouseButtonEvent event) {
        return false;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    default boolean keyPressed(KeyEvent event) {
        return false;
    }

    default boolean charTyped(CharacterEvent input) {
        return false;
    }

    record RendererSet(
            RoundRectRenderer bottomRoundRect,
            RectRenderer middleRect,
            RoundRectRenderer topRoundRect,
            TextureRenderer texture,
            TextRenderer font,
            TextRenderer icons
    ) {
    }

}
