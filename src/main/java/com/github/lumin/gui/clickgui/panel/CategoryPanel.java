package com.github.lumin.gui.clickgui.panel;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.renderers.RoundRectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.graphics.shaders.BlurShader;
import com.github.lumin.gui.IComponent;
import com.github.lumin.gui.clickgui.component.ModuleComponent;
import com.github.lumin.managers.Managers;
import com.github.lumin.modules.Category;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.utils.render.ColorUtils;
import com.github.lumin.utils.render.MouseUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.awt.*;

public class CategoryPanel implements IComponent {
    private float x, y, dragX, dragY;
    private float width = 110, height;
    private final Category category;
    private boolean dragging;
    private boolean opened = true;
    private final ObjectArrayList<ModuleComponent> moduleComponents = new ObjectArrayList<>();

    private final RoundRectRenderer bottomRoundRect = new RoundRectRenderer();
    private final RectRenderer middleRect = new RectRenderer();
    private final RoundRectRenderer topRoundRect = new RoundRectRenderer();
    private final TextRenderer font = new TextRenderer();

    private final RendererSet set = new RendererSet(bottomRoundRect, middleRect, topRoundRect, font);

    public CategoryPanel(Category category) {
        this.category = category;
        Managers.MODULE.getModules(category).forEach(module -> moduleComponents.add(new ModuleComponent(module)));
    }

    @Override
    public void render(RendererSet set0, int mouseX, int mouseY, float partialTicks) {
        update(mouseX, mouseY);

        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        float scaledWidth = width * guiScale;

        float componentOffsetY = 18 * guiScale;
        if (opened) {
            for (ModuleComponent component : moduleComponents) {
                component.setX(x + 2 * guiScale);
                component.setY(y + componentOffsetY);
                component.setWidth(scaledWidth - 4 * guiScale);
                component.setScale(guiScale);
                componentOffsetY += component.getHeight();
            }
        }
        height = componentOffsetY + 9 * guiScale;

        if (InterFace.INSTANCE.backgroundBlur.getValue() && InterFace.INSTANCE.blurMode.is("OnlyCategory")) {
            BlurShader.drawRoundedBlur(x, y - 1, scaledWidth, height, 7.0f, InterFace.INSTANCE.blurStrength.getValue().floatValue());
        }

        Color bgColor = InterFace.INSTANCE.backgroundColor.getValue();
        bottomRoundRect.addRoundRect(x, y - 1, scaledWidth, height, 7, ColorUtils.applyOpacity(bgColor, 0.9f));
        float fontScale = 0.9f * guiScale;
        float textHeight = font.getHeight(fontScale);
        float textWidth = font.getWidth(category.getName(), fontScale);
        float textY = y + (18f * guiScale - textHeight) / 2f - guiScale;
        float textX = x + (scaledWidth - textWidth) / 2f;
        font.addText(category.getName(), textX, textY, Color.WHITE, fontScale);

        if (opened) {
            for (ModuleComponent component : moduleComponents) {
                component.render(this.set, mouseX, mouseY, partialTicks);
            }
        }

        bottomRoundRect.drawAndClear();
        middleRect.drawAndClear();
        topRoundRect.drawAndClear();
        font.drawAndClear();

//        IComponent.super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        if (isHovered((int) event.x(), (int) event.y())) {
            switch (event.button()) {
                case 0 -> {
                    dragging = true;
                    dragX = (float) (x - event.x());
                    dragY = (float) (y - event.y());
                }
                case 1 -> opened = !opened;
            }
            return true;
        }

        boolean handled = false;
        if (opened) {
            for (ModuleComponent component : moduleComponents) {
                if (component.mouseClicked(event, focused)) {
                    handled = true;
                }
            }
        }

        return handled || IComponent.super.mouseClicked(event, focused);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            dragging = false;
        }

        boolean handled = false;
        if (opened) {
            for (ModuleComponent component : moduleComponents) {
                if (component.mouseReleased(event)) {
                    handled = true;
                }
            }
        }

        return handled || IComponent.super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        boolean handled = false;
        if (opened) {
            for (ModuleComponent component : moduleComponents) {
                if (component.keyPressed(event)) {
                    handled = true;
                }
            }
        }
        return handled || IComponent.super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        boolean handled = false;
        if (opened) {
            for (ModuleComponent component : moduleComponents) {
                if (component.charTyped(event)) {
                    handled = true;
                }
            }
        }
        return handled || IComponent.super.charTyped(event);
    }

    public void update(int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX + dragX;
            y = mouseY + dragY;
        }
    }

    public boolean isHovered(int mouseX, int mouseY) {
        float guiScale = InterFace.INSTANCE.scale.getValue().floatValue();
        return MouseUtils.isHovering(x, y, width * guiScale, 18 * guiScale, mouseX, mouseY);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getDragX() {
        return dragX;
    }

    public float getDragY() {
        return dragY;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isDragging() {
        return dragging;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setDragX(float dragX) {
        this.dragX = dragX;
    }

    public void setDragY(float dragY) {
        this.dragY = dragY;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }
}
