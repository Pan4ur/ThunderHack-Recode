package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
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
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventSetBlockState;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render3DEngine;

import java.util.*;

import static thunder.hack.modules.client.MainSettings.isRu;

public class AutoAnchorRecode extends Module {
    private final Setting<Float> targetRange = new Setting<>("Target Range", 10f, 1f, 20f);
    private final Setting<Integer> minDamage = new Setting<>("Min Target Damage", 5, 1, 36);
    private final Setting<Integer> maxDamage = new Setting<>("Max Self Damage", 8, 0, 36);
    private final Setting<Integer> maxFDamage = new Setting<>("Max Friend Damage", 12, 0, 36);
    private final Setting<Boolean> antiSelfPop = new Setting<>("Anti Self Pop", true);
    private final Setting<Boolean> antiFriendPop = new Setting<>("Anti Friend Pop", false);
    private final Setting<InteractionUtility.Interact> interactMode = new Setting<>("Interact Mode", InteractionUtility.Interact.Vanilla);
    private final Setting<Boolean> swing = new Setting<>("Swing", true);
    private final Setting<Integer> logicTimeout = new Setting<>("Logic Timeout", 30, 1, 2000);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<YawStepMode> yawStep = new Setting<>("YawStep", YawStepMode.Off);
    private final Setting<Integer> yawAngle = new Setting<>("YawAngle", 54, 5, 180, v -> yawStep.getValue() != YawStepMode.Off);
    public static final Setting<Integer> predictTicks = new Setting<>("PredictTicks", 5, 0, 40);
    private final Setting<Boolean> instant = new Setting<>("Instant", false);
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

    private int prevAnchorAmount, anchorSpeed, invTimer;
    private final Map<BlockPos, Integer> charges = new HashMap<>();
    private final Timer logicTimer = new Timer();
    private PlayerEntity target;
    private BlockPos targetPos;

    public AutoAnchorRecode() {
        super("AutoAnchorRecode", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.world == null) return;
        if (dimensionDisable.getValue() && mc.world.getDimension().respawnAnchorWorks()) {
            disable(isRu() ? "Ты в незере! Отключаем..." : "You are in the nether! Disabling...");
        }
    }

    @Override
    public void onDisable() {
        target = null;
        targetPos = null;
        charges.clear();
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (targetPos != null)
            Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(new Box(targetPos), HudEditor.getColor(0), 2));

