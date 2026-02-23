package com.github.lumin.modules;

import com.github.lumin.Lumin;
import com.github.lumin.modules.impl.client.InterFace;
import com.github.lumin.settings.AbstractSetting;
import com.github.lumin.settings.impl.*;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Module {

    private String englishName;
    private String chineseName;

    public Category category;

    public int keyBind;

    public enum BindMode {Toggle, Hold}

    private BindMode bindMode = BindMode.Toggle;

    private boolean enabled;

    public final List<AbstractSetting<?>> settings = new ArrayList<>();

    //private final BoolSetting hidden; // 控制是否在ModuleListHud中显示

    protected Minecraft mc;

    public Module(String englishName, String chineseName, Category category) {
        this.englishName = englishName;
        this.chineseName = chineseName;
        this.category = category;
        //addSetting(this.hidden = new BoolSetting("Hidden", "隐藏", false));
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

    private <T extends AbstractSetting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    //public boolean isHidden() {
    //    return hidden.getValue();
    //}

    public List<AbstractSetting<?>> getSettings() {
        return settings;
    }

    public BindMode getBindMode() {
        return bindMode;
    }

    public void setBindMode(BindMode bindMode) {
        this.bindMode = bindMode;
    }

    public String getDisplayName() {
        return InterFace.isEnglish() ? englishName : chineseName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getChineseName() {
        return chineseName;
    }

    protected IntSetting intSetting(String englishName, String chineseName, int defaultValue, int min, int max, int step, AbstractSetting.Dependency dependency) {
        return addSetting(new IntSetting(englishName, chineseName, defaultValue, min, max, step, dependency));
    }

    protected IntSetting intSetting(String englishName, String chineseName, int defaultValue, int min, int max, int step) {
        return addSetting(new IntSetting(englishName, chineseName, defaultValue, min, max, step));
    }

    protected BoolSetting boolSetting(String englishName, String chineseName, boolean defaultValue, AbstractSetting.Dependency dependency) {
        return addSetting(new BoolSetting(englishName, chineseName, defaultValue, dependency));
    }

    protected BoolSetting boolSetting(String englishName, String chineseName, boolean defaultValue) {
        return addSetting(new BoolSetting(englishName, chineseName, defaultValue));
    }

    protected DoubleSetting doubleSetting(String englishName, String chineseName, double defaultValue, double min, double max, double step, AbstractSetting.Dependency dependency) {
        return addSetting(new DoubleSetting(englishName, chineseName, defaultValue, min, max, step, dependency));
    }

    protected DoubleSetting doubleSetting(String englishName, String chineseName, double defaultValue, double min, double max, double step) {
        return addSetting(new DoubleSetting(englishName, chineseName, defaultValue, min, max, step));
    }

    protected StringSetting stringSetting(String englishName, String chineseName, String defaultValue, AbstractSetting.Dependency dependency) {
        return addSetting(new StringSetting(englishName, chineseName, defaultValue, dependency));
    }

    protected StringSetting stringSetting(String englishName, String chineseName, String defaultValue) {
        return addSetting(new StringSetting(englishName, chineseName, defaultValue));
    }

    protected ModeSetting modeSetting(String englishName, String chineseName, String defaultValue, String[] modes, AbstractSetting.Dependency dependency) {
        return addSetting(new ModeSetting(englishName, chineseName, defaultValue, modes, dependency));
    }

    protected ModeSetting modeSetting(String englishName, String chineseName, String defaultValue, String[] modes) {
        return addSetting(new ModeSetting(englishName, chineseName, defaultValue, modes));
    }

    protected ColorSetting colorSetting(String englishName, String chineseName, Color defaultValue, AbstractSetting.Dependency dependency) {
        return addSetting(new ColorSetting(englishName, chineseName, defaultValue, dependency));
    }

    protected ColorSetting colorSetting(String englishName, String chineseName, Color defaultValue) {
        return addSetting(new ColorSetting(englishName, chineseName, defaultValue));
    }

}
