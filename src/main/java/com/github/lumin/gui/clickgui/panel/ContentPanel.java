package com.github.lumin.gui.clickgui.panel;

import com.github.lumin.graphics.shaders.BlurShader;
import com.github.lumin.gui.IComponent;
import com.github.lumin.gui.clickgui.component.impl.ColorSettingComponent;
import com.github.lumin.gui.clickgui.panel.views.ModuleListView;
import com.github.lumin.gui.clickgui.panel.views.ModuleSettingsView;
import com.github.lumin.managers.Managers;
import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.utils.render.MouseUtils;
import com.github.lumin.utils.render.animation.Animation;
import com.github.lumin.utils.render.animation.Easing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;

public class ContentPanel implements IComponent {

    private final Minecraft mc = Minecraft.getInstance();

    private float x, y, width, height;
    private Category currentCategory;

    private final ModuleListView listView = new ModuleListView(mc);
    private final ModuleSettingsView settingsView = new ModuleSettingsView(mc);
    private final Animation viewAnimation = new Animation(Easing.EASE_OUT_QUAD, 150L);
    private boolean closeSettingsRequested;

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setCurrentCategory(Category category) {
        if (this.currentCategory == category) return;
        this.currentCategory = category;
        closeSettingsRequested = false;
        settingsView.clearModule();

        List<Module> modules = new ArrayList<>();
        for (Module module : Managers.MODULE.getModules()) {
            if (module.category == category) {
                modules.add(module);
            }
        }
        listView.setModules(modules);
    }

    @Override
    public void render(RendererSet set, int mouseX, int mouseY, float deltaTicks) {
        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float radius = guiScale * 8f;

        float panelWidth = this.width * guiScale;
        float panelHeight = this.height * guiScale;

        BlurShader.drawRoundedBlur(x, y, panelWidth, panelHeight, 0, radius, radius, 0, new java.awt.Color(30, 30, 30, 245), InterFace.INSTANCE.blurStrength.getValue().floatValue(), 1.0f);

        float target = (settingsView.isActive() && !closeSettingsRequested) ? 1.0f : 0.0f;
        viewAnimation.run(target);
        float t = viewAnimation.getValue();
        if (t < 0.0f) t = 0.0f;
        if (t > 1.0f) t = 1.0f;

        float slide = 10.0f * guiScale;
        float listX = x - slide * t;
        float settingsX = x + slide * (1.0f - t);

        if (!settingsView.isActive()) {
            listView.render(set, listX, y, width, height, mouseX, mouseY, deltaTicks);
            return;
        }

        if (t <= 0.0f) {
            if (closeSettingsRequested) {
                settingsView.clearModule();
                closeSettingsRequested = false;
            }
            listView.render(set, listX, y, width, height, mouseX, mouseY, deltaTicks);
            return;
        }

        if (t >= 1.0f) {
            settingsView.render(set, settingsX, y, width, height, mouseX, mouseY, deltaTicks);
            return;
        }

        listView.render(set, listX, y, width, height, mouseX, mouseY, deltaTicks);
        settingsView.render(set, settingsX, y, width, height, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float panelWidth = this.width * guiScale;
        float panelHeight = this.height * guiScale;

        if (ColorSettingComponent.hasActivePicker() && ColorSettingComponent.isMouseOutOfPicker((int) event.x(), (int) event.y())) {
            ColorSettingComponent.closeActivePicker();
            return true;
        }

        if (ColorSettingComponent.hasActivePicker() && settingsView.isActive()) {
            boolean handled = settingsView.mouseClicked(event, focused, x, y, width, height);
            if (settingsView.consumeExitRequest()) {
                closeSettingsRequested = true;
                return true;
            }
            return handled;
        }

        if (!MouseUtils.isHovering(x, y, panelWidth, panelHeight, event.x(), event.y())) {
            listView.clickOutside();
            settingsView.clickOutside();
            return false;
        }

        if (settingsView.isActive()) {
            boolean handled = settingsView.mouseClicked(event, focused, x, y, width, height);
            if (settingsView.consumeExitRequest()) {
                closeSettingsRequested = true;
                return true;
            }
            return handled;
        }

        boolean handled = listView.mouseClicked(event, focused, x, y, width, height);
        Module open = listView.consumeRequestedSettingsModule();
        if (open != null) {
            closeSettingsRequested = false;
            settingsView.setModule(open);
            return true;
        }
        return handled;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (ColorSettingComponent.hasActivePicker() && settingsView.isActive()) {
            return settingsView.mouseReleased(event, x, y, width, height);
        }
        if (settingsView.isActive()) {
            return settingsView.mouseReleased(event, x, y, width, height);
        }
        return listView.mouseReleased(event, x, y, width, height);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (settingsView.isActive()) {
            return settingsView.mouseScrolled(mouseX, mouseY, scrollY);
        }
        return listView.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (settingsView.isActive()) {
            return settingsView.keyPressed(event);
        }
        return listView.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (settingsView.isActive()) {
            return settingsView.charTyped(event);
        }
        return listView.charTyped(event);
    }

}
