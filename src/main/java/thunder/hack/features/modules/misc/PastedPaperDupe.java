package thunder.hack.features.modules.misc;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import meteordevelopment.orbit.EventHandler;
import thunder.hack.features.modules.Module;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.events.impl.EventPostTick;

import java.util.List;
import java.util.Optional;

public class PastedPaperDupe extends Module {

    public PastedPaperDupe() {
        super("PastedPaperDupe", Category.MISC);
    }

    @EventHandler
    private void onTick(EventPostTick event) {
        if(!(mc.player.getInventory().getMainHandStack().getItem()  == Items.WRITABLE_BOOK)) {
            disable("Please hold a writable book!");
            return;
        }
        for (int i = 9; i < 44; i++) {
            if (36 + mc.player.getInventory().selectedSlot == i) continue;
            mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(
                    mc.player.currentScreenHandler.syncId,
                    mc.player.currentScreenHandler.getRevision(),
                    i,
                    1,
                    SlotActionType.THROW,
                    ItemStack.EMPTY,
                    Int2ObjectMaps.emptyMap()
            ));
        }
        mc.player.networkHandler.sendPacket(new BookUpdateC2SPacket(
                mc.player.getInventory().selectedSlot, List.of(""), Optional.of("The quick brown fox jumps over the lazy dog"
        )));
        toggle();
    }
}
