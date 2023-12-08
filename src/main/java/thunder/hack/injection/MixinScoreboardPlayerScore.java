package thunder.hack.injection;

import net.minecraft.scoreboard.ScoreboardEntry;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.modules.misc.NameProtect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static thunder.hack.modules.Module.mc;

@Mixin(ScoreboardEntry.class)
public class MixinScoreboardPlayerScore {
    @Inject(method = "owner", at = @At("HEAD"), cancellable = true)
    private void getPlayerName(CallbackInfoReturnable<String> ci) {
        if (ModuleManager.nameProtect.isEnabled() && ci.getReturnValue() != null && mc.getSession().getUsername().contains(ci.getReturnValue()))
            ci.setReturnValue(NameProtect.newName.getValue());
    }
}