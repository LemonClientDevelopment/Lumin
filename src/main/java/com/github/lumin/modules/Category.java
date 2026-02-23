package com.github.lumin.modules;

import com.github.lumin.modules.impl.client.InterFace;

import java.util.Locale;

public enum Category {

    COMBAT("战斗"),
    MOVEMENT("移动"),
    PLAYER("玩家"),
    VISUAL("渲染"),
    CLIENT("客户端");

    public final String cnName;

    Category(String cnName) {
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
