package com.github.lumin.gui.clickgui.component.impl;

import com.github.lumin.gui.Component;
import com.github.lumin.settings.impl.IntSetting;

public class IntSettingComponent extends Component {
    private final IntSetting setting;
    private boolean dragging;

    public IntSettingComponent(IntSetting setting) {
        this.setting = setting;
    }


}
