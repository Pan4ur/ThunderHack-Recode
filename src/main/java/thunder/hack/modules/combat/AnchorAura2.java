package thunder.hack.modules.combat;

import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import thunder.hack.Thunderhack;
import thunder.hack.core.ModuleManager;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;

import java.util.ArrayList;
import java.util.List;

import static thunder.hack.modules.client.MainSettings.isRu;

public class AnchorAura2 extends Module {
    private final Setting<Float> targetRange = new Setting<>("Target Range", 10f, 1f, 20f);
    private final Setting<Boolean> onlyOwn = new Setting<>("Only Own", false);
    private final Setting<Integer> minDamage = new Setting<>("Min Target Damage", 5, 1, 36);
    private final Setting<Integer> maxDamage = new Setting<>("Max Self Damage", 8, 0, 36);
    private final Setting<Boolean> antiSelfPop = new Setting<>("Anti Self Pop", true);
    private final Setting<Boolean> antiFriendPop = new Setting<>("Anti Friend Pop", false);
    private final Setting<InventoryUtility.SwitchMode> switchMode = new Setting<>("Switch Mode", InventoryUtility.SwitchMode.All);
    private final Setting<Boolean> switchBack = new Setting<>("Switch Back", true);
    private final Setting<InteractionUtility.Interact> interactMode = new Setting<>("Interact Mode", InteractionUtility.Interact.Vanilla);
    private final Setting<Boolean> swing = new Setting<>("Swing", true);
    private final Setting<Integer> logicTimeout = new Setting<>("Logic Timeout", 30, 0, 2000);

