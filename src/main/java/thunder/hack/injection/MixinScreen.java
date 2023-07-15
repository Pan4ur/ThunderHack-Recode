package thunder.hack.injection;


import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(Screen.class)
public abstract class MixinScreen {
/*
    @ModifyArgs(method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V"))
    private void getList(Args args, MatrixStack matrixStack, ItemStack itemStack, int x, int y) {
        Tooltips tooltips = (Tooltips) Thunderhack.moduleRegistry.get(Tooltips.class);

        if (hasItems(itemStack) && tooltips.storage.getValue() || (itemStack.getItem() == Items.ENDER_CHEST && tooltips.echest.getValue())) {
          //  if(args.size() < 3) return;
          //  List<Text> lines = args.get(1);

         //   int yChanged = y - 4;
          //  yChanged -= 10 * lines.size();

            args.set(4, 30);
        }
    }

 */

}