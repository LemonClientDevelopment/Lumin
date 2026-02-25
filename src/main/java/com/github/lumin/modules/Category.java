package com.github.lumin.modules;

import com.github.lumin.modules.impl.client.InterFace;

import java.util.Locale;

public enum Category {

    COMBAT("\uF01D", "战斗", "Combat related modules"),
    PLAYER("\uF002", "玩家", "Player abilities and stats"),
    VISUAL("\uF019", "渲染", "Visual effects and ESP"),
    MISC("\uF008", "移动", "Movement and utilities"),
    CLIENT("\uF003", "客户端", "Client settings and config");

    public final String icon;
    private final String cnName;
    public final String description;

    Category(String icon, String cnName, String description) {
        this.icon = icon;
        this.cnName = cnName;
        this.description = description;
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