package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.Module;
import thunder.hack.modules.render.HoleESP;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.world.HoleUtility;

import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;

public class PistonPush extends Module {
    private final Setting<Float> range = new Setting<>("Target Range", 5.f, 1.5f, 7.f);
    private final Setting<Integer> actionShift = new Setting<>("Place Per Tick", 2, 1, 2);
    private final Setting<Integer> actionInterval = new Setting<>("Delay", 0, 0, 5);
    private final Setting<PlaceUtility.PlaceMode> placeMode = new Setting<>("Place Mode", PlaceUtility.PlaceMode.All);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<Boolean> strictDirection = new Setting<>("Strict Direction", false);

    private final Setting<ChargeType> chargeType = new Setting<>("Charge Type", ChargeType.All);
    private final Setting<PistonType> pistonType = new Setting<>("Piston Type", PistonType.All);
    private final Setting<Boolean> autoSwap = new Setting<>("Auto Swap", true);
    private final Setting<Boolean> swing = new Setting<>("Swing", true);

    private final Setting<Parent> render = new Setting<>("Render", new Parent(false, 0));
    private final Setting<ColorSetting> fillColor = new Setting<>("Fill Color", new ColorSetting(new Color(255, 0, 0, 50))).withParent(render);
    private final Setting<ColorSetting> lineColor = new Setting<>("Line Color", new ColorSetting(new Color(255, 0, 0, 200))).withParent(render);
    private final Setting<Integer> lineWidth = new Setting<>("Line Width", 2, 1, 5).withParent(render);

    private Runnable postAction = null;
    private PlayerEntity target;
    private BlockPos pistonPos;
    private BlockPos chargePos;
    private boolean firstPlace;
    private int delay;

    private final ConcurrentHashMap<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

