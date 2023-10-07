package thunder.hack.injection.accesors;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExplosionS2CPacket.class)
public interface IExplosionS2CPacket {
    @Mutable
    @Accessor("playerVelocityX")
    void setMotionX(float velocityX);

    @Mutable
    @Accessor("playerVelocityY")
    void setMotionY(float velocityY);

    @Mutable
    @Accessor("playerVelocityZ")
    void setMotionZ(float velocityZ);

    @Accessor("playerVelocityX")
    float getMotionX();

    @Accessor("playerVelocityY")
    float getMotionY();

    @Accessor("playerVelocityZ")
    float getMotionZ();
}
