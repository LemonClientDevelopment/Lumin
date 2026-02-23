package com.github.lumin.gui.clickgui;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.shaders.BlurShader;
import com.github.lumin.gui.clickgui.panel.CategoryPanel;
import com.github.lumin.modules.Category;
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
import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {

    public int scroll;
    private float accumulatedScroll = 0;
    private final List<CategoryPanel> panels = new ArrayList<>();

    private final RectRenderer rectRenderer = new RectRenderer();

    public ClickGuiScreen() {
        super(Component.literal("ClickGui"));

        float width = 0;
        for (Category category : Category.values()) {
            CategoryPanel panel = new CategoryPanel(category);
            panel.setX(50 + width);
            panel.setY(20);
            panels.add(panel);
            width += panel.getWidth() + 10;
        }
    }

    @Override
    protected void init() {

    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        final int guiW = getMinecraft().getWindow().getGuiScaledWidth();
        final int guiH = getMinecraft().getWindow().getGuiScaledHeight();

        final float wheel = getDWheel();
        if (wheel != 0) {
            scroll += wheel > 0 ? 15 : -15;
            for (CategoryPanel panel : panels) {
                if (!panel.isDragging()) {
                    panel.setY(panel.getY() + (wheel > 0 ? 15 : -15));
                }
            }
        }

        if (InterFace.INSTANCE.backgroundBlur.getValue() && InterFace.INSTANCE.blurMode.is("FullScreen")) {
            BlurShader.drawQuadBlur(0, 0, guiW, guiH, InterFace.INSTANCE.blurStrength.getValue().floatValue());
        }

        rectRenderer.addRect(0, 0, guiW, guiH, new Color(18, 18, 18, 110));
        rectRenderer.drawAndClear();

        panels.forEach(panel -> panel.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean focused) {
        boolean handled = false;
        for (CategoryPanel panel : panels) {
            if (panel.mouseClicked(event, focused)) {
                handled = true;
            }
        }
        return handled || super.mouseClicked(event, focused);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        boolean handled = false;
        for (CategoryPanel panel : panels) {
            if (panel.mouseReleased(event)) {
                handled = true;
            }
        }
        return handled || super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        boolean handled = false;
        for (CategoryPanel panel : panels) {
            if (panel.keyPressed(event)) {
                handled = true;
            }
        }
        return handled || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent input) {
        boolean handled = false;
        for (CategoryPanel panel : panels) {
            if (panel.charTyped(input)) {
                handled = true;
            }
        }
        return handled || super.charTyped(input);
    }

    @Override
    public void onClose() {
        ClickGui.INSTANCE.setEnabled(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    private float getDWheel() {
        float scroll = accumulatedScroll;
        accumulatedScroll = 0;
        return scroll;
    }
}
