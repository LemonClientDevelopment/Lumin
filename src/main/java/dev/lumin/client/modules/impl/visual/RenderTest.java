package dev.lumin.client.modules.impl.visual;

import dev.lumin.client.graphics.skija.Skija;
import dev.lumin.client.graphics.skija.util.SkijaHelper;
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

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {

        Skija.draw(canvas -> SkijaHelper.drawRoundRect(10, 10, 100, 100, 2, SkijaHelper.paintColor(Color.BLACK)));

    }

}
