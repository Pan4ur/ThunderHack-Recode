package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IPlayerMoveC2SPacket;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", Category.MOVEMENT);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Rubberband);
    public final Setting<FallDistance> fallDistance = new Setting<>("FallDistance", FallDistance.Calc);
    public final Setting<Integer> fallDistanceValue = new Setting<>("FallDistanceVal", 10, 2, 100, v -> fallDistance.getValue() == FallDistance.Custom);

    private thunder.hack.utility.Timer pearlCooldown = new thunder.hack.utility.Timer();

    private enum Mode {
        Rubberband, Items, MatrixOffGround
    }

    private enum FallDistance {
        Calc, Custom
    }

    private boolean cancelGround = false;

    @EventHandler
    public void onSync(EventSync e) {
        if(fullNullCheck())
            return;
        if (isFalling()) {
            if (mode.getValue() == Mode.Rubberband) sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            if (mode.getValue() == Mode.Items) {
                mc.player.setPitch(90);
                BlockPos playerPos = BlockPos.ofFloored(mc.player.getPos());

                SearchInvResult snowResult = InventoryUtility.findItemInHotBar(Items.POWDER_SNOW_BUCKET);
                SearchInvResult waterResult = InventoryUtility.findItemInHotBar(Items.WATER_BUCKET);
                SearchInvResult pearlResult = InventoryUtility.findItemInHotBar(Items.ENDER_PEARL);
                SearchInvResult webResult = InventoryUtility.findItemInHotBar(Items.COBWEB);
                SearchInvResult vinesResult = InventoryUtility.findItemInHotBar(Items.TWISTING_VINES);

                if (waterResult.found()) {
                    doWaterDrop(waterResult, playerPos);
                } else if (pearlResult.found()) {
                    doPearlDrop(pearlResult);
                } else if (webResult.found()) {
                    doWebDrop(webResult, playerPos);
                } else if (vinesResult.found()) {
                    doVinesDrop(vinesResult, playerPos);
                } else if (snowResult.found()) {
                    doSnowDrop(snowResult, playerPos);
                }
            }
            if (mode.getValue() == Mode.MatrixOffGround) cancelGround = true;
        }
    }

    private void doWaterDrop(SearchInvResult waterResult, BlockPos playerPos) {
        if (mc.world.getBlockState(playerPos.down()).isSolid() || mc.world.getBlockState(playerPos.down().down()).isSolid()) {
            InventoryUtility.saveSlot();
            waterResult.switchTo();
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            InventoryUtility.returnSlot();
        }
    }

    private void doPearlDrop(SearchInvResult pearlResult) {
        if (pearlCooldown.passedMs(5000)) {
            InventoryUtility.saveSlot();
            pearlResult.switchTo();
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            InventoryUtility.returnSlot();
            pearlCooldown.reset();
        }
    }

    private void doWebDrop(SearchInvResult webResult, BlockPos playerPos) {
        if (mc.world.getBlockState(playerPos.down()).isSolid() || mc.world.getBlockState(playerPos.down().down()).isSolid()) {
            InventoryUtility.saveSlot();
            if (mc.world.getBlockState(playerPos.down()).isSolid())
                InteractionUtility.placeBlock(playerPos, false, InteractionUtility.Interact.Vanilla, InteractionUtility.PlaceMode.Normal, webResult.slot(), false, true);
            else
                InteractionUtility.placeBlock(playerPos.down(), false, InteractionUtility.Interact.Vanilla, InteractionUtility.PlaceMode.Normal, webResult.slot(), false, true);
            mc.player.swingHand(Hand.MAIN_HAND);
            InventoryUtility.returnSlot();
        }
    }

    private void doVinesDrop(SearchInvResult vinesResult, BlockPos playerPos) {
        if (mc.world.getBlockState(playerPos.down()).isSolid() || mc.world.getBlockState(playerPos.down().down()).isSolid()) {
            InventoryUtility.saveSlot();
            if (mc.world.getBlockState(playerPos.down()).isSolid())
                InteractionUtility.placeBlock(playerPos, false, InteractionUtility.Interact.Vanilla, InteractionUtility.PlaceMode.Normal, vinesResult.slot(), false, true);
            else
                InteractionUtility.placeBlock(playerPos.down(), false, InteractionUtility.Interact.Vanilla, InteractionUtility.PlaceMode.Normal, vinesResult.slot(), false, true);
            mc.player.swingHand(Hand.MAIN_HAND);
            InventoryUtility.returnSlot();
        }
    }

    private void doSnowDrop(SearchInvResult snowResult, BlockPos playerPos) {
        if (mc.world.getBlockState(playerPos.down()).isSolid() || mc.world.getBlockState(playerPos.down().down()).isSolid()) {
            InventoryUtility.saveSlot();
            if (mc.world.getBlockState(playerPos.down()).isSolid())
                InteractionUtility.placeBlock(playerPos, false, InteractionUtility.Interact.Vanilla, InteractionUtility.PlaceMode.Normal, snowResult.slot(), false, true);
            else
                InteractionUtility.placeBlock(playerPos.down(), false, InteractionUtility.Interact.Vanilla, InteractionUtility.PlaceMode.Normal, snowResult.slot(), false, true);
            mc.player.swingHand(Hand.MAIN_HAND);
            InventoryUtility.returnSlot();
        }
    }

    public boolean isFalling() {
        if(mc.player.isFallFlying())
            return false;

        switch (fallDistance.getValue()) {
            case Custom -> {
                return mc.player.fallDistance > fallDistanceValue.getValue();
            }
            case Calc -> {
                return (((mc.player.fallDistance - 3) / 2F) + 3.5F) > mc.player.getHealth() / 3f;
            }
        }
        return false;
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValueAsString() + " " + (isFalling() ? "Ready" : "");
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (e.getPacket() instanceof PlayerMoveC2SPacket pac) {
            if (cancelGround)
                ((IPlayerMoveC2SPacket) pac).setOnGround(false);
        }
    }

    @Override
    public void onEnable() {
        cancelGround = false;
    }
}
