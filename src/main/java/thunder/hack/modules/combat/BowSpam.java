package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import thunder.hack.utility.player.PlayerUtility;

public class BowSpam extends Module {
    private final Setting<Integer> ticks = new Setting<>("Delay", 3, 0, 20);

    private static BowSpam instance;

    public BowSpam() {
        super("BowSpam", Category.COMBAT);
        instance = this;
    }

    @EventHandler
    public void onSync(EventSync event) {
        if ((mc.player.getOffHandStack().getItem() == Items.BOW || mc.player.getMainHandStack().getItem() == Items.BOW) && mc.player.isUsingItem()) {
            if (mc.player.getItemUseTime() >= this.ticks.getValue()) {
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                sendPacket(new PlayerInteractItemC2SPacket(mc.player.getOffHandStack().getItem() == Items.BOW ? Hand.OFF_HAND : Hand.MAIN_HAND, PlayerUtility.getWorldActionId(mc.world)));
                mc.player.stopUsingItem();
            }
        }
    }

    public static BowSpam getInstance() {
        return instance;
    }
}
