package thunder.hack.injection;

import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.manager.client.ModuleManager;

@Mixin({ClientBrandRetriever.class})
public class MixinClientBrandRetriever {
    @Inject(method = "getClientModName", at = {@At("HEAD")}, cancellable = true, remap = false)
    private static void getClientModNameHook(CallbackInfoReturnable<String> cir) {
        if(ModuleManager.clientSpoof.isEnabled())
            cir.setReturnValue(ModuleManager.clientSpoof.getClientName());
    }
}