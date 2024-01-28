package thunder.hack.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.model.ModelUtil;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.modules.Module;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class AutoArmor extends Module {
    public AutoArmor() {
        super("AutoArmor", Category.PLAYER);
    }

    private int tickDelay = 0;
    public final Setting<Boolean> noMove = new Setting<>("No Move", true);
    public final Setting<Integer> delay = new Setting<>("Delay", 5, 1, 10);


    @EventHandler
    public void onTick(PlayerUpdateEvent event) {
        if (mc.player.playerScreenHandler != mc.player.currentScreenHandler)
            return;

        if(MovementUtility.isMoving() && noMove.getValue()) return;
        if(ModuleManager.elytraPlus.isEnabled()) return;

        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        tickDelay = delay.getValue();

        Map<EquipmentSlot, int[]> armorMap = new HashMap<>(4);
        armorMap.put(EquipmentSlot.FEET, new int[] { 36, getProtection(mc.player.getInventory().getStack(36)), -1, -1 });
        armorMap.put(EquipmentSlot.LEGS, new int[] { 37, getProtection(mc.player.getInventory().getStack(37)), -1, -1 });
        armorMap.put(EquipmentSlot.CHEST, new int[] { 38, getProtection(mc.player.getInventory().getStack(38)), -1, -1 });
        armorMap.put(EquipmentSlot.HEAD, new int[] { 39, getProtection(mc.player.getInventory().getStack(39)), -1, -1 });

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            int prot = getProtection(stack);
            if (prot > 0) {
                for (Entry<EquipmentSlot, int[]> e: armorMap.entrySet()) {
                    if (e.getKey() == (stack.getItem() instanceof ElytraItem ? EquipmentSlot.CHEST : ((ArmorItem) stack.getItem()).getSlotType())) {
                        if (prot > e.getValue()[1] && prot > e.getValue()[3]) {
                            e.getValue()[2] = i;
                            e.getValue()[3] = prot;
                        }
                    }
                }
            }
        }

        for (Entry<EquipmentSlot, int[]> e: armorMap.entrySet()) {
            if (e.getValue()[2] != -1) {
                if (e.getValue()[1] == -1 && e.getValue()[2] < 9) {
                    if (e.getValue()[2] != mc.player.getInventory().selectedSlot) {
                        mc.player.getInventory().selectedSlot = e.getValue()[2];
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(e.getValue()[2]));
                    }
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 36 + e.getValue()[2], 1, SlotActionType.QUICK_MOVE, mc.player);
                    mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                } else if (mc.player.playerScreenHandler == mc.player.currentScreenHandler) {
                    int armorSlot = (e.getValue()[0] - 34) + (39 - e.getValue()[0]) * 2;
                    int newArmorslot = e.getValue()[2] < 9 ? 36 + e.getValue()[2] : e.getValue()[2];
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, newArmorslot, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, armorSlot, 0, SlotActionType.PICKUP, mc.player);
                    if (e.getValue()[1] != -1)
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, newArmorslot, 0, SlotActionType.PICKUP, mc.player);
                    mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                }
                return;
            }
        }
    }

    private int getProtection(ItemStack is) {
        if (is.getItem() instanceof ArmorItem || is.getItem() instanceof ElytraItem) {
            int prot = 0;

            if (is.getItem() instanceof ElytraItem) {
                if (!ElytraItem.isUsable(is))
                    return 0;
                prot = 1;
                if(ModuleManager.elytraRecast.isEnabled() || ModuleManager.elytraPlus.isEnabled()
                        || (ModuleManager.elytraSwap.isEnabled() && mc.player.getInventory().getStack(38).getItem() instanceof ElytraItem)){
                    prot = 999;
                }
            }
            if (is.hasEnchantments()) {
                for (Entry<Enchantment, Integer> e: EnchantmentHelper.get(is).entrySet()) {
                    if (e.getKey() instanceof ProtectionEnchantment)
                        prot += e.getValue();
                }
            }
            return (is.getItem() instanceof ArmorItem ? ((ArmorItem) is.getItem()).getProtection() : 0) + prot;
        } else if (!is.isEmpty()) {
            return 0;
        }

        return -1;
    }
}
