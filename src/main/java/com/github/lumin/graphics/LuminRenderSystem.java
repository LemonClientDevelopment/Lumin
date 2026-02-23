package com.github.lumin.graphics;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import org.joml.Vector3f;
import org.joml.Vector4f;

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

    public static QuadRenderingInfo prepareQuadRendering(int vertexCount) {
        LuminRenderSystem.applyOrthoProjection();

        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        if (target.getColorTextureView() == null) return null;

        final var indexCount = vertexCount / 4 * 6;

        RenderSystem.AutoStorageIndexBuffer autoIndices =
                RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer ibo = autoIndices.getBuffer(indexCount);

        GpuBufferSlice dynamicUniforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(),
                new Vector4f(1, 1, 1, 1),
                new Vector3f(0, 0, 0),
                TextureTransform.DEFAULT_TEXTURING.getMatrix()
        );

        return new QuadRenderingInfo(target, autoIndices, ibo, indexCount, dynamicUniforms);
    }

    public record QuadRenderingInfo(
            RenderTarget target,
            RenderSystem.AutoStorageIndexBuffer autoIndices,
            GpuBuffer ibo,
            int indexCount,
            GpuBufferSlice dynamicUniforms
    ) {
    }

}
