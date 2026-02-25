package com.github.lumin.graphics.renderers;

import com.github.lumin.graphics.LuminRenderPipelines;
import com.github.lumin.graphics.LuminRenderSystem;
import com.github.lumin.graphics.LuminTexture;
import com.github.lumin.graphics.buffer.LuminBuffer;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.ARGB;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.io.InputStream;
import java.util.*;

public class TextureRenderer implements IRenderer {
    private final Minecraft mc = Minecraft.getInstance();

    private static final int STRIDE = 44;
    private final long bufferSize;
    private final Map<Identifier, Batch> batches = new LinkedHashMap<>();
    private final Map<Identifier, LuminTexture> textureCache = new HashMap<>();

    public TextureRenderer() {
        this(32 * 1024);
    }

    public TextureRenderer(long bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void addQuadTexture(Identifier texture, float x, float y, float width, float height, float u0, float v0, float u1, float v1, Color color) {
        addRoundedTexture(texture, x, y, width, height, 0f, u0, v0, u1, v1, color);
    }

    public void addRoundedTexture(Identifier texture, float x, float y, float width, float height, float radius, float u0, float v0, float u1, float v1, Color color) {
        Batch batch = batches.computeIfAbsent(texture, k -> new Batch(new LuminBuffer(bufferSize, GpuBuffer.USAGE_VERTEX)));
        batch.buffer.tryMap();
        batch.flushBufferFlag = true;

        if (batch.currentOffset + (long) STRIDE * 4L > bufferSize) {
            return;
        }

        int argb = ARGB.toABGR(color.getRGB());

        float x2 = x + width;
        float y2 = y + height;

        float innerX1 = x + radius;
        float innerY1 = y + radius;
        float innerX2 = x2 - radius;
        float innerY2 = y2 - radius;

        long baseAddr = MemoryUtil.memAddress(batch.buffer.getMappedBuffer());
        long p = baseAddr + batch.currentOffset;

        // Vertex 0: x, y
        writeVertex(p, x, y, u0, v0, argb, innerX1, innerY1, innerX2, innerY2, radius);
        // Vertex 1: x, y2
        writeVertex(p + STRIDE, x, y2, u0, v1, argb, innerX1, innerY1, innerX2, innerY2, radius);
        // Vertex 2: x2, y2
        writeVertex(p + STRIDE * 2L, x2, y2, u1, v1, argb, innerX1, innerY1, innerX2, innerY2, radius);
        // Vertex 3: x2, y
        writeVertex(p + STRIDE * 3L, x2, y, u1, v0, argb, innerX1, innerY1, innerX2, innerY2, radius);

        batch.currentOffset += (long) STRIDE * 4L;
        batch.vertexCount += 4;
    }

    private void writeVertex(long addr, float x, float y, float u, float v, int color, float ix1, float iy1, float ix2, float iy2, float radius) {
        MemoryUtil.memPutFloat(addr, x);
        MemoryUtil.memPutFloat(addr + 4, y);
        MemoryUtil.memPutFloat(addr + 8, 0.0f); // z
        MemoryUtil.memPutInt(addr + 12, color);
        MemoryUtil.memPutFloat(addr + 16, u);
        MemoryUtil.memPutFloat(addr + 20, v);
        MemoryUtil.memPutFloat(addr + 24, ix1);
        MemoryUtil.memPutFloat(addr + 28, iy1);
        MemoryUtil.memPutFloat(addr + 32, ix2);
        MemoryUtil.memPutFloat(addr + 36, iy2);
        MemoryUtil.memPutFloat(addr + 40, radius);
    }

    @Override
    public void draw() {
        if (batches.isEmpty()) return;

        LuminRenderSystem.applyOrthoProjection();

        var target = Minecraft.getInstance().getMainRenderTarget();
        if (target.getColorTextureView() == null) return;

        GpuBufferSlice dynamicUniforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(),
                new Vector4f(1, 1, 1, 1),
                new Vector3f(0, 0, 0),
                TextureTransform.DEFAULT_TEXTURING.getMatrix()
        );

        for (Map.Entry<Identifier, Batch> entry : batches.entrySet()) {
            Identifier textureId = entry.getKey();
            Batch batch = entry.getValue();
            if (batch.vertexCount == 0) continue;

            int indexCount = (batch.vertexCount / 4) * 6;
            RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
            GpuBuffer ibo = autoIndices.getBuffer(indexCount);

            LuminTexture texture = textureCache.computeIfAbsent(textureId, this::loadTexture);

            if (batch.flushBufferFlag) {
                batch.buffer.unmap();
            }
            batch.flushBufferFlag = false;

            try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "Rounded Texture Draw",
                    target.getColorTextureView(), OptionalInt.empty(),
                    target.getDepthTextureView(), OptionalDouble.empty())
            ) {
                pass.setPipeline(LuminRenderPipelines.TEXTURE);

                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", dynamicUniforms);

                pass.setVertexBuffer(0, batch.buffer.getGpuBuffer());
                pass.setIndexBuffer(ibo, autoIndices.type());
                pass.bindTexture("Sampler0", texture.textureView(), texture.sampler());

                pass.drawIndexed(0, 0, indexCount, 1);
            }
        }
    }

    private LuminTexture loadTexture(Identifier identifier) {
        Optional<Resource> resource = mc.getResourceManager().getResource(identifier);
        if (resource.isEmpty()) {
            throw new RuntimeException("Couldn't find resource at " + identifier);
        }

        try (InputStream is = resource.get().open(); NativeImage image = NativeImage.read(is)) {
            GpuTexture texture = RenderSystem.getDevice().createTexture(
                    () -> "Lumin-Texture: " + identifier,
                    GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST,
                    TextureFormat.RGBA8,
                    image.getWidth(),
                    image.getHeight(),
                    1,
                    1
            );

            var view = RenderSystem.getDevice().createTextureView(texture);
            var sampler = RenderSystem.getDevice().createSampler(
                    AddressMode.CLAMP_TO_EDGE,
                    AddressMode.CLAMP_TO_EDGE,
                    FilterMode.NEAREST,
                    FilterMode.NEAREST,
                    1,
                    OptionalDouble.empty()
            );

            RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, image);
            return new LuminTexture(texture, view, sampler);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load texture " + identifier, e);
        }
    }

    @Override
    public void clear() {
        for (Batch batch : batches.values()) {
            batch.currentOffset = 0;
            batch.vertexCount = 0;
            batch.flushBufferFlag = false;
        }
    }

    @Override
    public void close() {
        clear();
        for (Batch batch : batches.values()) {
            batch.buffer.close();
        }
        batches.clear();
        for (LuminTexture texture : textureCache.values()) {
            texture.close();
        }
        textureCache.clear();
    }

    private static final class Batch {
        final LuminBuffer buffer;
        long currentOffset;
        int vertexCount;
        public boolean flushBufferFlag;

        private Batch(LuminBuffer buffer) {
            this.buffer = buffer;
        }
    }
}