        super.onRender3D(stack);
    }

    @Override
    public void onThread() {
        if (fullNullCheck()) return;
        if (!logicTimer.passedMs(logicTimeout.getValue())) return;

        if (target == null) {
            target = ThunderHack.combatManager.getNearestTarget(targetRange.getValue());
            return;
        }
        if (target.getPos().squaredDistanceTo(mc.player.getEyePos()) > targetRange.getPow2Value()) {
            target = null;
            return;
        }
        logicTimer.reset();

        // Finding new best target pos
        BlockPos best = findAnchorBlocks().stream()
                .filter(this::isFriendsSafe)
                .max(Comparator.comparingDouble(bp -> ExplosionUtility.getAnchorExplosionDamage(bp, target)))
                .orElse(null);

        if (targetPos != null && best != null) {
            if (ExplosionUtility.getAnchorExplosionDamage(targetPos, mc.player) >= ExplosionUtility.getAnchorExplosionDamage(best, mc.player)) {
                targetPos = best;
            }
            if (mc.player.squaredDistanceTo(targetPos.toCenterPos()) >= range.getPow2Value()) {
                targetPos = null;
                return;
            }
            if (!InteractionUtility.canPlaceBlock(targetPos, interactMode.getValue(), false) && !mc.world.getBlockState(targetPos).getBlock().equals(Blocks.RESPAWN_ANCHOR)) {
                targetPos = null;
                return;
            }
        }

        if (targetPos == null) {
            targetPos = best;
            doPlace();
        }
    }

    @Override
    public String getDisplayInfo() {
        return anchorSpeed + " a/s";
    }

    @EventHandler
    private void onSync(EventSync event) {
        if (invTimer++ >= 20) {
            anchorSpeed = prevAnchorAmount - InventoryUtility.getItemCount(Items.END_CRYSTAL);
            prevAnchorAmount = InventoryUtility.getItemCount(Items.RESPAWN_ANCHOR);
            invTimer = 0;
        }
    }

    @EventHandler
    private void onBlockStateChange(EventSetBlockState event) {
        if (mc.player != null && mc.world != null && !shouldPause()) {
            if (!event.getPos().equals(targetPos)) {
                return;
            }
            final BlockState blockState = mc.world.getBlockState(targetPos);

            if (!(blockState.getBlock() instanceof RespawnAnchorBlock)) {
                if (blockState.isReplaceable()) {
                    doPlace();
                }
                return;
            }
            if (charges.get(targetPos) >= chargeCount.getValue()) {
                doBreak();
                return;
            }

            doCharge();
        }
    }

    private @NotNull List<BlockPos> findAnchorBlocks() {
        List<BlockPos> positions = new ArrayList<>();

        if (mc.player != null && mc.world != null) {
            BlockPos centerPos = mc.player.getBlockPos();

            final Iterable<BlockPos> checkBlocks = BlockPos.iterateOutwards(centerPos.up(),
                    range.getValue().intValue() + 1,
                    range.getValue().intValue() + 1,
                    range.getValue().intValue() + 1
            );
            for (BlockPos pos : checkBlocks) {
                if (mc.player.squaredDistanceTo(pos.toCenterPos()) <= range.getPow2Value()
                        && (InteractionUtility.canPlaceBlock(pos, interactMode.getValue(), false))) {
                    positions.add(pos);
                }
            }
        }

        return positions;
    }

    private boolean isFriendsSafe(BlockPos blockPos) {
        for (AbstractClientPlayerEntity player : ThunderHack.friendManager.getNearFriends()) {
            if (ExplosionUtility.getAnchorExplosionDamage(blockPos, player) > maxFDamage.getValue()) {
                return false;
            }
        }

        if (ExplosionUtility.getAnchorExplosionDamage(blockPos, mc.player) >= maxDamage.getValue())
            return false;

        return target == null || !(ExplosionUtility.getAnchorExplosionDamage(blockPos, target) < minDamage.getValue());
    }

    private boolean shouldPause() {
        return (onAura.getValue() && ModuleManager.aura.isEnabled())
                || (onAC.getValue() && ModuleManager.autoCrystal.isEnabled())
                || (onMine.getValue() && PlayerUtility.isMining())
                || (onEat.getValue() && PlayerUtility.isEating());
    }

    private void doPlace() {
        InteractionUtility.placeBlock(targetPos,
                rotate.getValue(),
                interactMode.getValue(),
                InteractionUtility.PlaceMode.Packet,
                InventoryUtility.getAnchor(),
                true,
                false
        );
        if (instant.getValue()) {
            for (int i = 0; i < chargeCount.getValue(); i++)
                doCharge();
            doBreak();
        }
    }

    private void doCharge() {
        SearchInvResult glowResult = InventoryUtility.getGlowStone();
        if (glowStoneDisable.getValue() && !glowResult.found()) {
            disable(isRu() ? "В хотбаре не найден светящийся камень! Отключаем..." : "No glowstone found in hotbar! Disabling...");
            return;
        }

        // Charging
        BlockPos targetPosCopy = new BlockPos(targetPos);
        int preSlot = mc.player.getInventory().selectedSlot;
        glowResult.switchTo();
        InteractionUtility.BreakData data = InteractionUtility.getBreakData(targetPosCopy, InteractionUtility.Interact.Vanilla);

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
        sendPacket(new HandSwingC2SPacket(hand));
    }

    private enum YawStepMode {
        Off,
        On
    }
}