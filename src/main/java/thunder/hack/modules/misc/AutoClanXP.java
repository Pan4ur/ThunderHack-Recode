package thunder.hack.modules.misc;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import thunder.hack.modules.Module;
import thunder.hack.utility.player.InventoryUtility;

public class AutoClanXP extends Module{
    public AutoClanXP(){
        super("AutoClanXP", Category.MISC);
    }
    @Override
    public void onUpdate(){
        assert mc.interactionManager != null;
        assert mc.player != null;

        BlockHitResult bhr = new BlockHitResult(new Vec3d( mc.player.getX(), mc.player.getY(), mc.player.getZ() + Math.random()), mc.player.getHorizontalFacing(), mc.player.getBlockPos(), false);

        int redstone = InventoryUtility.findItemInHotBar(Items.REDSTONE).slot();
        sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bhr, redstone));
        mc.interactionManager.breakBlock(mc.player.getBlockPos());
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
