package com.github.lumin.modules.impl.movement;

import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

public class Sprint extends Module {
    public static Sprint INSTANCE = new Sprint();

    public Sprint() {
        super("Sprint", "疾跑", "idk", "idk", Category.PLAYER);

        keyBind = GLFW.GLFW_KEY_G;
    }

    @SubscribeEvent
    private void onClientTick(ClientTickEvent.Pre event) {
        if (nullCheck()) return;
        mc.options.keySprint.setDown(true);
    }

    @Override
    protected void onDisable() {
        mc.options.keySprint.setDown(false);
    }
}
