package com.github.lumin.gui.clickgui.panel;

import com.github.lumin.gui.IComponent;
import com.github.lumin.modules.Category;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.utils.render.MouseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.ClientAsset;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Sidebar implements IComponent {

    private final Minecraft mc = Minecraft.getInstance();
    private final List<CategoryBar> categoryBars = new ArrayList<>();

    public Sidebar() {
        for (Category category : Category.values()) {
            categoryBars.add(new CategoryBar(category));
        }
    }

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

        Color color = new Color(25, 25, 25, 130);
        set.bottomRoundRect().addRoundRect(x, y, width, height, radius, 0, 0, radius, color);

        var player = mc.player;
        String playerName = null;
        ClientAsset.Texture skin = null;
        if (player != null) {
            playerName = player.getName().getString();
            skin = player.getSkin().body();
        }

        float padding = 12 * guiScale;
        float headSize = 32 * guiScale;
        float headX = x + padding;
        float headY = y + padding;

        // Outline
        float outline = 0.5f * guiScale;
        set.bottomRoundRect().addRoundRect(headX - outline, headY - outline, headSize + outline * 2, headSize + outline * 2, radius + outline, Color.WHITE);

        // Face
        if (skin != null) {
            set.bottomRoundRect().addRoundRect(headX, headY, headSize, headSize, radius, Color.WHITE);
            set.texture().addRoundedTexture(skin.texturePath(), headX, headY, headSize, headSize, radius, 0.125f, 0.125f, 0.25f, 0.25f, Color.WHITE);
        }

        float textX = headX + headSize + 6 * guiScale;
        float nameY = headY * guiScale;
        float accountY = nameY + 20 * guiScale;

        if (playerName != null) {
            set.font().addText(player.getName().getString(), textX, nameY, guiScale * 1.5f, Color.WHITE);
            set.font().addText("Account", textX, accountY, guiScale * 0.7f, Color.GRAY);
        }

        // Category Placeholder
        float categoryY = headY + headSize + padding;

        float itemHeight = 24 * guiScale;
        float itemPadding = 4 * guiScale;
        float categoryHeight = categoryBars.size() * (itemHeight + itemPadding) + itemPadding;

        if (categoryHeight > 0) {
            set.bottomRoundRect().addRoundRect(headX, categoryY, width - padding * 2, categoryHeight, radius, new Color(35, 35, 35, 180));

            float currentY = categoryY + itemPadding;
            float itemWidth = width - padding * 2 - itemPadding * 2;
            float itemX = headX + itemPadding;

            for (CategoryBar bar : categoryBars) {
                bar.x = itemX;
                bar.y = currentY;
                bar.width = itemWidth;
                bar.height = itemHeight;
                bar.render(set, mouseX, mouseY, guiScale);
                currentY += itemHeight + itemPadding;
            }
        }
    }

    private static class CategoryBar {

        private final Category category;
        public float x, y, width, height;

        public CategoryBar(Category category) {
            this.category = category;
        }

        public void render(RendererSet set, int mouseX, int mouseY, float guiScale) {
            boolean hovered = MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);

            if (hovered) {
                set.bottomRoundRect().addRoundRect(x, y, width, height, 8 * guiScale, new Color(255, 255, 255, 30));
            }

            float iconScale = guiScale * 1.0f;
            float iconWidth = set.icons().getWidth(category.icon, iconScale);
            float iconHeight = set.icons().getHeight(iconScale);

            float iconX = x + 8 * guiScale;
            float iconY = y + (height - iconHeight) / 2f - guiScale;

            set.icons().addText(category.icon, iconX, iconY, iconScale, hovered ? Color.WHITE : Color.GRAY);

            float textX = iconX + iconWidth + 6 * guiScale;
            float nameY = y + guiScale;
            float descriptionY = nameY + 12 * guiScale;

            set.font().addText(category.getName(), textX, nameY, guiScale * 0.9f, Color.WHITE);
            set.font().addText(category.description, textX, descriptionY, guiScale * 0.6f, Color.GRAY);
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
