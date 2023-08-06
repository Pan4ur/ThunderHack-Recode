package thunder.hack.injection.accesors;

import net.minecraft.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BeaconBlockEntity.class)
public interface IBeaconBlockEntity {
    @Accessor(value = "level")
    int getLevel();
}
