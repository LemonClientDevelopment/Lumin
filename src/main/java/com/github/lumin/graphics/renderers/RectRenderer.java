package com.github.lumin.graphics.renderers;

import com.github.lumin.graphics.LuminRenderPipelines;
import com.github.lumin.graphics.LuminRenderSystem;
import com.github.lumin.graphics.buffer.LuminBuffer;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.ARGB;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class RectRenderer implements IRenderer {
    private static final long BUFFER_SIZE = 1024 * 1024;
    private final LuminBuffer buffer = new LuminBuffer(BUFFER_SIZE, GpuBuffer.USAGE_VERTEX);
    private long currentOffset = 0;
    private int vertexCount = 0;

    public void addRect(float x, float y, float width, float height, Color color) {
        int argb = ARGB.toABGR(color.getRGB());

        addVertex(x, y, argb);
        addVertex(x, y + height, argb);
        addVertex(x + width, y + height, argb);
        addVertex(x + width, y, argb);
    }

    private void addVertex(float vx, float vy, int color) {
        long baseAddr = MemoryUtil.memAddress(buffer.getMappedBuffer());
        long p = baseAddr + currentOffset;

        MemoryUtil.memPutFloat(p, vx);
        MemoryUtil.memPutFloat(p + 4, vy);
        MemoryUtil.memPutFloat(p + 8, 0.0f);
        MemoryUtil.memPutInt(p + 12, color);

        currentOffset += 16;
        vertexCount++;
    }

    @Override
    public void draw() {
        if (vertexCount == 0) return;
        LuminRenderSystem.QuadRenderingInfo info = LuminRenderSystem.prepareQuadRendering(vertexCount);

        if (info == null) return;

        if (info.target().getColorTextureView() == null) return;
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Rect Draw",
                info.target().getColorTextureView(), OptionalInt.empty(),
                info.target().getDepthTextureView(), OptionalDouble.empty())
        ) {
            pass.setPipeline(LuminRenderPipelines.RECTANGLE);

            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", info.dynamicUniforms());

            pass.setVertexBuffer(0, buffer.getGpuBuffer());
            pass.setIndexBuffer(info.ibo(), info.autoIndices().type());
            pass.drawIndexed(0, 0, info.indexCount(), 1);
        }
    }

    @Override
    public void clear() {
        vertexCount = 0;
        currentOffset = 0;
    }

    @Override
    public void close() {
        clear();
        buffer.close();
    }
}
