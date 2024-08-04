package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.math.BlockPos;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.MovementUtility;

import static thunder.hack.features.modules.combat.Criticals.getEntity;
import static thunder.hack.features.modules.combat.Criticals.getInteractType;

public class MoreKnockback extends Module {
    public MoreKnockback() {
        super("MoreKnockback", Category.COMBAT);
    }

    public Setting<Boolean> inMove = new Setting<>("InMove", true);
    public Setting<Integer> hurtTime = new Setting<>("HurtTime", 10, 0, 10);
    public Setting<Integer> chance = new Setting<>("Chance", 100, 0, 100);

    @EventHandler
    public void onSendPacket(PacketEvent.Send event) {
        if ((!MovementUtility.isMoving() || inMove.getValue())
                && event.getPacket() instanceof PlayerInteractEntityC2SPacket
                && getInteractType(event.getPacket()) == Criticals.InteractType.ATTACK
                && !(getEntity(event.getPacket()) instanceof EndCrystalEntity)
                && getEntity(event.getPacket()) instanceof LivingEntity lent
                && lent.hurtTime <= hurtTime.getValue()
                && MathUtility.random(0, 100) >= (100 - chance.getValue())
                && !canCrit()) {

            if (mc.player.isSprinting()) sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            debug("wtap");
            mc.player.setSprinting(true);
            mc.player.lastSprinting = true;
        }
    }

    private boolean canCrit() {
        boolean reasonForSkipCrit =
                        mc.player.getAbilities().flying
                        || (mc.player.isFallFlying()
                        || ModuleManager.elytraPlus.isEnabled())
                        || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                        || mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos())).getBlock() == Blocks.COBWEB
                        || mc.player.isInLava()
                        || mc.player.isSubmergedInWater();

        if (mc.player.getAttackCooldownProgress(0.5f) < 0.9f)
            return false;

        if (ModuleManager.criticals.isEnabled() && !ModuleManager.criticals.mode.is(Criticals.Mode.Grim))
            return true;

        if (ModuleManager.criticals.isEnabled() && ModuleManager.criticals.mode.is(Criticals.Mode.Grim) && !mc.player.isOnGround())
            return true;

        if (!reasonForSkipCrit)
            return !mc.player.isOnGround() && mc.player.fallDistance > 0f;

        return false;
    }
}
