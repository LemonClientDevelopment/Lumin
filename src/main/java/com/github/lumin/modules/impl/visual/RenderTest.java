package com.github.lumin.modules.impl.visual;

import com.github.lumin.graphics.renderers.RectRenderer;
import com.github.lumin.graphics.renderers.TextRenderer;
import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
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
    private final TextRenderer textRenderer = new TextRenderer();

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {

        rectRenderer.addRect(10, 10, 100, 100, Color.WHITE);
        textRenderer.addText("What The Fuck", 50, 50, Color.BLACK, 1.0f);


        rectRenderer.drawAndClear();
        textRenderer.drawAndClear();

    }

}
