package dev.lumin.client.modules;

import dev.lumin.client.Lumin;
import net.neoforged.neoforge.common.NeoForge;

public class Module {

    public String englishName;
    public String chineseName;

    public Category category;

    public int keyBind;

    private boolean enabled;

    public Module(String englishName, String chineseName, Category category) {
        this.englishName = englishName;
        this.chineseName = chineseName;
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

            Lumin.LOGGER.info("{} has been enabled", englishName);
        } else {
            try {
                NeoForge.EVENT_BUS.unregister(this);
            } catch (Exception ignored) {
            }

            Lumin.LOGGER.info("{} has been disabled", englishName);
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
