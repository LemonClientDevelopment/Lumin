package dev.lumin.client.managers.impl;

import dev.lumin.client.modules.Module;
import dev.lumin.client.modules.impl.client.ClickGui;
import dev.lumin.client.modules.impl.visual.RenderTest;

import java.util.List;

public class ModuleManager {

    private static ModuleManager INSTANCE = null;
    private List<Module> modules;

    private ModuleManager() {
        initModules();
    }

    public static ModuleManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ModuleManager();
        }
        return INSTANCE;
    }

    private void initModules() {
        modules = List.of(
                // Combat

                // Movement

                // Visual
                RenderTest.INSTANCE,

                // Client
                ClickGui.INSTANCE
        );
    }

    public void onKeyPress(int keyCode) {
        for (final var module : modules) {
            if (module.keyBind == keyCode) module.toggle();
        }
    }

}
