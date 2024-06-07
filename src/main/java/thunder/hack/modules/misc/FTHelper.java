package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import org.apache.commons.lang3.StringUtils;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.notification.Notification;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static thunder.hack.modules.client.ClientSettings.isRu;

public class FTHelper extends Module {

    public FTHelper() {
        super("FTHelper", Category.MISC);
    }

    public final Setting<Boolean> trueSight = new Setting<>("TrueSight", true);
    private final Setting<Boolean> spek = new Setting<>("SpekNotify", true);
    private final Setting<Bind> desorient = new Setting<>("Desorient", new Bind(-1, false, false));
    private final Setting<Bind> trap = new Setting<>("Trap", new Bind(-1, false, false));
    public final Setting<Boolean> aucHelper = new Setting<>("AucHelper", true);
    private final Setting<GroupBy> groupBy = new Setting<>("GroupBy", GroupBy.ItemType, v-> aucHelper.getValue());
    private final Setting<Integer> contrast = new Setting<>("Contrast", 4,1,15, v-> aucHelper.getValue());

    private enum GroupBy {
        Name, ItemType
    }

    private List<AucItem> result = new ArrayList<>();

    private final Timer disorientTimer = new Timer();
    private final Timer trapTimer = new Timer();

    @EventHandler
    private void onSync(EventSync event) {
        if (fullNullCheck()) return;

        if (isKeyPressed(desorient.getValue().getKey()) && disorientTimer.passedMs(3000) && mc.currentScreen == null) {
            use(InventoryUtility.findInHotBar(i -> i.getItem() == Items.ENDER_EYE && i.getName().getString().contains("Дезориентация")),
                    InventoryUtility.findInInventory(i -> i.getItem() == Items.ENDER_EYE && i.getName().getString().contains("Дезориентация")));
            disorientTimer.reset();
        }

        if (isKeyPressed(trap.getValue().getKey()) && trapTimer.passedMs(3000) && mc.currentScreen == null) {
            use(InventoryUtility.findInHotBar(i -> i.getItem() == Items.NETHERITE_SCRAP && i.getName().getString().contains("Трапка")),
                    InventoryUtility.findInInventory(i -> i.getItem() == Items.NETHERITE_SCRAP && i.getName().getString().contains("Трапка")));
            trapTimer.reset();
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof GameMessageS2CPacket pac && spek.getValue()) {
            String content = pac.content().getString().toLowerCase();
            if (content.contains("спек") || content.contains("ызус") || content.contains("spec") || content.contains("spek") || content.contains("ызул")) {
                String name = ThunderUtility.solveName(pac.content().getString());
                ThunderHack.notificationManager.publicity("SpekNotification", isRu() ? name + " хочет чтобы за ним проследили" : name + " wants to be followed", 3, Notification.Type.WARNING);
            }
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest) {
            if (mc.currentScreen.getTitle().getString().contains("Аукцион") || mc.currentScreen.getTitle().getString().contains("Поиск")) {

                result.clear();
                Map<String, Integer> itemMap = new HashMap<>();

                int slot = 0;

                for (ItemStack itemStack : chest.getStacks()) {
                    if (slot > 44)
                        continue;

                    int price = getPrice(itemStack);

                    if (itemStack.getCount() > 1)
                        price /= itemStack.getCount();

                    itemMap.put(getKey(itemStack), Math.min(itemMap.getOrDefault(getKey(itemStack), 999999999), price));
                    slot++;
                }

                slot = 0;
                for (ItemStack itemStack : chest.getStacks()) {
                    if (itemMap.get(getKey(itemStack)) != null) {

                        int price = getPrice(itemStack);

                        if (itemStack.getCount() > 1)
                            price /= itemStack.getCount();

                        result.add(new AucItem(itemStack.getItem(), price, itemMap.get(getKey(itemStack)), slot));
                    }
                    slot++;
                }
            }
        }
    }

    private String getKey(ItemStack stack) {
        if(groupBy.is(GroupBy.Name)) {
            return stack.getName().getString();
        } else {
            return stack.getTranslationKey();
        }
    }

    public void onRenderChest(DrawContext context, Slot slot) {
        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest)
            if (mc.currentScreen.getTitle().getString().contains("Аукцион") || mc.currentScreen.getTitle().getString().contains("Поиск"))
                for (AucItem item : result)
                    if (item.id == slot.id && slot.id <= 44 && !slot.getStack().isEmpty()) {
                        float ratio = (float) (Math.pow(item.lowestPrice, contrast.getValue()) / Math.pow(item.price, contrast.getValue()));

                        Render2DEngine.drawRect(context.getMatrices(), slot.x, slot.y,
                                16, 16, Render2DEngine.interpolateColorC(new Color(0xFF000000, true), new Color(0x00FF00), ratio));
                        return;
                    }
    }

    public int getPrice(ItemStack stack) {
        if (stack.getComponents().toString() == null) return 999999999;

        String string2 = StringUtils.substringBetween(stack.getComponents().toString(), "\"text\":\" $", "\"}]");

        if (string2 == null) return 999999999;

        string2 = string2.replaceAll(" ", "");

        int price = 999999999;

        try {
            price = Integer.parseInt(string2);
        } catch (NumberFormatException  ignored) {}

        return price;
    }


    private record AucItem(Item item, int price, int lowestPrice, int id) {
    }

    private void use(SearchInvResult result, SearchInvResult invResult) {
        if (result.found()) {
            InventoryUtility.saveAndSwitchTo(result.slot());
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
            InventoryUtility.returnSlot();
        } else if (invResult.found()) {
            clickSlot(invResult.slot(), mc.player.getInventory().selectedSlot, SlotActionType.SWAP);
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
            clickSlot(invResult.slot(), mc.player.getInventory().selectedSlot, SlotActionType.SWAP);
            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        }
        disorientTimer.reset();
    }
}
