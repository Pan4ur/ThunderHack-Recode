package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.DeathEvent;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanParent;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.BlockAnimationUtility;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.world.HoleUtility;

import java.util.*;
import java.util.List;

import static thunder.hack.modules.client.MainSettings.isRu;

public final class AutoAnchor extends Module {
    private final Setting<Float> targetRange = new Setting<>("Target Range", 10f, 1f, 20f);
    private final Setting<Integer> minDamage = new Setting<>("Min Target Damage", 5, 1, 36);
    private final Setting<Integer> maxDamage = new Setting<>("Max Self Damage", 8, 0, 36);
    private final Setting<Integer> maxFDamage = new Setting<>("Max Friend Damage", 12, 0, 36);
    private final Setting<Boolean> antiSelfPop = new Setting<>("Anti Self Pop", true);
    private final Setting<Boolean> antiFriendPop = new Setting<>("Anti Friend Pop", false);
    private final Setting<InteractionUtility.Interact> interactMode = new Setting<>("Interact Mode", InteractionUtility.Interact.Vanilla);
    private final Setting<Boolean> swing = new Setting<>("Swing", true);
    private final Setting<Integer> actionDelay = new Setting<>("Action Delay", 500, 1, 2000);
    private final Setting<Integer> logicDelay = new Setting<>("Logic Delay", 30, 1, 2000);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<YawStepMode> yawStep = new Setting<>("Yaw Step", YawStepMode.Off);
    private final Setting<Integer> yawAngle = new Setting<>("Yaw Angle", 54, 5, 180, v -> yawStep.getValue() != YawStepMode.Off);
    public static final Setting<Integer> predictTicks = new Setting<>("Predict Ticks", 5, 0, 40);
    private final Setting<Float> range = new Setting<>("Place Range", 5f, 1f, 7f);
    private final Setting<Integer> chargeCount = new Setting<>("Charge Count", 1, 1, 4);

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

    // Render
    private final Setting<BooleanParent> render = new Setting<>("Render", new BooleanParent(false));
    private final Setting<Boolean> pop = new Setting<>("Render Pop", true).withParent(render);
    private final Setting<Boolean> death = new Setting<>("Render Death", true).withParent(render);
    private final Setting<ColorSetting> fillColor = new Setting<>("Block Fill Color", new ColorSetting(HudEditor.getColor(0))).withParent(render);
    private final Setting<ColorSetting> lineColor = new Setting<>("Block Line Color", new ColorSetting(HudEditor.getColor(0))).withParent(render);
    private final Setting<Integer> lineWidth = new Setting<>("Block Line Width", 2, 1, 10).withParent(render);

    private final Map<BlockPos, Integer> charges = new HashMap<>();
    private final Timer logicTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private PlayerEntity target;
    private BlockPos targetPos;
    private boolean rotated;

    public AutoAnchor() {
        super("AutoAnchor", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.world == null) return;
        if (dimensionDisable.getValue() && mc.world.getDimension().respawnAnchorWorks()) {
            disable(isRu() ? "Ты в незере! Отключаем..." : "You are in the nether! Disabling...");
            return;
        }
        rotated = false;
    }

