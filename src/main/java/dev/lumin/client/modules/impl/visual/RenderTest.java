package dev.lumin.client.modules.impl.visual;

import dev.lumin.client.graphics.renderers.RectRenderer;
import dev.lumin.client.modules.AbstractModule;
import dev.lumin.client.modules.Category;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class RenderTest extends AbstractModule {

    private static RenderTest INSTANCE;

    private RenderTest() {
        super("render_test", Category.VISUAL);
        keyBind = GLFW.GLFW_KEY_U;
    }

    public static RenderTest getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RenderTest();
        }
        return INSTANCE;
    }

    private final RectRenderer rectRenderer = new RectRenderer();

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        rectRenderer.addRect(10, 10, 200, 200, Color.BLACK);
        rectRenderer.drawAndClear();
    }


}
