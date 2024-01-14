package thunder.hack.modules.movement;

import thunder.hack.modules.Module;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class AutoWalk extends Module {

    public AutoWalk() {
        super("AutoWalk", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        mc.options.forwardKey.setPressed(true);
    }
}
