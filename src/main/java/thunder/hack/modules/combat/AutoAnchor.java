package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.EventPostTick;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.EventTick;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render3DEngine;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static thunder.hack.modules.client.MainSettings.isRu;

public class AutoAnchor extends Module {
    private final Setting<Float> targetRange = new Setting<>("Target Range", 10f, 1f, 20f);
    private final Setting<Boolean> onlyOwn = new Setting<>("Only Own", false);
    private final Setting<Integer> minDamage = new Setting<>("Min Target Damage", 5, 1, 36);
    private final Setting<Integer> maxDamage = new Setting<>("Max Self Damage", 8, 0, 36);
    private final Setting<Integer> maxFDamage = new Setting<>("Max Friend Damage", 12, 0, 36);
    private final Setting<Boolean> antiSelfPop = new Setting<>("Anti Self Pop", true);
    private final Setting<Boolean> antiFriendPop = new Setting<>("Anti Friend Pop", false);
    private final Setting<InventoryUtility.SwitchMode> switchMode = new Setting<>("Switch Mode", InventoryUtility.SwitchMode.All);
    private final Setting<Boolean> switchBack = new Setting<>("Switch Back", true);
    private final Setting<InteractionUtility.Interact> interactMode = new Setting<>("Interact Mode", InteractionUtility.Interact.Vanilla);
    private final Setting<Boolean> swing = new Setting<>("Swing", true);
    private final Setting<Integer> logicTimeout = new Setting<>("Logic Timeout", 30, 1, 2000);
    private final Setting<YawStepMode> yawStep = new Setting<>("YawStep", YawStepMode.Off);
    private final Setting<Integer> yawAngle = new Setting<>("YawAngle", 54, 5, 180, v -> yawStep.getValue() != YawStepMode.Off);
    public static Setting<Integer> predictTicks = new Setting<>("PredictTicks", 5, 0, 40);

