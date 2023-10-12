package dev.thunderhack.mixins;

import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.modules.misc.NameProtect;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.thunderhack.modules.Module.mc;

@Mixin(ScoreboardPlayerScore.class)
public class MixinScoreboardPlayerScore {
    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    private void getPlayerName(CallbackInfoReturnable<String> ci) {
        if (ModuleManager.nameProtect.isEnabled() && ci.getReturnValue() != null && mc.getSession().getUsername().contains(ci.getReturnValue()))
            ci.setReturnValue(NameProtect.newName.getValue());
    }
}