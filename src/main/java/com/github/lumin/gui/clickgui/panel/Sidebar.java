package com.github.lumin.gui.clickgui.panel;

import com.github.lumin.gui.IComponent;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.utils.render.MouseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.awt.*;

public class Sidebar implements IComponent {

    private final Minecraft mc = Minecraft.getInstance();

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

        Color color = new Color(25, 25, 25, 220);
        set.bottomRoundRect().addRoundRect(x, y, width, height, radius, 0, 0, radius, color);

        if (mc.player != null) {
            var skin = mc.player.getSkin().body();

            float padding = 12 * guiScale;
            float headSize = 32 * guiScale;
            float headX = x + padding;
            float headY = y + padding;

            // Outline
            float outline = 0.5f * guiScale;
            set.bottomRoundRect().addRoundRect(headX - outline, headY - outline, headSize + outline * 2, headSize + outline * 2, radius + outline, Color.WHITE);

            // Face
            set.bottomRoundRect().addRoundRect(headX, headY, headSize, headSize, radius, Color.WHITE);
            set.texture().addRoundedTexture(skin.texturePath(), headX, headY, headSize, headSize, radius, 0.125f, 0.125f, 0.25f, 0.25f, Color.WHITE);

            float textX = headX + headSize + 6 * guiScale;
            float nameY = headY * guiScale;
            float accountY = nameY + 20 * guiScale;

            set.font().addText(mc.player.getName().getString(), textX, nameY, Color.WHITE, guiScale * 1.5f);
            set.font().addText("Account", textX, accountY, Color.GRAY, guiScale * 0.7f);

        }
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
