package com.github.lumin.graphics.text.ttf;

import com.github.lumin.graphics.LuminRenderPipelines;
import com.github.lumin.graphics.LuminRenderSystem;
import com.github.lumin.graphics.buffer.LuminBuffer;
import com.github.lumin.graphics.text.GlyphDescriptor;
import com.github.lumin.graphics.text.ITextRenderer;
import com.github.lumin.utils.resources.ResourceLocationUtils;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.util.ARGB;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class TtfTextRenderer implements ITextRenderer {

    private static final float DEFAULT_SCALE = 0.35f;
    private static final float SPACING = 2f;
    private static final int STRIDE = 24; // Pos(12) + UV(8) + Color(4)

    private static final long BUFFER_SIZE = 4 * 1024 * 1024; // 4MB
    private final LuminBuffer buffer = new LuminBuffer(BUFFER_SIZE, GpuBuffer.USAGE_VERTEX);

    private final Map<TtfGlyphAtlas, AtlasBatch> batches = new LinkedHashMap<>();

    private final TtfFontLoader fontLoader =
            new TtfFontLoader(ResourceLocationUtils.getIdentifier("fonts/pingfang.ttf"));

    private GpuBuffer ttfInfoUniformBuf = null;
    private long currentOffset = 0;
    private int totalVertexCount = 0;

    private record AtlasBatch(long startOffset, int vertexCount) {}

    @Override
    public void addText(String text, float x, float y, Color color, float scale) {
        scale = scale * DEFAULT_SCALE;
        fontLoader.checkAndLoadChars(text);
        int argb = ARGB.toABGR(color.getRGB());
        float xOffset = 0f;

        for (char ch : text.toCharArray()) {
            GlyphDescriptor glyph = fontLoader.getGlyph(ch);
            if (glyph == null) continue;

            TtfGlyphAtlas atlas = glyph.atlas();
            float baselineY = y + (fontLoader.fontFile.pixelAscent * scale);

            float x1 = x + xOffset;
            float x2 = x1 + glyph.width() * scale;
            float y1 = baselineY + glyph.yOffset() * scale;
            float y2 = y1 + glyph.height() * scale;

            long glyphOffset = currentOffset;

            addVertex(x1, y1, glyph.uv().u0(), glyph.uv().v0(), argb);
            addVertex(x1, y2, glyph.uv().u0(), glyph.uv().v1(), argb);
            addVertex(x2, y2, glyph.uv().u1(), glyph.uv().v1(), argb);
            addVertex(x2, y1, glyph.uv().u1(), glyph.uv().v0(), argb);

            // Update the batch information of the atlas
            batches.merge(atlas, new AtlasBatch(glyphOffset, 4),
                    (old, val) -> new AtlasBatch(old.startOffset(), old.vertexCount() + 4));

            xOffset += glyph.advance() * scale + SPACING;
        }
    }

    private void addVertex(float vx, float vy, float u, float v, int color) {
        long baseAddr = MemoryUtil.memAddress(buffer.getMappedBuffer());
        long p = baseAddr + currentOffset;

        MemoryUtil.memPutFloat(p, vx);
        MemoryUtil.memPutFloat(p + 4, vy);
        MemoryUtil.memPutFloat(p + 8, 0.0f);
        MemoryUtil.memPutFloat(p + 12, u);
        MemoryUtil.memPutFloat(p + 16, v);
        MemoryUtil.memPutInt(p + 20, color);

        currentOffset += STRIDE;
        totalVertexCount++;
    }

    @Override
    public void draw() {
        if (totalVertexCount == 0) return;
        LuminRenderSystem.applyOrthoProjection();

        if (ttfInfoUniformBuf == null) {
            final var size = new Std140SizeCalculator().putFloat().get();
            ttfInfoUniformBuf = RenderSystem.getDevice().createBuffer(
                    () -> "Lumin TTF UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, size);

            try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder()
                    .mapBuffer(ttfInfoUniformBuf, false, true)) {
                Std140Builder.intoBuffer(mappedView.data())
                        .putFloat(0.5f);
            }
        }

        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        if (target.getColorTextureView() == null) return;

        GpuBufferSlice dynamicUniforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(), new Vector4f(1, 1, 1, 1),
                new Vector3f(0, 0, 0), TextureTransform.DEFAULT_TEXTURING.getMatrix()
        );

        for (Map.Entry<TtfGlyphAtlas, AtlasBatch> entry : batches.entrySet()) {
            TtfGlyphAtlas atlas = entry.getKey();
            AtlasBatch batch = entry.getValue();

            int indexCount = batch.vertexCount() / 4 * 6;
            int vertexOffset = (int) (batch.startOffset() / STRIDE);

            RenderSystem.AutoStorageIndexBuffer autoIndices =
                    RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
            GpuBuffer ibo = autoIndices.getBuffer(indexCount);

            try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "Lumin TTF Draw",
                    target.getColorTextureView(), OptionalInt.empty(),
                    target.getDepthTextureView(), OptionalDouble.empty())
            ) {
                pass.setPipeline(LuminRenderPipelines.TTF_FONT);

                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", dynamicUniforms);
                pass.setUniform("TtfInfo", ttfInfoUniformBuf);

                pass.setVertexBuffer(0, buffer.getGpuBuffer());
                pass.setIndexBuffer(ibo, autoIndices.type());
                pass.bindTexture("Sampler0", atlas.getTexture().textureView(), atlas.getTexture().sampler());

                pass.drawIndexed(0, vertexOffset, indexCount, 1);
            }
        }
    }

    @Override
    public void clear() {
        currentOffset = 0;
        totalVertexCount = 0;
        batches.clear();
    }
}