package thunder.hack.injection.accesors;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayerInteractionManager.class)
public interface IInteractionManager {
    @Accessor(value = "currentBreakingProgress")
    float getCurBlockDamageMP();

    @Accessor(value = "currentBreakingProgress")
    void setCurBlockDamageMP(float a);
}