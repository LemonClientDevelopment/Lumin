package dev.lumin.client.utils.i18n;

import dev.lumin.client.modules.Category;

public class I18nUtils {

    /****** Categories ******/
    private static final TranslateComponent CATEGORY_COMBAT_COMPONENT
            = new TranslateComponent("category", "combat");

    private static final TranslateComponent CATEGORY_MOVEMENT_COMPONENT
            = new TranslateComponent("category", "movement");

    private static final TranslateComponent CATEGORY_MISC_COMPONENT
            = new TranslateComponent("category", "misc");

    private static final TranslateComponent CATEGORY_VISUAL_COMPONENT
            = new TranslateComponent("category", "visual");

    private static final TranslateComponent CATEGORY_CLIENT_COMPONENT
            = new TranslateComponent("category", "client");

    public static TranslateComponent getTranslateComponentFromCategory(Category category) {
        return switch (category) {
            case COMBAT -> CATEGORY_COMBAT_COMPONENT;
            case MOVEMENT -> CATEGORY_MOVEMENT_COMPONENT;
            case MISC -> CATEGORY_MISC_COMPONENT;
            case VISUAL -> CATEGORY_VISUAL_COMPONENT;
            case CLIENT -> CATEGORY_CLIENT_COMPONENT;
        };
    }

}
