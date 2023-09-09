package thunder.hack.modules.combat;

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
import org.jetbrains.annotations.NotNull;
import thunder.hack.Thunderhack;
import thunder.hack.core.ModuleManager;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static thunder.hack.modules.client.MainSettings.isRu;

public class AnchorAura2 extends Module {
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
    private final Setting<Integer> logicTimeout = new Setting<>("Logic Timeout", 30, 0, 2000);

    // Place
    private final Setting<Parent> place = new Setting<>("Place", new Parent(false, 0));
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.All).withParent(place);
    private final Setting<Boolean> placeRotate = new Setting<>("Rotate On Place", false).withParent(place);
    private final Setting<Float> placeRange = new Setting<>("Place Range", 5f, 1f, 7f).withParent(place);
    private final Setting<Integer> placeTimeout = new Setting<>("Place Timeout", 25, 0, 1000).withParent(place);

    // Charge
    private final Setting<Parent> charge = new Setting<>("Charge", new Parent(false, 0));
    private final Setting<InteractMode> chargeMode = new Setting<>("Charge Mode", InteractMode.All).withParent(charge);
    private final Setting<Boolean> chargeRotate = new Setting<>("Charge Rotate", false).withParent(charge);
    private final Setting<Integer> chargeCount = new Setting<>("Charge Count", 1, 1, 4).withParent(charge);
    private final Setting<Float> chargeRange = new Setting<>("Charge Range", 5f, 1f, 7f).withParent(charge);
    private final Setting<Integer> chargeTimeout = new Setting<>("Charge Timeout", 25, 0, 1000).withParent(charge);

    // Explode
    private final Setting<Parent> explode = new Setting<>("Explode", new Parent(false, 0));
    private final Setting<InteractMode> explodeMode = new Setting<>("Explode Mode", InteractMode.All).withParent(explode);
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
        Normal,
        All
    }

    private final List<BlockPos> ownAnchors = new ArrayList<>();
    private final Map<BlockPos, Integer> charges = new HashMap<>();
    private PlayerEntity target;
    private BlockPos targetPos;

    public AnchorAura2() {
        super("AnchorAura228", "Ебашит якоря как героин", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.world == null) return;
        if (dimensionDisable.getValue() && mc.world.getDimension().respawnAnchorWorks()) {
            disable(isRu() ? "В данном измерении не работают якоря возрожденя! Выключение..." : "There are respawn anchors don't work! Disabling...");
        }

        target = null;
        targetPos = null;
        charges.clear();

        // Threads
        new LogicThread().start();
        new PlaceThread().start();
        new ChargeThread().start();
        new ExplodeThread().start();

        super.onEnable();
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

    private void interact(BlockHitResult result, @NotNull Setting<InteractMode> explodeMode) {
        if (explodeMode.getValue() == InteractMode.Packet || explodeMode.getValue() == InteractMode.All) {
            sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, PlayerUtility.getWorldActionId(mc.world)));
        }
        if (explodeMode.getValue() == InteractMode.Normal || explodeMode.getValue() == InteractMode.All) {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
        }

        if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
        else sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    private final class LogicThread extends Thread {
        @Override
        public synchronized void run() {
            while (isEnabled()) {
                try {
                    sleep(logicTimeout.getValue());
                } catch (InterruptedException ignored) {
                }

                if (fullNullCheck()) continue;

                if (target == null) {
                    target = Thunderhack.combatManager.getNearestTarget(targetRange.getValue());
                    continue;
                }
                if (target.getPos().squaredDistanceTo(mc.player.getEyePos()) > targetRange.getValue() * targetRange.getValue()) {
                    target = null;
                    continue;
                }

                // Finding new best target pos
                BlockPos best = null;
                for (BlockPos blockPos : findAnchorBlocks()) {
                    boolean friendDamageCorrect = true;
                    for (AbstractClientPlayerEntity player : Thunderhack.friendManager.getOnlineFriends()) {
                        if (ExplosionUtility.getAnchorExplosionDamage(blockPos, player) > maxFDamage.getValue()) {
                            friendDamageCorrect = false;
                            break;
                        }
                    }
                    if (ExplosionUtility.getAnchorExplosionDamage(blockPos, mc.player) <= maxDamage.getValue()
                            && ExplosionUtility.getAnchorExplosionDamage(blockPos, target) >= minDamage.getValue()
                            && friendDamageCorrect) {
                        if (best == null) {
                            best = blockPos;
                        } else if (ExplosionUtility.getAnchorExplosionDamage(best, mc.player) >= ExplosionUtility.getAnchorExplosionDamage(blockPos, mc.player)
                                && ExplosionUtility.getAnchorExplosionDamage(best, target) <= ExplosionUtility.getAnchorExplosionDamage(blockPos, target)) {
                            best = blockPos;
                        }
                    }
                }

                if (targetPos == null) {
                    targetPos = best;
                } else if (mc.player.squaredDistanceTo(targetPos.toCenterPos()) <= placeRange.getPow2Value()
                        || !InteractionUtility.canPlaceBlock(targetPos, interactMode.getValue(), false) && !mc.world.getBlockState(targetPos).getBlock().equals(Blocks.RESPAWN_ANCHOR)) {
                    if ((ExplosionUtility.getAnchorExplosionDamage(targetPos, mc.player) >= ExplosionUtility.getAnchorExplosionDamage(best, mc.player)
                            && ExplosionUtility.getAnchorExplosionDamage(targetPos, target) <= ExplosionUtility.getAnchorExplosionDamage(best, target))) {
                        targetPos = best;
                    }
                }
            }
        }
    }

    private final class PlaceThread extends Thread {
        @Override
        public void run() {
            while (isEnabled()) {
                try {
                    sleep(placeTimeout.getValue());
                } catch (InterruptedException ignored) {
                }

                if (target == null || targetPos == null) continue;
                if (!isCorrectPos(targetPos) || shouldPause()) continue;

                SearchInvResult anchor = InventoryUtility.getAnchor();
                if (!anchor.found() && anchorDisable.getValue()) {
                    disable(isRu() ? "В хотбаре не найдены якоря возрождения! Выключение..." : "No respawn anchors in hotbar! Disabling...");
                    return;
                }

                boolean result = InteractionUtility.placeBlock(targetPos, placeRotate.getValue(), interactMode.getValue(), placeMode.getValue(), anchor, switchBack.getValue(), switchMode.getValue(), false);
                if (result)
                    ownAnchors.add(targetPos);
            }
        }

        private synchronized boolean isCorrectPos(@NotNull BlockPos pos) {
            if (mc.player == null || mc.world == null) return false;
            return mc.player.squaredDistanceTo(pos.toCenterPos()) <= placeRange.getPow2Value() && mc.world.getBlockState(pos).isAir();
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

                if (target == null || targetPos == null || fullNullCheck()) continue;
                if (!isCorrectPos(targetPos) || shouldPause()) continue;

                SearchInvResult glowResult = InventoryUtility.getGlowStone();
                if (glowStoneDisable.getValue() && !glowResult.found()) {
                    disable(isRu() ? "В хотбаре не найден светящийся камень! Выключение..." : "No glowstone in hotbar! Disabling...");
                    return;
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

                    sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                    interact(result, chargeMode);

                    charges.put(targetPos,
                            charges.containsKey(targetPos) ?
                                    charges.get(targetPos) + 1 : 1);
                }

                if (switchBack.getValue())
                    InventoryUtility.switchTo(preSlot, switchMode.getValue());
            }
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
            while (isEnabled()) {
                try {
                    sleep(explodeTimeout.getValue());
                } catch (InterruptedException ignored) {
                }

                if (target == null || targetPos == null || fullNullCheck()) continue;
                if (!isCorrectPos(targetPos) || shouldPause()) continue;
                if (antiSelfPop.getValue() && mc.player.getHealth() - ExplosionUtility.getAnchorExplosionDamage(targetPos, mc.player) <= 0)
                    continue;
                if (antiFriendPop.getValue()) {
                    boolean shouldContinue = false;

                    for (AbstractClientPlayerEntity entity : Thunderhack.friendManager.getOnlineFriends()) {
                        if (entity.getHealth() - ExplosionUtility.getAnchorExplosionDamage(targetPos, entity) <= 0) {
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

                InteractionUtility.BreakData data = InteractionUtility.getBreakData(targetPos, interactMode.getValue());
                if (data != null && data.vector() != null) {
                    if (explodeRotate.getValue()) {
                        final float[] angles = InteractionUtility.calculateAngle(targetPos.toCenterPos());
                        sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angles[0], angles[1], mc.player.isOnGround()));
                    }

                    BlockHitResult result = new BlockHitResult(data.vector(), data.dir(), targetPos, false);

                    sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                    interact(result, explodeMode);
                    sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                }

                if (switchBack.getValue())
                    InventoryUtility.switchTo(preSlot, switchMode.getValue());
            }
        }

        private synchronized boolean isCorrectPos(@NotNull BlockPos pos) {
            if (mc.player == null || mc.world == null) return false;
            if (mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR
                    && (mc.world.getBlockState(pos).get(RespawnAnchorBlock.CHARGES) >= chargeCount.getValue()
                    || (charges.containsKey(pos) && charges.get(pos) >= chargeCount.getValue()))) {
                if (onlyOwn.getValue() && !ownAnchors.contains(pos)) return false;
                return mc.player.squaredDistanceTo(pos.toCenterPos()) <= explodeRange.getPow2Value();
            }

            return false;
        }
    }
}
