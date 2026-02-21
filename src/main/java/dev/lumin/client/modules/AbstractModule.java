package dev.lumin.client.modules;

import dev.lumin.client.Lumin;
import dev.lumin.client.utils.i18n.TranslateComponent;
import net.neoforged.neoforge.common.NeoForge;

public abstract class AbstractModule {

    public TranslateComponent name;
    public Category category;
    public int keyBind;

    private boolean enabled;

    public AbstractModule(String nameKey, Category category) {
        this.name = new TranslateComponent("modules", nameKey);
        this.category = category;
    }

    public void toggle() {
        enabled = !enabled;

        if (enabled) {
            // Will throw an exception when the module doesn't have any listeners
            try {
                NeoForge.EVENT_BUS.register(this);
            } catch (Exception ignored) {
            }

            Lumin.LOGGER.info("{} has been enabled", name.suffix());
        } else {
            try {
                NeoForge.EVENT_BUS.unregister(this);
            } catch (Exception ignored) {
            }

            Lumin.LOGGER.info("{} has been disabled", name.suffix());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            toggle();
        }
    }

}
