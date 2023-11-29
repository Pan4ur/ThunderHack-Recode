package thunder.hack.injection.accesors;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerEntity.class)
public interface IClientPlayerEntity {
    @Invoker(value = "sendMovementPackets")
    void iSendMovementPackets();

    @Accessor(value = "lastYaw")
    float getLastYaw();

    @Accessor(value = "lastPitch")
    float getLastPitch();

    @Accessor(value = "lastYaw")
    void setLastYaw(float yaw);

    @Accessor(value = "lastPitch")
    void setLastPitch(float pitch);

    @Accessor(value = "mountJumpStrength")
    void setMountJumpStrength(float v);
}
