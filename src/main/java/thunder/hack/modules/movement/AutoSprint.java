package thunder.hack.modules.movement;

import thunder.hack.core.impl.ModuleManager;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.Module;
import thunder.hack.modules.combat.Aura;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class AutoSprint extends Module {
    public static final Setting<Boolean> sprint = new Setting<>("KeepSprint", true);
    public static final Setting<Float> motion = new Setting("Motion", 1f, 0f, 1f, v -> sprint.getValue());
    private final Setting<Boolean> stopWhileUsing = new Setting<>("StopWhileUsing", false);
    private final Setting<Boolean> pauseWhileAura = new Setting<>("PauseWhileAura", false);

    public AutoSprint() {
        super("AutoSprint", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        mc.player.setSprinting(
                mc.player.getHungerManager().getFoodLevel() > 6
                        && !mc.player.horizontalCollision
                        && mc.player.input.movementForward > 0
                        && (!mc.player.isSneaking() || (ModuleManager.noSlow.isEnabled() && ModuleManager.noSlow.sneak.getValue()))
                        && (!mc.player.isUsingItem() || !stopWhileUsing.getValue())
                        && (!ModuleManager.aura.isEnabled() || Aura.target == null || !pauseWhileAura.getValue())
        );
    }
}
