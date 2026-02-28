package com.github.lumin.gui.clickgui.panel;

import com.github.lumin.graphics.renderers.RoundRectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.graphics.renderers.TextureRenderer;
import com.github.lumin.gui.IComponent;
import com.github.lumin.modules.impl.client.InterFace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class Panel implements IComponent {

    private final Minecraft mc = Minecraft.getInstance();

    private final RoundRectRenderer bottomRoundRect = new RoundRectRenderer();
    private final RoundRectRenderer topRoundRect = new RoundRectRenderer();
    private final TextureRenderer textureRenderer = new TextureRenderer();
    private final TextRenderer fontRenderer = new TextRenderer();

    private final RendererSet set = new RendererSet(bottomRoundRect, topRoundRect, textureRenderer, fontRenderer, null, null, null, null);

    private final Sidebar sidebar = new Sidebar();
    private final ContentPanel contentPanel = new ContentPanel();

    public Panel() {
        sidebar.setOnSelect(contentPanel::setCurrentCategory);
        contentPanel.setCurrentCategory(sidebar.getSelectedCategory());
    }

    public void render(RendererSet set, int mouseX, int mouseY, float deltaTicks) {

        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();

        float screenWidth = mc.getWindow().getGuiScaledWidth();
        float screenHeight = mc.getWindow().getGuiScaledHeight();

        float width = screenWidth * 0.5f; // 占比为屏幕的 1/2
        float height = width * 9.0f / 16.0f; // 16:9

        float scaledWidth = width * guiScale;
        float scaledHeight = height * guiScale;

        float x = screenWidth / 2.0f - scaledWidth / 2.0f;
        float y = screenHeight / 2.0f - scaledHeight / 2.0f;

        float sidebarWidth = width / 4;  // 比例为1:4
        float contentWidth = width - sidebarWidth;

        sidebar.setBounds(x, y, sidebarWidth, height);
        contentPanel.setBounds(x + sidebarWidth * guiScale, y, contentWidth, height);

        sidebar.render(this.set, mouseX, mouseY, deltaTicks);
        contentPanel.render(this.set, mouseX, mouseY, deltaTicks);

        bottomRoundRect.drawAndClear();
        topRoundRect.drawAndClear();
        textureRenderer.drawAndClear();
        fontRenderer.drawAndClear();

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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return sidebar.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || contentPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

}