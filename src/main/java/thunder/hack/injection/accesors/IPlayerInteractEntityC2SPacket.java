package thunder.hack.injection.accesors;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerInteractEntityC2SPacket.class)
public interface IPlayerInteractEntityC2SPacket {
    @Invoker("write")
    void write(PacketByteBuf buf);
}
