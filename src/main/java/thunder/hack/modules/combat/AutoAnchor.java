package thunder.hack.modules.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import thunder.hack.events.impl.EventPlaceBlock;
import thunder.hack.injection.accesors.IMinecraftClient;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.InventoryUtil;

public class AutoAnchor extends Module {
    public AutoAnchor() {
        super("AnchorAura", Category.COMBAT);
    }


    public Setting<Mode> mode = new Setting<>("Mode", Mode.Legit);
    public Setting<Integer> swapDelay = new Setting<>("SwapDelay", 100, 0, 1000);
    public Setting<Integer> charge = new Setting<>("Charge", 5, 1, 5);

    private enum Mode{
        Legit, Rage
    }

    @Subscribe
    public void onBlockPlace(EventPlaceBlock event){
        if(mode.getValue() == Mode.Rage) return;
        if(event.getBlock() == Blocks.RESPAWN_ANCHOR && mc.options.useKey.isPressed()){
            int glowSlot = InventoryUtil.getItemSlotHotbar(Items.GLOWSTONE);
            if(glowSlot == -1 ) return;
            new LegitThread(glowSlot,mc.player.getInventory().selectedSlot,swapDelay.getValue()).start();
        }
    }

    public class LegitThread extends Thread {
        int glowSlot,originalSlot,delay;

        public LegitThread(int glowSlot, int originalSlot, int delay) {
            this.glowSlot = glowSlot;
            this.originalSlot = originalSlot;
            this.delay = delay;
        }

        @Override
        public void run() {
            try {sleep(delay);} catch (Exception ignored) {}

            mc.player.getInventory().selectedSlot= glowSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(glowSlot));
            try {sleep(delay);} catch (Exception ignored) {}
            for (int i = 0; i < charge.getValue(); i++) {
                ((IMinecraftClient)mc).idoItemUse();
            }

            try {sleep(delay);} catch (Exception ignored) {}

            mc.player.getInventory().selectedSlot = originalSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));

            try {sleep(delay);} catch (Exception ignored) {}
            if(charge.getValue() < 5) ((IMinecraftClient)mc).idoItemUse();

            super.run();
        }
    }
}
