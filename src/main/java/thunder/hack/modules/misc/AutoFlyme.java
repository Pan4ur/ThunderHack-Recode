package thunder.hack.modules.misc;

import com.google.common.eventbus.Subscribe;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtil;
import thunder.hack.utility.Timer;

public class AutoFlyme extends Module {
    public final Setting<Boolean> instantSpeed = new Setting<>("InstantSpeed", true);
    public final Setting<Boolean> hover = new Setting<>("hover", false);
    public Setting<Float> hoverY = new Setting("hoverY", 0.228f, 0.0f, 1.0f, v -> hover.getValue());

    private final Timer timer = new Timer();

    public AutoFlyme() {
        super("AutoFlyme", "Автоматически пишет /flyme", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (!mc.player.getAbilities().flying) {
            mc.player.networkHandler.sendChatCommand("flyme");
            mc.player.getAbilities().flying = true;
        }
    }

    @Override
    public void onUpdate() {
        if (!mc.player.getAbilities().flying && timer.passedMs(1000) && !mc.player.isOnGround() && mc.player.input.jumping) {
            mc.player.networkHandler.sendChatCommand("flyme");
            timer.reset();
        }
        if(!mc.options.jumpKey.isPressed() && hover.getValue() && mc.player.getAbilities().flying && mc.player.getAbilities().flying && !mc.player.isOnGround() && !mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, -hoverY.getValue(), 0.0)).iterator().hasNext()){
            mc.player.setVelocity(mc.player.getVelocity().x,-0.05,mc.player.getVelocity().z);
        }
    }

    @Subscribe
    public void onUpdateWalkingPlayer(final EventSync event) {
        if (!instantSpeed.getValue() || !mc.player.getAbilities().flying) return;
        if (MovementUtil.isMoving()) {
            final double[] dir = MovementUtil.forward(1.05f);
            mc.player.setVelocity(dir[0],mc.player.getVelocity().y,dir[1]);
        } else {
            mc.player.setVelocity(0,mc.player.getVelocity().y,0);
        }
    }
}
