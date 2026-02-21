package dev.lumin.client.modules.impl.visual;

import dev.lumin.client.modules.Category;
import dev.lumin.client.modules.Module;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class RenderTest extends Module {

    public static RenderTest INSTANCE = new RenderTest();

    private RenderTest() {
        super("RenderTest", "渲染测试", Category.VISUAL);
        keyBind = GLFW.GLFW_KEY_U;
    }

    @SubscribeEvent
    public void onRenderGui() {
    }

}
