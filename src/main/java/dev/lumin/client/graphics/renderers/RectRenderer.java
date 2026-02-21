package dev.lumin.client.graphics.renderers;

import com.mojang.blaze3d.vertex.*;
import dev.lumin.client.graphics.LuminRenderTypes;

import java.awt.*;

public class RectRenderer implements IRenderer {

    private BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

    public void addRect(float x, float y, float width, float height, Color color) {
        // CCW
        int argb = color.getRGB();
        bufferBuilder.addVertex(x, y, 0).setColor(argb);
        bufferBuilder.addVertex(x, y + height, 0).setColor(argb);
        bufferBuilder.addVertex(x + width, y + height, 0).setColor(argb);
        bufferBuilder.addVertex(x + width, y, 0).setColor(argb);
    }

    @Override
    public void draw() {
        MeshData meshData = bufferBuilder.build();
        if (meshData == null) return;

        LuminRenderTypes.RECTANGLE.draw(meshData);
    }

    @Override
    public void clear() {
        bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }

}
