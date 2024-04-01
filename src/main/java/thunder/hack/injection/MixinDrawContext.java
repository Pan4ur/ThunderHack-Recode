package thunder.hack.injection;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.gui.font.FontRenderers;

@Mixin(DrawContext.class)
public class MixinDrawContext {

    @Shadow
    @Final
    private MatrixStack matrices;

 //   @Inject(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;IIIZ)I", at = @At("HEAD"), cancellable = true)
    public void drawTextHook(TextRenderer textRenderer, OrderedText text, int x, int y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
        MutableText text1 = Text.empty();
        text.accept((i, style, codePoint) -> {
            text1.append(Text.literal(new String(Character.toChars(codePoint))).setStyle(style));
            return true;
        });

        FontRenderers.sf_medium.drawString(matrices, text1.getString(), x,y, color);
        cir.setReturnValue((int) FontRenderers.sf_medium.getStringWidth(text.toString()));
    }
}
