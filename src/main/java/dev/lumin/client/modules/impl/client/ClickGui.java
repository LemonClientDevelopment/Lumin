package dev.lumin.client.modules.impl.client;

import dev.lumin.client.modules.Category;
import dev.lumin.client.modules.Module;
import org.lwjgl.glfw.GLFW;

public class ClickGui extends Module {
    public static ClickGui INSTANCE = new ClickGui();

    public ClickGui() {
        super("ClickGui", "控制面板", Category.CLIENT);

        keyBind = GLFW.GLFW_KEY_RIGHT_SHIFT;
    }
}
