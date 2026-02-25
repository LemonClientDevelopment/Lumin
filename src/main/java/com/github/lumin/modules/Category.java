package com.github.lumin.modules;

import com.github.lumin.modules.impl.client.InterFace;

import java.util.Locale;

public enum Category {

    COMBAT("\uF01D", "战斗"),
    MOVEMENT("\uF008", "移动"),
    PLAYER("\uF002", "玩家"),
    VISUAL("\uF019", "渲染"),
    CLIENT("\uF003", "客户端");

    public final String icon;
    public final String cnName;

    Category(String icon, String cnName) {
        this.icon = icon;
        this.cnName = cnName;
    }

    public String getName() {
        if (InterFace.isEnglish()) {
            String lower = name().toLowerCase(Locale.ROOT);
            return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
        } else {
            return cnName;
        }
    }

}
