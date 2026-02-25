package com.github.lumin.managers.impl;

import com.github.lumin.modules.Module;
import com.github.lumin.modules.impl.client.*;
import com.github.lumin.modules.impl.movement.Sprint;
import com.github.lumin.modules.impl.visual.RenderTest;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.List;

public class ModuleManager {
    private List<Module> modules;

    public ModuleManager() {
        initModules();
    }

    private void initModules() {
        modules = List.of(
                // Combat

                // Movement
                Sprint.INSTANCE,

                // Visual
                RenderTest.INSTANCE,

                // Client
                ClickGui.INSTANCE,
                InterFace.INSTANCE,
                Test1.INSTANCE,
                Test2.INSTANCE,
                Test3.INSTANCE
        );
    }

    public List<Module> getModules() {
        return modules;
    }

    public void onKeyEvent(int keyCode, int action) {
        for (final var module : modules) {
            if (module.keyBind == keyCode) {
                if (module.getBindMode() == Module.BindMode.Hold) {
                    if (action == InputConstants.PRESS || action == InputConstants.REPEAT) {
                        module.setEnabled(true);
                    } else if (action == InputConstants.RELEASE) {
                        module.setEnabled(false);
                    }
                } else {
                    if (action == InputConstants.PRESS) {
                        module.toggle();
                    }
                }
            }
        }
    }

}
