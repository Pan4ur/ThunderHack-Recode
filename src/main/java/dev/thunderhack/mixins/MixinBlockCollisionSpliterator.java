package dev.thunderhack.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.event.events.EventCollision;

@Mixin(value = BlockCollisionSpliterator.class, priority = 800)
public abstract class MixinBlockCollisionSpliterator {
    // я надеюсь это никто не будет редиректить
    @Redirect(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/BlockView;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState computeNextHook(BlockView instance, BlockPos blockPos) {
        if(!ModuleManager.antiWeb.isEnabled() && !ModuleManager.phase.isEnabled()) return instance.getBlockState(blockPos);
        EventCollision event = new EventCollision(instance.getBlockState(blockPos), blockPos);
        ThunderHack.EVENT_BUS.post(event);
        return event.getState();
    }
}

