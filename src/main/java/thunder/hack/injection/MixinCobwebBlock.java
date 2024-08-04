package thunder.hack.injection;

import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.movement.AntiWeb;
import thunder.hack.utility.player.InteractionUtility;

import static thunder.hack.core.manager.IManager.mc;

@Mixin(CobwebBlock.class)
public class MixinCobwebBlock {
    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void onEntityCollisionHook(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (ModuleManager.antiWeb.isEnabled() && AntiWeb.mode.getValue() == AntiWeb.Mode.Ignore && entity == mc.player) {
            ci.cancel();
            if (AntiWeb.grim.getValue())
                InteractionUtility.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP, id));
        }
    }
}