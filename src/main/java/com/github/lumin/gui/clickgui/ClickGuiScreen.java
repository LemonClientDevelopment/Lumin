package com.github.lumin.gui.clickgui;

import com.github.lumin.gui.clickgui.panel.CategoryPanel;
import com.github.lumin.modules.impl.client.ClickGui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private final List<CategoryPanel> panels = new ArrayList<>();

    public ClickGuiScreen() {
        super(Component.literal("ClickGui"));
    }

    @Override
    protected void init() {

    }

    @Override
    public void removed() {

    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

    }

    @Override
    public boolean keyPressed(KeyEvent event) {

    }

    @Override
    public void onClose() {
        if (ClickGui.INSTANCE.isEnabled()) {
            ClickGui.INSTANCE.setEnabled(false);
        } else {
            super.onClose();
        }
    }

    @Override
    public void renderBackground(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }
}
