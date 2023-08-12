package thunder.hack.modules.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import thunder.hack.Thunderhack;
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

import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;

public class PistonPush extends Module {
    private final Setting<Float> range = new Setting<>("Target Range", 5.f, 0.f, 7.f);
    private final Setting<Integer> actionShift = new Setting<>("Place Per Tick", 1, 1, 15);
    private final Setting<Integer> actionInterval = new Setting<>("Delay", 0, 0, 5);
    private final Setting<PlaceUtility.PlaceMode> placeMode = new Setting<>("Place Mode", PlaceUtility.PlaceMode.All);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<Boolean> strictDirection = new Setting<>("Strict Direction", false);
    private final Setting<LogicType> logicType = new Setting<>("Logic Type", LogicType.All);
    private final Setting<ChargeType> chargeType = new Setting<>("Charge Type", ChargeType.All);
    private final Setting<PistonType> pistonType = new Setting<>("Piston Type", PistonType.All);
    private final Setting<Boolean> autoSwap = new Setting<>("Auto Swap", true);
    private final Setting<Boolean> swing = new Setting<>("Swing", true);

    private final Setting<Parent> render = new Setting<>("Render", new Parent(false, 0));
    private final Setting<ColorSetting> fillColor = new Setting<>("Fill Color", new ColorSetting(new Color(255, 0, 0, 50))).withParent(render);
    private final Setting<ColorSetting> lineColor = new Setting<>("Line Color", new ColorSetting(new Color(255, 0, 0, 200))).withParent(render);
    private final Setting<Integer> lineWidth = new Setting<>("Line Width", 2, 1, 5).withParent(render);

    private PlayerEntity target;
    private BlockPos pistonPos;
    private BlockPos chargePos;
    private LogicType currentLogic;
    private int delay;

    private final ConcurrentHashMap<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

    public PistonPush() {
        super("PistonPush", "Выталкивает чела из холки с помощью поршней", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        target = null;
        pistonPos = null;
        chargePos = null;
        currentLogic = null;
        delay = 0;

        super.onEnable();
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) return;
        if (!isPlayerTargetCorrect(target)) {
            findTarget();
            return;
        }
        if (delay != actionInterval.getValue()) {
            delay++;
            return;
        }

        delay = 0;
        int actions = 0;

        if (pistonPos == null || chargePos == null) {
            findPlacePoses();
            return;
        }

        switch (currentLogic) {
            case ChargePlace -> {
                placeCharge();
                actions++;
                if (actions >= actionShift.getValue()) return;
                placePiston();
            }
            case PlaceCharge -> {
                placePiston();
                actions++;
                if (actions >= actionShift.getValue()) return;
                placeCharge();
            }
        }

        super.onUpdate();
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

    private void placeCharge() {
        if (getChargeSlot() == -1
                || (!autoSwap.getValue()
                && getChargeSlot() != mc.player.getInventory().selectedSlot)) return;

        PlaceUtility.place(chargePos, rotate.getValue(), strictDirection.getValue(), Hand.MAIN_HAND, getChargeSlot(), false, placeMode.getValue());
        if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
        renderPoses.put(chargePos, System.currentTimeMillis());
    }

    private void placePiston() {
        if (getPistonSlot() == -1
                || (!autoSwap.getValue()
                && getPistonSlot() != mc.player.getInventory().selectedSlot)) return;
        doPistonRotate();
        PlaceUtility.forcePlace(pistonPos, strictDirection.getValue(), Hand.MAIN_HAND, getPistonSlot(), true, placeMode.getValue());
        if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
        renderPoses.put(pistonPos, System.currentTimeMillis());
    }

    private void doPistonRotate() {
        Vec3d rotateVec = new Vec3d(0, 0, 0);
        // FIXME piston rotations

        // Do rotation
        final float[] angle = PlaceUtility.calculateAngle(rotateVec);
        sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle[0], angle[1], mc.player.isOnGround()));
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
        BlockPos[] surroundPoses = {
                target.getBlockPos().add(1, 1, 0),
                target.getBlockPos().add(-1, 1, 0),
                target.getBlockPos().add(0, 1, 1),
                target.getBlockPos().add(0, 1, -1)
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
                if (target.getBlockX() != chPos.getX() && target.getBlockZ() != chPos.getZ()) {
                    if (PlaceUtility.canPlaceBlock(chPos, strictDirection.getValue())
                            && (logicType.getValue() == LogicType.All
                            || logicType.getValue() == LogicType.ChargePlace)) {
                        chargePos = chPos;
                        pistonPos = pos;
                        currentLogic = LogicType.ChargePlace;
                        return;
                    } else if (mc.world.getBlockState(chPos).getBlock().equals(Blocks.AIR)
                            && (logicType.getValue() == LogicType.All
                            || logicType.getValue() == LogicType.PlaceCharge)) {
                        if (chargeType.getValue() == ChargeType.Torch) {
                            if (PlaceUtility.canPlaceBlock(pos, strictDirection.getValue())) {
                                chargePos = chPos;
                                pistonPos = pos;
                                currentLogic = LogicType.PlaceCharge;
                                return;
                            }
                        } else {
                            chargePos = chPos;
                            pistonPos = pos;
                            currentLogic = LogicType.PlaceCharge;
                            return;
                        }
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
                && (HoleESP.validBedrock(player.getBlockPos())
                || HoleESP.validIndestructible(player.getBlockPos())
                || HoleESP.validTwoBlockIndestructibleXZ(player.getBlockPos())
                || HoleESP.validTwoBlockBedrockXZ(player.getBlockPos())
                || HoleESP.validTwoBlockIndestructibleXZ1(player.getBlockPos())
                || HoleESP.validTwoBlockBedrockXZ1(player.getBlockPos())
                || HoleESP.validQuadIndestructible(player.getBlockPos())
                || HoleESP.validBedrock(player.getBlockPos()));
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
