package thunder.hack.modules.player;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryCleaner extends Module {
    public InventoryCleaner() {
        super("InventoryCleaner", Category.PLAYER);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.NexusGrief);
    private final Setting<DropWhen> dropWhen = new Setting<>("DropWhen", DropWhen.NotInInventory);
    private final Setting<Integer> delay = new Setting<>("Delay", 50, 0, 500);
    private final Setting<Boolean> cleanChests = new Setting<>("CleanChests", false);

    private static final List<Item> nexusShit = Arrays.asList(
            Items.PAPER, Items.OAK_SAPLING, Items.PLAYER_HEAD, Items.WOODEN_AXE,
            Items.KNOWLEDGE_BOOK, Items.BIRCH_SAPLING, Items.STONE_SHOVEL, Items.SPRUCE_SAPLING,
            Items.WHEAT_SEEDS, Items.TORCH, Items.BUCKET, Items.FLINT_AND_STEEL, Items.STICK
    );

    public List<String> items = new ArrayList<>();
    private final Timer delayTimer = new Timer();
    private boolean dirty;

    public void onRender3D(MatrixStack stack) {
        boolean inInv = mc.currentScreen instanceof GenericContainerScreen;

        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest && cleanChests.getValue())
            for (int i = 0; i < chest.getInventory().size(); i++) {
                Slot slot = chest.getSlot(i);
                if (slot.hasStack() && dropThisShit(slot.getStack()) && !(mc.currentScreen.getTitle().getString().contains("Аукцион") || mc.currentScreen.getTitle().getString().contains("покупки")))
                    if (delayTimer.every(delay.getValue())) {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 1, SlotActionType.THROW, mc.player);
                        dirty = true;
                    }
            }

        if (dropWhen.getValue() == DropWhen.Inventory && !inInv) return;
        if (dropWhen.getValue() == DropWhen.NotInInventory && inInv) return;

        for (int slot = 0; slot < 36; slot++) {
            ItemStack itemFromslot = mc.player.getInventory().getStack(slot);
            if (nexusShit.contains(itemFromslot.getItem()) && mode.getValue() == Mode.NexusGrief) {
                // Порой бывает что деревянный топорик - твое единственное оружие...
                if (itemFromslot.getItem() == Items.WOODEN_AXE && InventoryUtility.getItemCount(Items.WOODEN_AXE) == 1)
                    continue;
                drop(slot);
            }
            if (mode.getValue() == Mode.BlackList && dropThisShit(itemFromslot))
                drop(slot);
        }

        if (dirty && delayTimer.passedMs(delay.getValue() + 100)) {
            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            debug("after click cleaning...");
            dirty = false;
        }
    }

    private void drop(int slot) {
        if (delayTimer.every(delay.getValue())) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot < 9 ? slot + 36 : slot, 1, SlotActionType.THROW, mc.player);
            dirty = true;
        }
    }

    private boolean dropThisShit(ItemStack stack) {
        return items.contains(stack.getItem().getTranslationKey().replace("block.minecraft.", "").replace("item.minecraft.", ""));
    }

    public enum DropWhen {
        Inventory, Always, NotInInventory
    }

    public enum Mode {
        NexusGrief, BlackList
    }
}
