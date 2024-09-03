package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventEntityRemoved;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
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
import java.util.List;
import java.util.*;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public final class PistonAura extends Module {
    private final Setting<Integer> placeDelay = new Setting<>("Delay/Place", 1, 0, 25);
    private final Setting<Integer> blocksPerTick = new Setting<>("Block/Tick", 3, 1, 4);
    private final Setting<Pattern> patternsSetting = new Setting<>("Pattern", Pattern.All);
    private final Setting<Boolean> supportPlace = (new Setting<>("SupportPlace", false));
    private final Setting<Boolean> bypass = new Setting<>("FireBypass", false);
    private final Setting<Boolean> oldVersion = (new Setting<>("OldVersion", false));
    private final Setting<Boolean> trap = new Setting<>("Trap", false);
    private final Setting<Float> targetRange = new Setting<>("Target Range", 10.0f, 0.0f, 12.0f);
    private final Setting<Float> placeRange = new Setting<>("PlaceRange", 4.0f, 1.0f, 7.0f);
    private final Setting<Float> wallRange = new Setting<>("WallRange", 4.0f, 1.0f, 7.0f);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.Normal);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Strict);
    private final Setting<InteractionUtility.Rotate> rotate = new Setting<>("Rotate", InteractionUtility.Rotate.None);

    public PlayerEntity target;
    private BlockPos targetPos, pistonPos, crystalPos, redStonePos, firePos, pistonHeadPos;
    private boolean builtTrap, isFire;

    private final Timer trapTimer = new Timer();
    private final Timer attackTimer = new Timer();

    private int delay = 0;
    private Runnable postAction = null;
    private Stage stage = Stage.Searching;
    private EndCrystalEntity lastCrystal;
    private Vec3d rotations;

    public PistonAura() {
        super("PistonAura", Category.COMBAT);
    }

    public void reset() {
        builtTrap = false;
        target = null;
        stage = Stage.Searching;
        trapTimer.reset();
        attackTimer.reset();
        rotations = Vec3d.ZERO;
        pistonPos = null;
        targetPos = null;
        firePos = null;
        pistonHeadPos = null;
        lastCrystal = null;
        delay = 0;
        trapTimer.reset();
        attackTimer.reset();
        postAction = null;
    }

    @Override
    public void onEnable() {
        reset();
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (delay < placeDelay.getValue()) delay++;

        if (stage == Stage.Break) {
            if (isFire) {
                reset();
                return;
            }
            breakCrystal();
            return;
        }

        if (delay < placeDelay.getValue()) return;

        handlePistonAura(false);
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPostSync(EventPostSync event) {
        if (postAction != null) {
            delay = 0;
            postAction.run();
            postAction = null;
            int extraBlocks = 1;
            while (extraBlocks < blocksPerTick.getValue()) {
                handlePistonAura(true);
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


    public void handlePistonAura(boolean extra) {
        if (!InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN).found() && (trap.getValue() || supportPlace.getValue())) {
            disable(isRu() ? "Нет обсидиана!" : "No obsidian!");
            return;
        }

        if (!InventoryUtility.findBlockInHotBar(Blocks.REDSTONE_BLOCK).found() && !InventoryUtility.findBlockInHotBar(Blocks.REDSTONE_TORCH).found()) {
            disable(isRu() ? "Нет редстоуна!" : "No redstone!");
            return;
        }

        if (!InventoryUtility.findItemInHotBar(Items.END_CRYSTAL).found() && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
            disable(isRu() ? "Нет кристаллов!" : "No crystals!");
            return;
        }

        if (!(InventoryUtility.findItemInHotBar(Items.PISTON).found() || InventoryUtility.findItemInHotBar(Items.STICKY_PISTON).found())) {
            disable(isRu() ? "Нет поршней!" : "No pistons!");
            return;
        }

        switch (stage) {
            case Searching -> {
                findPos();
                stage = Stage.Trap;
            }
            case Trap -> buildTrap();
            case Piston -> placePiston(extra);
            case Fire -> placeFire(extra);
            case Crystal -> placeCrystal(extra);
            case RedStone -> placeRedStone(extra);
            case Break -> {
                if (isFire)
                    stage = Stage.Searching;
            }
        }
    }

    private void placeRedStone(boolean extra) {
        if (redStonePos == null) {
            stage = Stage.Searching;
            return;
        }

        if (mc.world.getBlockState(redStonePos).getBlock() instanceof RedstoneBlock) {
            stage = Stage.Break;
        }

        if (mc.world.getBlockState(redStonePos.down()).isReplaceable() && supportPlace.getValue()) {
            InteractionUtility.placeBlock(redStonePos.down(), rotate.getValue(), interact.getValue(), placeMode.getValue(), InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN), false, false);
            return;
        }

        final float[] angle = InteractionUtility.getPlaceAngle(redStonePos, interact.getValue(), false);
        if (angle == null) return;
        if (extra) {
            sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle[0], angle[1], mc.player.isOnGround()));
        } else {
            mc.player.setYaw(angle[0]);
            mc.player.setPitch(angle[1]);
        }

        postAction = () -> {
            int redstone_slot = -1;

            SearchInvResult redBlockResult = InventoryUtility.findBlockInHotBar(Blocks.REDSTONE_BLOCK);
            SearchInvResult redTorchResult = InventoryUtility.findBlockInHotBar(Blocks.REDSTONE_TORCH);

            if (!redBlockResult.found()) {
                if (!redTorchResult.found()) disable(isRu() ? "Нет редстоуна!" : "No redstone!");
                else redstone_slot = redTorchResult.slot();
            } else redstone_slot = redBlockResult.slot();

            InteractionUtility.placeBlock(redStonePos, InteractionUtility.Rotate.None, interact.getValue(), placeMode.getValue(), redstone_slot, false, false);
            stage = Stage.Break;
        };
    }

    private void placeCrystal(boolean extra) {
        if (crystalPos == null) {
            stage = Stage.Searching;
            return;
        }

        if (mc.world.getBlockState(crystalPos).isReplaceable() && supportPlace.getValue()) {
            InteractionUtility.placeBlock(crystalPos, rotate.getValue(), interact.getValue(), placeMode.getValue(), InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN), false, false);
            return;
        }

        BlockHitResult result = getPlaceData(crystalPos);
        if (result == null) return;
        float[] angle = InteractionUtility.calculateAngle(rotations);
        if (extra) {
            sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle[0] + MathUtility.random(-0.2f, 0.2f), angle[1], mc.player.isOnGround()));
        } else {
            mc.player.setYaw(angle[0] + MathUtility.random(-0.2f, 0.2f));
            mc.player.setPitch(angle[1]);
        }

        postAction = () -> {
            boolean offHand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
            int prev_slot = -1;
            if (!offHand) {
                int crystal_slot = InventoryUtility.findItemInHotBar(Items.END_CRYSTAL).slot();
                prev_slot = mc.player.getInventory().selectedSlot;
                if (crystal_slot != -1) {
                    mc.player.getInventory().selectedSlot = crystal_slot;
                    sendPacket(new UpdateSelectedSlotC2SPacket(crystal_slot));
                }
            }

            sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(offHand ? Hand.OFF_HAND : Hand.MAIN_HAND, result, id));
            sendPacket(new HandSwingC2SPacket(offHand ? Hand.OFF_HAND : Hand.MAIN_HAND));

            if (!offHand) {
                mc.player.getInventory().selectedSlot = prev_slot;
                sendPacket(new UpdateSelectedSlotC2SPacket(prev_slot));
            }

            stage = Stage.RedStone;
        };
    }

    private void placeFire(boolean extra) {
        if (firePos == null) {
            stage = Stage.Searching;
            return;
        }

        if (mc.world.getBlockState(firePos).getBlock() instanceof FireBlock) stage = Stage.Crystal;

        if (mc.world.getBlockState(firePos.down()).isReplaceable() && supportPlace.getValue()) {
            InteractionUtility.placeBlock(firePos.down(), rotate.getValue(), interact.getValue(), placeMode.getValue(), InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN), false, false);
            return;
        }

        float[] angle = InteractionUtility.getPlaceAngle(firePos, interact.getValue(), false);
        if (angle == null) return;
        if (extra) {
            sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle[0], angle[1], mc.player.isOnGround()));
        } else {
            mc.player.setYaw(angle[0]);
            mc.player.setPitch(angle[1]);
        }
        postAction = () -> {
            InteractionUtility.placeBlock(firePos, InteractionUtility.Rotate.None, interact.getValue(), placeMode.getValue(), InventoryUtility.findItemInHotBar(Items.FLINT_AND_STEEL).slot(), false, false);
            stage = Stage.Crystal;
        };
    }

    public void buildTrap() {
        if (!trap.getValue()) {
            stage = Stage.Piston;
            return;
        }
        if (mc.world.getBlockState(targetPos.add(0, 2, 0)).getBlock() == Blocks.OBSIDIAN || pistonPos.getY() >= targetPos.add(0, 2, 0).getY()) {
            stage = Stage.Piston;
            return;
        }

        if (!builtTrap) {
            final BlockPos offset = new BlockPos(crystalPos.getX() - targetPos.getX(), 0, crystalPos.getZ() - targetPos.getZ());
            final BlockPos trapBase = targetPos.add(offset.getX() * -1, 0, offset.getZ() * -1);

            List<BlockPos> trapPos = new ArrayList<>();
            trapPos.add(targetPos.add(0, 2, 0));
            trapPos.add(trapBase.add(0, 2, 0));
            trapPos.add(trapBase.add(0, 1, 0));

            InventoryUtility.saveSlot();
            for (BlockPos bp : trapPos) {
                if (InteractionUtility.placeBlock(bp, rotate.getValue(), interact.getValue(), placeMode.getValue(), InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN), false, false)) {
                    if (bp == targetPos.add(0, 2, 0)) {
                        builtTrap = true;
                        stage = Stage.Piston;
                    }
                    break;
                }
            }
            InventoryUtility.returnSlot();
        }
    }

    public void placePiston(boolean extra) {
        if (pistonPos == null) {
            stage = Stage.Searching;
            return;
        }

        if (pistonHeadPos == null) {
            stage = Stage.Searching;
            return;
        }

        if (mc.world.getBlockState(pistonPos).getBlock() instanceof PistonBlock) {
            stage = isFire ? Stage.Fire : Stage.Crystal;
        }

        if (mc.world.getBlockState(pistonPos.down()).isReplaceable() && supportPlace.getValue()) {
            InteractionUtility.placeBlock(pistonPos.down(), rotate.getValue(), interact.getValue(), placeMode.getValue(), InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN), false, false);
            return;
        }

        final float[] angle = InteractionUtility.getPlaceAngle(pistonPos, interact.getValue(), false);
        if (angle == null) return;
        if (extra) {
            sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle[0], angle[1], mc.player.isOnGround()));
        } else {
            mc.player.setYaw(angle[0]);
            mc.player.setPitch(angle[1]);
        }


        postAction = () -> {
            int piston_slot;
            if (!InventoryUtility.findBlockInHotBar(Blocks.PISTON).found()) {
                if (!InventoryUtility.findBlockInHotBar(Blocks.STICKY_PISTON).found()) {
                    disable(isRu() ? "Нет поршней!" : "No pistons!");
                    return;
                } else {
                    piston_slot = InventoryUtility.findBlockInHotBar(Blocks.STICKY_PISTON).slot();
                }
            } else {
                piston_slot = InventoryUtility.findBlockInHotBar(Blocks.PISTON).slot();
            }


            final float angle2 = InteractionUtility.calculateAngle(pistonHeadPos.toCenterPos(), pistonPos.toCenterPos())[0];

            sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle2, 0, mc.player.isOnGround()));
            float prevYaw = mc.player.getYaw();
            mc.player.setYaw(angle2);
            mc.player.prevYaw = angle2;
            ((IClientPlayerEntity) mc.player).setLastYaw(angle2);
            int prevSlot = mc.player.getInventory().selectedSlot;
            InteractionUtility.placeBlock(pistonPos, InteractionUtility.Rotate.None, interact.getValue(), placeMode.getValue(), piston_slot, false, false);
            sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            mc.player.getInventory().selectedSlot = prevSlot;
            mc.player.setYaw(prevYaw);

            stage = isFire ? Stage.Fire : Stage.Crystal;
        };
    }

    public @Nullable BlockHitResult getPlaceData(BlockPos bp) {
        Block base = mc.world.getBlockState(bp).getBlock();
        Block freeSpace = mc.world.getBlockState(bp.up()).getBlock();
        Block legacyFreeSpace = mc.world.getBlockState(bp.up().up()).getBlock();

        if (base != Blocks.OBSIDIAN && base != Blocks.BEDROCK)
            return null;

        if (!(freeSpace == Blocks.AIR && (!oldVersion.getValue() || legacyFreeSpace == Blocks.AIR)))
            return null;

        if (checkEntities(bp)) return null;

        Vec3d crystalVec = new Vec3d(0.5f + bp.getX(), 1f + bp.getY(), 0.5f + bp.getZ());

        BlockHitResult interactResult = null;

        switch (interact.getValue()) {
            case Vanilla -> interactResult = getDefaultInteract(crystalVec, bp);
            case Strict -> interactResult = getStrictInteract(bp);
            case Legit -> interactResult = getLegitInteract(bp);
        }

        return interactResult;
    }

    private boolean checkEntities(@NotNull BlockPos base) {
        Box posBoundingBox = new Box(base.up());

        posBoundingBox = posBoundingBox.expand(0, 1f, 0);

        for (Entity ent : mc.world.getEntities()) {
            if (ent == null) continue;
            if (ent.getBoundingBox().intersects(posBoundingBox)) {
                if (ent instanceof ExperienceOrbEntity)
                    continue;
                if (ent instanceof EndCrystalEntity) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private @Nullable BlockHitResult getDefaultInteract(Vec3d crystalVector, BlockPos bp) {
        if (PlayerUtility.squaredDistanceFromEyes(crystalVector) > placeRange.getPow2Value())
            return null;

        BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), crystalVector, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));

        if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
            if (PlayerUtility.squaredDistanceFromEyes(crystalVector) > wallRange.getPow2Value())
                return null;

        return new BlockHitResult(crystalVector, Direction.DOWN, bp, false);
    }

    public @Nullable BlockHitResult getStrictInteract(@NotNull BlockPos bp) {
        float bestDistance = 999f;
        Direction bestDirection = null;
        Vec3d bestVector = null;

        if (mc.player.getEyePos().getY() > bp.up().getY()) {
            bestDirection = Direction.UP;
            bestVector = new Vec3d(bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5);
        } else if (mc.player.getEyePos().getY() < bp.getY()) {
            bestDirection = Direction.DOWN;
            bestVector = new Vec3d(bp.getX() + 0.5, bp.getY(), bp.getZ() + 0.5);
        } else {
            for (Direction dir : Direction.values()) {
                Vec3d directionVec = new Vec3d(bp.getX() + 0.5 + dir.getVector().getX() * 0.5, bp.getY() + 0.5 + dir.getVector().getY() * 0.5, bp.getZ() + 0.5 + dir.getVector().getZ() * 0.5);
                float distance = PlayerUtility.squaredDistanceFromEyes(directionVec);
                if (bestDistance > distance) {
                    bestDirection = dir;
                    bestVector = directionVec;
                    bestDistance = distance;
                }
            }
        }

        if (bestVector == null)
            return null;

        if (PlayerUtility.squaredDistanceFromEyes(bestVector) > placeRange.getPow2Value())
            return null;

        BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), bestVector, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));

        if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
            if (PlayerUtility.squaredDistanceFromEyes(bestVector) > wallRange.getPow2Value())
                return null;

        return new BlockHitResult(bestVector, bestDirection, bp, false);
    }

    public BlockHitResult getLegitInteract(BlockPos bp) {
        float bestDistance = 999f;
        BlockHitResult bestResult = null;
        for (float x = 0f; x <= 1f; x += 0.2f) {
            for (float y = 0f; y <= 1f; y += 0.2f) {
                for (float z = 0f; z <= 1f; z += 0.2f) {
                    Vec3d point = new Vec3d(bp.getX() + x, bp.getY() + y, bp.getZ() + z);
                    float distance = PlayerUtility.squaredDistanceFromEyes(point);

                    BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                    if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
                        if (distance > wallRange.getPow2Value())
                            continue;

                    BlockHitResult result = ExplosionUtility.rayCastBlock(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player), bp);
                    if (distance > placeRange.getPow2Value())
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

    public void breakCrystal() {
        for (Entity ent : mc.world.getEntities()) {
            if (!(ent instanceof EndCrystalEntity) || target.squaredDistanceTo(ent.getPos()) > 16 || ent.age < 2)
                continue;
            float[] angle = InteractionUtility.calculateAngle(ent.getPos());
            mc.player.setYaw(angle[0] + MathUtility.random(-3f, 3f));
            mc.player.setPitch(angle[1]);
            if (attackTimer.passedMs(200)) {
                mc.interactionManager.attackEntity(mc.player, ent);
                mc.player.swingHand(Hand.MAIN_HAND);
                attackTimer.reset();
            }
            lastCrystal = (EndCrystalEntity) ent;
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPacketReceive(PacketEvent.@NotNull Receive event) {
        if (event.getPacket() instanceof PlaySoundS2CPacket && ((PlaySoundS2CPacket) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS) && ((PlaySoundS2CPacket) event.getPacket()).getSound().value().equals(SoundEvents.ENTITY_GENERIC_EXPLODE)) {
            if (lastCrystal == null || !lastCrystal.isAlive())
                return;
            double soundRange = lastCrystal.squaredDistanceTo(((PlaySoundS2CPacket) event.getPacket()).getX() + 0.5, ((PlaySoundS2CPacket) event.getPacket()).getY() + 0.5, ((PlaySoundS2CPacket) event.getPacket()).getZ() + 0.5);
            if (soundRange > 121)
                return;
            reset();
        }

        if (event.getPacket() instanceof PlaySoundFromEntityS2CPacket && ((PlaySoundFromEntityS2CPacket) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS) && ((PlaySoundFromEntityS2CPacket) event.getPacket()).getSound().value().equals(SoundEvents.ENTITY_GENERIC_EXPLODE)) {
            if (lastCrystal == null || !lastCrystal.isAlive())
                return;
            if (((PlaySoundFromEntityS2CPacket) event.getPacket()).getEntityId() != lastCrystal.getId())
                return;
            reset();
        }
    }


    @EventHandler
    @SuppressWarnings("unused")
    public void onEntityRemove(EventEntityRemoved e) {
        if (lastCrystal != null && lastCrystal == e.entity)
            reset();
    }


    @Override
    public @NotNull String getDisplayInfo() {
        return stage.toString() + (target != null ? " | " + target.getName().getString() : "");
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (pistonPos == null || crystalPos == null || redStonePos == null) {
            return;
        }
        Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(new Box(pistonHeadPos.down()), Render2DEngine.injectAlpha(Color.CYAN, 100)));
        Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(new Box(crystalPos), Render2DEngine.injectAlpha(Color.PINK, 100)));
        Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(new Box(pistonPos.down()), Render2DEngine.injectAlpha(Color.GREEN, 100)));
        Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(new Box(redStonePos.down()), Render2DEngine.injectAlpha(Color.RED, 100)));

        if (firePos != null)
            Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(new Box(firePos.down()), Render2DEngine.injectAlpha(Color.yellow, 100)));
    }


    private void findPos() {
        ArrayList<Structure> list = new ArrayList<>();
        for (PlayerEntity target : Objects.requireNonNull(getPlayersSorted(targetRange.getValue()))) {
            for (int i = 0; i <= 2; i++) {
                if (patternsSetting.getValue() == Pattern.Small || patternsSetting.getValue() == Pattern.All) {
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(1, i, -1),//piston
                            new BlockPos(0, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(1, 0, -2)},//redstone
                            new BlockPos[]{new BlockPos(0, 0, 1), new BlockPos(1, i, 1), new BlockPos(0, 1 + i, 1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(1, i, 1),//piston
                            new BlockPos(0, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(1, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, -1), new BlockPos(1, i, -1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(1, i, 1),//piston
                            new BlockPos(1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(2, i, 1)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 1), new BlockPos(-1, i, 0), new BlockPos(-1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(-1, i, 1),//piston
                            new BlockPos(-1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(-2, i, 1)},//redstone
                            new BlockPos[]{new BlockPos(1, i, 1), new BlockPos(1, i, 0), new BlockPos(1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-1, i, 1),//piston
                            new BlockPos(0, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(-1, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, -1), new BlockPos(0, i, -1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-1, i, -1),//piston
                            new BlockPos(0, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(-1, i, -2)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 1), new BlockPos(0, i, 1), new BlockPos(0, 1 + i, 1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, -1),//crystal
                            new BlockPos(-1, i, -1),//piston
                            new BlockPos(-1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(-2, i, -1)},//redstone
                            new BlockPos[]{new BlockPos(1, i, 0), new BlockPos(1, i, -1), new BlockPos(1, i + 1, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, -1),//crystal
                            new BlockPos(1, i, -1),//piston
                            new BlockPos(1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(2, i, -1)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 0), new BlockPos(-1, i, -1), new BlockPos(-1, 1 + i, 0)})//fire
                    );
                }
                if (patternsSetting.getValue() == Pattern.Cross || patternsSetting.getValue() == Pattern.All) {
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(2, i, -1),//piston
                            new BlockPos(1, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(3, i, -1), new BlockPos(2, i, -2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, -1), new BlockPos(1, i, 1), new BlockPos(0, i, 1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(2, i, 1),//piston
                            new BlockPos(1, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(3, i, 1), new BlockPos(2, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, 1), new BlockPos(1, i, -1), new BlockPos(0, i, -1), new BlockPos(0, 1 + i, -1), new BlockPos(0, 1 + i, 1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(1, i, 2),//piston
                            new BlockPos(1, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(1, i, 3), new BlockPos(2, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(1, i, 0), new BlockPos(-1, i, 0), new BlockPos(-1, i, 1), new BlockPos(-1, 1 + i, 0), new BlockPos(1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(-1, i, 2),//piston
                            new BlockPos(-1, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(-1, i, 3), new BlockPos(-2, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 0), new BlockPos(1, i, 1), new BlockPos(1, i, 0), new BlockPos(-1, 1 + i, 0), new BlockPos(1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-2, i, 1),//piston
                            new BlockPos(-1, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(-3, i, 1), new BlockPos(-2, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, 1), new BlockPos(-1, i, -1), new BlockPos(0, i, -1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-2, i, -1),//piston
                            new BlockPos(-1, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(-3, i, -1), new BlockPos(-2, i, -2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, -1), new BlockPos(0, i, -1), new BlockPos(-1, i, 1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, -1),//crystal
                            new BlockPos(-1, i, -2),//piston
                            new BlockPos(-1, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(-1, i, -3), new BlockPos(-2, i, -2)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 0), new BlockPos(1, i, 0), new BlockPos(1, i, -1), new BlockPos(-1, 1 + i, 0), new BlockPos(1, 1 + i, 0)})//fire
                    );
                }
                if (patternsSetting.getValue() == Pattern.Liner || patternsSetting.getValue() == Pattern.All) {
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(2, i, 0),//piston
                            new BlockPos(1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(3, i, 0)},//redstone
                            new BlockPos[]{new BlockPos(1, i, 1), new BlockPos(1, i, -1), new BlockPos(0, i, 1),
                                    new BlockPos(0, i, -1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-2, i, 0),//piston
                            new BlockPos(-1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(-3, i, 0)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, -1), new BlockPos(-1, i, 1), new BlockPos(0, i, 1),
                                    new BlockPos(0, i, -1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(0, i, 2),//piston
                            new BlockPos(0, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(0, i, 3)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 1), new BlockPos(1, i, 1), new BlockPos(-1, i, 0),
                                    new BlockPos(1, i, 0), new BlockPos(1, 1 + i, 0), new BlockPos(-1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, -1),//crystal
                            new BlockPos(0, i, -2),//piston
                            new BlockPos(0, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(0, i, -3)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, -1), new BlockPos(1, i, -1), new BlockPos(-1, i, 0),
                                    new BlockPos(1, i, 0), new BlockPos(1, 1 + i, 0), new BlockPos(-1, 1 + i, 0)})//fire
                    );
                }
            }
            this.target = target;
        }


        if (bypass.getValue() && InventoryUtility.findItemInHotBar(Items.FLINT_AND_STEEL).found()) {
            List<Structure> bestStructure = list.stream()
                    .filter(Structure::isFirePa)
                    .sorted(Comparator.comparingDouble(Structure::getMaxRange))
                    .toList();
            if (bestStructure.isEmpty()) {
                isFire = false;
                bestStructure = list.stream()
                        .filter(Structure::isNormalPa)
                        .sorted(Comparator.comparingDouble(Structure::getMaxRange))
                        .toList();
                if (!bestStructure.isEmpty()) {
                    Structure structure = bestStructure.get(0);
                    pistonPos = structure.getPistonPos();
                    crystalPos = structure.getCrystalPos();
                    redStonePos = structure.getRedStonePos();
                    pistonHeadPos = structure.getPistonHeadPos();
                    targetPos = structure.targetPos;
                    target = structure.getTarget();
                } else {
                    disable(isRu() ? "Нет цели или места!" : "No target or free space!");
                }
            } else {
                isFire = true;
                Structure structure = bestStructure.get(0);
                pistonPos = structure.getPistonPos();
                crystalPos = structure.getCrystalPos();
                redStonePos = structure.getRedStonePos();
                firePos = structure.getFirePos();
                pistonHeadPos = structure.getPistonHeadPos();
                targetPos = structure.targetPos;
                target = structure.getTarget();
            }
        } else {
            isFire = false;
            List<Structure> bestStructure = list.stream()
                    .filter(Structure::isNormalPa)
                    .sorted(Comparator.comparingDouble(Structure::getMaxRange))
                    .toList();
            if (!bestStructure.isEmpty()) {
                Structure structure = bestStructure.getFirst();
                pistonPos = structure.getPistonPos();
                crystalPos = structure.getCrystalPos();
                redStonePos = structure.getRedStonePos();
                pistonHeadPos = structure.getPistonHeadPos();
                targetPos = structure.targetPos;
                target = structure.getTarget();
            } else {
                disable(isRu() ? "Нет цели или места!" : "No target or free space!");
            }
        }
    }


    public static @NotNull List<PlayerEntity> getPlayersSorted(float range) {
        synchronized (mc.world.getPlayers()) {
            List<PlayerEntity> playerList = new ArrayList<>();
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (mc.player != player && !Managers.FRIEND.isFriend(player) && mc.player.squaredDistanceTo(player) <= range * range) {
                    playerList.add(player);
                }
            }
            playerList.sort(Comparator.comparing(player -> mc.player.squaredDistanceTo(player)));
            return playerList;
        }
    }


    public class Structure {
        private final BlockPos pistonPos;
        private BlockPos crystalPos;
        private final BlockPos targetPos;
        private BlockPos redStonePos;
        private BlockPos firePos;
        private final PlayerEntity target;

        private BlockPos pistonHeadPos;

        public BlockPos getPistonHeadPos() {
            return pistonHeadPos;
        }

        public BlockPos getPistonPos() {
            return pistonPos;
        }

        public BlockPos getCrystalPos() {
            return crystalPos;
        }

        public BlockPos getRedStonePos() {
            return redStonePos;
        }

        public BlockPos getFirePos() {
            return firePos;
        }

        public PlayerEntity getTarget() {
            return target;
        }

        public Structure(@NotNull PlayerEntity target, @NotNull BlockPos crystalPos, @NotNull BlockPos pistonPos, @NotNull BlockPos pistonHeadPos, BlockPos[] redStonePos, BlockPos[] firePos) {
            this.target = target;
            this.targetPos = BlockPos.ofFloored(target.getPos());
            this.pistonPos = canPlace(targetPos.add(pistonPos.getX(), pistonPos.getY() + 1, pistonPos.getZ())) ? targetPos.add(pistonPos.getX(), pistonPos.getY() + 1, pistonPos.getZ()) : null;
            this.crystalPos = getPlaceData(targetPos.add(crystalPos.getX(), crystalPos.getY(), crystalPos.getZ())) != null ? targetPos.add(crystalPos.getX(), crystalPos.getY(), crystalPos.getZ()) : null;
            this.pistonHeadPos = mc.world.isAir(targetPos.add(pistonHeadPos.getX(), pistonHeadPos.getY() + 1, pistonHeadPos.getZ())) ? targetPos.add(pistonHeadPos.getX(), pistonHeadPos.getY() + 1, pistonHeadPos.getZ()) : null;

            if (this.pistonHeadPos != null && !mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(this.pistonHeadPos)).isEmpty()) {
                this.pistonHeadPos = null;
            }

            if (this.crystalPos != null && !mc.world.getNonSpectatingEntities(Entity.class, new Box(this.crystalPos)).isEmpty()) {
                this.crystalPos = null;
            }

            this.redStonePos = null;
            List<BlockPos> tempRed = Arrays.stream(redStonePos)
                    .map(blockPos -> targetPos.add(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ()))
                    .toList();
            BlockState preState = mc.world.getBlockState(pistonPos);
            mc.world.setBlockState(pistonPos, Blocks.PISTON.getDefaultState());
            for (BlockPos pos : tempRed) {
                if (canPlace(pos)) {
                    this.redStonePos = pos;
                    break;
                }
            }
            mc.world.setBlockState(pistonPos, preState);

            this.firePos = null;
            List<BlockPos> tempFire = Arrays.stream(firePos)
                    .map(blockPos -> targetPos.add(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ()))
                    .toList();
            for (BlockPos pos : tempFire) {
                if (canPlace(pos)) {
                    this.firePos = pos;
                    break;
                }
            }
        }

        public boolean isNormalPa() {
            return pistonPos != null && crystalPos != null && targetPos != null && redStonePos != null && pistonHeadPos != null;
        }

        public boolean isFirePa() {
            return pistonPos != null && crystalPos != null && targetPos != null && redStonePos != null && pistonHeadPos != null && firePos != null;
        }

        private boolean canPlace(BlockPos pos) {
            if (pos == null) return false;

            BlockState prevBlockState = null;
            if (supportPlace.getValue()) {
                prevBlockState = mc.world.getBlockState(pos.down());
                if (prevBlockState.isReplaceable()) {
                    mc.world.setBlockState(pos.down(), Blocks.OBSIDIAN.getDefaultState());
                } else prevBlockState = null;
            }

            boolean canPlace =  InteractionUtility.canPlaceBlock(pos, interact.getValue(), false);

            if (prevBlockState != null) {
                mc.world.setBlockState(pos.down(), prevBlockState);
            }

            return canPlace;
        }

        public double getMaxRange() {
            if (this.pistonPos == null || this.crystalPos == null || this.redStonePos == null) return 999;
            final double piston = InteractionUtility.squaredDistanceFromEyes(this.pistonPos.toCenterPos());
            final double crystal = InteractionUtility.squaredDistanceFromEyes(this.crystalPos.toCenterPos());
            final double redStone = InteractionUtility.squaredDistanceFromEyes(this.redStonePos.toCenterPos());

            BlockPos firePos = this.firePos != null ? this.firePos : this.pistonPos;
            final double fire = InteractionUtility.squaredDistanceFromEyes(firePos.toCenterPos());
            return Math.max(Math.max(fire, crystal), Math.max(redStone, piston));
        }
    }

    public enum Stage {
        Searching,
        Trap,
        Piston,
        Fire,
        Crystal,
        RedStone,
        Break
    }

    public enum Pattern {
        Small,
        Cross,
        Liner,
        All
    }
}
