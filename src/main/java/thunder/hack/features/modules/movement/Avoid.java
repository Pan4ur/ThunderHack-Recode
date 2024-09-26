package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.TripwireBlock;
import thunder.hack.events.impl.EventCollision;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

import static net.minecraft.block.TripwireHookBlock.ATTACHED;

public class Avoid extends Module {
    public Avoid() {
        super("Avoid", Category.MOVEMENT);
    }

    private final Setting<Boolean> voidAir = new Setting<>("Void", true);
    private final Setting<Boolean> cactus = new Setting<>("Cactus", true);
    private final Setting<Boolean> fire = new Setting<>("Fire", true);
    private final Setting<Boolean> berryBush = new Setting<>("BerryBush", true);
    private final Setting<Boolean> powderSnow = new Setting<>("PowderSnow", true);
    private final Setting<Boolean> unloaded = new Setting<>("Unloaded", true);
    private final Setting<Boolean> lava = new Setting<>("Lava", true);
    private final Setting<Boolean> plate = new Setting<>("Plate", true);
    private final Setting<Boolean> trapString = new Setting<>("Tripwire", true);

    @EventHandler
    public void onCollide(EventCollision e) {
        if (fullNullCheck()) return;
        Block b = e.getState().getBlock();

        boolean avoidUnloaded = !mc.world.isChunkLoaded(e.getPos().getX() >> 4, e.getPos().getZ() >> 4) && unloaded.getValue();
        boolean avoidVoid = e.getPos().getY() < mc.world.getBottomY() && voidAir.getValue();
        boolean avoidCactus = b == Blocks.CACTUS && cactus.getValue();
        boolean avoidFire = (b == Blocks.FIRE || b == Blocks.SOUL_FIRE) && fire.getValue();
        boolean avoidBerryBush = (b == Blocks.SWEET_BERRY_BUSH) && berryBush.getValue();
        boolean avoidSusSnow = (b == Blocks.POWDER_SNOW) && powderSnow.getValue();
        boolean avoidLava = (b == Blocks.LAVA) && lava.getValue();
        boolean avoidPlate = (b instanceof PressurePlateBlock || b == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE || b == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE) && plate.getValue();
        boolean avoidTrapString = (b instanceof TripwireBlock) && e.getState().get(ATTACHED) && trapString.getValue();

        if (avoidUnloaded || avoidFire || avoidCactus || avoidLava || avoidBerryBush || avoidSusSnow || avoidPlate || avoidTrapString || avoidVoid)
            e.setState(Blocks.DIRT.getDefaultState());
    }
}