    // Place
    private final Setting<Parent> place = new Setting<>("Place", new Parent(false, 0));
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.Normal).withParent(place);
    private final Setting<RotateMode> placeRotate = new Setting<>("Rotate On Place", RotateMode.Normal).withParent(place);
    private final Setting<Float> placeRange = new Setting<>("Place Range", 5f, 1f, 7f).withParent(place);
    private final Setting<Integer> placeTimeout = new Setting<>("Place Timeout", 25, 1, 1000).withParent(place);

    // Charge
    private final Setting<Parent> charge = new Setting<>("Charge", new Parent(false, 0));
    private final Setting<InteractMode> chargeMode = new Setting<>("Charge Mode", InteractMode.Normal).withParent(charge);
    private final Setting<RotateMode> chargeRotate = new Setting<>("Charge Rotate", RotateMode.Normal).withParent(charge);
    private final Setting<Integer> chargeCount = new Setting<>("Charge Count", 1, 1, 4).withParent(charge);
    private final Setting<Float> chargeRange = new Setting<>("Charge Range", 5f, 1f, 7f).withParent(charge);
    private final Setting<Integer> chargeTimeout = new Setting<>("Charge Timeout", 25, 1, 1000).withParent(charge);

    // Explode
    private final Setting<Parent> explode = new Setting<>("Explode", new Parent(false, 0));
    private final Setting<InteractMode> explodeMode = new Setting<>("Explode Mode", InteractMode.Normal).withParent(explode);
    private final Setting<RotateMode> explodeRotate = new Setting<>("Explode Rotate", RotateMode.Normal).withParent(charge);
    private final Setting<Float> explodeRange = new Setting<>("Explode Range", 5f, 1f, 7f).withParent(explode);
    private final Setting<Integer> explodeTimeout = new Setting<>("Explode Timeout", 25, 1, 1000).withParent(explode);

    // Pause
    private final Setting<Parent> pause = new Setting<>("Pause", new Parent(false, 0));
    private final Setting<Boolean> onEat = new Setting<>("Pause On Eat", false).withParent(pause);
    private final Setting<Boolean> onMine = new Setting<>("Pause On Mine", false).withParent(pause);
    private final Setting<Boolean> onAC = new Setting<>("Pause On Auto Crystal", false).withParent(pause);
    private final Setting<Boolean> onAura = new Setting<>("Pause On Aura", false).withParent(pause);

    // Auto Disable
    private final Setting<Parent> disable = new Setting<>("Disabling", new Parent(false, 0));
    private final Setting<Boolean> anchorDisable = new Setting<>("Disable No Anchors", false).withParent(disable);
    private final Setting<Boolean> glowStoneDisable = new Setting<>("Disable No GlowStone", true).withParent(disable);
    private final Setting<Boolean> dimensionDisable = new Setting<>("Disable Nether", true).withParent(disable);

    private final AtomicBoolean ticking = new AtomicBoolean(false);


    private enum InteractMode {
        Packet,
        Normal
    }

    private enum RotateMode {
        Packet,
        Normal,
        None
    }

    private enum YawStepMode {
        Off,
        On
    }

    private final List<BlockPos> ownAnchors = new ArrayList<>();
    private final Map<BlockPos, Integer> charges = new HashMap<>();
    private PlayerEntity target;
    private BlockPos targetPos;
    private Vec3d rotations;

    private Thread logicThread;
    private Thread placeThread;
    private Thread chargeThread;
    private Thread explodeThread;

    public AutoAnchor() {
        super("AutoAnchor", "Ебашит якоря как-героин", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.world == null) return;
        if (dimensionDisable.getValue() && mc.world.getDimension().respawnAnchorWorks()) {
            disable(isRu() ? "В данном измерении не работают якоря возрождения! Выключение..." : "There are respawn anchors don't work! Disabling...");
        }

        target = null;
        targetPos = null;
        charges.clear();

        // Threads
        logicThread = new LogicThread();
        placeThread = new PlaceThread();
        chargeThread = new ChargeThread();
        explodeThread = new ExplodeThread();

        logicThread.start();
        placeThread.start();
        chargeThread.start();
        explodeThread.start();

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (logicThread != null && !logicThread.isInterrupted()) {
            logicThread.interrupt();
            logicThread = null;
        }
        if (placeThread != null && !placeThread.isInterrupted()) {
            placeThread.interrupt();
            placeThread = null;
        }
        if (chargeThread != null && !chargeThread.isInterrupted()) {
            chargeThread.interrupt();
            chargeThread = null;
        }
        if (explodeThread != null && !explodeThread.isInterrupted()) {
            explodeThread.interrupt();
            explodeThread = null;
        }
    }

    @Override
    public void onRender3D(MatrixStack event) {
        if (targetPos != null) {
            Render3DEngine.drawBoxOutline(new Box(targetPos), HudEditor.getColor(0), 2);
        }

        super.onRender3D(event);
    }

    private @NotNull List<BlockPos> findAnchorBlocks() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos centerPos = mc.player.getBlockPos();

        int r = (int) Math.ceil(placeRange.getValue()) + 1;
        int h = placeRange.getValue().intValue();

        for (int i = centerPos.getX() - r; i < centerPos.getX() + r; i++) {
            for (int j = centerPos.getY() - h; j < centerPos.getY() + h; j++) {
                for (int k = centerPos.getZ() - r; k < centerPos.getZ() + r; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (mc.player.squaredDistanceTo(pos.toCenterPos()) <= placeRange.getPow2Value()
                            && (InteractionUtility.canPlaceBlock(pos, interactMode.getValue(), false))) {
                        positions.add(pos);
                    }
                }
            }
        }

        return positions;
    }

    private @NotNull List<BlockPos> findAnchors() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos centerPos = mc.player.getBlockPos();

        int r = (int) Math.ceil(placeRange.getValue()) + 1;
        int h = placeRange.getValue().intValue();

        for (int i = centerPos.getX() - r; i < centerPos.getX() + r; i++) {
            for (int j = centerPos.getY() - h; j < centerPos.getY() + h; j++) {
                for (int k = centerPos.getZ() - r; k < centerPos.getZ() + r; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (mc.player.squaredDistanceTo(pos.toCenterPos()) <= placeRange.getPow2Value()
                            && mc.world.getBlockState(pos).getBlock() instanceof RespawnAnchorBlock) {
                        positions.add(pos);
                    }
                }
            }
        }

        return positions;
    }

    private boolean shouldPause() {
        return (onAura.getValue() && ModuleManager.aura.isEnabled())
                || (onAC.getValue() && ModuleManager.autoCrystal.isEnabled())
                || (onMine.getValue() && PlayerUtility.isMining())
                || (onEat.getValue() && PlayerUtility.isEating());
    }

    private void interact(BlockHitResult result, @NotNull Setting<InteractMode> explodeMode, Hand hand) {
        if (explodeMode.getValue() == InteractMode.Packet) {
            sendPacket(new PlayerInteractBlockC2SPacket(hand, result, PlayerUtility.getWorldActionId(mc.world)));
        }
        if (explodeMode.getValue() == InteractMode.Normal) {
            mc.interactionManager.interactBlock(mc.player, hand, result);
        }

        if (swing.getValue()) mc.player.swingHand(hand);
        else sendPacket(new HandSwingC2SPacket(hand));
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (rotations == null) return;

        if (placeRotate.getValue() == RotateMode.Normal || explodeRotate.getValue() == RotateMode.Normal || chargeRotate.getValue() == RotateMode.Normal) {
            float[] yp = InteractionUtility.calculateAngle(rotations);
            if (yawStep.getValue() == YawStepMode.On) {
                float yawDiff = MathHelper.wrapDegrees(yp[0] - ((IClientPlayerEntity) ((mc.player))).getLastYaw());
                if (Math.abs(yawDiff) > yawAngle.getValue()) {
                    yp[0] = ((IClientPlayerEntity) ((mc.player))).getLastYaw() + (yawDiff * (yawAngle.getValue() / Math.abs(yawDiff)));
                }
            }
            double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
            yp[0] = (float) (yp[0] - (yp[0] - ((IClientPlayerEntity) ((mc.player))).getLastYaw()) % gcdFix);
            yp[1] = (float) (yp[1] - (yp[1] - ((IClientPlayerEntity) ((mc.player))).getLastYaw()) % gcdFix);
            mc.player.setYaw(yp[0]);
            mc.player.setPitch(yp[1]);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTick(EventTick e) {
        ticking.set(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPostTick(EventPostTick e) {
        ticking.set(false);
    }

    private final class LogicThread extends Thread {
        @Override
        public synchronized void run() {
            while (isEnabled()) {
                if (!ticking.get()) {
                    try {
                        sleep(logicTimeout.getValue());
                    } catch (InterruptedException ignored) {
                    }
                } else continue;

                if (fullNullCheck()) continue;

                if (target == null) {
                    target = ThunderHack.combatManager.getNearestTarget(targetRange.getValue());
                    continue;
                }
                if (target.getPos().squaredDistanceTo(mc.player.getEyePos()) > targetRange.getPow2Value()) {
                    target = null;
                    continue;
                }

                // Finding new best target pos
                BlockPos best1 = findAnchorBlocks().stream()
                        .filter(AutoAnchor.this::isFriendsSafe)
                        .max(Comparator.comparingDouble(bp -> ExplosionUtility.getAnchorExplosionDamage(bp, target)))
                        .orElse(null);

                if (targetPos != null && best1 != null) {
                    if ((ExplosionUtility.getAnchorExplosionDamage(targetPos, mc.player) >= ExplosionUtility.getAnchorExplosionDamage(best1, mc.player))) {
                        targetPos = best1;
                    }
                    if (mc.player.squaredDistanceTo(targetPos.toCenterPos()) >= placeRange.getPow2Value()) {
                        targetPos = null;
                        continue;
                    }
                    if (!InteractionUtility.canPlaceBlock(targetPos, interactMode.getValue(), false) && !mc.world.getBlockState(targetPos).getBlock().equals(Blocks.RESPAWN_ANCHOR)) {
                        targetPos = null;
                        continue;
                    }
                }

                if (targetPos == null) {
                    targetPos = best1;
                }
            }
        }
    }

    public synchronized boolean isFriendsSafe(BlockPos blockPos) {
        for (AbstractClientPlayerEntity player : ThunderHack.friendManager.getOnlineFriends()) {
            if (ExplosionUtility.getAnchorExplosionDamage(blockPos, player) > maxFDamage.getValue()) {
                return false;
            }
        }

        if (ExplosionUtility.getAnchorExplosionDamage(blockPos, mc.player) >= maxDamage.getValue())
            return false;

        return target == null || !(ExplosionUtility.getAnchorExplosionDamage(blockPos, target) < minDamage.getValue());
    }

    private final class PlaceThread extends Thread {
        @Override
        public void run() {
            while (isEnabled()) {
                try {
                    sleep(placeTimeout.getValue());
                } catch (InterruptedException ignored) {
                }

                while (ticking.get()) {
                }

                if (target == null || targetPos == null)
                    continue;

                BlockPos targetPosCopy = new BlockPos(targetPos);


                if (!isCorrectPos(targetPosCopy) || shouldPause())
                    continue;

                SearchInvResult anchor = InventoryUtility.getAnchor();
                if (!anchor.found() && anchorDisable.getValue()) {
                    disable(isRu() ? "В хотбаре не найдены якоря возрождения! Выключение..." : "No respawn anchors in hotbar! Disabling...");
                    return;
                }

                if (placeRotate.getValue() == RotateMode.Normal) {
                    BlockHitResult bhr = InteractionUtility.getPlaceResult(targetPosCopy, interactMode.getValue(), false);
                    if (bhr != null) {
                        rotations = bhr.getPos();
                    }
                }

                boolean result = InteractionUtility.placeBlock(targetPosCopy, placeRotate.getValue() == RotateMode.Packet, interactMode.getValue(), placeMode.getValue(), anchor, switchBack.getValue(), switchMode.getValue(), false);
                if (result)
                    ownAnchors.add(targetPosCopy);
            }
        }

        private synchronized boolean isCorrectPos(@NotNull BlockPos pos) {
            if (mc.player == null || mc.world == null) return false;
            return mc.player.squaredDistanceTo(pos.toCenterPos()) <= placeRange.getPow2Value() + 1f && mc.world.getBlockState(pos).isReplaceable();
        }
    }

    private final class ChargeThread extends Thread {
        @Override
        public void run() {
            while (isEnabled()) {
                try {
                    sleep(chargeTimeout.getValue());
                } catch (InterruptedException ignored) {
                }

                while (ticking.get()) {
                }

                BlockPos targetPosCopy = null;
                if (targetPos != null) {
                    targetPosCopy = new BlockPos(targetPos);
                } else if (target != null) {
                    targetPosCopy = findAnchors().stream().filter(AutoAnchor.this::isFriendsSafe).max(Comparator.comparingDouble(bp -> ExplosionUtility.getAnchorExplosionDamage(bp, target))).orElse(null);
                }

                if (target == null || targetPosCopy == null || fullNullCheck()) continue;
                if (!isCorrectPos(targetPosCopy) || shouldPause()) continue;

                SearchInvResult glowResult = InventoryUtility.getGlowStone();
                if (glowStoneDisable.getValue() && !glowResult.found()) {
                    disable(isRu() ? "В хотбаре не найден светящийся камень! Выключение..." : "No glowstone in hotbar! Disabling...");
                    return;
                }

                // Charging
                int preSlot = mc.player.getInventory().selectedSlot;
                glowResult.switchTo(switchMode.getValue());
                InteractionUtility.BreakData data = InteractionUtility.getBreakData(targetPosCopy, interactMode.getValue());

                if (data != null && data.vector() != null) {
                    if (chargeRotate.getValue() == RotateMode.Packet) {
                        final float[] angles = InteractionUtility.calculateAngle(targetPosCopy.toCenterPos());
                        sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angles[0], angles[1], mc.player.isOnGround()));
                    }

                    BlockHitResult result = new BlockHitResult(data.vector(), data.dir(), targetPosCopy, false);

                    if (chargeRotate.getValue() == RotateMode.Normal) {
                        rotations = result.getPos();
                    }

                    sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                    interact(result, chargeMode, Hand.MAIN_HAND);

                    charges.put(targetPosCopy,
                            charges.containsKey(targetPosCopy) ?
                                    charges.get(targetPosCopy) + 1 : 1);
                }

                if (switchBack.getValue())
                    InventoryUtility.switchTo(preSlot, switchMode.getValue());
            }
        }

        private synchronized boolean isCorrectPos(@NotNull BlockPos pos) {
            if (fullNullCheck()) return false;

            Map<BlockPos, Integer> syncCharges
                    = Collections.synchronizedMap(charges);

            int serverSideCharges = 0;

            boolean isAnchor = mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR;

            try {
                if (isAnchor) {
                    serverSideCharges = mc.world.getBlockState(pos).get(RespawnAnchorBlock.CHARGES);
                }
            } catch (Exception ignored) {
            }

            if (isAnchor && (serverSideCharges < chargeCount.getValue() || (syncCharges.containsKey(pos) && syncCharges.get(pos) < chargeCount.getValue()))) {
                if (onlyOwn.getValue() && !ownAnchors.contains(pos)) return false;
                return mc.player.squaredDistanceTo(pos.toCenterPos()) <= chargeRange.getPow2Value();
            }
            return false;
        }

    }

    private final class ExplodeThread extends Thread {
        @Override
        public void run() {
            while (isEnabled()) {
                try {
                    sleep(explodeTimeout.getValue());
                } catch (InterruptedException ignored) {
                }

                while (ticking.get()) {
                }

                if (target == null || targetPos == null || fullNullCheck()) continue;
                BlockPos targetPosCopy = new BlockPos(targetPos);
                if (!isCorrectPos(targetPosCopy) || shouldPause()) continue;
                if (antiSelfPop.getValue() && mc.player.getHealth() - ExplosionUtility.getAnchorExplosionDamage(targetPosCopy, mc.player) <= 0)
                    continue;
                if (antiFriendPop.getValue()) {
                    boolean shouldContinue = false;

                    for (AbstractClientPlayerEntity entity : ThunderHack.friendManager.getOnlineFriends()) {
                        if (entity.getHealth() - ExplosionUtility.getAnchorExplosionDamage(targetPosCopy, entity) <= 0) {
                            shouldContinue = true;
                            break;
                        }
                    }

                    if (shouldContinue) continue;
                }


                int preSlot = mc.player.getInventory().selectedSlot;
                if (mc.player.getMainHandStack().getItem().equals(Items.GLOWSTONE)) {
                    SearchInvResult result = InventoryUtility.findInHotBar(stack -> !stack.getItem().equals(Items.GLOWSTONE));
                    result.switchTo(switchMode.getValue());
                }

                InteractionUtility.BreakData data = InteractionUtility.getBreakData(targetPosCopy, interactMode.getValue());
                if (data != null && data.vector() != null) {
                    if (explodeRotate.getValue() == RotateMode.Packet) {
                        final float[] angles = InteractionUtility.calculateAngle(targetPosCopy.toCenterPos());
                        sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angles[0], angles[1], mc.player.isOnGround()));
                    }

                    SearchInvResult anchorResult = InventoryUtility.getAnchor();

                    if (anchorResult.found()) anchorResult.switchTo();

                    BlockHitResult result = new BlockHitResult(data.vector(), data.dir(), targetPosCopy, false);

                    if (explodeRotate.getValue() == RotateMode.Normal) {
                        rotations = result.getPos();
                    }

                    sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                    interact(result, explodeMode, Hand.OFF_HAND);
                }

                if (switchBack.getValue())
                    InventoryUtility.switchTo(preSlot, switchMode.getValue());
            }
        }

        private synchronized boolean isCorrectPos(@NotNull BlockPos pos) {
            if (fullNullCheck()) return false;

            Map<BlockPos, Integer> syncCharges
                    = Collections.synchronizedMap(charges);
            if (mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR
                    && (mc.world.getBlockState(pos).get(RespawnAnchorBlock.CHARGES) >= chargeCount.getValue()
                    || (syncCharges.containsKey(pos) && syncCharges.get(pos) >= chargeCount.getValue()))) {
                if (onlyOwn.getValue() && !ownAnchors.contains(pos)) return false;
                return mc.player.squaredDistanceTo(pos.toCenterPos()) <= explodeRange.getPow2Value();
            }

            return false;
        }
    }
}
