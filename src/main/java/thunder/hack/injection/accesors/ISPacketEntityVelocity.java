package thunder.hack.injection.accesors;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface ISPacketEntityVelocity {
    @Mutable
    @Accessor("velocityX")
    void setMotionX(int velocityX);

    @Mutable
    @Accessor("velocityY")
    void setMotionY(int velocityY);

    @Mutable
    @Accessor("velocityZ")
    void setMotionZ(int velocityZ);
}