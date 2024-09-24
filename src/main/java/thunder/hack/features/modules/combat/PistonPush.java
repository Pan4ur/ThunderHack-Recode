package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.world.HoleUtility;

import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PistonPush extends Module {
    private final Setting<Float> range = new Setting<>("Target Range", 5.f, 1.5f, 7.f);
    private final Setting<Integer> blocksPerTick = new Setting<>("Blocks/Tick", 2, 1, 2);
    private final Setting<Integer> delayPerPlace = new Setting<>("Delay/Place", 0, 0, 5);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.Normal);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Strict);

    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<ChargeType> chargeType = new Setting<>("Charge Type", ChargeType.All);
    private final Setting<PistonType> pistonType = new Setting<>("Piston Type", PistonType.All);
    private final Setting<Boolean> autoSwap = new Setting<>("Auto Swap", true);
    private final Setting<Boolean> swing = new Setting<>("Swing", true);

    private final Setting<SettingGroup> render = new Setting<>("Render", new SettingGroup(false, 0));
    private final Setting<ColorSetting> fillColor = new Setting<>("Fill Color", new ColorSetting(new Color(255, 0, 0, 50))).addToGroup(render);
    private final Setting<ColorSetting> lineColor = new Setting<>("Line Color", new ColorSetting(new Color(255, 0, 0, 200))).addToGroup(render);
    private final Setting<Integer> lineWidth = new Setting<>("Line Width", 2, 1, 5).addToGroup(render);

    private PlayerEntity target;
    private BlockPos pistonPos;
    private BlockPos chargePos;
    private boolean firstPlace;
    private int delay;

    private final ConcurrentHashMap<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

    public PistonPush() {
        super("PistonPush", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        target = null;
        pistonPos = null;
        chargePos = null;
        placeRunnable = null;
        delay = 0;
        firstPlace = true;
    }

    private Runnable placeRunnable = null;

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

        if (delay < delayPerPlace.getValue()) {
            delay++;
            return;
        }

        handlePistonPush(false);
    }

    @EventHandler
    public void onPostSync(EventPostSync event) {
        int blocksPlaced = 0;
        while (blocksPlaced < blocksPerTick.getValue()) {
            handlePistonPush(true);
            if (placeRunnable != null) {
                placeRunnable.run();
                placeRunnable = null;
            } else {
                return;
            }
            blocksPlaced++;
        }
        delay = 0;
    }

    public void handlePistonPush(boolean onSync) {
        if (firstPlace) placePiston(onSync);
        else placeCharge(onSync);
    }

    public void onRender3D(MatrixStack stack) {
        renderPoses.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 500) {
                renderPoses.remove(pos);
            } else {
                Render3DEngine.FILLED_QUEUE.add(
                        new Render3DEngine.FillAction(new Box(pos), Render2DEngine.injectAlpha(fillColor.getValue().getColorObject(), (int) (fillColor.getValue().getAlpha() * (1f - ((System.currentTimeMillis() - time) / 500f)))))
                );
                Render3DEngine.OUTLINE_QUEUE.add(
                        new Render3DEngine.OutlineAction(new Box(pos), lineColor.getValue().getColorObject(), lineWidth.getValue())
                );
            }
        });
    }

    private void placeCharge(boolean onSync) {
        if (!getChargeSlot().found() || (!autoSwap.getValue() && !getChargeSlot().isHolding()))
            return;

        if (chargePos == null)
            return;


        if (rotate.getValue()) {
            final float[] angle = InteractionUtility.getPlaceAngle(chargePos, interact.getValue(), false);
            if (angle == null) {
                return;
            }
            if (onSync) {
                sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle[0], angle[1], mc.player.isOnGround()));
            } else {
                mc.player.setYaw(angle[0]);
                mc.player.setPitch(angle[1]);
            }
        }

        placeRunnable = () -> {
            int prevSlot = mc.player.getInventory().selectedSlot;
            InteractionUtility.placeBlock(chargePos, InteractionUtility.Rotate.None, interact.getValue(), placeMode.getValue(), getChargeSlot(), true, false);
            sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            mc.player.getInventory().selectedSlot = prevSlot;
            firstPlace = true;
            if (swing.getValue())
                mc.player.swingHand(Hand.MAIN_HAND);
            renderPoses.put(chargePos, System.currentTimeMillis());
        };
    }

    private void placePiston(boolean extra) {
        if (pistonPos == null)
            return;

        if (!getPistonSlot().found() || (!autoSwap.getValue() && !getPistonSlot().isHolding()))
            return;

        if (rotate.getValue()) {
            final float[] angle = InteractionUtility.getPlaceAngle(pistonPos, interact.getValue(), false);
            if (angle == null)
                return;

            if (extra) sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle[0], angle[1], mc.player.isOnGround()));
            else {
                mc.player.setYaw(angle[0]);
                mc.player.setPitch(angle[1]);
            }
        }

        placeRunnable = () -> {
            final float angle = InteractionUtility.calculateAngle(target.getEyePos(), pistonPos.toCenterPos())[0];
            sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle, 0, mc.player.isOnGround()));
            float prevYaw = mc.player.getYaw();
            mc.player.setYaw(angle);
            mc.player.prevYaw = angle;
            ((IClientPlayerEntity) mc.player).setLastYaw(angle);
            int prevSlot = mc.player.getInventory().selectedSlot;
            InteractionUtility.placeBlock(pistonPos, InteractionUtility.Rotate.None, interact.getValue(), placeMode.getValue(), getPistonSlot(), true, false);
            sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            mc.player.getInventory().selectedSlot = prevSlot;
            mc.player.setYaw(prevYaw);
            firstPlace = false;
            if (swing.getValue())
                mc.player.swingHand(Hand.MAIN_HAND);
            renderPoses.put(pistonPos, System.currentTimeMillis());
        };
    }

    private SearchInvResult getPistonSlot() {
        final SearchInvResult stickyPistonSlot = InventoryUtility.findBlockInHotBar(Blocks.STICKY_PISTON);
        final SearchInvResult pistonSlot = InventoryUtility.findBlockInHotBar(Blocks.PISTON);
        SearchInvResult finalResult = null;

        switch (pistonType.getValue()) {
            case Normal -> finalResult = pistonSlot;
            case Sticky -> finalResult = stickyPistonSlot;
            case All -> finalResult = pistonSlot.found() ? pistonSlot : stickyPistonSlot;
        }

        return finalResult;
    }

    private SearchInvResult getChargeSlot() {
        final SearchInvResult redstoneTorchSlot = InventoryUtility.findBlockInHotBar(Blocks.REDSTONE_TORCH);
        final SearchInvResult redstoneBlockSlot = InventoryUtility.findBlockInHotBar(Blocks.REDSTONE_BLOCK);
        SearchInvResult finalResult = null;

        switch (chargeType.getValue()) {
            case Block -> finalResult = redstoneBlockSlot;
            case Torch -> finalResult = redstoneTorchSlot;
            case All -> finalResult = redstoneTorchSlot.found() ? redstoneTorchSlot : redstoneBlockSlot;
        }

        return finalResult;
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
            if (!InteractionUtility.canPlaceBlock(pos, interact.getValue(), false)) continue;

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
                    if (chargeType.getValue() == ChargeType.Torch && chPos.equals(pos.up())) {
                        continue;
                    }
                    if (InteractionUtility.canPlaceBlock(chPos, interact.getValue(), false)) {
                        chargePos = chPos;
                        break;
                    }
                }
            }

            pistonPos = pos;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isPlayerTargetCorrect(PlayerEntity player) {
        if (player == null) return false;

        return !Managers.FRIEND.isFriend(player)
                && player != mc.player
                && player.distanceTo(((mc.player))) <= range.getValue()
                && !player.isDead()
                && player.getHealth() + player.getAbsorptionAmount() > 0
                && HoleUtility.isHole(player.getBlockPos());
    }

    private void findTarget() {
        for (PlayerEntity player : Managers.ASYNC.getAsyncPlayers()) {
            if (!isPlayerTargetCorrect(player)) continue;

            target = player;
            break;
        }
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
