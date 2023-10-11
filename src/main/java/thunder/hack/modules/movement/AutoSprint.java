package thunder.hack.modules.movement;

import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class AutoSprint extends Module {
    public static final Setting<Boolean> sprint = new Setting<>("KeepSprint", true);
    public static final Setting<Float> motion = new Setting("Motion", 1f, 0f, 1f, v -> sprint.getValue());
    private final Setting<Boolean> stopWhileUsing = new Setting<>("StopWhileUsing", false);
    private final Setting<Boolean> omni = new Setting<>("Omni", false);

    public AutoSprint() {
        super("AutoSprint", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        if (mc.player.getHungerManager().getFoodLevel() <= 6) return;
        if (mc.player.horizontalCollision) return;
        if (mc.player.input.movementForward < 0 && !omni.getValue()) return;
        if (mc.player.isSneaking()) return;
        if (mc.player.isUsingItem() && stopWhileUsing.getValue()) return;

        if (!MovementUtility.isMoving()) return;

        mc.player.setSprinting(true);
    }
}
