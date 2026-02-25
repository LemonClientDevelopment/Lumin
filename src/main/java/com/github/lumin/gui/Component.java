package com.github.lumin.gui;

import com.github.lumin.utils.render.MouseUtils;

public class Component implements IComponent {
    private float x, y, width, height;
    protected float scale = 1.0f;

    public boolean isHovered(float mouseX, float mouseY) {
        return MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);
    }

    public boolean isHovered(float mouseX, float mouseY, float height) {
        return MouseUtils.isHovering(x, y, width, height, mouseX, mouseY);
    }

    public boolean isVisible() {
        return true;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
