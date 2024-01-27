package thunder.hack.injection;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.client.EventMouse;

import static thunder.hack.modules.Module.mc;
import static thunder.hack.system.Systems.MANAGER;

@Mixin(Mouse.class)
public class MixinMouse {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void onMouseButtonHook(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window == mc.getWindow().getHandle()) {
            if (action == 0) MANAGER.MODULE.onMoseKeyReleased(button);
            if (action == 1) MANAGER.MODULE.onMoseKeyPressed(button);

            ThunderHack.EVENT_BUS.post(new EventMouse(button, action));
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScrollHook(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == mc.getWindow().getHandle()) {
            ThunderHack.EVENT_BUS.post(new EventMouse((int) vertical, 2));
        }
    }
}