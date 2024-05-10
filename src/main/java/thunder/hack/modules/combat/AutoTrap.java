package thunder.hack.modules.combat;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.CombatManager;
import thunder.hack.modules.base.TrapModule;
import thunder.hack.setting.Setting;

public final class AutoTrap extends TrapModule {
    private final Setting<CombatManager.TargetBy> targetBy = new Setting<>("Target By", CombatManager.TargetBy.Distance);

    public AutoTrap() {
        super("AutoTrap", Category.COMBAT);
    }

    @Override
    protected boolean needNewTarget() {
        return target == null
                || target.distanceTo(mc.player) > range.getValue()
                || target.getHealth() + target.getAbsorptionAmount() <= 0
                || target.isDead();
    }

    @Override
    protected @Nullable PlayerEntity getTarget() {
        return ThunderHack.combatManager.getTarget(range.getValue(), targetBy.getValue());
    }
}