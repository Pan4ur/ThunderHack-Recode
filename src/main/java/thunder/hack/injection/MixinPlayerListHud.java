package thunder.hack.injection;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Nullables;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.client.ClientSettings;

import java.util.Comparator;
import java.util.List;

import static thunder.hack.features.modules.Module.mc;

@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {
    private static final Comparator<Object> ENTRY_ORDERING = Comparator.comparingInt((entry) -> ((PlayerListEntry) entry).getGameMode() == GameMode.SPECTATOR ? 1 : 0)
            .thenComparing((entry) -> Nullables.mapOrElse(((PlayerListEntry) entry).getScoreboardTeam(), Team::getName, ""))
            .thenComparing((entry) -> ((PlayerListEntry) entry).getProfile().getName(), String::compareToIgnoreCase);

    @Inject(method = "collectPlayerEntries", at = @At("HEAD"), cancellable = true)
    private void collectPlayerEntriesHook(CallbackInfoReturnable<List<PlayerListEntry>> cir) {
        if (ClientSettings.futureCompatibility.getValue())
            return;

        if (ThunderHack.isFuturePresent())
            return;

        if (ModuleManager.extraTab.isEnabled())
            cir.setReturnValue(mc.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).limit(1000).toList());
        else
            cir.setReturnValue(mc.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).limit(80).toList());
    }
}