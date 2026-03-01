package com.github.lumin.utils.render;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public final class WorldToScreen {

    public static Vector2f projectToGui(Vec3 worldPos, Vec3 cameraPos, Matrix4f modelViewRotation, Matrix4f projection, float guiWidth, float guiHeight) {
        float x = (float) (worldPos.x - cameraPos.x);
        float y = (float) (worldPos.y - cameraPos.y);
        float z = (float) (worldPos.z - cameraPos.z);

        Vector4f clip = new Vector4f(x, y, z, 1.0f);
        modelViewRotation.transform(clip);
        projection.transform(clip);

        if (clip.w <= 0.0f) return null;

        float ndcX = clip.x / clip.w;
        float ndcY = clip.y / clip.w;
        float ndcZ = clip.z / clip.w;

        if (ndcZ < -1.0f || ndcZ > 1.0f) return null;

        float sx = (ndcX * 0.5f + 0.5f) * guiWidth;
        float sy = (1.0f - (ndcY * 0.5f + 0.5f)) * guiHeight;

        if (Float.isNaN(sx) || Float.isNaN(sy) || Float.isInfinite(sx) || Float.isInfinite(sy)) return null;

        return new Vector2f(sx, sy);
    }

}

