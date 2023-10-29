package thunder.hack.injection.accesors;

import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInteractBlockC2SPacket.class)
public interface IPlayerInteractBlockC2SPacket {
    @Mutable
    @Accessor("hand")
    void setHand(Hand hand);
}
