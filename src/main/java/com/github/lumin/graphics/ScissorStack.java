package com.github.lumin.graphics;

import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.Minecraft;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScissorStack {

    private static final Deque<ScissorRegion> stack = new ArrayDeque<>();

    public static void push(RenderPass pass, int x, int y, int width, int height) {
        ScissorRegion current = stack.peekLast();

        ScissorRegion newRegion;
        if (current != null) {
            newRegion = current.intersect(x, y, width, height);
        } else {
            newRegion = new ScissorRegion(x, y, width, height);
        }

        stack.addLast(newRegion);
        applyScissor(pass, newRegion);
    }

    public static void pop(RenderPass pass) {
        if (stack.isEmpty()) {
            pass.disableScissor();
            return;
        }

        stack.removeLast();

        ScissorRegion current = stack.peekLast();
        if (current != null) {
            applyScissor(pass, current);
        } else {
            pass.disableScissor();
        }
    }

    public static void clear(RenderPass pass) {
        stack.clear();
        pass.disableScissor();
    }

    public static ScissorRegion getCurrent() {
        return stack.peekLast();
    }

    public static boolean isEmpty() {
        return stack.isEmpty();
    }

    public static int getStackDepth() {
        return stack.size();
    }

    private static void applyScissor(RenderPass pass, ScissorRegion region) {
        if (region == null || region.isEmpty()) {
            pass.disableScissor();
            return;
        }

        var window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();

        int scissorX = (int) (region.x * scale);
        int scissorY = (int) (window.getHeight() - (region.y + region.height) * scale);
        int scissorWidth = (int) (region.width * scale);
        int scissorHeight = (int) (region.height * scale);

        pass.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    public static boolean isPointInside(double x, double y) {
        ScissorRegion current = getCurrent();
        if (current == null) {
            return true;
        }
        return current.contains(x, y);
    }

    public static boolean isRectInside(double x, double y, double width, double height) {
        ScissorRegion current = getCurrent();
        if (current == null) {
            return true;
        }
        return current.containsRect(x, y, width, height);
    }

    public record ScissorRegion(int x, int y, int width, int height) {

        public boolean isEmpty() {
            return width <= 0 || height <= 0;
        }

        public boolean contains(double px, double py) {
            return px >= x && px < x + width && py >= y && py < y + height;
        }

        public boolean containsRect(double rx, double ry, double rw, double rh) {
            return rx >= x && ry >= y && rx + rw <= x + width && ry + rh <= y + height;
        }

        public ScissorRegion intersect(int otherX, int otherY, int otherWidth, int otherHeight) {
            int newX = Math.max(x, otherX);
            int newY = Math.max(y, otherY);
            int newWidth = Math.min(x + width, otherX + otherWidth) - newX;
            int newHeight = Math.min(y + height, otherY + otherHeight) - newY;

            if (newWidth <= 0 || newHeight <= 0) {
                return new ScissorRegion(0, 0, 0, 0);
            }

            return new ScissorRegion(newX, newY, newWidth, newHeight);
        }

        public ScissorRegion intersect(ScissorRegion other) {
            return intersect(other.x, other.y, other.width, other.height);
        }
    }
}
