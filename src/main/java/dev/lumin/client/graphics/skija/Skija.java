package dev.lumin.client.graphics.skija;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lumin.client.graphics.skija.util.state.States;
import io.github.humbleui.skija.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;

public class Skija {

    private static final Minecraft mc = Minecraft.getInstance();

    public static DirectContext context;

    private static BackendRenderTarget renderTarget;
    public static Surface surface;

    public static Canvas canvas;

    private static int lastFboId = -1;
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    public static void initSkia(int fboId, int width, int height) {
        if (context == null) {
            context = DirectContext.makeGL();
        }

        if (surface != null) {
            surface.close();
        }

        if (renderTarget != null) {
            renderTarget.close();
        }

        renderTarget = BackendRenderTarget.makeGL(width, height, 0, 8, fboId, FramebufferFormat.GR_GL_RGBA8);
        surface = Surface.wrapBackendRenderTarget(context, renderTarget, SurfaceOrigin.BOTTOM_LEFT, ColorType.RGBA_8888, ColorSpace.getSRGB());
        canvas = surface.getCanvas();

        lastFboId = fboId;
        lastWidth = width;
        lastHeight = height;
    }

    public static void draw(Consumer<Canvas> drawingLogic) {
        if (context == null) {
            context = DirectContext.makeGL();
        }

        States.INSTANCE.push();

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "lumin_skija", mc.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(), null, OptionalDouble.empty())) {
            renderPass.setPipeline(RenderPipelines.GUI);

            int currentFboId = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
            int width = mc.getWindow().getWidth();
            int height = mc.getWindow().getHeight();

            if (renderTarget == null || currentFboId != lastFboId || width != lastWidth || height != lastHeight) {
                initSkia(currentFboId, width, height);
            }

            context.resetGLAll();
            canvas.save();

            float scaleFactor = (float) mc.getWindow().getGuiScale();
            canvas.scale(scaleFactor, scaleFactor);

            drawingLogic.accept(canvas);

            canvas.restore();
            context.flush(surface);
        }

        States.INSTANCE.pop();
    }

}