    public PistonPush() {
        super("PistonPush", "Выталкивает чела-из холки помощью-поршней", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        target = null;
        pistonPos = null;
        chargePos = null;
        postAction = null;
        delay = 0;
        firstPlace = true;
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (!isPlayerTargetCorrect(target)) {
            findTarget();
            return;
        }

        if (pistonPos == null || chargePos == null) {
            findPlacePoses();
            return;
        }

        if (delay < actionInterval.getValue()) {
            delay++;
            return;
        }

        handlePistonPush(false);
    }

    @EventHandler
    public void onPostSync(EventPostSync event) {
        if (postAction != null) {
            delay = 0;
            postAction.run();
            postAction = null;
            int extraBlocks = 1;
            while (extraBlocks < actionShift.getValue()) {
                handlePistonPush(true);
                if (postAction != null) {
                    postAction.run();
                    postAction = null;
                } else {
                    return;
                }
                extraBlocks++;
            }
        }
        postAction = null;
    }

    public void handlePistonPush(boolean extra) {
        if (firstPlace) {
            placePiston(extra);
        } else {
            placeCharge(extra);
        }
    }

    public void onRender3D(MatrixStack stack) {
        renderPoses.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 500) {
                renderPoses.remove(pos);
            } else {
                Render3DEngine.drawFilledBox(stack, new Box(pos), Render2DEngine.injectAlpha(fillColor.getValue().getColorObject(), (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)))));
                Render3DEngine.drawBoxOutline(new Box(pos), lineColor.getValue().getColorObject(), lineWidth.getValue());
            }
        });
    }

    private void placeCharge(boolean extra) {
        if (getChargeSlot() == -1
                || (!autoSwap.getValue()
                && getChargeSlot() != mc.player.getInventory().selectedSlot)) return;

        if (rotate.getValue()) {
            final float[] angle = PlaceUtility.calcAngle(chargePos, strictDirection.getValue(), true);
            if (angle == null) {
                return;
            }
            if (extra) {
                Thunderhack.placeManager.rotate(angle[0], angle[1]);
            } else {
                mc.player.setYaw(angle[0]);
                mc.player.setPitch(angle[1]);
            }
        }

        postAction = () -> {
            int prevSlot = mc.player.getInventory().selectedSlot;
            PlaceUtility.forcePlace(chargePos, strictDirection.getValue(), Hand.MAIN_HAND, getChargeSlot(), true, placeMode.getValue());
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            mc.player.getInventory().selectedSlot = prevSlot;
            firstPlace = true;
            if (swing.getValue())
                mc.player.swingHand(Hand.MAIN_HAND);
            renderPoses.put(chargePos, System.currentTimeMillis());
        };
    }

    private void placePiston(boolean extra) {
        if (getPistonSlot() == -1 || (!autoSwap.getValue() && getPistonSlot() != mc.player.getInventory().selectedSlot))
            return;

        if (rotate.getValue()) {
            final float[] angle = PlaceUtility.calcAngle(pistonPos, strictDirection.getValue(), true);
            if (angle == null) {
                return;
            }
            if (extra) {
                Thunderhack.placeManager.rotate(angle[0], angle[1]);
            } else {
                mc.player.setYaw(angle[0]);
                mc.player.setPitch(angle[1]);
            }
        }
        postAction = () -> {
            // без комментариев
            final float angle = PlaceUtility.calculateAngle(target.getEyePos(), pistonPos.toCenterPos())[0];
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle, 0, mc.player.isOnGround()));
            float prevYaw = mc.player.getYaw();
            mc.player.setYaw(angle);
            mc.player.prevYaw = angle;
            ((IClientPlayerEntity) mc.player).setLastYaw(angle);
            int prevSlot = mc.player.getInventory().selectedSlot;
            PlaceUtility.forcePlace(pistonPos, strictDirection.getValue(), Hand.MAIN_HAND, getPistonSlot(), true);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            mc.player.getInventory().selectedSlot = prevSlot;
            mc.player.setYaw(prevYaw);
            firstPlace = false;
            if (swing.getValue())
                mc.player.swingHand(Hand.MAIN_HAND);
            renderPoses.put(pistonPos, System.currentTimeMillis());
        };
    }

    private int getPistonSlot() {
        final SearchInvResult stickyPistonSlot = InventoryUtility.findBlockInHotBar(Blocks.STICKY_PISTON);
        final SearchInvResult pistonSlot = InventoryUtility.findBlockInHotBar(Blocks.PISTON);
        int finalSlot = -1;

        switch (pistonType.getValue()) {
            case Normal -> finalSlot = pistonSlot.slot();
            case Sticky -> finalSlot = stickyPistonSlot.slot();
            case All -> finalSlot = pistonSlot.found() ? pistonSlot.slot() : stickyPistonSlot.slot();
        }

        return finalSlot;
    }

    private int getChargeSlot() {
        final SearchInvResult redstoneTorchSlot = InventoryUtility.findBlockInHotBar(Blocks.REDSTONE_TORCH);
        final SearchInvResult redstoneBlockSlot = InventoryUtility.findBlockInHotBar(Blocks.REDSTONE_BLOCK);
        int finalSlot = -1;

        switch (chargeType.getValue()) {
            case Block -> finalSlot = redstoneBlockSlot.slot();
            case Torch -> finalSlot = redstoneTorchSlot.slot();
            case All -> finalSlot = redstoneTorchSlot.found() ? redstoneTorchSlot.slot() : redstoneBlockSlot.slot();
        }

        return finalSlot;
    }

    private void findPlacePoses() {
        BlockPos targetBP = BlockPos.ofFloored(target.getPos());

        BlockPos[] surroundPoses = {
                targetBP.add(1, 1, 0),
                targetBP.add(-1, 1, 0),
                targetBP.add(0, 1, 1),
                targetBP.add(0, 1, -1)
        };

        for (BlockPos pos : surroundPoses) {
            if (!PlaceUtility.canPlaceBlock(pos, strictDirection.getValue())) continue;

            BlockPos[] chargePoses = {
                    pos.add(0, 1, 0),
                    pos.add(0, -1, 0),
                    pos.add(1, 0, 0),
                    pos.add(-1, 0, 0),
                    pos.add(0, 0, 1),
                    pos.add(0, 0, -1)
            };

            for (BlockPos chPos : chargePoses) {
                if (chPos == targetBP) continue;
                if (mc.world.getBlockState(chPos).isReplaceable()) {
                    if (chargeType.getValue() == ChargeType.Torch) {

                        if(chPos == pos.up()) continue;

                        if (PlaceUtility.canPlaceBlock(pos, strictDirection.getValue())) {
                            chargePos = chPos;
                            pistonPos = pos;
                            return;
                        }
                    } else {
                        chargePos = chPos;
                        pistonPos = pos;
                        return;
                    }
                }
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isPlayerTargetCorrect(PlayerEntity player) {
        if (player == null) return false;

        return !Thunderhack.friendManager.isFriend(player)
                && player != mc.player
                && player.distanceTo(((mc.player))) <= range.getValue()
                && !player.isDead()
                && player.getHealth() + player.getAbsorptionAmount() > 0
                && HoleUtility.isHole(player.getBlockPos());
    }

    private void findTarget() {
        for (PlayerEntity player : Thunderhack.asyncManager.getAsyncPlayers()) {
            if (!isPlayerTargetCorrect(player)) continue;

            target = player;
            break;
        }
    }

    private enum LogicType {
        ChargePlace,
        PlaceCharge,
        All
    }

    private enum ChargeType {
        Block,
        Torch,
        All
    }

    private enum PistonType {
        Sticky,
        Normal,
        All
    }
}