    // Place
    private final Setting<Parent> place = new Setting<>("Place", new Parent(false, 0));
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.All).withParent(place);
    private final Setting<Boolean> placeRotate = new Setting<>("Rotate On Place", false).withParent(place);
    private final Setting<Float> placeRange = new Setting<>("Place Range", 5f, 1f, 7f).withParent(place);
    private final Setting<Integer> placeTimeout = new Setting<>("Place Timeout", 25, 0, 1000).withParent(place);

    // Charge
    private final Setting<Parent> charge = new Setting<>("Charge", new Parent(false, 0));
    private final Setting<InteractMode> chargeMode = new Setting<>("Charge Mode", InteractMode.Both).withParent(charge);
    private final Setting<Boolean> chargeRotate = new Setting<>("Charge Rotate", false).withParent(charge);
    private final Setting<Integer> chargeCount = new Setting<>("Charge Count", 1, 1, 4).withParent(charge);
    private final Setting<Float> chargeRange = new Setting<>("Charge Range", 5f, 1f, 7f).withParent(charge);
    private final Setting<Integer> chargeTimeout = new Setting<>("Charge Timeout", 25, 0, 1000).withParent(charge);

    // Explode
    private final Setting<Parent> explode = new Setting<>("Explode", new Parent(false, 0));
    private final Setting<InteractMode> explodeMode = new Setting<>("Explode Mode", InteractMode.Both).withParent(explode);
    private final Setting<Boolean> explodeRotate = new Setting<>("Explode Rotate", false).withParent(charge);
    private final Setting<Float> explodeRange = new Setting<>("Explode Range", 5f, 1f, 7f).withParent(explode);
    private final Setting<Integer> explodeTimeout = new Setting<>("Explode Timeout", 25, 0, 1000).withParent(explode);

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
    private final Setting<Boolean> dimensionDisable = new Setting<>("Disable Wrong Dimension", true).withParent(disable);

    private enum InteractMode {
        Packet,
        Vanilla,
        Both
    }

    private final Timer logicTimer = new Timer();
    private final List<BlockPos> ownAnchors = new ArrayList<>();
    private PlayerEntity target;
    private BlockPos targetPos;

    // Threads
    private final PlaceThread placeThread = new PlaceThread();
    private final ChargeThread chargeThread = new ChargeThread();
    private final ExplodeThread explodeThread = new ExplodeThread();

    public AnchorAura2() {
        super("AnchorAura228", "Ебашит якоря как героин", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.world == null) return;
        if (dimensionDisable.getValue() && !mc.world.getDimension().respawnAnchorWorks()) {
            disable(isRu() ? "В данном измерении не работают якоря возрожденя! Выключение..." : "There are respawn anchors don't work! Disabling...");
        }

        placeThread.start();
        chargeThread.start();
        explodeThread.start();

        super.onEnable();
    }

    @Override
    public void onDisable() {
        placeThread.interrupt();
        chargeThread.interrupt();
        explodeThread.interrupt();

        super.onDisable();
    }

    @Override
    public synchronized void onThread() {
        if (fullNullCheck()) return;
        if (logicTimer.passedMs(logicTimeout.getValue())) logicTimer.reset();
        else return;

        if (target.getPos().squaredDistanceTo(mc.player.getEyePos()) > targetRange.getValue() * targetRange.getValue()) {
            target = null;
        }
        if (target == null) {
            target = Thunderhack.combatManager.getNearestTarget(targetRange.getValue());
        }

        // Finding new best target pos
        BlockPos best = null;
        for (BlockPos blockPos : findAnchorBlocks()) {
            if (ExplosionUtility.getAnchorExplosionDamage(blockPos, mc.player) <= maxDamage.getValue()
                    && ExplosionUtility.getAnchorExplosionDamage(blockPos, target) >= minDamage.getValue()) {
                if (best == null) {
                    best = blockPos;
                } else if (ExplosionUtility.getAnchorExplosionDamage(best, mc.player) >= ExplosionUtility.getAnchorExplosionDamage(blockPos, mc.player)
                        && ExplosionUtility.getAnchorExplosionDamage(best, target) <= ExplosionUtility.getAnchorExplosionDamage(blockPos, target)) {
                    best = blockPos;
                }
            }
        }

        if (mc.player.squaredDistanceTo(targetPos.toCenterPos()) > placeRange.getPow2Value()) {
            if (ExplosionUtility.getAnchorExplosionDamage(targetPos, mc.player) >= ExplosionUtility.getAnchorExplosionDamage(best, mc.player)
                    && ExplosionUtility.getAnchorExplosionDamage(targetPos, target) <= ExplosionUtility.getAnchorExplosionDamage(best, target)) {
                targetPos = best;
            }
        }

        super.onThread();
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
                    if (mc.player.squaredDistanceTo(pos.toCenterPos()) <= placeRange.getPow2Value() + 2 && InteractionUtility.canPlaceBlock(pos, interactMode.getValue(), false)) {
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

    private final class PlaceThread extends Thread {
        @Override
        public void run() {
            try {
                sleep(placeTimeout.getValue());
            } catch (InterruptedException ignored) {
            }

            if (target == null || targetPos == null) return;
            if (!isCorrectPos(targetPos) || shouldPause()) return;

            SearchInvResult anchor = InventoryUtility.getAnchor();
            if (!anchor.found() && anchorDisable.getValue()) {
                disable(isRu() ? "В хотбаре не найдены якоря возрождения! Выключение..." : "No respawn anchors in hotbar! Disabling...");
            }

            boolean result = InteractionUtility.placeBlock(targetPos, placeRotate.getValue(), interactMode.getValue(), placeMode.getValue(), anchor, switchBack.getValue(), switchMode.getValue(), false);
            if (result)
                ownAnchors.add(targetPos);

            super.run();
        }

        private synchronized boolean isCorrectPos(@NotNull BlockPos pos) {
            if (mc.player == null || mc.world == null) return false;
            return mc.player.squaredDistanceTo(pos.toCenterPos()) <= placeRange.getPow2Value() && mc.world.getBlockState(pos).isAir();
        }
    }

    private final class ChargeThread extends Thread {
        @Override
        public void run() {
            try {
                sleep(chargeTimeout.getValue());
            } catch (InterruptedException ignored) {
            }

            if (target == null || targetPos == null || fullNullCheck()) return;
            if (!isCorrectPos(targetPos) || shouldPause()) return;

            SearchInvResult glowResult = InventoryUtility.getGlowStone();
            if (glowStoneDisable.getValue() && !glowResult.found()) {
                disable(isRu() ? "В хотбаре не найден светящийся камень! Выключение..." : "No glowstone in hotbar! Disabling...");
            }

            // Charging
            int preSlot = mc.player.getInventory().selectedSlot;
            glowResult.switchTo(switchMode.getValue());
            InteractionUtility.BreakData data = InteractionUtility.getBreakData(targetPos, interactMode.getValue());

            if (data != null && data.vector() != null) {
                if (chargeRotate.getValue()) {
                    final float[] angles = InteractionUtility.calculateAngle(targetPos.toCenterPos());
                    sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angles[0], angles[1], mc.player.isOnGround()));
                }

                BlockHitResult result = new BlockHitResult(data.vector(), data.dir(), targetPos, false);
                if (chargeMode.getValue() == InteractMode.Packet && chargeMode.getValue() == InteractMode.Both) {
                    sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                    sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, PlayerUtility.getWorldActionId(mc.world)));
                } else if (chargeMode.getValue() == InteractMode.Vanilla && chargeMode.getValue() == InteractMode.Both) {
                    mc.player.setSneaking(false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
                }
                if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
            }

            if (switchBack.getValue())
                InventoryUtility.switchTo(preSlot, switchMode.getValue());

            super.run();
        }

        private synchronized boolean isCorrectPos(@NotNull BlockPos pos) {
            if (mc.player == null || mc.world == null) return false;
            if (mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR
                    && mc.world.getBlockState(pos).get(RespawnAnchorBlock.CHARGES) < chargeCount.getValue()) {
                if (onlyOwn.getValue() && !ownAnchors.contains(pos)) return false;
                return mc.player.squaredDistanceTo(pos.toCenterPos()) <= chargeRange.getPow2Value();
            }
            return false;
        }
    }

    private final class ExplodeThread extends Thread {
        @Override
        public void run() {
            try {
                sleep(explodeTimeout.getValue());
            } catch (InterruptedException ignored) {
            }

            if (target == null || targetPos == null || fullNullCheck()) return;
            if (!isCorrectPos(targetPos) || shouldPause()) return;
            if (antiSelfPop.getValue() && mc.player.getHealth() - ExplosionUtility.getAnchorExplosionDamage(targetPos, mc.player) <= 0)
                return;

            int preSlot = mc.player.getInventory().selectedSlot;
            if (mc.player.getMainHandStack().getItem().equals(Items.GLOWSTONE)) {
                SearchInvResult result = InventoryUtility.findInHotBar(stack -> !stack.getItem().equals(Items.GLOWSTONE));
                result.switchTo(switchMode.getValue());
            }

            InteractionUtility.BreakData data = InteractionUtility.getBreakData(targetPos, interactMode.getValue());
            if (data != null && data.vector() != null) {
                if (explodeRotate.getValue()) {
                    final float[] angles = InteractionUtility.calculateAngle(targetPos.toCenterPos());
                    sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angles[0], angles[1], mc.player.isOnGround()));
                }

                BlockHitResult result = new BlockHitResult(data.vector(), data.dir(), targetPos, false);
                if (explodeMode.getValue() == InteractMode.Packet && explodeMode.getValue() == InteractMode.Both) {
                    sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, PlayerUtility.getWorldActionId(mc.world)));
                } else if (explodeMode.getValue() == InteractMode.Vanilla && explodeMode.getValue() == InteractMode.Both) {
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
                }
                if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
            }

            if (switchBack.getValue())
                InventoryUtility.switchTo(preSlot, switchMode.getValue());

            super.run();
        }

        private synchronized boolean isCorrectPos(@NotNull BlockPos pos) {
            if (mc.player == null || mc.world == null) return false;
            if (mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR
                    && mc.world.getBlockState(pos).get(RespawnAnchorBlock.CHARGES) >= chargeCount.getValue()) {
                if (onlyOwn.getValue() && !ownAnchors.contains(pos)) return false;
                return mc.player.squaredDistanceTo(pos.toCenterPos()) <= explodeRange.getPow2Value();
            }

            return false;
        }
    }
}
