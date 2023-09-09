package thunder.hack.modules.player;

import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

public class ElytraSwap extends Module {
    public ElytraSwap() {
        super("ElytraSwap", Category.PLAYER);
    }


    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Enable);
    private final Setting<Bind> switchButton = new Setting<>("SwitchButton", new Bind(-1, false, false), v -> mode.getValue() == Mode.Bind);
    private final Setting<Bind> fireWorkButton = new Setting<>("FireWorkButton", new Bind(-1, false, false), v -> mode.getValue() == Mode.Bind);
    private final Setting<FireWorkMode> fireWorkMode = new Setting<>("FireWorkMode", FireWorkMode.Normal, v -> mode.getValue() == Mode.Bind);

    private Timer switchTimer = new Timer();
    private Timer fireworkTimer = new Timer();

    private enum Mode {
        Enable, Bind
    }

    private enum FireWorkMode {
        Silent, Normal
    }

    @Override
    public void onEnable() {
        if (mode.getValue() == Mode.Enable)
            swapChest(true);
    }

    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.Bind) {
            if (switchButton.getValue().getKey() != -1 && InputUtil.isKeyPressed(mc.getWindow().getHandle(), switchButton.getValue().getKey()) && switchTimer.passedMs(500)) {
                swapChest(false);
                switchTimer.reset();
            }
            if (fireWorkButton.getValue().getKey() != -1 && InputUtil.isKeyPressed(mc.getWindow().getHandle(), fireWorkButton.getValue().getKey()) && fireworkTimer.passedMs(500)) {
                useFireWork();
                fireworkTimer.reset();
            }
        }
    }

    public void useFireWork() {
        SearchInvResult hotbarFireWorkResult = InventoryUtility.findItemInHotBar(Items.FIREWORK_ROCKET);
        SearchInvResult fireWorkResult = InventoryUtility.findItemInInventory(Items.FIREWORK_ROCKET);

        InventoryUtility.saveSlot();
        if (hotbarFireWorkResult.found()) {
            hotbarFireWorkResult.switchTo();
        } else if (fireWorkResult.found()) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fireWorkResult.slot(), mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        } else {
            sendMessage(MainSettings.isRu() ? "У тебя нет фейерверков!" : "You don't have rockets!");
            return;
        }

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

        if(fireWorkMode.getValue() == FireWorkMode.Silent){
            InventoryUtility.returnSlot();
            if(!hotbarFireWorkResult.found()){
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fireWorkResult.slot(), mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            }
        }
    }

    public static int getChestPlateSlot() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE};
        for (Item item : items) {
            SearchInvResult slot = InventoryUtility.findItemInInventory(item);
            if (slot.found()) {
                return slot.slot();
            }
        }
        return -1;
    }

    private void swapChest(boolean disable) {
        if (mc.player.getInventory().getStack(38).getItem() == Items.ELYTRA) {
            int slot = getChestPlateSlot();
            if (slot != -1) {
                clickSlot(slot);
                clickSlot(6);
                clickSlot(slot);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            } else {
                disable(MainSettings.isRu() ? "У тебя нет нагрудника!" : "You don't have chestplate!");
                return;
            }
        } else if (InventoryUtility.getItemSlot(Items.ELYTRA) != -1) {
            int slot = InventoryUtility.getItemSlot(Items.ELYTRA);
            clickSlot(slot);
            clickSlot(6);
            clickSlot(slot);
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        } else {
            disable(MainSettings.isRu() ? "У тебя нет элитры!" : "You don't have elytra!");
            return;
        }

        if (disable)
            disable(MainSettings.isRu() ? "Свапнул! Отключаю.." : "Swapped! Disabling..");
    }
}
