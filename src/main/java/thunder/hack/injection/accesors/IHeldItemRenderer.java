package thunder.hack.injection.accesors;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HeldItemRenderer.class)
public interface IHeldItemRenderer {
    @Accessor(value="equipProgressMainHand")
    void setEquippedProgressMainHand(float var1);

    @Accessor(value="equipProgressMainHand")
    float getEquippedProgressMainHand();

    @Accessor(value="mainHand")
    void setItemStackMainHand(ItemStack var1);
}