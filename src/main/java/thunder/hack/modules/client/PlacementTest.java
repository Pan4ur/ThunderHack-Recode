package thunder.hack.modules.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;

public class PlacementTest extends Module {
    public PlacementTest() {
        super("PlacementTest", Category.CLIENT);
    }

    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Strict);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("PlaceMode", InteractionUtility.PlaceMode.Normal);

    private Timer timer = new Timer();

    @EventHandler
    public void onSync(EventSync e){

        BlockPos bp = new BlockPos(0,-60,0);

        if(timer.passedMs(500)){
            InteractionUtility.placeBlock(bp, false, interact.getValue(), placeMode.getValue());
            timer.reset();
        }

        float[] angle = InteractionUtility.getPlaceAngle(bp, interact.getValue());
        if(angle != null) {
            mc.player.setYaw(angle[0]);
            mc.player.setPitch(angle[1]);
        }
    }
}
