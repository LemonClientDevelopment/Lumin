package com.github.lumin.gui.clickgui.panel;

import com.github.lumin.graphics.shaders.BlurShader;
import com.github.lumin.gui.IComponent;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.utils.render.MouseUtils;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.awt.*;

public class ContentPanel implements IComponent {

    private float x, y, width, height;

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(RendererSet set, int mouseX, int mouseY, float deltaTicks) {

        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float radius = guiScale * 8f;

        float width = this.width * guiScale;
        float height = this.height * guiScale;

        BlurShader.drawRoundedBlur(x, y, width, height, 0, radius, radius, 0, new Color(30, 30, 30, 245), InterFace.INSTANCE.blurStrength.getValue().floatValue(), 1.0f);


    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        return MouseUtils.isHovering(x, y, width, height, event.x(), event.y());
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return MouseUtils.isHovering(x, y, width, height, event.x(), event.y());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return false;
    }
}
