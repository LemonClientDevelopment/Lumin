package com.github.lumin.gui.clickgui.component.impl;

import com.github.lumin.gui.Component;
import com.github.lumin.settings.impl.DoubleSetting;

public class DoubleSettingComponent extends Component {
    private final DoubleSetting setting;
    private boolean dragging;

    public DoubleSettingComponent(DoubleSetting setting) {
        this.setting = setting;
    }


}
