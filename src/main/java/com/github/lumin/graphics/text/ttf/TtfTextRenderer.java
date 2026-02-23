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
import java.util.*;

public class TtfTextRenderer implements ITextRenderer {

    private static final float DEFAULT_SCALE = 0.35f;
    private static final float SPACING = 1f;
    private static final int STRIDE = 24;
    private final long bufferSize;

    private final TtfFontLoader fontLoader =
            new TtfFontLoader(ResourceLocationUtils.getIdentifier("fonts/pingfang.ttf"));

    private final Map<TtfGlyphAtlas, LuminBuffer> atlasBuffers = new LinkedHashMap<>();
    private final Map<TtfGlyphAtlas, Long> atlasOffsets = new HashMap<>();

    private GpuBuffer ttfInfoUniformBuf = null;

    public TtfTextRenderer(long bufferSize) {
        this.bufferSize = bufferSize;
    }

    public TtfTextRenderer() {
        this(2 * 1024 * 1024);
    }

    @Override
    public void addText(String text, float x, float y, Color color, float scale) {
        final var finalScale = scale * DEFAULT_SCALE;
        fontLoader.checkAndLoadChars(text);
        int argb = ARGB.toABGR(color.getRGB());

        float xOffset = 0f;
        float yOffset = 0f;

        for (char ch : text.toCharArray()) {
            boolean skipCurrent = false;
            switch (ch) {
                case ' ': {
                    xOffset += 3.0f * scale;
                    skipCurrent = true;
                    break;
                }
                case '\n': {
                    xOffset = 0f;
                    yOffset += fontLoader.fontFile.fontHeight * finalScale;
                    skipCurrent = true;
                    break;
                }
            }

            if (skipCurrent) continue;

            GlyphDescriptor glyph = fontLoader.getGlyph(ch);
            if (glyph == null) continue;

            TtfGlyphAtlas atlas = glyph.atlas();

            LuminBuffer buffer = atlasBuffers.computeIfAbsent(atlas,
                    k -> new LuminBuffer(bufferSize, GpuBuffer.USAGE_VERTEX));
            long currentOffset = atlasOffsets.getOrDefault(atlas, 0L);

            float baselineY = yOffset + y + (fontLoader.fontFile.pixelAscent * finalScale);
            float x1 = x + xOffset;
            float x2 = x1 + glyph.width() * finalScale;
            float y1 = baselineY + glyph.yOffset() * finalScale;
            float y2 = y1 + glyph.height() * finalScale;

            long baseAddr = MemoryUtil.memAddress(buffer.getMappedBuffer());
            long p = baseAddr + currentOffset;

            writeToAddr(p, x1, y1, glyph.uv().u0(), glyph.uv().v0(), argb);
            writeToAddr(p + STRIDE, x1, y2, glyph.uv().u0(), glyph.uv().v1(), argb);
            writeToAddr(p + STRIDE * 2, x2, y2, glyph.uv().u1(), glyph.uv().v1(), argb);
            writeToAddr(p + STRIDE * 3, x2, y1, glyph.uv().u1(), glyph.uv().v0(), argb);

            atlasOffsets.put(atlas, currentOffset + (STRIDE * 4));
            xOffset += glyph.advance() * finalScale + SPACING * scale;
        }
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
    public float getHeight(float scale) {
        return fontLoader.fontFile.fontHeight * DEFAULT_SCALE * scale;
    }

    @Override
    public float getWidth(String text, float scale) {
        fontLoader.checkAndLoadChars(text);

        final var finalScale = scale * DEFAULT_SCALE;
        float maxLine = 0.0f;
        float currentLine = 0.0f;

        for (char ch : text.toCharArray()) {
            switch (ch) {
                case ' ': {
                    currentLine += 3.0f * scale;
                    break;
                }
                case '\n': {
                    maxLine = Math.max(maxLine, currentLine);
                    currentLine = 0.0f;
                    break;
                }
                default: {
                    GlyphDescriptor glyph = fontLoader.getGlyph(ch);
                    if (glyph == null) break;
                    currentLine += glyph.advance() * finalScale + SPACING * scale;
                    break;
                }
            }
        }

        maxLine = Math.max(maxLine, currentLine);
        return maxLine;
    }

    @Override
    public void draw() {
        if (atlasBuffers.isEmpty()) return;

        LuminRenderSystem.applyOrthoProjection();

        if (ttfInfoUniformBuf == null) {
            final var size = new Std140SizeCalculator().putFloat().get();
            ttfInfoUniformBuf = RenderSystem.getDevice().createBuffer(
                    () -> "Lumin TTF UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, size);

            try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder()
                    .mapBuffer(ttfInfoUniformBuf, false, true)) {
                Std140Builder.intoBuffer(mappedView.data()).putFloat(0.475f);
            }
        }

        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        if (target.getColorTextureView() == null) return;

        GpuBufferSlice dynamicUniforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(), new Vector4f(1, 1, 1, 1),
                new Vector3f(0, 0, 0), TextureTransform.DEFAULT_TEXTURING.getMatrix()
        );

        for (Map.Entry<TtfGlyphAtlas, LuminBuffer> entry : atlasBuffers.entrySet()) {
            TtfGlyphAtlas atlas = entry.getKey();
            LuminBuffer luminBuffer = entry.getValue();
            long writtenBytes = atlasOffsets.getOrDefault(atlas, 0L);

            if (writtenBytes == 0) continue;

            int vertexCount = (int) (writtenBytes / STRIDE);
            int indexCount = (vertexCount / 4) * 6;

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

                pass.setVertexBuffer(0, luminBuffer.getGpuBuffer());
                pass.setIndexBuffer(ibo, autoIndices.type());
                pass.bindTexture("Sampler0", atlas.getTexture().textureView(), atlas.getTexture().sampler());

                pass.drawIndexed(0, 0, indexCount, 1);
            }
        }
    }

    @Override
    public void clear() {
        atlasOffsets.replaceAll((a, v) -> 0L);
    }

    @Override
    public void close() {
        for (LuminBuffer buffer : atlasBuffers.values()) {
            buffer.getGpuBuffer().close();
        }
        atlasBuffers.clear();
        atlasOffsets.clear();
        if (ttfInfoUniformBuf != null) ttfInfoUniformBuf.close();
    }
}
