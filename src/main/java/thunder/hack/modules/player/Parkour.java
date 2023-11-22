package thunder.hack.modules.player;

import thunder.hack.modules.Module;

public class Parkour extends Module {

    public Parkour() {
        super("Parkour", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        if (mc.player.isOnGround() && !mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.1, 0, -0.1).offset(0, -2, 0)).iterator().hasNext())
            mc.player.jump();
    }
}
