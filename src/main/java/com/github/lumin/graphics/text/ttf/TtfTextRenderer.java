package com.github.lumin.graphics.text.ttf;

import com.github.lumin.graphics.LuminRenderPipelines;
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
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class TtfTextRenderer implements ITextRenderer {

    private static final float DEFAULT_SCALE = 0.35f;

    private final HashMap<TtfGlyphAtlas, BufferBuilder> buffers = new HashMap<>();
    private final TtfFontLoader fontLoader =
            new TtfFontLoader(ResourceLocationUtils.getIdentifier("fonts/pingfang.ttf"));

    private GpuBuffer ttfInfoUniformBuf = null;

    @Override
    public void addText(String text, float x, float y, Color color, float scale) {
        scale = scale * DEFAULT_SCALE;

        fontLoader.checkAndLoadChars(text);
        float xOffset = 0f;

        for (char ch : text.toCharArray()) {
            GlyphDescriptor glyph = fontLoader.getGlyph(ch);
            if (glyph == null) continue;

            BufferBuilder buffer = buffers.computeIfAbsent(glyph.atlas(), a ->
                    Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR));

            float x1 = x + xOffset;
            float x2 = x1 + glyph.width() * scale;
            float y1 = y + glyph.yOffset() * scale;
            float y2 = y1 + glyph.height() * scale;

            buffer.addVertex(x1, y1, 0).setUv(glyph.uv().u0(), glyph.uv().v0()).setColor(color.getRGB());
            buffer.addVertex(x1, y2, 0).setUv(glyph.uv().u0(), glyph.uv().v1()).setColor(color.getRGB());
            buffer.addVertex(x2, y2, 0).setUv(glyph.uv().u1(), glyph.uv().v1()).setColor(color.getRGB());
            buffer.addVertex(x2, y1, 0).setUv(glyph.uv().u1(), glyph.uv().v0()).setColor(color.getRGB());

            xOffset += glyph.advance() * scale;
        }
    }

    @Override
    public void draw() {
        if (ttfInfoUniformBuf == null) {
            final var size = new Std140SizeCalculator()
                    .putFloat()
                    .get();

            ttfInfoUniformBuf = RenderSystem.getDevice().createBuffer(
                    () -> "Lumin TTF UBO",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
                    size
            );

            try (GpuBuffer.MappedView mappedView = RenderSystem
                    .getDevice()
                    .createCommandEncoder()
                    .mapBuffer(ttfInfoUniformBuf, false, true)
            ) {
                Std140Builder.intoBuffer(mappedView.data())
                        .putFloat(0.52f);
            }
        }

        for (Map.Entry<TtfGlyphAtlas, BufferBuilder> entry : buffers.entrySet()) {
            TtfGlyphAtlas atlas = entry.getKey();
            BufferBuilder buffer = entry.getValue();

            try (final var meshData = buffer.build()) {
                if (meshData == null) continue;

                GpuBufferSlice dynamicUniforms = RenderSystem.getDynamicUniforms().writeTransform(
                        RenderSystem.getModelViewMatrix(),
                        new Vector4f(1, 1, 1, 1),
                        new Vector3f(0, 0, 0),
                        TextureTransform.DEFAULT_TEXTURING.getMatrix()
                );

                GpuBuffer vbo = DefaultVertexFormat.POSITION_TEX_COLOR
                        .uploadImmediateVertexBuffer(meshData.vertexBuffer());
                RenderSystem.AutoStorageIndexBuffer autoIndices =
                        RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
                GpuBuffer ibo = autoIndices.getBuffer(meshData.drawState().indexCount());

                RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
                if (target.getColorTextureView() == null) return;

                try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> "Lumin TTF Draw",
                        target.getColorTextureView(), OptionalInt.empty(),
                        target.getDepthTextureView(), OptionalDouble.empty())
                ) {

                    pass.setPipeline(LuminRenderPipelines.TTF_FONT);

                    RenderSystem.bindDefaultUniforms(pass);
                    pass.setUniform("DynamicTransforms", dynamicUniforms);
                    pass.setUniform("TtfInfo", ttfInfoUniformBuf);
                    pass.setVertexBuffer(0, vbo);
                    pass.setIndexBuffer(ibo, autoIndices.type());

                    pass.bindTexture("Sampler0", atlas.getTexture().textureView(), atlas.getTexture().sampler());

                    pass.drawIndexed(0, 0, meshData.drawState().indexCount(), 1);
                }
            }
        }
    }

    @Override
    public void clear() {
        buffers.clear();
    }

}
