package dev.lumin.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lumin.client.graphics.LuminRenderPipelines;
import dev.lumin.client.managers.impl.ModuleManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = Lumin.MODID, value = Dist.CLIENT)
public class EventHandler {

    @SubscribeEvent
    static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        LuminRenderPipelines.onRegisterRenderPipelines(event);
    }

    @SubscribeEvent
    static void onKeyPress(InputEvent.Key event) {
        if (event.getAction() != InputConstants.PRESS) return;
        ModuleManager.getInstance().onKeyPress(event.getKey());
    }

}
