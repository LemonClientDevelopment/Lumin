package com.github.lumin.gui.clickgui.component.impl;

import com.github.lumin.gui.Component;
import com.github.lumin.settings.impl.ModeSetting;

public class ModeSettingComponent extends Component {
    private final ModeSetting setting;
    private boolean opened;

    public ModeSettingComponent(ModeSetting setting) {
        this.setting = setting;
    }

}
