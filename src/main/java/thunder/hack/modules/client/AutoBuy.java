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

    public final Setting<Float> shulkerMultiplier = new Setting<>("ShulkerMultiplier", 5f, 1f, 100f);
    public final Setting<Bind> openGui = new Setting<>("OpenGui", new Bind(-1, false, false));
    public final Setting<String> command = new Setting<>("Command", "ah");
    public final Setting<Integer> minDelay = new Setting<>("MinUpdateDelay", 400, 50, 1000);
    public final Setting<Integer> maxDelay = new Setting<>("MaxUpdateDelay", 550, 100, 3000);
    public final Setting<Integer> reAhCount = new Setting<>("ReAhCount", 12, 2, 30);
    public final Setting<Boolean> logFake = new Setting<>("LogFake", true);

    private final Timer updateTimer = new Timer();
    private final Timer reAhTimer = new Timer();
    private final Timer buyTimer = new Timer();
    private final Timer fakeItemTimeout = new Timer();

    private Pair<ItemStack, Integer> lastStack;

    public static final ArrayList<AutoBuyItem> items = new ArrayList<>();
    public static final ArrayList<ItemLog> log = new ArrayList<>();

    private int updateCount;
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
        }
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (!active)
            return;

        if (updateCount > reAhCount.getValue()) {
            updateCount = 0;
            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            mc.player.closeScreen();
            return;
        }

        if (fakeItemTimeout.passedMs(500) && lastStack != null) {
            if(logFake.getValue()) {
                log.add(new ItemLog(lastStack.getLeft(), "фейк, за: " + lastStack.getRight() + " " + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
                sendMessage("Fake");
            }
            lastStack = null;
        }

        if (mc.currentScreen == null && reAhTimer.every(ServerManager.getPing() + 100))
            mc.player.networkHandler.sendChatCommand(command.getValue());

        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest) {
            if (mc.currentScreen.getTitle().getString().contains("(")) {
                int slot = 0;
                for (ItemStack itemStack : chest.getStacks()) {
                    NbtList lore = getLoreTagList(itemStack);
                    if (lore != null) {
                        int price = getPrice(lore.toString());
                        if (isGoodDeal(itemStack, price, lore)) {
                            Buy(slot);
                            return;
                        }
                    }
                    slot++;
                }
                sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                mc.player.closeScreen();
            } else if (mc.currentScreen.getTitle().getString().contains("Аукционы") || mc.currentScreen.getTitle().getString().contains("Поиск")) {
                for (ItemStack itemStack : chest.getStacks()) {
                    NbtList lore = getLoreTagList(itemStack);
                    if (lore != null) {
                        int price = getPrice(lore.toString());
                        String seller = getSeller(lore.toString());
                        if (isGoodDeal(itemStack, price, lore) && buyTimer.every(1000)) {
                            sendMessage(itemStack.getName().getString() + " " + price + " " + seller);
                            lastStack = new Pair<>(itemStack, price);
                            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                            mc.player.closeScreen();
                            mc.player.networkHandler.sendChatCommand("ah " + seller);
                            updateCount = 0;
                            reAhTimer.setMs(-800);
                            fakeItemTimeout.reset();
                            return;
                        }
                    }
                }

                if (updateTimer.every((int) MathUtility.random(minDelay.getValue(), maxDelay.getValue()))) {
                    if (updateCount++ < reAhCount.getValue()) clickSlot(49);
                    else {
                        sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                        mc.player.closeScreen();
                    }
                }
            }
        }
    }

    public boolean isGoodDeal(ItemStack is, int price, NbtList lore) {
        if (is.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
            NbtCompound compoundTag = is.getSubNbt("BlockEntityTag");
            DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(27, ItemStack.EMPTY);
            if (compoundTag != null) {
                Inventories.readNbt(compoundTag, itemStacks);
                for (ItemStack s : itemStacks)
                    if (isGoodDeal(s, (int) (price * shulkerMultiplier.getValue()), lore))
                        return true;
            }
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
        sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        mc.player.closeScreen();
        updateTimer.setMs(-1000);
        reAhTimer.setMs(-1000);
        buyTimer.setMs(-1000);
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
