package dev.lumin.client.skija;

import dev.lumin.client.skija.util.state.States;
import io.github.humbleui.skija.*;
import net.minecraft.client.Minecraft;

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

        Surface face = surface;
        if (face != null) {
            face.close();
        }

        BackendRenderTarget render = renderTarget;
        if (render != null) {
            render.close();
        }

        renderTarget = BackendRenderTarget.makeGL(mc.getWindow().getWidth(), mc.getWindow().getHeight(), 0, 8, getMinecraftFBO()/*fbo*/, FramebufferFormat.GR_GL_RGBA8);
        BackendRenderTarget target = renderTarget;
        surface = Surface.wrapBackendRenderTarget(context, target, SurfaceOrigin.BOTTOM_LEFT, ColorType.RGBA_8888, ColorSpace.getSRGB());
        canvas = surface.getCanvas();
    }

    public static int getMinecraftFBO() {
        return mc.frameBufferId;
    }

    public static void draw(Consumer<Canvas> drawingLogic) {
        States.INSTANCE.push();
        context.resetGLAll();
        canvas.save();
        drawingLogic.accept(canvas);
        canvas.restore();
        surface.flush();
        context.flush();
        States.INSTANCE.pop();
    }

}
