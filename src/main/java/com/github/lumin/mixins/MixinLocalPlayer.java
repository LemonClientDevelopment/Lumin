package com.github.lumin.mixins;

import com.github.lumin.events.MotionEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {

    @Unique
    private MotionEvent lumin$motionEvent;

    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    private void onPreSendPosition(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        lumin$motionEvent = NeoForge.EVENT_BUS.post(new MotionEvent(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.onGround()));
        if (lumin$motionEvent.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendPosition", at = @At("RETURN"))
    private void onPostSendPosition(CallbackInfo ci) {
        lumin$motionEvent = null;
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getX()D"))
    private double redirectGetX(LocalPlayer instance) {
        return lumin$motionEvent != null ? lumin$motionEvent.getX() : instance.getX();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getY()D"))
    private double redirectGetY(LocalPlayer instance) {
        return lumin$motionEvent != null ? lumin$motionEvent.getY() : instance.getY();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getZ()D"))
    private double redirectGetZ(LocalPlayer instance) {
        return lumin$motionEvent != null ? lumin$motionEvent.getZ() : instance.getZ();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;position()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 redirectPosition(LocalPlayer instance) {
        return lumin$motionEvent != null ? new Vec3(lumin$motionEvent.getX(), lumin$motionEvent.getY(), lumin$motionEvent.getZ()) : instance.position();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F"))
    private float redirectGetYRot(LocalPlayer instance) {
        return lumin$motionEvent != null ? lumin$motionEvent.getYaw() : instance.getYRot();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
    private float redirectGetXRot(LocalPlayer instance) {
        return lumin$motionEvent != null ? lumin$motionEvent.getPitch() : instance.getXRot();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;onGround()Z"))
    private boolean redirectOnGround(LocalPlayer instance) {
        return lumin$motionEvent != null ? lumin$motionEvent.isOnGround() : instance.onGround();
    }

}
