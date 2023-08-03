package thunder.hack.injection;


import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.Thunderhack;
import thunder.hack.core.CommandManager2;

import java.util.Objects;


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

    @Inject(method = "handleTextClick", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", ordinal = 1, remap = false), cancellable = true)
    private void onRunCommand(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (Objects.requireNonNull(style.getClickEvent()).getValue().startsWith(String.valueOf(CommandManager2.PREFIX)))
            try {
                CommandManager2 manager = Thunderhack.commandManager2;
                manager.getDispatcher().execute(style.getClickEvent().getValue().substring(1), manager.getSource());
                cir.setReturnValue(true);
            } catch (CommandSyntaxException ignored) {
            }
    }
}