package thunder.hack.modules.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import org.apache.commons.lang3.StringUtils;
import thunder.hack.core.impl.ServerManager;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.autobuy.AutoBuyGui;
import thunder.hack.gui.autobuy.AutoBuyItem;
import thunder.hack.gui.autobuy.ItemLog;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AutoBuy extends Module {

    public AutoBuy() {
        super("AutoBuy", Category.CLIENT);
    }

    public final Setting<Float> shulkerMultiplier = new Setting<>("ShulkerMultiplier", 1.5f, 1f, 5f);
    public final Setting<Bind> openGui = new Setting<>("OpenGui", new Bind(-1, false, false));
    public final Setting<String> command = new Setting<>("Command", "ah");
    public final Setting<String> an = new Setting<>("An", "an235");
    public final Setting<Integer> minDelay = new Setting<>("MinUpdateDelay", 400, 50, 1000);
    public final Setting<Integer> maxDelay = new Setting<>("MaxUpdateDelay", 550, 100, 3000);
    public final Setting<Integer> buyDelay = new Setting<>("BuyDelay", 800, 0, 3000);

    private final Timer updateTimer = new Timer();
    private final Timer reAhTimer = new Timer();
    private final Timer buyTimer = new Timer();
    private final Timer chatTimer = new Timer();

    private Pair<ItemStack, Integer> lastStack;

    public static final ArrayList<AutoBuyItem> items = new ArrayList<>();
    public static final ArrayList<ItemLog> log = new ArrayList<>();

    private int messageCount;
    private static boolean active;
    public static int successfully;


    @Override
    public void onEnable() {
        mc.setScreen(AutoBuyGui.getAutoBuyGui());
    }

    @Override
    public void onUpdate() {
        if (isKeyPressed(openGui.getValue().getKey())) mc.setScreen(AutoBuyGui.getAutoBuyGui());
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof GameMessageS2CPacket pac) {
            if (pac.content().getString().contains("успешно")) {
                if (lastStack != null)
                    log.add(new ItemLog(lastStack.getLeft(), "успешно, за: " + lastStack.getRight() + " " + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
                successfully++;
                lastStack = null;
            }

            if (pac.content().getString().contains("Этот товар уже купили!")) {
                if (lastStack != null)
                    log.add(new ItemLog(lastStack.getLeft(), "не успел, за: " + lastStack.getRight() + " " + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
                lastStack = null;
            }

            if (pac.content().getString().contains("У Вас не хватает денег!")) {
                if (lastStack != null)
                    log.add(new ItemLog(lastStack.getLeft(), "не хватило денег, за: " + lastStack.getRight() + " " + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
                lastStack = null;
                disable("У Вас не хватает денег!");
            }

            if (pac.content().getString().contains("Данной команды не существует!")) {
                updateTimer.setMs(-35000);
                reAhTimer.setMs(-35000);
                buyTimer.setMs(-35000);
                chatTimer.setMs(-35000);
                mc.player.networkHandler.sendChatCommand(an.getValue().toString());
            }

            if(pac.content().getString().contains("Здесь нет команд!")) {
                mc.player.setPitch(1);
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if(chatTimer.passedMs(3000))
            messageCount = 0;

        if (mc.player.getX() == -3 && mc.player.getY() == 1 && mc.player.getZ() == 11 && chatTimer.passedMs(100)) {
            updateTimer.setMs(-35000);
            reAhTimer.setMs(-35000);
            buyTimer.setMs(-35000);
            chatTimer.setMs(-35000);
            mc.player.networkHandler.sendChatCommand(an.getValue().toString());
        }

        if (!active)
            return;

        if (mc.currentScreen == null && reAhTimer.every(ServerManager.getPing() + 100))
            openAh(null, command.getValue());

        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest) {
            if (mc.currentScreen.getTitle().getString().contains("Аукционы") || mc.currentScreen.getTitle().getString().contains("Поиск")) {
                int slot = 0;
                for (ItemStack itemStack : chest.getStacks()) {
                    NbtList lore = getLoreTagList(itemStack);
                    if (lore != null) {
                        int price = getPrice(lore.toString());
                        if (isGoodDeal(itemStack, price, lore) && buyTimer.every(1000)) {
                            sendMessage(itemStack.getName().getString() + " " + price);
                            lastStack = new Pair<>(itemStack, price);
                            int finalSlot = slot;
                            new Thread(() -> {
                                active = false;
                                try {Thread.sleep((long) (buyDelay.getValue() + MathUtility.random(0, 50)));} catch (Exception ignored) {}
                                Buy(finalSlot);
                                try {Thread.sleep(500);} catch (Exception ignored) {}
                                active = true;
                            }).start();
                            return;
                        }
                    }
                    slot++;
                }
                if (updateTimer.every((int) MathUtility.random(minDelay.getValue(), maxDelay.getValue())))
                    clickSlot(49);
            } else if(mc.currentScreen.getTitle().getString().contains("Подозрительная цена")) {
                new Thread(() -> {
                    active = false;
                    try {Thread.sleep((long) (buyDelay.getValue() + MathUtility.random(0, 50)));} catch (Exception ignored) {}
                    Buy(1);
                    try {Thread.sleep(500);} catch (Exception ignored) {}
                    active = true;
                }).start();
            }
        }
    }

    private void openAh(String seller, String command)  {
        if(messageCount > 4)
            return;

        if(seller != null)
            mc.player.networkHandler.sendChatCommand("ah " + seller);
        else
            mc.player.networkHandler.sendChatCommand(command);

        messageCount++;
        chatTimer.reset();
    }

    public boolean isGoodDeal(ItemStack is, int price, NbtList lore) {
        if (is.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
            boolean empty = true;
            NbtCompound compoundTag = is.getSubNbt("BlockEntityTag");
            DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(27, ItemStack.EMPTY);
            if (compoundTag != null) {
                Inventories.readNbt(compoundTag, itemStacks);
                for (ItemStack s : itemStacks) {
                    if (isGoodDeal(s, (int) (price * shulkerMultiplier.getValue()), lore))
                        return true;
                    if(!s.isEmpty())
                        empty = false;
                }
            }
            if(empty)
                return false;
        }

        for (AutoBuyItem abItem : items)
            if (is.getItem() == abItem.getItem() && is.getCount() >= abItem.getCount() && price <= abItem.getPrice()) {
                if (abItem.checkForStar() && !is.getName().getString().contains("★"))
                    return false;

                if(!abItem.getAttributes().isEmpty())
                    for (String atr : abItem.getAttributes())
                        if(!lore.toString().toLowerCase().contains(atr.toLowerCase()) && !is.getName().getString().toLowerCase().contains(atr.toLowerCase()))
                            return false;

                if (abItem.getEnchantments().isEmpty()) {
                    return true;
                } else {
                    if (is.getEnchantments().isEmpty())
                        return false;

                    ArrayList<Pair<String, Integer>> ench = getABEnchants(is);
                    int matches = 0;
                    for (Pair<String, Integer> expected : abItem.getEnchantments()) {
                        for (Pair<String, Integer> real : ench) {
                            if (real.getLeft().equals(expected.getLeft()) && real.getRight() >= expected.getRight())
                                matches++;
                        }
                    }
                    if (matches == abItem.getEnchantments().size())
                        return true;
                }
            }
        return false;
    }

    public void Buy(int slot) {
        sendMessage("buy 1 =" + slot);
        clickSlot(slot, SlotActionType.QUICK_MOVE);
    }

    public static NbtList getLoreTagList(ItemStack stack) {
        NbtCompound displayTag = stack.getOrCreateSubNbt("display");
        if (!(stack.hasNbt() && stack.getNbt().contains("display", NbtElement.COMPOUND_TYPE) && stack.getOrCreateSubNbt("display").contains("Lore", NbtElement.LIST_TYPE)))
            return null;
        return displayTag.getList("Lore", NbtElement.STRING_TYPE);
    }

    public int getPrice(String string) {
        if (string == null) return 9999999;
        String string2 = StringUtils.substringBetween(string, "\"text\":\" $", "\"}]");
        if (string2 == null) return 9999999;
        string2 = string2.replaceAll(" ", "");
        return Integer.parseInt(string2);
    }

    public String getSeller(String string) {
        if (string == null) return "null";
        String string2 = StringUtils.substringBetween(string, "{\"color\":\"gold\",\"text\":\" ", "\"}]");
        if (string2 == null) return "null";
        return string2;
    }

    public ArrayList<Pair<String, Integer>> getABEnchants(ItemStack stack) {
        ArrayList<Pair<String, Integer>> result = new ArrayList<>();
        for (NbtElement tag : stack.getEnchantments()) {
            String id = StringUtils.substringBetween(tag.toString(), "id:\"minecraft:", "\",lvl");
            String lvl = StringUtils.substringBetween(tag.toString(), ",lvl:", "s");
            int lvlInt = Integer.parseInt(lvl);
            if (id == null)
                id = "none";
            result.add(new Pair<>(id, lvlInt));
        }
        return result;
    }

    public static Pair<String, Integer> parseEnchant(String val) {
        if (val.isEmpty()) return null;
        if (!val.contains(":")) return new Pair<>(val, 0);
        String[] array = val.split(":");
        String id = array[0];
        String lvl = array[1];
        int lvlInt = Integer.parseInt(lvl);
        return new Pair<>(id, lvlInt);
    }

    public void toggleActive() {
        active = !active;
    }
}
