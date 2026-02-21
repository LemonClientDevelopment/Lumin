package dev.lumin.client.modules.impl.client;

import dev.lumin.client.modules.Category;
import dev.lumin.client.modules.Module;
import dev.lumin.client.settings.impl.ModeSetting;
import io.github.humbleui.skija.FilterBlurMode;

public class InterFace extends Module {
    public static InterFace INSTANCE = new InterFace();

    public InterFace() {
        super("Interface", "界面", Category.CLIENT);
    }

    /*public static ColorSetting mainColor = new ColorValue("FirstColor",new Color(101, 78, 163));
    public static ColorSetting secondeColor = new ColorValue("SecondColor",new Color(234, 175, 200));
    public static ModeSetting colorMode = new ModeValue("Color Mode","Tenacity", new String[]{"Fade", "Rainbow", "Astolfo", "Dynamic","Tenacity", "Static", "Double"});
    public static final IntSetting colorspeed = new NumberValue("ColorSpeed",() -> colorMode.is("Tenacity") || colorMode.is("Dynamic"), 4, 1, 10, 1);
    public static final NumberValue colorIndex = new NumberValue("Color Seperation", () -> colorMode.is("Tenacity"), 1, 1, 50, 1);
    public static BoolValue watermark = new BoolValue("WaterMark",true);
    public static ModeValue waterMarkmode = new ModeValue("WaterMarkMode",()-> watermark.get(),"Exhi",new String[]{"Exhi","Type","Type2","Type3"});
    public static final NumberValue opacity = new NumberValue("Opacity", 0.5, 0, 1, 0.05);
    public static final NumberValue radius = new NumberValue("Radius",3, 0, 17.5, 0.5);
    public static BoolValue white = new BoolValue("White", false);
    public static final BoolValue shader = new BoolValue("Shader",false);
    public static final NumberValue shaderRadius = new NumberValue("Shader Radius", shader::get, 8.0, 0.0, 30.0, 0.1f);*/
    public static ModeSetting filterBlurMode = new ModeSetting("FilterBlur", "滤镜模糊", "NORMAL", new String[]{"NORMAL", "SOLID", "OUTER", "INNER"});

    public static FilterBlurMode filterBlurMode() {
        return switch (filterBlurMode.getValue()) {
            case "SOLID" -> FilterBlurMode.SOLID;
            case "OUTER" -> FilterBlurMode.OUTER;
            case "INNER" -> FilterBlurMode.INNER;
            default -> FilterBlurMode.NORMAL;
        };
    }
}
