package com.github.lumin.graphics.renderers;

import com.github.lumin.graphics.LuminRenderPipelines;
import com.github.lumin.graphics.LuminRenderSystem;
import com.github.lumin.graphics.LuminTexture;
import com.github.lumin.graphics.buffer.LuminBuffer;
import com.github.lumin.utils.resources.ResourceLocationUtils;
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
    private static final int STRIDE = 24;
    private final long bufferSize;
    private final Map<Identifier, Batch> batches = new LinkedHashMap<>();
    private final Map<Identifier, LuminTexture> textureCache = new HashMap<>();

    public TextureRenderer() {
        this(32 * 1024);
    }

    public TextureRenderer(long bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void addTexture(String texturePath, float x, float y, float width, float height) {
        this.addTexture(ResourceLocationUtils.getIdentifier(texturePath), x, y, width, height, Color.WHITE);
    }

    public void addTexture(Identifier texture, float x, float y, float width, float height) {
        this.addTexture(texture, x, y, width, height, Color.WHITE);
    }

    public void addTexture(Identifier texture, float x, float y, float width, float height, Color color) {
        this.addTexture(texture, x, y, width, height, 0.0f, 0.0f, 1.0f, 1.0f, color);
    }

    public void addTexture(Identifier texture, float x, float y, float width, float height, float u0, float v0, float u1, float v1, Color color) {
        Batch batch = batches.computeIfAbsent(texture, k -> new Batch(new LuminBuffer(bufferSize, GpuBuffer.USAGE_VERTEX)));

        if (batch.currentOffset + (long) STRIDE * 4L > bufferSize) {
            return;
        }

        int argb = ARGB.toABGR(color.getRGB());

        float x1 = x;
        float y1 = y;
        float x2 = x + width;
        float y2 = y + height;

        long baseAddr = MemoryUtil.memAddress(batch.buffer.getMappedBuffer());
        long p = baseAddr + batch.currentOffset;

        writeToAddr(p, x1, y1, u0, v0, argb);
        writeToAddr(p + STRIDE, x1, y2, u0, v1, argb);
        writeToAddr(p + STRIDE * 2L, x2, y2, u1, v1, argb);
        writeToAddr(p + STRIDE * 3L, x2, y1, u1, v0, argb);

        batch.currentOffset += (long) STRIDE * 4L;
        batch.vertexCount += 4;
    }

    private void writeToAddr(long p, float x, float y, float u, float v, int color) {
        MemoryUtil.memPutFloat(p, x);
        MemoryUtil.memPutFloat(p + 4, y);
        MemoryUtil.memPutFloat(p + 8, 0.0f);
        MemoryUtil.memPutFloat(p + 12, u);
        MemoryUtil.memPutFloat(p + 16, v);
        MemoryUtil.memPutInt(p + 20, color);
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

            try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "Texture Draw",
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
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(identifier);
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
                    FilterMode.LINEAR,
                    FilterMode.LINEAR,
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

        private Batch(LuminBuffer buffer) {
            this.buffer = buffer;
        }
    }
}

