package thunder.hack.injection;

import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventMouse;
import thunder.hack.utility.Util;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void onMouseButtonHook(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window == Util.mc.getWindow().getHandle()) {
            Thunderhack.EVENT_BUS.post(new EventMouse(button,action));
        }
    }

}