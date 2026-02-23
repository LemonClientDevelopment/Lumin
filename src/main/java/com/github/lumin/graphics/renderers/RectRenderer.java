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
    private static final int STRIDE = 16;

    private final LuminBuffer buffer = new LuminBuffer(BUFFER_SIZE, GpuBuffer.USAGE_VERTEX);
    private long currentOffset = 0;
    private int vertexCount = 0;
    private boolean flushBufferFlag = false;

    /**
     * 基础矩形
     */
    public void addRect(float x, float y, float width, float height, Color color) {
        addRawRect(x, y, width, height, color, color, color, color);
    }

    /**
     * 垂直渐变
     */
    public void addVerticalGradient(float x, float y, float width, float height, Color top, Color bottom) {
        // 顺序：左上(top), 左下(bottom), 右下(bottom), 右上(top)
        addRawRect(x, y, width, height, top, bottom, bottom, top);
    }

    /**
     * 水平渐变
     */
    public void addHorizontalGradient(float x, float y, float width, float height, Color left, Color right) {
        addRawRect(x, y, width, height, left, left, right, right);
    }

    /**
     * 左上 左下 右下 右上
     */
    public void addRawRect(float x, float y, float w, float h, Color c1, Color c2, Color c3, Color c4) {
        buffer.tryMap();
        flushBufferFlag = true;

        int argb1 = ARGB.toABGR(c1.getRGB());
        int argb2 = ARGB.toABGR(c2.getRGB());
        int argb3 = ARGB.toABGR(c3.getRGB());
        int argb4 = ARGB.toABGR(c4.getRGB());

        addVertex(x, y, argb1);
        addVertex(x, y + h, argb2);
        addVertex(x + w, y + h, argb3);
        addVertex(x + w, y, argb4);
    }

    private void addVertex(float vx, float vy, int color) {
        long baseAddr = MemoryUtil.memAddress(buffer.getMappedBuffer());
        long p = baseAddr + currentOffset;

        // Position: float x, y, z (12 bytes)
        MemoryUtil.memPutFloat(p, vx);
        MemoryUtil.memPutFloat(p + 4, vy);
        MemoryUtil.memPutFloat(p + 8, 0.0f);

        // Color: int abgr (4 bytes)
        MemoryUtil.memPutInt(p + 12, color);

        currentOffset += STRIDE;
        vertexCount++;
    }

    @Override
    public void draw() {
        if (vertexCount == 0) return;
        if (flushBufferFlag) buffer.unmap();
        flushBufferFlag = false;

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
        flushBufferFlag = false;
    }

    @Override
    public void close() {
        clear();
        buffer.close();
    }
}