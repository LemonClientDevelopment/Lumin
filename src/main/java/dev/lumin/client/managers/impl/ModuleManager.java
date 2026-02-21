package dev.lumin.client.managers.impl;

import dev.lumin.client.modules.AbstractModule;
import dev.lumin.client.modules.impl.visual.RenderTest;

import java.util.List;

public class ModuleManager {

    private static ModuleManager INSTANCE = null;
    private List<AbstractModule> modules;

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
                /* VISUAL */
                RenderTest.getInstance()
        );
    }

    public void onKeyPress(int keyCode) {
        for (final var module : modules) {
            if (module.keyBind == keyCode) module.toggle();
        }
    }

}
