package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.world.ExplosionUtility;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.Objects;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public final class AutoBed extends Module {
    private final Setting<InteractionUtility.Interact> interactMode = new Setting<>("InteractMode", InteractionUtility.Interact.Vanilla);
    public static final Setting<Float> range = new Setting<>("Range", 4f, 2f, 6.0f);
    public static final Setting<Float> wallRange = new Setting<>("WallRange", 4f, 0f, 6.0f);
    public static final Setting<Integer> placeDelay = new Setting<>("PlaceDelay", 100, 0, 1000);
    public static final Setting<Integer> explodeDelay = new Setting<>("ExplodeDelay", 100, 0, 1000);
    public static final Setting<Float> minDamage = new Setting<>("MinDamage", 8f, 0f, 25.0f);
    public static final Setting<Float> maxSelfDamage = new Setting<>("MaxSelfDamage", 4f, 0f, 25.0f);
    private final Setting<Boolean> dimCheck = new Setting<>("DimensionCheck", false);
    public final Setting<Boolean> switchToHotbar = new Setting<>("SwitchToHotbar", true);
    public final Setting<Boolean> oldPlace = new Setting<>("1.12 Place",false);
    public final Setting<Boolean> autoSwap = new Setting<>("AutoSwap", true);
    public final Setting<Boolean> autoCraft = new Setting<>("AutoCraft", true);
    public static final Setting<Integer> minBeds = new Setting<>("MinBeds", 4, 0, 10);
    public static final Setting<Integer> bedsPerCraft = new Setting<>("BedsPerCraft", 8, 1, 27);
    private final Setting<SettingGroup> renderCategory = new Setting<>("Render", new SettingGroup(false, 0));
    private final Setting<Boolean> render = new Setting<>("Render", true).addToGroup(renderCategory);
    private final Setting<Boolean> rselfDamage = new Setting<>("SelfDamage", true).addToGroup(renderCategory);
    private final Setting<Boolean> drawDamage = new Setting<>("RenderDamage", true).addToGroup(renderCategory);
    private final Setting<ColorSetting> fillColor = new Setting<>("Fill", new ColorSetting(Render2DEngine.injectAlpha(HudEditor.getColor(0), 150))).addToGroup(renderCategory);
    private final Setting<ColorSetting> lineColor = new Setting<>("Line", new ColorSetting(HudEditor.getColor(0))).addToGroup(renderCategory);
    private final Setting<ColorSetting> textColor = new Setting<>("Text", new ColorSetting(Color.WHITE)).addToGroup(renderCategory);

    private PlayerEntity target;
    private BedData bestBed, bestPos;
    private float rotationYaw, rotationPitch;

    private final Timer placeTimer = new Timer();
    private final Timer explodeTimer = new Timer();

    public AutoBed() {
        super("AutoBed", Category.COMBAT);
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (bestBed != null || bestPos != null) {
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
        }
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent e) {
        target = findTarget();

        if (mc.world.getDimension().bedWorks() && dimCheck.getValue()) {
            disable(isRu() ? "Кровати не взрываются в этом измерении!" : "Beds don't explode in this dimension!");
            return;
        }

        if (target != null && (target.isDead() || target.getHealth() < 0)) {
            target = null;
            return;
        }

        bestBed = findBedToExplode();
        bestPos = findBlockToPlace();

        if (bestBed != null || bestPos != null) {
            float[] angle;

            angle = InteractionUtility.calculateAngle(Objects.requireNonNullElseGet(bestPos, () -> bestBed).hitResult().getPos());

            rotationYaw = (angle[0]);
            rotationPitch = (angle[1]);
            ModuleManager.rotations.fixRotation = rotationYaw;
        }

        if (autoCraft.getValue()) {
            if (InventoryUtility.getBedsCount() <= minBeds.getValue()) {
                craftBed();
                return;
            }
            if (mc.player.currentScreenHandler instanceof CraftingScreenHandler) {
                sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                mc.player.closeScreen();
            }
        }
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {
        if (!(mc.player.getMainHandStack().getItem() instanceof BedItem) && autoSwap.getValue() && bestPos != null) {
            SearchInvResult hotBarResult = InventoryUtility.findBedInHotBar();
            if (hotBarResult.found()) {
                hotBarResult.switchTo();
            } else if (switchToHotbar.getValue()) {
                SearchInvResult invResult = InventoryUtility.findBed();
                if (invResult.found() && !(mc.currentScreen instanceof CraftingScreen)) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invResult.slot(), mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                }
            }
        }

        if (bestBed != null && explodeTimer.passedMs(explodeDelay.getValue())) {
            sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bestBed.hitResult(), id));
            mc.player.swingHand(Hand.MAIN_HAND);
            explodeTimer.reset();
        }

        if (!(mc.player.getMainHandStack().getItem() instanceof BedItem))
            return;

        if (bestPos != null && placeTimer.passedMs(placeDelay.getValue()) && !(mc.world.getBlockState(bestPos.hitResult().getBlockPos().up()).getBlock() instanceof BedBlock)) {
            final float angle2 = InteractionUtility.calculateAngle(bestPos.hitResult.getBlockPos().toCenterPos(), bestPos.hitResult.getBlockPos().offset(bestPos.dir).toCenterPos())[0];
            sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle2, 0, mc.player.isOnGround()));
            float prevYaw = mc.player.getYaw();
            mc.player.setYaw(angle2);
            mc.player.prevYaw = angle2;
            ((IClientPlayerEntity) mc.player).setLastYaw(angle2);
            sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bestPos.hitResult(), id));
            mc.player.swingHand(Hand.MAIN_HAND);
            placeTimer.reset();
            mc.player.setYaw(prevYaw);
        }
    }

    @Override
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
        return Managers.COMBAT.getNearestTarget(12f);
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
                    float damage = ExplosionUtility.getExplosionDamage(b.toCenterPos().add(0, -0.5, 0), target, false);
                    float selfDamage = ExplosionUtility.getExplosionDamage(b.toCenterPos().add(0, -0.5, 0), mc.player, false);
                    mc.world.setBlockState(b, state);

                    if (damage < minDamage.getValue())
                        continue;

                    if (selfDamage > maxSelfDamage.getValue())
                        continue;

                    if (selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() + 2f)
                        continue;

                    if (bestData != null && bestData.damage > damage)
                        continue;

                    if (bhr != null)
                        bestData = new BedData(bhr, damage, selfDamage, bhr.getSide());
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
                if (state2.getBlock() instanceof BedBlock && !placeTimer.passedMs(1500) && bestPos != null)
                    return bestPos;

                if (!state.isReplaceable()) {
                    BlockHitResult bhr = InteractionUtility.getPlaceResult(b.up(), interactMode.getValue(), false);
                    if (bhr != null) {

                        BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), bhr.getPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                        if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != b)
                            continue;

                        float damage = ExplosionUtility.getExplosionDamage(b.up().toCenterPos().add(0, -0.5, 0), target, false);
                        float selfDamage = ExplosionUtility.getExplosionDamage(b.up().toCenterPos().add(0, -0.5, 0), mc.player, false);

                        if (damage < minDamage.getValue())
                            continue;

                        if (selfDamage > maxSelfDamage.getValue())
                            continue;

                        if (selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() + 2f)
                            continue;

                        if (bestData != null && bestData.damage > damage)
                            continue;


                        float bestDirdmg = 0;
                        Direction bestDir = null;
                        for (Direction dir : Direction.values()) {
                            if (dir == Direction.DOWN || dir == Direction.UP)
                                continue;
                            BlockPos offset = b.up().offset(dir);

                            if(!mc.world.getBlockState(offset).isReplaceable())
                                continue;

                            if(oldPlace.getValue() && mc.world.getBlockState(b.offset(dir)).isReplaceable()){
                                continue;
                            }

                            float dirdamage = ExplosionUtility.getExplosionDamage(offset.toCenterPos().add(0, -0.5, 0), target, false);
                            float dirSelfDamage = ExplosionUtility.getExplosionDamage(offset.toCenterPos().add(0, -0.5, 0), mc.player, false);
                            if (dirdamage > bestDirdmg && dirSelfDamage <= maxSelfDamage.getValue()) {
                                bestDir = dir;
                                bestDirdmg = dirdamage;
                            }
                        }

                        bestData = bestDir == null ? null : new BedData(bhr, damage, selfDamage, bestDir);
                    }
                }
            }
        }
        return bestData;
    }


    public void craftBed() {
        int intRange = (int) (Math.floor(range.getValue()) + 1);
        Iterable<BlockPos> blocks_ = BlockPos.iterateOutwards(new BlockPos(BlockPos.ofFloored(mc.player.getPos()).up()), intRange, intRange, intRange);

        for (BlockPos b : blocks_) {
            BlockState state = mc.world.getBlockState(b);
            if (state.getBlock() instanceof CraftingTableBlock) {
                BlockHitResult result = getInteractResult(b);
                if (result != null) {
                    if (mc.player.currentScreenHandler instanceof CraftingScreenHandler craft) {
                        mc.player.getRecipeBook().setGuiOpen(craft.getCategory(), true);
                        for (RecipeResultCollection results : mc.player.getRecipeBook().getOrderedResults()) {
                            for (RecipeEntry<?> recipe : results.getRecipes(true)) {
                                if (recipe.value().getResult(results.getRegistryManager()).getItem() instanceof BedItem) {
                                    for (int i = 0; i < bedsPerCraft.getValue(); i++)
                                        mc.interactionManager.clickRecipe(mc.player.currentScreenHandler.syncId, recipe, false);
                                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
                                    break;
                                }
                            }
                        }
                    } else {
                        float[] angle = InteractionUtility.calculateAngle(result.getPos());
                        mc.player.setYaw(angle[0]);
                        mc.player.setPitch(angle[1]);
                        sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, id));
                    }
                }
            }
        }
    }

    public BlockHitResult getInteractResult(@NotNull BlockPos bp) {
        float bestDistance = 999f;
        BlockHitResult bestResult = null;
        for (float x = 0f; x < 1f; x += 0.25f) {
            for (float y = 0f; y < 0.5f; y += 0.125f) {
                for (float z = 0f; z < 1f; z += 0.25f) {
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

        float bestDistance2 = 999f;
        Direction bestDirection = null;

        if (mc.player.getEyePos().getY() > bp.up().getY()) {
            bestDirection = Direction.UP;
        } else if (mc.player.getEyePos().getY() < bp.getY()) {
            bestDirection = Direction.DOWN;
        } else {
            for (Direction dir : Direction.values()) {
                Vec3d directionVec = new Vec3d(bp.getX() + 0.5 + dir.getVector().getX() * 0.5, bp.getY() + 0.5 + dir.getVector().getY() * 0.5, bp.getZ() + 0.5 + dir.getVector().getZ() * 0.5);
                float distance = PlayerUtility.squaredDistanceFromEyes(directionVec);
                if (bestDistance2 > distance) {
                    bestDirection = dir;
                    bestDistance2 = distance;
                }
            }
        }

        if(bestResult == null)
            return null;

        return new BlockHitResult(bestResult.getPos(), bestDirection, bestResult.getBlockPos(), false);
    }

    private record BedData(BlockHitResult hitResult, float damage, float selfDamage, Direction dir) {
    }

}

