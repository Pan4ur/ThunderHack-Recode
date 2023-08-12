package thunder.hack.modules.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ChatScreen;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.ExplosionUtility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoTotem extends Module {
    public Setting<OffHand> offhand = new Setting<>("Item", OffHand.Totem);
    public Setting<Float> healthF = new Setting<>("HP", 16f, 0f, 20f);
    public Setting<Float> healthS = new Setting<>("ShieldGappleHp", 16f, 0f, 20f, v -> offhand.getValue() == OffHand.Shield);
    public Setting<Boolean> matrix = new Setting<>("Matrix", true);
    public Setting<Boolean> safe = new Setting<>("Safe", true);
    public Setting<Boolean> rcGap = new Setting<>("RCGap", false);
    public Setting<Boolean> crapple = new Setting<>("CrappleSpoof", true, v -> offhand.getValue() == OffHand.GApple);

    private enum OffHand {Totem, Crystal, GApple, Shield}

    private final Timer timer = new Timer();

    public AutoTotem() {
        super("AutoTotem", "AutoTotem", Category.COMBAT);
    }

    @Subscribe
    public void onPostMotion(EventPostSync e) {
        if (mc.currentScreen == null || mc.currentScreen instanceof ChatScreen) {
            int itemSlot = getItemSlot();
            int nearest_slot = findNearestCurrentItem();
            if (itemSlot != -1 && timer.passedMs(235)) {
                int prevCurrentItem = mc.player.getInventory().selectedSlot;
                if(itemSlot >= 9) {
                    if(matrix.getValue()) {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, nearest_slot, SlotActionType.SWAP, mc.player);
                       // mc.player.networkHandler.sendPacket( new PickFromInventoryC2SPacket(itemSlot));

                        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(nearest_slot));
                        mc.player.getInventory().selectedSlot = nearest_slot;
                        ItemStack itemstack = mc.player.getOffHandStack();
                        mc.player.setStackInHand(Hand.OFF_HAND, mc.player.getMainHandStack());
                        mc.player.setStackInHand(Hand.MAIN_HAND, itemstack);
                        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prevCurrentItem));
                        mc.player.getInventory().selectedSlot = prevCurrentItem;
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, nearest_slot, SlotActionType.SWAP, mc.player);
                      //  mc.player.networkHandler.sendPacket( new PickFromInventoryC2SPacket(itemSlot));
                        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                        mc.player.resetLastAttackedTicks();
                    } else {
                        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                    }
                } else {
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(itemSlot));
                    mc.player.getInventory().selectedSlot = itemSlot;
                    ItemStack itemstack = mc.player.getOffHandStack();
                    mc.player.setStackInHand(Hand.OFF_HAND, mc.player.getMainHandStack());
                    mc.player.setStackInHand(Hand.MAIN_HAND, itemstack);
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prevCurrentItem));
                    mc.player.getInventory().selectedSlot = prevCurrentItem;
                    mc.player.resetLastAttackedTicks();
                }
                timer.reset();
            }
        }
    }


    @Subscribe
    public void onPacketSend(PacketEvent.Send e){
        if(e.getPacket() instanceof UpdateSelectedSlotC2SPacket){
            if(mc.player.isUsingItem() && (mc.player.getMainHandStack().getItem() == Items.GOLDEN_APPLE  || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE)) mc.player.stopUsingItem();
        }
    }

    public static int findNearestCurrentItem() {
        int currentItem = mc.player.getInventory().selectedSlot;
        if (currentItem == 8) return 7;
        if (currentItem == 0) return 1;
        return currentItem - 1;
    }

    public int getItemSlot(){
        int itemSlot = -1;
        int gappleSlot = InventoryUtility.getItemSlot(Items.ENCHANTED_GOLDEN_APPLE);
        int crappleSlot = InventoryUtility.getItemSlot(Items.GOLDEN_APPLE);
        int shieldSlot = InventoryUtility.getItemSlot(Items.SHIELD);
        Item item = null;
        if (offhand.getValue() == OffHand.Totem) {
            if(!mc.player.getOffHandStack().getName().toString().toLowerCase().contains("руна") && !mc.player.getOffHandStack().getName().toString().toLowerCase().contains("шар")) {
                item = Items.TOTEM_OF_UNDYING;
            }
        } else if (offhand.getValue() == OffHand.Crystal) {
            item = Items.END_CRYSTAL;
        } else if (offhand.getValue() == OffHand.GApple) {
            if (crapple.getValue()) {
                if (mc.player.hasStatusEffect(StatusEffects.ABSORPTION)) {
                    if (crappleSlot != -1) {
                        item = Items.GOLDEN_APPLE;
                    } else if (gappleSlot != -1) {
                        item = Items.ENCHANTED_GOLDEN_APPLE;
                    }
                } else if (gappleSlot != -1) {
                    item = Items.ENCHANTED_GOLDEN_APPLE;
                }
            } else {
                if (gappleSlot != -1) {
                    item = Items.ENCHANTED_GOLDEN_APPLE;
                } else if (crappleSlot != -1) {
                    item = Items.GOLDEN_APPLE;
                }
            }
        } else {
            if(shieldSlot != -1){
                if(mc.player.getHealth() + mc.player.getAbsorptionAmount() <= healthS.getValue()){
                    if (gappleSlot != -1) {
                        item = Items.ENCHANTED_GOLDEN_APPLE;
                    } else if (crappleSlot != -1) {
                        item = Items.GOLDEN_APPLE;
                    }
                } else {
                    if (!mc.player.getItemCooldownManager().isCoolingDown(Items.SHIELD)) {
                        item = Items.SHIELD;
                    } else {
                        if (gappleSlot != -1) {
                            item = Items.ENCHANTED_GOLDEN_APPLE;
                        } else if (crappleSlot != -1) {
                            item = Items.GOLDEN_APPLE;
                        }
                    }
                }
            } else if(crappleSlot != -1){
                item = Items.GOLDEN_APPLE;
            }
        }

        if (rcGap.getValue() && (mc.player.getMainHandStack().getItem() instanceof SwordItem) && mc.options.useKey.isPressed()) {
            if (crappleSlot != -1) item = Items.GOLDEN_APPLE;
            if (gappleSlot != -1) item = Items.ENCHANTED_GOLDEN_APPLE;
        }

        if (safe.getValue()) {
            if ((mc.player.getHealth() + mc.player.getAbsorptionAmount()) - (((mc.player.fallDistance - 3) / 2F) + 3.5F) < 0.5)
                item = Items.TOTEM_OF_UNDYING;
            if (mc.player.isFallFlying())
                item = Items.TOTEM_OF_UNDYING;
            if(matrix.getValue() && Aura.target != null && Thunderhack.moduleManager.get(Aura.class).isEnabled() && Aura.target instanceof PlayerEntity && (((PlayerEntity) Aura.target).getMainHandStack().getItem() == Items.OBSIDIAN || ((PlayerEntity) Aura.target).getMainHandStack().getItem() == Items.END_CRYSTAL) )
                item = Items.TOTEM_OF_UNDYING;

            for (Entity entity : mc.world.getEntities()) {
                if (entity == null || !entity.isAlive()) continue;
                if (mc.player.squaredDistanceTo(entity) > 36) continue;
                if (!(entity instanceof EndCrystalEntity)) continue;
                if ((mc.player.getHealth() + mc.player.getAbsorptionAmount()) - ExplosionUtility.getSelfExplosionDamage(entity.getPos()) < 0.5) {
                    item = Items.TOTEM_OF_UNDYING;
                    break;
                }
            }
        }

        if ((mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= healthF.getValue() && InventoryUtility.getItemSlot(Items.TOTEM_OF_UNDYING) != -1)
            item = Items.TOTEM_OF_UNDYING;

        for (int i = 9; i < 45; i++) {
            if(mc.player.getOffHandStack().getItem() == item) return -1;
            if (mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem().equals(item)) {
                itemSlot = i >= 36 ? i - 36 : i;
                break;
            }
        }
        if(item == mc.player.getMainHandStack().getItem() && mc.options.useKey.isPressed()) return -1;
        return itemSlot;
    }
}
