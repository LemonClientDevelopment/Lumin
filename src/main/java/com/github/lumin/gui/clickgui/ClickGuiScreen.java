package com.github.lumin.gui.clickgui;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.shaders.BlurShader;
import com.github.lumin.gui.clickgui.component.impl.ColorSettingComponent;
import com.github.lumin.gui.clickgui.panel.Panel;
import com.github.lumin.modules.impl.client.ClickGui;
import com.github.lumin.modules.impl.client.InterFace;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class ClickGuiScreen extends Screen {

    private final Panel panel = new Panel();

    private final RectRenderer rectRenderer = new RectRenderer();

    public ClickGuiScreen() {
        super(Component.literal("ClickGui"));
    }

    @Override
    protected void init() {

    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        final int guiW = getMinecraft().getWindow().getGuiScaledWidth();
        final int guiH = getMinecraft().getWindow().getGuiScaledHeight();

        if (InterFace.INSTANCE.backgroundBlur.getValue() && InterFace.INSTANCE.blurMode.is("FullScreen")) {
            BlurShader.drawQuadBlur(0, 0, guiW, guiH, InterFace.INSTANCE.blurStrength.getValue().floatValue());
        }

        rectRenderer.addRect(0, 0, guiW, guiH, new Color(18, 18, 18, 110));
        rectRenderer.drawAndClear();

        panel.render(null, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean focused) {
        return panel.mouseClicked(event, focused) || super.mouseClicked(event, focused);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        return panel.mouseReleased(event) || super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        return panel.keyPressed(event) || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent event) {
        return panel.charTyped(event) || super.charTyped(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return panel.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        ColorSettingComponent.closeActivePicker();
        ClickGui.INSTANCE.setEnabled(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

}
