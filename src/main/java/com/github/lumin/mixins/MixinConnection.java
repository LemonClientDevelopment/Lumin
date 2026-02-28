package com.github.lumin.mixins;

import com.github.lumin.events.PacketEvent;
import com.github.lumin.utils.network.PacketUtils;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Connection.class)
public class MixinConnection {

    @Shadow
    private void sendPacket(Packet<?> packet, @Nullable ChannelFutureListener sendListener, boolean flush) {
    }

    @Shadow
    private static <T extends PacketListener> void genericsFtw(Packet<T> packet, PacketListener listener) {
    }

    @Redirect(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V"))
    private void onReceivePacket(Packet<?> packet, PacketListener pListener) {
        PacketEvent.Receive event = NeoForge.EVENT_BUS.post(new PacketEvent.Receive(packet));

        if (!event.isCanceled()) {
            genericsFtw(event.getPacket(), pListener);
        }
    }

    @Redirect(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;sendPacket(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"))
    private void onSend(Connection instance, Packet<?> packet, @Nullable ChannelFutureListener sendListener, boolean flush) {
        if (PacketUtils.bypassPackets.contains(packet)) {
            PacketUtils.bypassPackets.remove(packet);
            this.sendPacket(packet, sendListener, flush);
        } else {
            PacketEvent.Send event = NeoForge.EVENT_BUS.post(new PacketEvent.Send(packet));
            if (!event.isCanceled()) {
                this.sendPacket(event.getPacket(), sendListener, flush);
            }
        }
    }

}
