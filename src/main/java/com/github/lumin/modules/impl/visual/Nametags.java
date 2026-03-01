package com.github.lumin.modules.impl.visual;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.graphics.text.StaticFontLoader;
import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
import com.github.lumin.settings.impl.BoolSetting;
import com.github.lumin.settings.impl.ColorSetting;
import com.github.lumin.settings.impl.IntSetting;
import com.github.lumin.utils.render.WorldToScreen;
import com.google.common.base.Suppliers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Nametags extends Module {

    public static final Nametags INSTANCE = new Nametags();

    public Nametags() {
        super("名牌显示", "看迪克", Category.VISUAL);
    }

    private final IntSetting range = intSetting("距离", 64, 8, 256, 1);
    private final BoolSetting showSelf = boolSetting("显示自己", false);
    private final BoolSetting showHealth = boolSetting("显示血条", true);
    private final BoolSetting showHealthText = boolSetting("显示血量数值", true);
    private final BoolSetting showItems = boolSetting("显示装备", false); // vibe coding
    private final IntSetting maxItems = intSetting("最大物品数", 7, 0, 12, 1);
    private final ColorSetting backgroundColor = colorSetting("背景颜色", new Color(0, 0, 0, 140));
    private final ColorSetting textColor = colorSetting("文字颜色", Color.WHITE);

    private final Supplier<RectRenderer> rectRendererSupplier = Suppliers.memoize(RectRenderer::new);
    private final Supplier<TextRenderer> textRendererSupplier = Suppliers.memoize(TextRenderer::new);

    private final List<TagInfo> tags = new ArrayList<>();

    @SubscribeEvent
    private void onRenderGui(RenderGuiEvent.Post event) {
        if (nullCheck()) return;
        if (tags.isEmpty()) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        RectRenderer rectRenderer = rectRendererSupplier.get();
        TextRenderer textRenderer = textRendererSupplier.get();

        Color bg = backgroundColor.getValue();
        Color fg = textColor.getValue();

        float paddingX = 0f; // x预留
        float paddingY = 0f; // y预留
        float barGap = 2.0f;
        float barHeight = 2.0f;
        float itemGap = 2.0f;
        float itemSize = 16.0f;
        float anchorOffset = 8.0f;
        float healthTextGap = 6.0f;

        for (TagInfo tag : tags) {
            float scale = tag.scale();
            float textW = textRenderer.getWidth(tag.text(), scale, StaticFontLoader.REGULAR);
            float textH = textRenderer.getHeight(scale, StaticFontLoader.REGULAR);

            float hpW = 0.0f;
            boolean drawHealthTextInLine = showHealth.getValue() && showHealthText.getValue();
            if (drawHealthTextInLine) {
                hpW = textRenderer.getWidth(tag.healthText(), scale, StaticFontLoader.REGULAR);
            }

            int itemCount = showItems.getValue() ? Math.min(tag.items().size(), Math.max(0, maxItems.getValue())) : 0;
            float itemRowW = itemCount > 0 ? (itemCount * itemSize + (itemCount - 1) * itemGap) * scale : 0.0f;
            float itemRowH = itemCount > 0 ? itemSize * scale : 0.0f;

            float topLineW = textW;
            if (drawHealthTextInLine && hpW > 0.0f) {
                topLineW = textW + healthTextGap + hpW;
            }

            float contentW = Math.max(topLineW, itemRowW);
            float contentH = textH;
            if (showHealth.getValue()) contentH += barGap + barHeight;
            if (itemCount > 0) contentH += itemGap + itemRowH;

            float boxW = contentW + paddingX * 2.0f;
            float boxH = contentH + paddingY * 2.0f;

            float boxLeft = tag.x() - boxW * 0.5f;
            float boxTop = tag.y() - boxH - anchorOffset;

            float contentLeft = tag.x() - contentW * 0.5f;
            float cursorY = boxTop + paddingY;

            rectRenderer.addRect(boxLeft, boxTop, boxW, boxH, bg);

            float textY = cursorY;
            if (drawHealthTextInLine && hpW > 0.0f) {
                int scX = (int) contentLeft;
                int scY = (int) textY;
                int scW = (int) Math.max(0.0f, contentW - hpW - healthTextGap);
                int scH = (int) (textH + 2.0f);

                if (scW > 0) {
                    textRenderer.setScissor(scX, scY, scW, scH);
                }
                textRenderer.addText(tag.text(), contentLeft, textY, scale, fg, StaticFontLoader.REGULAR);
                if (scW > 0) {
                    textRenderer.clearScissor();
                }

                float hpX = contentLeft + contentW - hpW;
                textRenderer.addText(tag.healthText(), hpX, textY, scale, fg, StaticFontLoader.REGULAR);
            } else {
                float textX = tag.x() - textW * 0.5f;
                textRenderer.addText(tag.text(), textX, textY, scale, fg, StaticFontLoader.REGULAR);
            }
            cursorY += textH;

            if (showHealth.getValue()) {
                cursorY += barGap;

                int bgBarColor = new Color(0, 0, 0, 110).getRGB();
                rectRenderer.addRect(contentLeft, cursorY, contentW, barHeight, new Color(bgBarColor, true));

                float frac = clamp01(tag.healthFrac());
                Color hpColor = lerpColor(new Color(220, 60, 60, 220), new Color(80, 220, 80, 220), frac);
                rectRenderer.addRect(contentLeft, cursorY, contentW * frac, barHeight, hpColor);

                cursorY += barHeight;
            }

            if (itemCount > 0) {
                cursorY += itemGap;

                float itemsLeft = tag.x() - itemRowW * 0.5f;

                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().translate(itemsLeft, cursorY);
                guiGraphics.pose().scale(scale, scale);
                guiGraphics.pose().translate(-itemsLeft, -cursorY);

                int seed = 0;
                for (int i = 0; i < itemCount; i++) {
                    ItemStack stack = tag.items().get(i);
                    if (stack == null || stack.isEmpty()) continue;
                    int ix = (int) (itemsLeft + i * (itemSize + itemGap));
                    int iy = (int) cursorY;
                    guiGraphics.renderItem(mc.player, stack, ix, iy, seed++);
                    guiGraphics.renderItemDecorations(mc.font, stack, ix, iy);
                }

                guiGraphics.pose().popMatrix();
            }
        }

        rectRenderer.drawAndClear();
        textRenderer.drawAndClear();
    }


    @SubscribeEvent
    private void onRenderAfterEntities(RenderLevelStageEvent.AfterEntities event) {
        if (nullCheck()) return;

        tags.clear();

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 cameraPos = mc.getEntityRenderDispatcher().camera.position();

        var window = mc.getWindow();
        float guiWidth = (float) window.getWidth() / (float) window.getGuiScale();
        float guiHeight = (float) window.getHeight() / (float) window.getGuiScale();

        float aspect = (float) window.getWidth() / (float) window.getHeight();

        int fovDeg = mc.options.fov().get();
        float fovRad = (float) Math.toRadians(fovDeg);

        float far = (float) Math.max(256.0, mc.gameRenderer.getRenderDistance());
        Matrix4f projection = new Matrix4f().setPerspective(fovRad, aspect, 0.05f, far);
        Matrix4f modelViewRotation = new Matrix4f(event.getModelViewMatrix());

        float maxRange = range.getValue();

        for (Player player : mc.level.players()) {
            if (!showSelf.getValue() && player == mc.player) continue;

            Vec3 playerPos = player.getPosition(partialTick);
            float dist = (float) playerPos.distanceTo(cameraPos);
            if (dist > maxRange) continue;

            Vec3 headPos = playerPos.add(0.0, player.getBbHeight() + 0.35, 0.0);
            Vector2f screen = WorldToScreen.projectToGui(headPos, cameraPos, modelViewRotation, projection, guiWidth, guiHeight);
            if (screen == null) continue;

            if (screen.x < -64.0f || screen.y < -64.0f || screen.x > guiWidth + 64.0f || screen.y > guiHeight + 64.0f)
                continue;

            String text = player.getName().getString();
            float scale = Math.max(0.65f, 1.0f - (dist / maxRange) * 0.35f);

            float maxHealth = player.getMaxHealth();
            float health = player.getHealth() + player.getAbsorptionAmount();
            float frac = maxHealth > 0.0f ? health / maxHealth : 0.0f;
            String hpText = String.format("%.1f", health);

            List<ItemStack> items = new ArrayList<>();
            if (showItems.getValue()) {
                ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
                ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

                if (!head.isEmpty()) items.add(head);
                if (!chest.isEmpty()) items.add(chest);
                if (!legs.isEmpty()) items.add(legs);
                if (!feet.isEmpty()) items.add(feet);

                ItemStack main = player.getMainHandItem();
                if (!main.isEmpty()) items.add(main);
                ItemStack off = player.getOffhandItem();
                if (!off.isEmpty()) items.add(off);
            }

            tags.add(new TagInfo(text, hpText, frac, items, screen.x, screen.y, scale));
        }
    }

    private static float clamp01(float v) {
        return Mth.clamp(v, 0.0f, 1.0f);
    }

    private static Color lerpColor(Color a, Color b, float t) {
        t = clamp01(t);
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        int al = (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(r, g, bl, al);
    }

    private record TagInfo(String text, String healthText, float healthFrac, List<ItemStack> items, float x, float y,
                           float scale) {
    }

}
