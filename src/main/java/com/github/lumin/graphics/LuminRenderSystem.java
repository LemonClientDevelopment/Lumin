package com.github.lumin.graphics;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;

public class LuminRenderSystem {

    public static final CachedOrthoProjectionMatrixBuffer guiOrthoProjection =
            new CachedOrthoProjectionMatrixBuffer("gui", -1000.0F, 11000.0F, true);

    public static void applyOrthoProjection() {
        final var window = Minecraft.getInstance().getWindow();

        RenderSystem.setProjectionMatrix(
                guiOrthoProjection.getBuffer(
                        (float) window.getWidth() / window.getGuiScale(),
                        (float) window.getHeight() / window.getGuiScale()
                ),
                ProjectionType.ORTHOGRAPHIC);
    }

}
