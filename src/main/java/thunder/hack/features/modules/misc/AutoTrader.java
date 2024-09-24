package thunder.hack.features.modules.misc;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;

import java.util.Comparator;
import java.util.HashMap;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class AutoTrader extends Module {
    public AutoTrader() {
        super("AutoTrader", Category.MISC);
    }

    private final Setting<BooleanSettingGroup> buy = new Setting<>("Buy", new BooleanSettingGroup(true));
    private final Setting<String> buyItem = new Setting<>("BuyItem", "apple").addToGroup(buy);
    private final Setting<BooleanSettingGroup> sell = new Setting<>("Sell", new BooleanSettingGroup(false));
    private final Setting<String> sellItem = new Setting<>("SellItem", "bread").addToGroup(sell);
    private final Setting<SettingGroup> disable = new Setting<>("Disable", new SettingGroup(false, 0));
    private final Setting<Boolean> noVillagers = new Setting<>("NoVillagers", true).addToGroup(disable);
    private final Setting<Boolean> noItems = new Setting<>("NoItems", false).addToGroup(disable);

    private int interactTicks, cooldown;
    private int lastVillager;
    private HashMap<Integer, Integer> villagers = new HashMap<>();

    @Override
    public void onUpdate() {
        if (fullNullCheck())
            return;

        if (interactTicks > 0)
            interactTicks--;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        HashMap<Integer, Integer> cacheVillagers = new HashMap<>(villagers);
        cacheVillagers.forEach((id, time) -> {
            if (mc.player.age - time > 160)
                villagers.remove(id);
        });

        if (mc.currentScreen instanceof MerchantScreen merch) {
            MerchantScreenHandler msh = merch.getScreenHandler();
            TradeOfferList offers = msh.getRecipes();

            for (int i = 0; i < offers.size(); i++) {
                TradeOffer offer = offers.get(i);
                if (goodDeal(offer)) {
                    msh.switchTo(i);
                    msh.setRecipeIndex(i);
                    sendPacket(new SelectMerchantTradeC2SPacket(i));
                    clickSlot(2, SlotActionType.QUICK_MOVE);
                    cooldown = 3;
                    return;
                } else if (!msh.getSlot(0).getStack().isEmpty()) {
                    clickSlot(0, SlotActionType.QUICK_MOVE);
                    cooldown = 3;
                    return;
                } else if (!msh.getSlot(1).getStack().isEmpty()) {
                    clickSlot(1, SlotActionType.QUICK_MOVE);
                    cooldown = 3;
                    return;
                } else if (offer.isDisabled()) {
                    villagers.put(lastVillager, mc.player.age);
                }
            }
            mc.player.closeHandledScreen();
        } else if (interactTicks <= 0 && !(mc.currentScreen instanceof ClickGUI)) {
            Entity ent = Lists.newArrayList(mc.world.getEntities()).stream()
                    .filter(e -> (e instanceof VillagerEntity))
                    .filter(e -> mc.player.squaredDistanceTo(e) < 4f * 4f)
                    .filter(e -> !villagers.containsKey(e.getId()))
                    .min(Comparator.comparing(e -> mc.player.distanceTo(e))).orElse(null);

            if (ent != null) {
                float[] angles = InteractionUtility.calculateAngle(ent.getEyePos().add(Math.random() * 0.2, 0, Math.random() * 0.2));
                mc.player.setYaw(angles[0]);
                mc.player.setPitch(angles[1]);
                mc.interactionManager.interactEntity(mc.player, ent, Hand.MAIN_HAND);
                lastVillager = ent.getId();
                interactTicks = 12;
            } else if (noVillagers.getValue())
                disable(isRu() ? "Рядом нет жителей!" : "There are no villagers nearby!");
        }
    }

    private boolean goodDeal(TradeOffer offer) {
        boolean selectedBuyItem = (offer.getSellItem().getItem().getTranslationKey().equals("item.minecraft." + buyItem.getValue())
                || offer.getSellItem().getItem().getTranslationKey().equals("block.minecraft." + buyItem.getValue()));

        boolean selectedSellItem = (offer.getDisplayedFirstBuyItem().getItem().getTranslationKey().equals("item.minecraft." + sellItem.getValue())
                || offer.getDisplayedFirstBuyItem().getItem().getTranslationKey().equals("block.minecraft." + sellItem.getValue()));

        boolean haveItems = offer.getDisplayedFirstBuyItem().getCount() <= InventoryUtility.getItemCount(offer.getDisplayedFirstBuyItem().getItem());

        boolean canBuy = selectedBuyItem && !offer.isDisabled() && buy.getValue().isEnabled();

        boolean canSell = selectedSellItem && !offer.isDisabled() && sell.getValue().isEnabled();

        if ((canBuy || canSell) && !haveItems) {
            if (noItems.getValue())
                disable(isRu() ? "Кончились предметы!" : "Out of items!");
            return false;
        }

        return canBuy || canSell;
    }
}
