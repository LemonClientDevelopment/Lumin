package dev.lumin.client.graphics.skija;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import dev.lumin.client.graphics.skija.util.state.States;
import io.github.humbleui.skija.*;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture;

import java.util.function.Consumer;

public class Skija {

    private static final Minecraft mc = Minecraft.getInstance();

    public static DirectContext context;

    private static BackendRenderTarget renderTarget;
    public static Surface surface;

    public static Canvas canvas;

    public static void initSkia() {
        if (context == null) {
            context = DirectContext.makeGL();
        }

        if (surface != null) {
            surface.close();
        }

        if (renderTarget != null) {
            renderTarget.close();
        }

        renderTarget = BackendRenderTarget.makeGL(mc.getWindow().getWidth(), mc.getWindow().getHeight(), 0, 8, getMinecraftFBO(), FramebufferFormat.GR_GL_RGBA8);
        surface = Surface.wrapBackendRenderTarget(context, renderTarget, SurfaceOrigin.BOTTOM_LEFT, ColorType.RGBA_8888, ColorSpace.getSRGB());
        canvas = surface.getCanvas();
    }

    public static int getMinecraftFBO() {
        GpuTexture gpuTexture = mc.getMainRenderTarget().getColorTexture();

        if (gpuTexture instanceof GlTexture glTexture) {
            return glTexture.glId();
        }

        return 0;
    }

    public static void draw(Consumer<Canvas> drawingLogic) {
        if (context == null) {
            initSkia();
        }

        States.INSTANCE.push();
        context.resetGLAll();
        canvas.save();

        float scaleFactor = mc.getWindow().getGuiScale();
        canvas.scale(scaleFactor, scaleFactor);

        drawingLogic.accept(canvas);

        canvas.restore();
        context.flush(surface);
        States.INSTANCE.pop();
    }

}
