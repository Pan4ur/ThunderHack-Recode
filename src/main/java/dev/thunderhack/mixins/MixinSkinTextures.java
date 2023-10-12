package dev.thunderhack.mixins;

import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.modules.client.Media;

@Mixin(SkinTextures.class)
public class MixinSkinTextures {
    private static final Identifier SUN_SKIN = new Identifier("textures/sunskin.png");

    @Inject(method = "texture", at = @At("HEAD"), cancellable = true)
    public void getSkinTextureHook(CallbackInfoReturnable<Identifier> cir) {
        if (ModuleManager.media.isEnabled() && Media.skinProtect.getValue()) {
            cir.setReturnValue(SUN_SKIN);
        }
    }
}
