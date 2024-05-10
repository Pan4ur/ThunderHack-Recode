package thunder.hack.injection;

import thunder.hack.ThunderHack;
import thunder.hack.events.impl.PacketEvent;

import thunder.hack.modules.Module;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener listener, CallbackInfo info) {
        if(Module.fullNullCheck()) return;
        PacketEvent.Receive event = new PacketEvent.Receive(packet);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method = "handlePacket", at = @At("TAIL"), cancellable = true)
    private static <T extends PacketListener> void onHandlePacketPost(Packet<T> packet, PacketListener listener, CallbackInfo info) {
        if(Module.fullNullCheck()) return;
        PacketEvent.ReceivePost event = new PacketEvent.ReceivePost(packet);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"),cancellable = true)
    private void onSendPacketPre(Packet<?> packet, CallbackInfo info) {
        if(Module.fullNullCheck()) return;
        if(ThunderHack.core.silentPackets.contains(packet)) {
            ThunderHack.core.silentPackets.remove(packet);
            return;
        }

        PacketEvent.Send event = new PacketEvent.Send(packet);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled()) info.cancel();
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("RETURN"),cancellable = true)
    private void onSendPacketPost(Packet<?> packet, CallbackInfo info) {
        if(Module.fullNullCheck()) return;
        PacketEvent.SendPost event = new PacketEvent.SendPost(packet);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled()) info.cancel();
    }
}