package com.github.lumin.gui.clickgui.panel;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.renderers.RoundRectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.graphics.shaders.BlurShader;
import com.github.lumin.gui.IComponent;
import com.github.lumin.modules.impl.client.InterFace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class Panel implements IComponent {

    private final Minecraft mc = Minecraft.getInstance();

    private final RoundRectRenderer bottomRoundRect = new RoundRectRenderer();
    private final RectRenderer middleRect = new RectRenderer();
    private final RoundRectRenderer topRoundRect = new RoundRectRenderer();
    private final TextRenderer font = new TextRenderer();

    private final RendererSet set = new RendererSet(bottomRoundRect, middleRect, topRoundRect, font);

    private final Sidebar sidebar = new Sidebar();
    private final ContentPanel contentPanel = new ContentPanel();

    public void render(RendererSet set, int mouseX, int mouseY, float deltaTicks) {
        float screenWidth = mc.getWindow().getGuiScaledWidth();
        float screenHeight = mc.getWindow().getGuiScaledHeight();

        float width = screenWidth * 0.7f; // 占比为屏幕的70%
        float height = width * 9.0f / 16.0f; // 16:9

        float x = screenWidth / 2.0f - width / 2.0f;
        float y = screenHeight / 2.0f - height / 2.0f;

        if (InterFace.INSTANCE.backgroundBlur.getValue()) {
            BlurShader.drawRoundedBlur(x, y, width, height, 8f, InterFace.INSTANCE.blurStrength.getValue().floatValue());
        }

        float sidebarWidth = width / 4; // 比例为1:4
        float contentWidth = width - sidebarWidth;

        sidebar.setBounds(x, y, sidebarWidth, height);
        contentPanel.setBounds(x + sidebarWidth, y, contentWidth, height);

        sidebar.render(this.set, mouseX, mouseY, deltaTicks);
        contentPanel.render(this.set, mouseX, mouseY, deltaTicks);

        bottomRoundRect.drawAndClear();
        middleRect.drawAndClear();
        topRoundRect.drawAndClear();
        font.drawAndClear();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        return sidebar.mouseClicked(event, focused) || contentPanel.mouseClicked(event, focused);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return sidebar.mouseReleased(event) || contentPanel.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return sidebar.keyPressed(event) || contentPanel.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return sidebar.charTyped(event) || contentPanel.charTyped(event);
    }

}
