package thunder.hack.injection;

import net.minecraft.client.item.TooltipType;
import net.minecraft.item.Item;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.modules.render.Tooltips;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ShulkerBoxBlock.class)
public class MixinShulkerBoxBlock {
    @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true)
    private void onAppendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options, CallbackInfo info) {
        if (ModuleManager.tooltips == null) return;
        if (Tooltips.storage.getValue()) info.cancel();
    }
}