package thunder.hack.injection.accesors;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSignEditScreen.class)
public interface ISignEditScreen {
    @Accessor("blockEntity")
    SignBlockEntity getBlockEntity();

    @Accessor("front")
    boolean isFront();
}
