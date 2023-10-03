package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.Objects;

import static thunder.hack.modules.client.MainSettings.isRu;

public class AutoBed extends Module {
    public AutoBed() {
        super("AutoBed", Category.COMBAT);
    }

    public static final Setting<Float> range = new Setting<>("Range", 4f, 2f, 6.0f);
    public static final Setting<Float> wallRange = new Setting<>("WallRange", 4f, 0f, 6.0f);
    public static final Setting<Integer> placeDelay = new Setting<>("PlaceDelay", 100, 0, 1000);
    public static final Setting<Integer> explodeDelay = new Setting<>("ExplodeDelay", 100, 0, 1000);
    public static final Setting<Float> minDamage = new Setting<>("MinDamage", 8f, 0f, 25.0f);
    public static final Setting<Float> maxSelfDamage = new Setting<>("MaxSelfDamage", 4f, 0f, 25.0f);
    public final Setting<Boolean> switchToHotbar = new Setting<>("SwitchToHotbar", true);
    public final Setting<Boolean> autoSwap = new Setting<>("AutoSwap", true);
    private final Setting<Parent> renderCategory = new Setting<>("Render", new Parent(false, 0));
    private final Setting<Boolean> render = new Setting<>("Render", true).withParent(renderCategory);
    private final Setting<Boolean> rselfDamage = new Setting<>("SelfDamage", true).withParent(renderCategory);
    private final Setting<Boolean> drawDamage = new Setting<>("RenderDamage", true).withParent(renderCategory);
    private final Setting<ColorSetting> fillColor = new Setting<>("Fill", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<ColorSetting> lineColor = new Setting<>("Line", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<ColorSetting> textColor = new Setting<>("Text", new ColorSetting(Color.WHITE)).withParent(renderCategory);
    // later...
    // public final Setting<Boolean> autoCraft = new Setting<>("AutoCraft", true);

    private PlayerEntity target;
    private BedData bestBed, bestPos;

    private Timer placeTimer = new Timer();
    private Timer explodeTimer = new Timer();

    @EventHandler
    public void onSync(EventSync e) {
        target = findTarget();

        if (!(Objects.equals(mc.world.getRegistryKey().getValue().getPath(), "the_nether"))){
            disable(isRu() ? "Кровати не взрываются в этом мире!" : "Beds don't explode in this world!");
            return;
        }

        if (target == null)
            return;

        bestBed = findBedToExplode();
        bestPos = findBlockToPlace();

        if (bestBed != null || bestPos != null) {
            float[] angle;

            angle = InteractionUtility.calculateAngle(Objects.requireNonNullElseGet(bestPos, () -> bestBed).hitResult().getPos());

            mc.player.setYaw(angle[0]);
            mc.player.setPitch(angle[1]);
        }
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {

        if (!(mc.player.getMainHandStack().getItem() instanceof BedItem) && autoSwap.getValue()) {
            SearchInvResult hotBarResult = InventoryUtility.findBedInHotbar();
            if (hotBarResult.found()) {
                hotBarResult.switchTo();
            } else if (switchToHotbar.getValue()) {
                SearchInvResult invResult = InventoryUtility.findBed();
                if (invResult.found()) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invResult.slot(), mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                }
            }
        }

        if (bestBed != null && explodeTimer.passedMs(explodeDelay.getValue())) {
            sendPacket(new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, bestBed.hitResult(), PlayerUtility.getWorldActionId(mc.world)));
            mc.player.swingHand(Hand.OFF_HAND);
            explodeTimer.reset();
        }

        if (!(mc.player.getMainHandStack().getItem() instanceof BedItem)) {
            return;
        }

        if (bestPos != null && placeTimer.passedMs(placeDelay.getValue()) && !(mc.world.getBlockState(bestPos.hitResult().getBlockPos().up()).getBlock() instanceof BedBlock)) {
            final float angle2 = InteractionUtility.calculateAngle(bestPos.hitResult.getBlockPos().toCenterPos(), bestPos.hitResult.getBlockPos().offset(bestPos.dir).toCenterPos())[0];
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle2, 0, mc.player.isOnGround()));
            float prevYaw = mc.player.getYaw();
            mc.player.setYaw(angle2);
            mc.player.prevYaw = angle2;
            ((IClientPlayerEntity) mc.player).setLastYaw(angle2);
            sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bestPos.hitResult(), PlayerUtility.getWorldActionId(mc.world)));
            mc.player.swingHand(Hand.MAIN_HAND);
            placeTimer.reset();
            mc.player.setYaw(prevYaw);
        }
    }

    public void onRender3D(MatrixStack stack) {
        if (bestPos != null && render.getValue()) {
            Box box = new Box(bestPos.hitResult.getBlockPos().up());
            Box box2 = new Box(bestPos.hitResult.getBlockPos().up().offset(bestPos.dir));

            Box finalBox = box.union(box2).withMaxY(box.maxY - 0.45f);

            String dmg = MathUtility.round2(bestPos.damage()) + (rselfDamage.getValue() ? " / " + MathUtility.round2(bestPos.selfDamage()) : "");

            Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(finalBox, lineColor.getValue().getColorObject(), 2f));
            Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(finalBox, fillColor.getValue().getColorObject()));

            if (drawDamage.getValue())
                Render3DEngine.drawTextIn3D(dmg, finalBox.getCenter(), 0, 0.1, 0, textColor.getValue().getColorObject());
        }
    }

    private PlayerEntity findTarget() {
        return ThunderHack.combatManager.getNearestTarget(12f);
    }

    private BedData findBedToExplode() {
        int intRange = (int) (Math.floor(range.getValue()) + 1);
        Iterable<BlockPos> blocks_ = BlockPos.iterateOutwards(new BlockPos(BlockPos.ofFloored(mc.player.getPos()).up()), intRange, intRange, intRange);

        BedData bestData = null;

        for (BlockPos b : blocks_) {
            BlockState state = mc.world.getBlockState(b);
            if (PlayerUtility.squaredDistanceFromEyes(b.toCenterPos()) <= range.getPow2Value()) {
                if (state.getBlock() instanceof BedBlock) {
                    BlockHitResult bhr = getInteractResult(b);

                    mc.world.removeBlock(b, false);
                    float damage = ExplosionUtility.getExplosionDamage1(b.toCenterPos().add(0, -0.5, 0), target);
                    float selfDamage = ExplosionUtility.getExplosionDamage1(b.toCenterPos().add(0, -0.5, 0), mc.player);
                    mc.world.setBlockState(b, state);

                    if (damage < minDamage.getValue())
                        continue;

                    if (selfDamage > maxSelfDamage.getValue())
                        continue;

                    if (bestData != null && bestData.damage > damage)
                        continue;

                    if (bhr != null)
                        bestData = new BedData(bhr, damage, selfDamage, null);
                }
            }
        }
        return bestData;
    }

    private BedData findBlockToPlace() {
        int intRange = (int) (Math.floor(range.getValue()) + 1);
        Iterable<BlockPos> blocks_ = BlockPos.iterateOutwards(new BlockPos(BlockPos.ofFloored(mc.player.getPos()).up()), intRange, intRange, intRange);

        BedData bestData = null;

        for (BlockPos b : blocks_) {
            BlockState state = mc.world.getBlockState(b);
            BlockState state2 = mc.world.getBlockState(b.up());

            if (PlayerUtility.squaredDistanceFromEyes(b.toCenterPos()) <= range.getPow2Value()) {

                if (state2.getBlock() instanceof BedBlock && !placeTimer.passedMs(1500) && bestPos != null) {
                    return bestPos;
                }

                if (!state.isReplaceable()) {
                    BlockHitResult bhr = InteractionUtility.getPlaceResult(b.up(), InteractionUtility.Interact.Vanilla, false);
                    if (bhr != null) {
                        float damage = ExplosionUtility.getExplosionDamage1(b.up().toCenterPos().add(0, -0.5, 0), target);
                        float selfDamage = ExplosionUtility.getExplosionDamage1(b.up().toCenterPos().add(0, -0.5, 0), mc.player);

                        if (damage < minDamage.getValue())
                            continue;

                        if (selfDamage > maxSelfDamage.getValue())
                            continue;

                        if (bestData != null && bestData.damage > damage)
                            continue;


                        float bestDirdmg = 0;
                        Direction bestDir = Direction.NORTH;
                        for (Direction dir : Direction.values()) {
                            if (dir == Direction.DOWN || dir == Direction.UP)
                                continue;
                            BlockPos offset = b.up().offset(dir);
                            float dirdamage = ExplosionUtility.getExplosionDamage1(offset.toCenterPos().add(0, -0.5, 0), target);
                            float dirSelfDamage = ExplosionUtility.getExplosionDamage1(offset.toCenterPos().add(0, -0.5, 0), mc.player);
                            if (dirdamage > bestDirdmg && dirSelfDamage <= maxSelfDamage.getValue()) {
                                bestDir = dir;
                                bestDirdmg = dirdamage;
                            }
                        }

                        bestData = new BedData(bhr, damage, selfDamage, bestDir);
                    }
                }
            }
        }
        return bestData;
    }


    public BlockHitResult getInteractResult(BlockPos bp) {
        float bestDistance = 999f;
        BlockHitResult bestResult = null;
        for (float x = 0f; x <= 1f; x += 0.25f) {
            for (float y = 0f; y <= 0.5f; y += 0.125f) {
                for (float z = 0f; z <= 1f; z += 0.25f) {
                    Vec3d point = new Vec3d(bp.getX() + x, bp.getY() + y, bp.getZ() + z);
                    float distance = PlayerUtility.squaredDistanceFromEyes(point);

                    BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                    if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
                        if (distance > wallRange.getPow2Value())
                            continue;


                    BlockHitResult result = ExplosionUtility.rayCastBlock(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player), bp);
                    if (distance > range.getPow2Value())
                        continue;

                    if (distance < bestDistance) {
                        if (result != null && result.getType() == HitResult.Type.BLOCK) {
                            bestResult = result;
                            bestDistance = distance;
                        }
                    }
                }
            }
        }
        return bestResult;
    }

    private record BedData(BlockHitResult hitResult, float damage, float selfDamage, Direction dir) {
    }
}

