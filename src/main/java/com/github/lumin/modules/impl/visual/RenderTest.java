package com.github.lumin.modules.impl.visual;

import com.github.lumin.graphics.renderers.*;
import com.github.lumin.graphics.shaders.BlurShader;
import com.github.lumin.modules.Category;
import com.github.lumin.modules.Module;
import com.google.common.base.Suppliers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.function.Supplier;

public class RenderTest extends Module {

    public static RenderTest INSTANCE = new RenderTest();

    private RenderTest() {
        super("RenderTest", "渲染测试", Category.VISUAL);
        keyBind = GLFW.GLFW_KEY_U;
    }

    private final Supplier<RectRenderer> rectRendererSupplier = Suppliers.memoize(RectRenderer::new);
    private final Supplier<RoundRectRenderer> roundRectRendererSupplier = Suppliers.memoize(RoundRectRenderer::new);
    private final Supplier<LineRenderer> lineRendererSupplier = Suppliers.memoize(LineRenderer::new);
    private final Supplier<CircleRenderer> circleRendererSupplier = Suppliers.memoize(CircleRenderer::new);
    private final Supplier<GradientRectRenderer> gradientRectRendererSupplier = Suppliers.memoize(GradientRectRenderer::new);
    private final Supplier<TextRenderer> textRendererSupplier = Suppliers.memoize(TextRenderer::new);

    private long startTime = System.currentTimeMillis();

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        RectRenderer rectRenderer = rectRendererSupplier.get();
        RoundRectRenderer roundRectRenderer = roundRectRendererSupplier.get();
        LineRenderer lineRenderer = lineRendererSupplier.get();
        CircleRenderer circleRenderer = circleRendererSupplier.get();
        GradientRectRenderer gradientRectRenderer = gradientRectRendererSupplier.get();
        TextRenderer textRenderer = textRendererSupplier.get();

        BlurShader.drawRoundedBlur(50, 50, 100, 100, 5, 5);

        float baseX = 50;
        float baseY = 50;
        long elapsed = System.currentTimeMillis() - startTime;
        float anim = (float) Math.sin(elapsed / 1000.0) * 10;

        rectRenderer.addRect(baseX, baseY, 100, 100, new Color(255, 100, 100, 200));
        rectRenderer.addRect(baseX + 120, baseY + anim, 80, 60, new Color(100, 255, 100, 180));

        roundRectRenderer.addRoundRect(baseX, baseY + 130, 200, 80, 15, new Color(100, 100, 255, 180));
        roundRectRenderer.addRoundRect(baseX + 220, baseY + 130, 150, 80, 25, new Color(255, 200, 100, 200));

        lineRenderer.addRectOutline(baseX - 5, baseY - 5, 210, 240, 2, new Color(255, 255, 255, 150));
        lineRenderer.addLine(baseX + 250, baseY, baseX + 400 + anim, baseY + 100, 2, new Color(255, 100, 255));
        lineRenderer.addLine(baseX + 250, baseY + 100, baseX + 400 - anim, baseY, 2, new Color(100, 255, 255));

        float circleX = baseX + 500;
        float circleY = baseY + 80;
        circleRenderer.addFilledCircle(circleX, circleY, 40 + anim, new Color(255, 150, 50, 180));
        circleRenderer.addCircle(circleX + 100, circleY, 30, 5, new Color(50, 200, 255));
        circleRenderer.addRing(circleX + 200, circleY, 20, 35, new Color(200, 50, 255, 200));

        gradientRectRenderer.addHorizontalGradient(baseX, baseY + 230, 200, 40, new Color(255, 0, 0, 180), new Color(0, 255, 0, 180));
        gradientRectRenderer.addVerticalGradient(baseX + 220, baseY + 230, 200, 40, new Color(0, 0, 255, 180), new Color(255, 255, 0, 180));
        gradientRectRenderer.addGradientRect(baseX + 440, baseY + 230, 200, 40, new Color(255, 0, 255, 180), new Color(0, 255, 255, 180), GradientRectRenderer.GradientDirection.HORIZONTAL);

        textRenderer.addText("Lumin Render Test", baseX, baseY + 290, new Color(255, 255, 255), 1.5f);
        textRenderer.addText("Rectangles, RoundRects, Lines, Circles, Gradients", baseX, baseY + 320, new Color(200, 200, 200), 1.0f);
        textRenderer.addText("Animation: " + String.format("%.2f", anim), baseX, baseY + 345, new Color(150, 255, 150), 1.0f);

        rectRenderer.drawAndClear();
        roundRectRenderer.drawAndClear();
        lineRenderer.drawAndClear();
        circleRenderer.drawAndClear();
        gradientRectRenderer.drawAndClear();
        textRenderer.drawAndClear();
    }

    @Override
    protected void onEnable() {
        startTime = System.currentTimeMillis();
    }

}
