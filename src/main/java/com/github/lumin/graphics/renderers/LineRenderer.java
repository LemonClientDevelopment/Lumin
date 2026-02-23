package com.github.lumin.graphics.renderers;

import com.github.lumin.graphics.LuminRenderPipelines;
import com.github.lumin.graphics.LuminRenderSystem;
import com.github.lumin.graphics.buffer.LuminBuffer;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.util.ARGB;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class LineRenderer implements IRenderer {

    private static final long BUFFER_SIZE = 512 * 1024;
    private static final int STRIDE = 16;

    private final LuminBuffer buffer = new LuminBuffer(BUFFER_SIZE, GpuBuffer.USAGE_VERTEX);
    private long currentOffset = 0;
    private int vertexCount = 0;
    private boolean flushBufferFlag = false;

    public void addLine(float x1, float y1, float x2, float y2, float lineWidth, Color color) {
        buffer.tryMap();
        flushBufferFlag = true;

        int argb = ARGB.toABGR(color.getRGB());
        addVertex(x1, y1, argb);
        addVertex(x2, y2, argb);
    }

    public void addLine(float x1, float y1, float z1, float x2, float y2, float z2, Color color) {
        buffer.tryMap();
        flushBufferFlag = true;

        int argb = ARGB.toABGR(color.getRGB());
        addVertex(x1, y1, z1, argb);
        addVertex(x2, y2, z2, argb);
    }

    public void addLines(float[] vertices, int[] colors) {
        buffer.tryMap();
        flushBufferFlag = true;

        for (int i = 0; i < vertices.length / 3; i++) {
            addVertex(
                    vertices[i * 3],
                    vertices[i * 3 + 1],
                    vertices[i * 3 + 2],
                    colors[i]
            );
        }
    }

    public void addRectOutline(float x, float y, float width, float height, float lineWidth, Color color) {
        addLine(x, y, x + width, y, lineWidth, color);
        addLine(x + width, y, x + width, y + height, lineWidth, color);
        addLine(x + width, y + height, x, y + height, lineWidth, color);
        addLine(x, y + height, x, y, lineWidth, color);
    }

    private void addVertex(float x, float y, int color) {
        addVertex(x, y, 0.0f, color);
    }

    private void addVertex(float x, float y, float z, int color) {
        long baseAddr = MemoryUtil.memAddress(buffer.getMappedBuffer());
        long p = baseAddr + currentOffset;

        MemoryUtil.memPutFloat(p, x);
        MemoryUtil.memPutFloat(p + 4, y);
        MemoryUtil.memPutFloat(p + 8, z);
        MemoryUtil.memPutInt(p + 12, color);

        currentOffset += STRIDE;
        vertexCount++;
    }

    @Override
    public void draw() {
        if (vertexCount < 2) return;

        if (flushBufferFlag) {
            buffer.unmap();
        }
        flushBufferFlag = false;

        LuminRenderSystem.applyOrthoProjection();

        var target = net.minecraft.client.Minecraft.getInstance().getMainRenderTarget();
        if (target.getColorTextureView() == null) return;

        int indexCount = vertexCount;

        RenderSystem.AutoStorageIndexBuffer autoIndices =
                RenderSystem.getSequentialBuffer(VertexFormat.Mode.LINES);
        GpuBuffer ibo = autoIndices.getBuffer(indexCount);

        com.mojang.blaze3d.buffers.GpuBufferSlice dynamicUniforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(),
                new org.joml.Vector4f(1, 1, 1, 1),
                new org.joml.Vector3f(0, 0, 0),
                net.minecraft.client.renderer.rendertype.TextureTransform.DEFAULT_TEXTURING.getMatrix()
        );

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Line Draw",
                target.getColorTextureView(), OptionalInt.empty(),
                target.getDepthTextureView(), OptionalDouble.empty())
        ) {
            pass.setPipeline(LuminRenderPipelines.LINE);

            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicUniforms);

            pass.setVertexBuffer(0, buffer.getGpuBuffer());
            pass.setIndexBuffer(ibo, autoIndices.type());
            pass.drawIndexed(0, 0, indexCount, 1);
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