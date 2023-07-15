package thunder.hack.injection;


import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventKeyPress;
import thunder.hack.events.impl.EventKeyRelease;
import thunder.hack.gui.clickui.ClickUI;
import thunder.hack.utility.Util;
import thunder.hack.gui.hud.HudEditorGui;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        boolean whitelist = Util.mc.currentScreen == null || Util.mc.currentScreen instanceof ClickUI || Util.mc.currentScreen instanceof HudEditorGui;
        if (!whitelist) return;
        if (action == 1) {
            Thunderhack.moduleManager.onKeyPressed(key);
        }
        if (action == 2) action = 1;
        switch (action) {
            case 0 -> {
                EventKeyRelease event = new EventKeyRelease(key, scanCode);
               // mc.world.playSound(mc.player, mc.player.getBlockPos(), Thunderhack.KEYRELEASE_SOUNDEVENT, SoundCategory.BLOCKS, 1f, 1f);

                Thunderhack.EVENT_BUS.post(event);
                if (event.isCancelled()) ci.cancel();
            }
            case 1 -> {
                EventKeyPress event = new EventKeyPress(key, scanCode);
              //  mc.world.playSound(mc.player, mc.player.getBlockPos(), Thunderhack.KEYPRESS_SOUNDEVENT, SoundCategory.BLOCKS, 1f, 1f);

                Thunderhack.EVENT_BUS.post(event);
                if (event.isCancelled()) ci.cancel();
            }
        }
    }
}