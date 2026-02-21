package dev.lumin.client.modules;

import dev.lumin.client.Lumin;
import dev.lumin.client.settings.AbstractSetting;
import dev.lumin.client.settings.impl.BoolSetting;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

public class Module {

    public String englishName;
    public String chineseName;

    public Category category;

    public int keyBind;

    public enum BindMode {Toggle, Hold}

    private BindMode bindMode = BindMode.Toggle;

    private boolean enabled;

    public final List<AbstractSetting<?>> settings = new ArrayList<>();

    private final BoolSetting hidden;

    protected Minecraft mc;

    public Module(String englishName, String chineseName, Category category) {
        this.englishName = englishName;
        this.chineseName = chineseName;
        this.category = category;
        addSetting(this.hidden = new BoolSetting("Hidden", "隐藏", false));
        mc = Minecraft.getInstance();
    }

    protected boolean nullCheck() {
        return mc.player == null || mc.level == null;
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void toggle() {
        enabled = !enabled;

        if (enabled) {
            // Will throw an exception when the module doesn't have any listeners
            try {
                NeoForge.EVENT_BUS.register(this);
            } catch (Exception ignored) {
            }

            onEnable();

            Lumin.LOGGER.info("{} has been enabled", englishName);
        } else {
            try {
                NeoForge.EVENT_BUS.unregister(this);
            } catch (Exception ignored) {
            }

            onDisable();

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

    public void reset() {
        setEnabled(false);
        bindMode = BindMode.Toggle;
        for (AbstractSetting<?> setting : settings) {
            setting.reset();
        }
    }

    private void addSetting(AbstractSetting<?> setting) {
        settings.add(setting);
    }

    public boolean isHidden() {
        return hidden.getValue();
    }

    public List<AbstractSetting<?>> getSettings() {
        return settings;
    }

    public BindMode getBindMode() {
        return bindMode;
    }

    public void setBindMode(BindMode bindMode) {
        this.bindMode = bindMode;
    }

}
