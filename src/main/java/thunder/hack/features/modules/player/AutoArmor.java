package thunder.hack.features.modules.player;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.hud.HudEditorGui;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.MovementUtility;

import java.util.Arrays;
import java.util.List;

public class AutoArmor extends Module {
    public AutoArmor() {
        super("AutoArmor", Category.PLAYER);
    }

    private final Setting<EnchantPriority> head = new Setting<>("Head", EnchantPriority.Protection);
    private final Setting<EnchantPriority> body = new Setting<>("Body", EnchantPriority.Protection);
    private final Setting<EnchantPriority> tights = new Setting<>("Tights", EnchantPriority.Protection);
    private final Setting<EnchantPriority> feet = new Setting<>("Feet", EnchantPriority.Protection);
    private final Setting<ElytraPriority> elytraPriority = new Setting<>("ElytraPriority", ElytraPriority.Ignore);
    private final Setting<Integer> delay = new Setting<>("Delay", 5, 0, 10);
    private final Setting<Boolean> oldVersion = new Setting<>("OldVersion", false);
    private final Setting<Boolean> pauseInventory = new Setting<>("PauseInventory", false);
    private final Setting<Boolean> noMove = new Setting<>("NoMove", false);
    private final Setting<Boolean> ignoreCurse = new Setting<>("IgnoreCurse", true);
    private final Setting<Boolean> strict = new Setting<>("Strict", false);

    private int tickDelay = 0;

    List<ArmorData> armorList = Arrays.asList(
            new ArmorData(EquipmentSlot.FEET, 36, -1, -1, -1),
            new ArmorData(EquipmentSlot.LEGS, 37, -1, -1, -1),
            new ArmorData(EquipmentSlot.CHEST, 38, -1, -1, -1),
            new ArmorData(EquipmentSlot.HEAD, 39, -1, -1, -1)
    );

    @Override
    public void onUpdate() {
        if (mc.currentScreen != null && pauseInventory.getValue() && !(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof ClickGUI) && !(mc.currentScreen instanceof HudEditorGui))
            return;

        if (tickDelay-- > 0)
            return;

        armorList.forEach(ArmorData::reset);

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            int prot = getProtection(stack);
            if (prot > 0)
                for (ArmorData e : armorList) {
                    if (e.getEquipmentSlot() == (stack.getItem() instanceof ArmorItem ai ? ai.getSlotType() : EquipmentSlot.CHEST))
                        if (prot > e.getPrevProt() && prot > e.getNewProtection()) {
                            e.setNewSlot(i);
                            e.setNewProtection(prot);
                        }
                }
        }

        for (ArmorData armorPiece : armorList) {
            int slot = armorPiece.getNewSlot();
            if (slot != -1) {
                if ((armorPiece.getPrevProt() == -1 || !oldVersion.getValue()) && slot < 9) {
                    InventoryUtility.saveAndSwitchTo(slot);
                    sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
                    InventoryUtility.returnSlot();
                } else {
                    if (MovementUtility.isMoving() && noMove.getValue())
                        return;

                    int newArmorSlot = slot < 9 ? 36 + slot : slot;

                    if(strict.getValue())
                        sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));

                    clickSlot(newArmorSlot);
                    clickSlot((armorPiece.getArmorSlot() - 34) + (39 - armorPiece.getArmorSlot()) * 2);
                    if (armorPiece.getPrevProt() != -1)
                        clickSlot(newArmorSlot);

                    sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                }