    @Override
    public void onDisable() {
        target = null;
        targetPos = null;
        charges.clear();
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (targetPos == null || !render.getValue().isEnabled()) return;

        Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(
                new Box(targetPos),
                lineColor.getValue().getColorObject(),
                lineWidth.getValue()
        ));
        Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(
                new Box(targetPos),
                fillColor.getValue().getColorObject()
        ));
    }

    @Override
    public @Nullable String getDisplayInfo() {
        return target != null ? target.getName().getString() : null;
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPop(@NotNull TotemPopEvent event) {
        if (!event.getEntity().equals(target) || !render.getValue().isEnabled() || !pop.getValue()) return;

        BlockAnimationUtility.renderBlock(
                targetPos,
                lineColor.getValue().getColorObject(),
                lineWidth.getValue(),
                fillColor.getValue().getColorObject(),
                BlockAnimationUtility.BlockAnimationMode.Flash,
                BlockAnimationUtility.BlockRenderMode.All
        );
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onDeath(@NotNull DeathEvent event) {
        if (!event.getPlayer().equals(target) || !render.getValue().isEnabled() || !death.getValue()) return;

        BlockAnimationUtility.renderBlock(
                targetPos,
                lineColor.getValue().getColorObject(),
                lineWidth.getValue(),
                fillColor.getValue().getColorObject(),
                BlockAnimationUtility.BlockAnimationMode.Decrease,
                BlockAnimationUtility.BlockRenderMode.All
        );
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onSync(EventSync event) {
        if (rotate.getValue() && targetPos != null && mc.player != null) {
            float[] angle = InteractionUtility.calculateAngle(targetPos.toCenterPos());
            if (yawStep.getValue() == YawStepMode.On) {
                float yawDelta = MathHelper.wrapDegrees(angle[0] - ((IClientPlayerEntity) mc.player).getLastYaw());
                if (Math.abs(yawDelta) > yawAngle.getValue()) {
                    angle[0] = ((IClientPlayerEntity) mc.player).getLastYaw() + (yawDelta * (yawAngle.getValue() / Math.abs(yawDelta)));
                    rotated = false;
                } else rotated = true;
            } else rotated = true;

            double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
            mc.player.setYaw((float) (angle[0] - (angle[0] - ((IClientPlayerEntity) mc.player).getLastYaw()) % gcdFix));
            mc.player.setPitch((float) (angle[1] - (angle[1] - ((IClientPlayerEntity) mc.player).getLastPitch()) % gcdFix));
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPostSync(EventPostSync event) {
        new Thread(() -> {
            if (fullNullCheck()) return;
            if (!logicTimer.passedMs(logicDelay.getValue())) return;

            if (target == null) {
                target = ThunderHack.combatManager.getNearestTarget(targetRange.getValue());
                return;
            }
            if (target.getPos().squaredDistanceTo(mc.player.getEyePos()) > targetRange.getPow2Value()) {
                target = null;
                return;
            }
            logicTimer.reset();

            if (HoleUtility.isHole(target.getBlockPos())
                    || mc.world.getBlockState(target.getBlockPos().up(2)).getBlock().equals(Blocks.RESPAWN_ANCHOR)) {
                targetPos = target.getBlockPos().up(2);
                doPlace();
                return;
            }

            // Finding new best target pos
            BlockPos best = findAnchorBlocks().stream()
                    .filter(this::isFriendsSafe)
                    .max(Comparator.comparingDouble(bp -> ExplosionUtility.getAnchorExplosionDamage(bp, target)))
                    .orElse(null);

            if (targetPos != null && best != null) {
                if (ExplosionUtility.getSelfExplosionDamage(targetPos.toCenterPos(), predictTicks.getValue()) >= ExplosionUtility.getAnchorExplosionDamage(best, mc.player)) {
                    targetPos = best;
                }
                if (mc.player.squaredDistanceTo(targetPos.toCenterPos()) >= range.getPow2Value()) {
                    targetPos = null;
                    return;
                }
                if (!InteractionUtility.canPlaceBlock(targetPos, interactMode.getValue(), false)
                        && !mc.world.getBlockState(targetPos).getBlock().equals(Blocks.RESPAWN_ANCHOR)) {
                    targetPos = null;
                    return;
                }
            }

            if (targetPos == null) {
                targetPos = best;
                return;
            }
            doPlace();
        }).start();
    }

    private synchronized @NotNull List<BlockPos> findAnchorBlocks() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos centerPos = mc.player.getBlockPos();

        int r = (int) Math.ceil(range.getValue()) + 1;
        int h = range.getValue().intValue();

        for (int i = centerPos.getX() - r; i < centerPos.getX() + r; i++) {
            for (int j = centerPos.getY() - h; j < centerPos.getY() + h; j++) {
                for (int k = centerPos.getZ() - r; k < centerPos.getZ() + r; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (mc.player.squaredDistanceTo(pos.toCenterPos()) <= range.getPow2Value()
                            && (InteractionUtility.canPlaceBlock(pos, interactMode.getValue(), false))
                            || mc.world.getBlockState(pos).getBlock().equals(Blocks.RESPAWN_ANCHOR)) {
                        positions.add(pos);
                    }
                }
            }
        }

        return positions;
    }

    private synchronized boolean isFriendsSafe(BlockPos blockPos) {
        for (AbstractClientPlayerEntity player : ThunderHack.friendManager.getNearFriends()) {
            if (ExplosionUtility.getAnchorExplosionDamage(blockPos, player) > maxFDamage.getValue()) {
                return false;
            }
        }

        if (ExplosionUtility.getAnchorExplosionDamage(blockPos, mc.player) >= maxDamage.getValue())
            return false;

        return target == null || !(ExplosionUtility.getAnchorExplosionDamage(blockPos, target) < minDamage.getValue());
    }

    private boolean shouldFriendsPop(BlockPos pos) {
        for (AbstractClientPlayerEntity player : ThunderHack.friendManager.getNearFriends()) {
            if (mc.player.getHealth() + mc.player.getAbsorptionAmount() - ExplosionUtility.getAnchorExplosionDamage(pos, player) <= 0) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldPause() {
        return (onAura.getValue() && ModuleManager.aura.isEnabled())
                || (onAC.getValue() && ModuleManager.autoCrystal.isEnabled())
                || (onMine.getValue() && PlayerUtility.isMining())
                || (onEat.getValue() && PlayerUtility.isEating());
    }

    private void doPlace() {
        if (targetPos == null
                || shouldPause()
                || target == null
                || (rotate.getValue() && !rotated)
                || mc.player.getHealth() + mc.player.getAbsorptionAmount() - ExplosionUtility.getSelfExplosionDamage(targetPos.toCenterPos(), predictTicks.getValue()) <= 0
                || !ThunderHack.friendManager.getNearFriends().stream()
                .filter(friend -> friend.getHealth() + friend.getAbsorptionAmount() - ExplosionUtility.getAnchorExplosionDamage(targetPos, friend) <= 0)
                .toList()
                .isEmpty()
                || !InteractionUtility.canPlaceBlock(targetPos, interactMode.getValue(), false)) return;

        if (HoleUtility.isHole(target.getBlockPos())) {
            if (InteractionUtility.canPlaceBlock(targetPos, interactMode.getValue(), false)) {
                InteractionUtility.placeBlock(targetPos,
                        rotate.getValue(),
                        interactMode.getValue(),
                        InteractionUtility.PlaceMode.Packet,
                        InventoryUtility.getAnchor(),
                        true,
                        false
                );
                return;
            } else if (!mc.world.getBlockState(target.getBlockPos().up(2)).getBlock().equals(Blocks.RESPAWN_ANCHOR)) {
                for (int i = 2; i > 0; i--) {
                    for (BlockPos check : HoleUtility.getHolePoses(target.getPos())) {
                        check = check.up(i);
                        if (InteractionUtility.canPlaceBlock(check, interactMode.getValue(), false)) {
                            InteractionUtility.placeBlock(check,
                                    rotate.getValue(),
                                    interactMode.getValue(),
                                    InteractionUtility.PlaceMode.Packet,
                                    InventoryUtility.getAnchor(),
                                    true,
                                    false
                            );
                            return;
                        }
                    }
                }
            }

            if (placeTimer.passedMs(actionDelay.getValue())) {
                for (int i = 0; i < chargeCount.getValue(); i++)
                    doCharge();
                doBreak();
                placeTimer.reset();
            }
        } else if (placeTimer.passedMs(actionDelay.getValue())) {
            InteractionUtility.placeBlock(targetPos,
                    rotate.getValue(),
                    interactMode.getValue(),
                    InteractionUtility.PlaceMode.Packet,
                    InventoryUtility.getAnchor(),
                    true,
                    false
            );
            for (int i = 0; i < chargeCount.getValue(); i++)
                doCharge();
            doBreak();
            placeTimer.reset();
        }
    }

    private void doCharge() {
        SearchInvResult glowResult = InventoryUtility.getGlowStone();
        if (glowStoneDisable.getValue() && !glowResult.found()) {
            disable(isRu() ? "В хотбаре не найден светящийся камень! Отключаем..." : "No glowstone found in hotbar! Disabling...");
            return;
        }
        if (targetPos == null || (rotate.getValue() && !rotated)) return;

        // Charging
        BlockPos targetPosCopy = new BlockPos(targetPos);
        int preSlot = mc.player.getInventory().selectedSlot;
        glowResult.switchTo();
        InteractionUtility.BreakData data = InteractionUtility.getBreakData(targetPosCopy, InteractionUtility.Interact.Vanilla);

        if ((mc.player.getHealth() + mc.player.getAbsorptionAmount() - ExplosionUtility.getAnchorExplosionDamage(targetPosCopy, mc.player) <= 0 && antiSelfPop.getValue())
                || (shouldFriendsPop(targetPosCopy) && antiFriendPop.getValue()))
            return;

        if (data != null && data.vector() != null) {
            BlockHitResult result = new BlockHitResult(data.vector(), data.dir(), targetPosCopy, false);

            sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            interact(result, Hand.MAIN_HAND);

            if (charges.containsKey(targetPosCopy)) charges.put(targetPosCopy, charges.get(targetPosCopy) + 1);
            else charges.put(targetPosCopy, 1);
        }

        InventoryUtility.switchTo(preSlot);
    }

    private void doBreak() {
        if (targetPos == null || (rotate.getValue() && !rotated))
            return;
        BlockPos targetPosCopy = new BlockPos(targetPos);
        int preSlot = mc.player.getInventory().selectedSlot;
        if (mc.player.getMainHandStack().getItem().equals(Items.GLOWSTONE)) {
            SearchInvResult result = InventoryUtility.findInHotBar(stack -> !stack.getItem().equals(Items.GLOWSTONE));
            result.switchTo();
        }
        InteractionUtility.BreakData data = InteractionUtility.getBreakData(targetPosCopy, InteractionUtility.Interact.Vanilla);
        if (data != null && data.vector() != null) {
            SearchInvResult anchorResult = InventoryUtility.getAnchor();
            if (anchorDisable.getValue() && !anchorResult.found()) {
                disable(isRu() ? "В хотбаре не найдены якоря возрождения! Отключаем..." : "No respawn anchors found in hotbar! Disabling...");
                return;
            }

            if (anchorResult.found()) anchorResult.switchTo();

            BlockHitResult result = new BlockHitResult(data.vector(), data.dir(), targetPosCopy, false);

            sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            interact(result, Hand.OFF_HAND);
            charges.remove(targetPosCopy);
        }

        InventoryUtility.switchTo(preSlot);
    }

    private void interact(BlockHitResult result, Hand hand) {
        sendPacket(new PlayerInteractBlockC2SPacket(hand, result, PlayerUtility.getWorldActionId(mc.world)));
        if (swing.getValue()) mc.player.swingHand(hand);
        else sendPacket(new HandSwingC2SPacket(hand));
    }

    private enum YawStepMode {
        Off,
        On
    }
}