package dev.lumin.client.modules.impl.visual;

import dev.lumin.client.graphics.renderers.RectRenderer;
import dev.lumin.client.modules.Category;
import dev.lumin.client.modules.Module;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class RenderTest extends Module {

    public static RenderTest INSTANCE = new RenderTest();

    private RenderTest() {
        super("RenderTest", "渲染测试", Category.VISUAL);
        keyBind = GLFW.GLFW_KEY_U;
    }

    private final RectRenderer rectRenderer = new RectRenderer();

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        rectRenderer.addRect(10, 10, 200, 200, Color.BLACK);
        rectRenderer.drawAndClear();
    }


}
