package thunder.hack.injection;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Scoreboard.class)
public abstract class MixinScoreBoard {
    @Final
    @Shadow
    private Object2ObjectMap<String, Team> teamsByScoreHolder;

    @Inject(method = "removeScoreHolderFromTeam", at = @At("HEAD"), cancellable = true)
    public void removeScoreHolderFromTeamHook(String scoreHolderName, Team team, CallbackInfo ci) {
        ci.cancel();
        if (teamsByScoreHolder.get(scoreHolderName) != team) {
            //("Player is either on another team or not on any team. Cannot remove from team '" + team.getName() + "'.");
            return;
        }
        teamsByScoreHolder.remove(scoreHolderName);
        team.getPlayerList().remove(scoreHolderName);
    }
}
