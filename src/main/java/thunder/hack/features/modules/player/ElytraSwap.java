package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class ElytraSwap extends Module {
    public ElytraSwap() {
        super("ElytraSwap", Category.PLAYER);
    }

    private final Setting<Boolean> delay = new Setting<>("Delay", false);
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Enable);
    private final Setting<Bind> switchButton = new Setting<>("SwitchButton", new Bind(-1, false, false), v -> mode.getValue() == Mode.Bind);
    private final Setting<Bind> fireWorkButton = new Setting<>("FireWorkButton", new Bind(-1, false, false), v -> mode.getValue() == Mode.Bind);
    private final Setting<Boolean> startFireWork = new Setting<>("StartFireWork", true, v -> mode.getValue() == Mode.Bind);
    private final Setting<FireWorkMode> fireWorkMode = new Setting<>("FireWorkMode", FireWorkMode.Normal, v -> mode.getValue() == Mode.Bind);

    private final Timer switchTimer = new Timer();
    private final Timer fireworkTimer = new Timer();

    public static boolean swapping = false;

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
        if (mode.getValue() == Mode.Bind && mc.currentScreen == null) {
            if (switchButton.getValue().getKey() != -1 && isKeyPressed(switchButton.getValue().getKey()) && switchTimer.every(500))
                swapChest(false);

            if (fireWorkButton.getValue().getKey() != -1 && isKeyPressed(fireWorkButton.getValue().getKey()) && fireworkTimer.every(500) && mc.player.isFallFlying())
                useFireWork();
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.SendPost e) {
        if (e.getPacket() instanceof ClientCommandC2SPacket command
                && command.getMode() == ClientCommandC2SPacket.Mode.START_FALL_FLYING
                && mode.getValue() == Mode.Bind
                && startFireWork.getValue()) {
            useFireWork();
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
            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        } else {
            sendMessage(isRu() ? "У тебя нет фейерверков!" : "You've got no fireworks!");
            return;
        }

        sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
        sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        if (fireWorkMode.getValue() == FireWorkMode.Silent) {
            InventoryUtility.returnSlot();
            if (!hotbarFireWorkResult.found()) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fireWorkResult.slot(), mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
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
        SearchInvResult result = InventoryUtility.findItemInInventory(Items.ELYTRA);


        if (mc.player.getInventory().getStack(38).getItem() == Items.ELYTRA) {
            int slot = getChestPlateSlot();
            if (slot != -1) {
                if (delay.getValue())
                    Managers.ASYNC.run(() -> {
                        swapping = true;
                        clickSlot(slot);
                        try {
                            Thread.sleep(200);
                        } catch (Exception ignored) {
                        }
                        clickSlot(6);
                        try {
                            Thread.sleep(200);
                        } catch (Exception ignored) {
                        }
                        clickSlot(slot);
                        sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                        swapping = false;
                    });
                else {
                    clickSlot(slot);
                    clickSlot(6);
                    clickSlot(slot);
                    sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                }
            } else {
                if (disable) disable(isRu() ? "У тебя нет нагрудника!" : "You don't have a chestplate!");
                else sendMessage(isRu() ? "У тебя нет нагрудника!" : "You don't have a chestplate!");
                return;
            }
        } else if (result.found()) {
            if (delay.getValue())
                new Thread(() -> {
                    swapping = true;
                    clickSlot(result.slot());
                    try {
                        Thread.sleep(200);
                    } catch (Exception ignored) {
                    }
                    clickSlot(6);
                    try {
                        Thread.sleep(200);
                    } catch (Exception ignored) {
                    }
                    clickSlot(result.slot());
                    sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                    if (startFireWork.getValue() && mc.player.fallDistance > 0)
                        sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    swapping = false;
                }).start();
            else {
                clickSlot(result.slot());
                clickSlot(6);
                clickSlot(result.slot());
                sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                if (startFireWork.getValue() && mc.player.fallDistance > 0)
                    sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        } else {
            if (disable) disable(isRu() ? "У тебя нет элитры!" : "You don't have an elytra!");
            else sendMessage(isRu() ? "У тебя нет элитры!" : "You don't have an elytra!");
            return;
        }

        if (disable)
            disable(isRu() ? "Свапнул! Отключаю.." : "Swapped! Disabling..");
    }
}
