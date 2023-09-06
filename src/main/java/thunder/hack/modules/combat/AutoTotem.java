package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.player.InventoryUtility;

import java.util.HashMap;
import java.util.Map;

public class AutoTotem extends Module {
    public Setting<OffHand> offhand = new Setting<>("Item", OffHand.Totem);
    public Setting<Float> healthF = new Setting<>("HP", 16f, 0f, 20f);
    public Setting<Float> healthS = new Setting<>("ShieldGappleHp", 16f, 0f, 20f, v -> offhand.getValue() == OffHand.Shield);
    public Setting<Boolean> matrix = new Setting<>("Matrix", true);
    public Setting<Boolean> stopMotion = new Setting<>("stopMotion", false);
    public final Setting<Parent> safety = new Setting<>("Safety", new Parent(false, 0));
    public Setting<Boolean> griefInstant = new Setting<>("GriefInstant", false).withParent(safety);
    public Setting<Boolean> onElytra = new Setting<>("OnElytra", true).withParent(safety);
    public Setting<Boolean> onFall = new Setting<>("OnFall", true).withParent(safety);
    public Setting<Boolean> onCrystal = new Setting<>("OnCrystal", true).withParent(safety);
    public Setting<Boolean> onObsidianPlace = new Setting<>("OnObsidianPlace", false).withParent(safety);
    public Setting<Boolean> onCrystalInHand = new Setting<>("OnCrystalInHand", false).withParent(safety);
    public Setting<Boolean> onMinecartTnt = new Setting<>("OnMinecartTNT", true).withParent(safety);
    public Setting<Boolean> onTnt = new Setting<>("OnTNT", true).withParent(safety);
    public Setting<Boolean> rcGap = new Setting<>("RCGap", false);
    public Setting<Boolean> crapple = new Setting<>("CrappleSpoof", true, v -> offhand.getValue() == OffHand.GApple);

    private enum OffHand {Totem, Crystal, GApple, Shield}

    private int delay;
    private boolean freeze;

    public AutoTotem() {
        super("AutoTotem", "AutoTotem", Category.COMBAT);
    }

