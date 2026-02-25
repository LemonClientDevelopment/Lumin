package com.github.lumin.graphics.shaders.programs;

import com.github.lumin.utils.resources.ResourceLocationUtils;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.awt.*;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class BlurProgram {
    Minecraft mc = Minecraft.getInstance();

    private static final Identifier VERTEX_SHADER = ResourceLocationUtils.getIdentifier("blur");
    private static final Identifier FRAGMENT_SHADER = ResourceLocationUtils.getIdentifier("blur");
    private static final int UNIFORMS_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().get();

    private RenderPipeline pipeline;
    private MappableRingBuffer uniforms;
    private RenderTarget input;

    private void ensureProgram() {
        if (this.uniforms == null) {
            this.uniforms = new MappableRingBuffer(() -> "Lumin BlurUniforms", GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_UNIFORM, UNIFORMS_SIZE);
        }
        if (this.pipeline == null) {
            this.pipeline = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
                    .withLocation(ResourceLocationUtils.getIdentifier("pipeline/blur"))
                    .withVertexShader(VERTEX_SHADER)
                    .withFragmentShader(FRAGMENT_SHADER)
                    .withUniform("BlurUniforms", UniformType.UNIFORM_BUFFER)
                    .withSampler("InputSampler")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withCull(false)
                    .build();
        }
    }

    private void ensureInputBuffer(int width, int height) {
        if (this.input == null) {
            this.input = createRenderTarget("Lumin Blur Input", width, height, false);
            return;
        }
        if (this.input.width != width || this.input.height != height) {
            this.input.resize(width, height);
        }
    }

    private RenderTarget createRenderTarget(String name, int width, int height, boolean useDepth) {
        return new RenderTarget(name, useDepth) {
            @Override
            public void createBuffers(int width, int height) {
                RenderSystem.assertOnRenderThread();
                this.width = width;
                this.height = height;
                if (useDepth) {
                    this.depthTexture = RenderSystem.getDevice().createTexture(
                            () -> this.label + " / Depth",
                            GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT,
                            TextureFormat.DEPTH32,
                            width, height, 1, 1
                    );
                    this.depthTextureView = RenderSystem.getDevice().createTextureView(this.depthTexture);
                }
                this.colorTexture = RenderSystem.getDevice().createTexture(
                        () -> this.label + " / Color",
                        GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_COPY_DST,
                        TextureFormat.RGBA8,
                        width, height, 1, 1
                );
                this.colorTextureView = RenderSystem.getDevice().createTextureView(this.colorTexture);
            }
        };
    }

    public void render(float x, float y, float width, float height, float rTL, float rTR, float rBR, float rBL, Color color, float blurStrength, float blurOpacity) {
        this.ensureProgram();

        if (this.pipeline == null || this.uniforms == null) {
            return;
        }

        RenderTarget framebuffer = mc.getMainRenderTarget();
        if (framebuffer.getColorTexture() == null || framebuffer.getColorTextureView() == null) {
            return;
        }

        int fbWidth = mc.getWindow().getWidth();
        int fbHeight = mc.getWindow().getHeight();
        this.ensureInputBuffer(fbWidth, fbHeight);
        if (this.input.getColorTexture() == null || this.input.getColorTextureView() == null) {
            return;
        }

        float scale = (float) mc.getWindow().getGuiScale();
        float pxX = x * scale;
        float pxY = (-y + mc.getWindow().getGuiScaledHeight() - height) * scale;
        float pxW = width * scale;
        float pxH = height * scale;

        float rTLPx = Math.max(0.0f, rTL * scale);
        float rTRPx = Math.max(0.0f, rTR * scale);
        float rBRPx = Math.max(0.0f, rBR * scale);
        float rBLPx = Math.max(0.0f, rBL * scale);

        float quality = Math.max(0.0f, blurStrength);
        float alpha = Math.max(0.0f, Math.min(1.0f, blurOpacity));

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(
                framebuffer.getColorTexture(),
                this.input.getColorTexture(),
                0, 0, 0, 0, 0,
                framebuffer.width, framebuffer.height
        );

        try (GpuBuffer.MappedView view = encoder.mapBuffer(this.uniforms.currentBuffer(), false, true)) {
            Std140Builder builder = Std140Builder.intoBuffer(view.data());
            builder.putVec4(framebuffer.width, framebuffer.height, quality, alpha);
            builder.putVec4(pxW, pxH, pxX, pxY);
            builder.putVec4(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 1.0f);
            builder.putVec4(rTLPx, rTRPx, rBRPx, rBLPx);
        }

        int paddingPx = (int) Math.ceil(10.0f * scale);
        int scissorX = Math.max(0, (int) Math.floor(pxX) - paddingPx);
        int scissorY = Math.max(0, (int) Math.floor(pxY) - paddingPx);
        int scissorW = Math.min(fbWidth - scissorX, (int) Math.ceil(pxW) + paddingPx * 2);
        int scissorH = Math.min(fbHeight - scissorY, (int) Math.ceil(pxH) + paddingPx * 2);

        GpuTextureView depthView = framebuffer.useDepth ? framebuffer.getDepthTextureView() : null;
        try (RenderPass renderPass = encoder.createRenderPass(
                () -> "Lumin Blur",
                framebuffer.getColorTextureView(),
                OptionalInt.empty(),
                depthView,
                OptionalDouble.empty())
        ) {
            renderPass.setPipeline(this.pipeline);
            renderPass.enableScissor(scissorX, scissorY, Math.max(0, scissorW), Math.max(0, scissorH));
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("BlurUniforms", this.uniforms.currentBuffer());
            renderPass.bindTexture("InputSampler", this.input.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            renderPass.draw(0, 3);
        }
        this.uniforms.rotate();
    }
}
