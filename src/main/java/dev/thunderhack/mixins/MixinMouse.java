package dev.thunderhack.mixins;

import dev.thunderhack.event.events.EventMouse;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.thunderhack.ThunderHack;

import static dev.thunderhack.modules.Module.mc;

@Mixin(Mouse.class)
public class MixinMouse {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void onMouseButtonHook(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window == mc.getWindow().getHandle()) {
            if (action == 0) ThunderHack.moduleManager.onMoseKeyReleased(button);
            if (action == 1) ThunderHack.moduleManager.onMoseKeyPressed(button);

            ThunderHack.EVENT_BUS.post(new EventMouse(button, action));
        }
    }
}