    @Override
    public void onEnable(){
        freeze = false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(EventMove move){
        if(stopMotion.getValue() && freeze){
            move.cancel();
            move.set_x(0);
            move.set_z(0);
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (mc.currentScreen == null || mc.currentScreen instanceof ChatScreen) {
            swapTo(getItemSlot());
        }
        delay--;
    }

    //TODO Вспомнить зачем это было нужно
    /*
    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (e.getPacket() instanceof UpdateSelectedSlotC2SPacket) {
            if (mc.player.isUsingItem() && (mc.player.getMainHandStack().getItem() == Items.GOLDEN_APPLE || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE))
                mc.player.stopUsingItem();
        }
    }
     */

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e){
        if(e.getPacket() instanceof EntitySpawnS2CPacket spawn){
            if (spawn.getEntityType() == EntityType.END_CRYSTAL) {
                if(mc.player.squaredDistanceTo(spawn.getX(),spawn.getY(),spawn.getZ()) < 36){
                    for (int i = 9; i < 45; i++) {
                        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;
                        if (mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem().equals(Items.TOTEM_OF_UNDYING)) {
                            sendMessage("detected EntitySpawnS2CPacket");
                            if(!griefInstant.getValue()) {
                                swapTo(i >= 36 ? i - 36 : i);
                            } else {
                                mc.player.networkHandler.sendPacket( new PickFromInventoryC2SPacket(i >= 36 ? i - 36 : i));
                                mc.player.networkHandler.sendPacket( new PickFromInventoryC2SPacket(i >= 36 ? i - 36 : i));
                            }
                            delay = 20;
                            break;
                        }
                    }
                }
            }
        }
        if(e.getPacket() instanceof BlockUpdateS2CPacket blockUpdate){
            if(blockUpdate.getState().getBlock() == Blocks.OBSIDIAN && onObsidianPlace.getValue()){
                if(mc.player.squaredDistanceTo(blockUpdate.getPos().toCenterPos()) < 36){
                    for (int i = 9; i < 45; i++) {
                        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;
                        if (mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem().equals(Items.TOTEM_OF_UNDYING)) {
                            sendMessage("detected BlockUpdateS2CPacket");
                            if(!griefInstant.getValue()) {
                                swapTo(i >= 36 ? i - 36 : i);
                            } else {
                                mc.player.networkHandler.sendPacket( new PickFromInventoryC2SPacket(i >= 36 ? i - 36 : i));
                                mc.player.networkHandler.sendPacket( new PickFromInventoryC2SPacket(i >= 36 ? i - 36 : i));
                            }
                            delay = 20;
                            break;
                        }
                    }
                }
            }
        }
    }

    public void swapTo(int slot){
        if (slot != -1 && delay <= 0) {
            freeze = true;
            int nearest_slot = findNearestCurrentItem();
            int prevCurrentItem = mc.player.getInventory().selectedSlot;
            if (slot >= 9) {
                if (matrix.getValue()) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, nearest_slot, SlotActionType.SWAP, mc.player);
                    sendPacket(new UpdateSelectedSlotC2SPacket(nearest_slot));
                    mc.player.getInventory().selectedSlot = nearest_slot;
                    ItemStack itemstack = mc.player.getOffHandStack();
                    mc.player.setStackInHand(Hand.OFF_HAND, mc.player.getMainHandStack());
                    mc.player.setStackInHand(Hand.MAIN_HAND, itemstack);
                    sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                    sendPacket(new UpdateSelectedSlotC2SPacket(prevCurrentItem));
                    mc.player.getInventory().selectedSlot = prevCurrentItem;
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, nearest_slot, SlotActionType.SWAP, mc.player);
                    mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                    mc.player.resetLastAttackedTicks();
                } else {
                    clickSlot(slot);
                    clickSlot(45);
                    clickSlot(slot);
                    sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                }
            } else {
                sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                mc.player.getInventory().selectedSlot = slot;
                ItemStack itemstack = mc.player.getOffHandStack();
                mc.player.setStackInHand(Hand.OFF_HAND, mc.player.getMainHandStack());
                mc.player.setStackInHand(Hand.MAIN_HAND, itemstack);
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                sendPacket(new UpdateSelectedSlotC2SPacket(prevCurrentItem));
                mc.player.getInventory().selectedSlot = prevCurrentItem;
                mc.player.resetLastAttackedTicks();
            }
            delay = 5;
            freeze = false;
        }
    }

    public static int findNearestCurrentItem() {
        int currentItem = mc.player.getInventory().selectedSlot;
        if (currentItem == 8) return 7;
        if (currentItem == 0) return 1;
        return currentItem - 1;
    }

    public int getItemSlot() {
        int itemSlot = -1;
        int gappleSlot = InventoryUtility.getItemSlot(Items.ENCHANTED_GOLDEN_APPLE);
        int crappleSlot = InventoryUtility.getItemSlot(Items.GOLDEN_APPLE);
        int shieldSlot = InventoryUtility.getItemSlot(Items.SHIELD);
        Item item = null;
        if (offhand.getValue() == OffHand.Totem) {
            if (!mc.player.getOffHandStack().getName().toString().toLowerCase().contains("руна") && !mc.player.getOffHandStack().getName().toString().toLowerCase().contains("шар")) {
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
            if (shieldSlot != -1) {
                if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= healthS.getValue()) {
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
            } else if (crappleSlot != -1) {
                item = Items.GOLDEN_APPLE;
            }
        }

        if (rcGap.getValue() && (mc.player.getMainHandStack().getItem() instanceof SwordItem) && mc.options.useKey.isPressed()) {
            if (crappleSlot != -1) item = Items.GOLDEN_APPLE;
            if (gappleSlot != -1) item = Items.ENCHANTED_GOLDEN_APPLE;
        }


        if (onFall.getValue() && (mc.player.getHealth() + mc.player.getAbsorptionAmount()) - (((mc.player.fallDistance - 3) / 2F) + 3.5F) < 0.5)
            item = Items.TOTEM_OF_UNDYING;

        if (onElytra.getValue() && mc.player.isFallFlying())
            item = Items.TOTEM_OF_UNDYING;

        if (onCrystalInHand.getValue()) {
            for (PlayerEntity pl : Thunderhack.asyncManager.getAsyncPlayers()) {
                if (Thunderhack.friendManager.isFriend(pl)) continue;
                if(pl == mc.player) continue;
                if (mc.player.squaredDistanceTo(pl) < 36) {
                    if (pl.getMainHandStack().getItem() == Items.OBSIDIAN
                            || pl.getMainHandStack().getItem() == Items.END_CRYSTAL
                            || pl.getOffHandStack().getItem() == Items.OBSIDIAN
                            || pl.getOffHandStack().getItem() == Items.END_CRYSTAL)
                        item = Items.TOTEM_OF_UNDYING;
                }
            }
        }

        for (Entity entity : mc.world.getEntities()) {
            if (entity == null || !entity.isAlive()) continue;
            if (mc.player.squaredDistanceTo(entity) > 36) continue;

            if (onCrystal.getValue()) {
                if (!(entity instanceof EndCrystalEntity)) continue;
                if ((mc.player.getHealth() + mc.player.getAbsorptionAmount()) - ExplosionUtility.getSelfExplosionDamage(entity.getPos()) < 0.5) {
                    item = Items.TOTEM_OF_UNDYING;
                    break;
                }
            }

            if (onTnt.getValue()) {
                if (!(entity instanceof TntEntity)) continue;
                if ((mc.player.getHealth() + mc.player.getAbsorptionAmount()) - ExplosionUtility.getSelfExplosionDamage(entity.getPos()) < 0.5) {
                    item = Items.TOTEM_OF_UNDYING;
                    break;
                }
            }

            if (onMinecartTnt.getValue()) {
                if (!(entity instanceof TntMinecartEntity)) continue;
                if ((mc.player.getHealth() + mc.player.getAbsorptionAmount()) - ExplosionUtility.getSelfExplosionDamage(entity.getPos()) < 0.5) {
                    item = Items.TOTEM_OF_UNDYING;
                    break;
                }
            }
        }

        if ((mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= healthF.getValue() && InventoryUtility.getItemSlot(Items.TOTEM_OF_UNDYING) != -1)
            item = Items.TOTEM_OF_UNDYING;

        for (int i = 9; i < 45; i++) {
            if (mc.player.getOffHandStack().getItem() == item) return -1;
            if (mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem().equals(item)) {
                itemSlot = i >= 36 ? i - 36 : i;
                break;
            }
        }
        if (item == mc.player.getMainHandStack().getItem() && mc.options.useKey.isPressed()) return -1;
        return itemSlot;
    }
}
