package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import thunder.hack.ThunderHack;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.injection.accesors.ILivingEntity;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class TriggerBot extends Module {
    public TriggerBot() {
        super("TriggerBot", Category.COMBAT);
    }

    public final Setting<Float> attackRange = new Setting<>("Range", 3f, 1f, 7.0f);
    public final Setting<Boolean> smartCrit = new Setting<>("SmartCrit", true);
    public final Setting<Boolean> ignoreWalls = new Setting<>("IgnoreWals", false);

    @EventHandler
    public void onAttack(PlayerUpdateEvent e) {
        if (!autoCrit()) return;
        Entity ent = ThunderHack.playerManager.getRtxTarger(mc.player.getYaw(), mc.player.getPitch(), attackRange.getValue(), ignoreWalls.getValue());
        if (ent != null && !ThunderHack.friendManager.isFriend(ent.getName().getString())) {
            mc.interactionManager.attackEntity(mc.player, ent);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private boolean autoCrit() {
        boolean reasonForSkipCrit = !smartCrit.getValue() || mc.player.getAbilities().flying || mc.player.isFallFlying() || mc.player.hasStatusEffect(StatusEffects.SLOWNESS) || mc.player.isHoldingOntoLadder() || (mc.world.getBlockState(new BlockPos((int) Math.floor(mc.player.getX()), (int) (Math.floor(mc.player.getY())), (int) Math.floor(mc.player.getZ()))).getBlock() == Blocks.COBWEB);
        if (mc.player.fallDistance > 1 && mc.player.fallDistance < 1.14) return false;
        if (!(MathHelper.clamp(((float) ((ILivingEntity) mc.player).getLastAttackedTicks() + 0.5f) / Aura.getAttackCooldownProgressPerTick(), 0.0F, 1.0F) >= (0.93f)))
            return false;
        if (!mc.options.jumpKey.isPressed() && (!ModuleManager.targetStrafe.isEnabled() && !ModuleManager.speed.isEnabled()))
            return true;
        if (mc.player.isInLava()) return true;
        if (!mc.options.jumpKey.isPressed() && Aura.isAboveWater()) return true;
        double d2 = (double) ((int) mc.player.getY()) - mc.player.getY();
        if ((d2 == -0.01250004768371582 || d2 == -0.1875) && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, mc.player.getEyeHeight(mc.player.getPose()), 0.0)).iterator().hasNext() && !mc.player.isSneaking())
            return true;
        if (!reasonForSkipCrit) return !mc.player.isOnGround() && mc.player.fallDistance > 0.0f;
        return true;
    }
}