                tickDelay = delay.getValue();
                return;
            }
        }
    }

    private int getProtection(ItemStack is) {
        if (is.getItem() instanceof ArmorItem || is.getItem() instanceof ElytraItem) {
            int prot = 0;

            EquipmentSlot slot = is.getItem() instanceof ArmorItem ai ? ai.getSlotType() : EquipmentSlot.BODY;

            if (is.getItem() instanceof ElytraItem) {
                if (!ElytraItem.isUsable(is))
                    return 0;

                boolean ePlus = elytraPriority.is(ElytraPriority.ElytraPlus) && (ModuleManager.elytraRecast.isEnabled() || ModuleManager.elytraPlus.isEnabled());
                boolean ignore = elytraPriority.is(ElytraPriority.Ignore) && mc.player.getInventory().getStack(38).getItem() instanceof ElytraItem;

                if (ePlus || ignore || elytraPriority.is(ElytraPriority.Always))
                    prot = 999;
            }

            int blastMultiplier = 1;
            int protectionMultiplier = 1;

            switch (slot) {
                case HEAD -> {
                    if(head.is(EnchantPriority.Protection)) protectionMultiplier *= 2;
                    else blastMultiplier *= 2;
                }
                case BODY -> {
                    if(body.is(EnchantPriority.Protection)) protectionMultiplier *= 2;
                    else blastMultiplier *= 2;
                }
                case LEGS -> {
                    if(tights.is(EnchantPriority.Protection)) protectionMultiplier *= 2;
                    else blastMultiplier *= 2;
                }
                case FEET -> {
                    if(feet.is(EnchantPriority.Protection)) protectionMultiplier *= 2;
                    else blastMultiplier *= 2;
                }
            }

            if (is.hasEnchantments()) {
                ItemEnchantmentsComponent enchants = EnchantmentHelper.getEnchantments(is);

                //mc.world.getRegistryManager().get(Enchantments.BLAST_PROTECTION.getRegistryRef()).getEntry(Enchantments.BLAST_PROTECTION).get()
                if (enchants.getEnchantments().contains(mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.PROTECTION).get()))
                    prot += enchants.getLevel(mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.PROTECTION).get()) * protectionMultiplier;

                if (enchants.getEnchantments().contains(mc.world.getRegistryManager().get(Enchantments.BLAST_PROTECTION.getRegistryRef()).getEntry(Enchantments.BLAST_PROTECTION).get()))
                    prot += enchants.getLevel(mc.world.getRegistryManager().get(Enchantments.BLAST_PROTECTION.getRegistryRef()).getEntry(Enchantments.BLAST_PROTECTION).get()) * blastMultiplier;

                if (enchants.getEnchantments().contains(mc.world.getRegistryManager().get(Enchantments.BLAST_PROTECTION.getRegistryRef()).getEntry(Enchantments.BINDING_CURSE).get()) && ignoreCurse.getValue())
                    prot = -999;
            }

            return (is.getItem() instanceof ArmorItem armorItem ? (armorItem.getProtection() + (int) Math.ceil(armorItem.getToughness())) * 10 : 0) + prot;
        } else if (!is.isEmpty()) return 0;
        return -1;
    }

    public class ArmorData {
        private EquipmentSlot equipmentSlot;
        private int armorSlot, prevProtection, newSlot, newProtection;

        public ArmorData(EquipmentSlot equipmentSlot, int armorSlot, int prevProtection, int newSlot, int newProtection) {
            this.equipmentSlot = equipmentSlot;
            this.armorSlot = armorSlot;
            this.prevProtection = prevProtection;
            this.newSlot = newSlot;
            this.newProtection = newProtection;
        }

        public int getArmorSlot() {
            return armorSlot;
        }

        public int getPrevProt() {
            return prevProtection;
        }

        public void setPrevProt(int prevProtection) {
            this.prevProtection = prevProtection;
        }

        public int getNewSlot() {
            return newSlot;
        }

        public void setNewSlot(int newSlot) {
            this.newSlot = newSlot;
        }

        public int getNewProtection() {
            return newProtection;
        }

        public void setNewProtection(int newProtection) {
            this.newProtection = newProtection;
        }

        public EquipmentSlot getEquipmentSlot() {
            return equipmentSlot;
        }

        public void reset() {
            setPrevProt(getProtection(mc.player.getInventory().getStack(getArmorSlot())));
            setNewSlot(-1);
            setNewProtection(-1);
        }
    }

    private enum ElytraPriority {
        None, Always, ElytraPlus, Ignore
    }

    private enum EnchantPriority {
        Blast, Protection
    }
}