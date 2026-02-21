package dev.lumin.client;

import com.mojang.logging.LogUtils;
import dev.lumin.client.managers.Managers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

@Mod(value = Lumin.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Lumin.MODID, value = Dist.CLIENT)
public class Lumin {
    public static final String MODID = "lumin";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Lumin(IEventBus modEventBus, ModContainer modContainer) {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Managers.initManagers();

        Lumin.LOGGER.info("Lumin initialized");
    }

}
