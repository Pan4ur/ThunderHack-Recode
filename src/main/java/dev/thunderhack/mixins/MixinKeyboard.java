package dev.thunderhack.mixins;

import dev.thunderhack.event.events.EventKeyPress;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.event.events.EventKeyRelease;
import dev.thunderhack.gui.clickui.ClickUI;
import dev.thunderhack.gui.hud.HudEditorGui;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.thunderhack.modules.Module.mc;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        boolean whitelist = mc.currentScreen == null || mc.currentScreen instanceof ClickUI || mc.currentScreen instanceof HudEditorGui;
        if (!whitelist) return;

        if (action == 0) ThunderHack.moduleManager.onKeyReleased(key);
        if (action == 1) ThunderHack.moduleManager.onKeyPressed(key);
        if (action == 2) action = 1;

        switch (action) {
            case 0 -> {
                EventKeyRelease event = new EventKeyRelease(key, scanCode);
               // mc.world.playSound(mc.player, mc.player.getBlockPos(), Thunderhack.KEYRELEASE_SOUNDEVENT, SoundCategory.BLOCKS, 1f, 1f);

                ThunderHack.EVENT_BUS.post(event);
                if (event.isCancelled()) ci.cancel();
            }
            case 1 -> {
                EventKeyPress event = new EventKeyPress(key, scanCode);
              //  mc.world.playSound(mc.player, mc.player.getBlockPos(), Thunderhack.KEYPRESS_SOUNDEVENT, SoundCategory.BLOCKS, 1f, 1f);

                ThunderHack.EVENT_BUS.post(event);
                if (event.isCancelled()) ci.cancel();
            }
        }
    }
}