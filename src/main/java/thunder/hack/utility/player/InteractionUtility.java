package thunder.hack.utility.player;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.cmd.Command;
import thunder.hack.utility.math.ExplosionUtility;

import java.util.*;

import static thunder.hack.modules.Module.mc;

public final class InteractionUtility {
    private static final List<Block> SHIFT_BLOCKS = Arrays.asList(
            Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE,
            Blocks.BIRCH_TRAPDOOR, Blocks.BAMBOO_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.CHERRY_TRAPDOOR,
            Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER,
            Blocks.ACACIA_TRAPDOOR, Blocks.ENCHANTING_TABLE, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX
    );

    public static Map<BlockPos, Long> awaiting = new HashMap<>();

    public static boolean canSee(Entity entity) {
        Vec3d entityEyes = getEyesPos(entity);
        Vec3d entityPos = entity.getPos();
        return canSee(entityEyes, entityPos);
    }

    public static boolean canSee(Vec3d entityEyes, Vec3d entityPos) {
        if (mc.player == null || mc.world == null) return false;

        Vec3d playerEyes = getEyesPos(mc.player);
        if (mc.world.raycast(new RaycastContext(playerEyes, entityEyes, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS)
            return true;

        if (playerEyes.getY() > entityPos.getY())
            return mc.world.raycast(new RaycastContext(playerEyes, entityPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS;
        return false;
    }

    public static Vec3d getEyesPos(@NotNull Entity entity) {
        return entity.getPos().add(0, entity.getEyeHeight(entity.getPose()), 0);
    }

    public static float @NotNull [] calculateAngle(Vec3d to) {
        return calculateAngle(getEyesPos(mc.player), to);
    }

    public static float @NotNull [] calculateAngle(@NotNull Vec3d from, @NotNull Vec3d to) {
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));

        float yD = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
        float pD = (float) MathHelper.clamp(MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist))), -90f, 90f);

        return new float[]{yD, pD};
    }

    public static boolean placeBlock(BlockPos bp, boolean rotate, Interact interact, PlaceMode mode, int slot, boolean returnSlot, boolean ignoreEntities) {
        int prevItem = mc.player.getInventory().selectedSlot;
        if (slot != -1) InventoryUtility.switchTo(slot);
        else return false;

        boolean result = placeBlock(bp, rotate, interact, mode, ignoreEntities);

        if (returnSlot) InventoryUtility.switchTo(prevItem);
        return result;
    }

    public static boolean placeBlock(BlockPos bp, boolean rotate, Interact interact, PlaceMode mode, @NotNull SearchInvResult invResult, boolean returnSlot, boolean ignoreEntities) {
        int prevItem = mc.player.getInventory().selectedSlot;
        invResult.switchTo();
        boolean result = placeBlock(bp, rotate, interact, mode, ignoreEntities);
        if (returnSlot) InventoryUtility.switchTo(prevItem);

        return result;
    }

    public static boolean placeBlock(BlockPos bp, boolean rotate, Interact interact, PlaceMode mode, boolean ignoreEntities) {
        BlockHitResult result = getPlaceResult(bp, interact, ignoreEntities);
        if (result == null || mc.world == null || mc.interactionManager == null || mc.player == null) return false;

        boolean sprint = mc.player.isSprinting();
        boolean sneak = needSneak(mc.world.getBlockState(result.getBlockPos()).getBlock()) && !mc.player.isSneaking();

        if (sprint)
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        if (sneak)
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));

        float[] angle = calculateAngle(result.getPos());
        if (rotate)
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle[0], angle[1], mc.player.isOnGround()));

        if (mode == PlaceMode.Normal)
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);

        if (mode == PlaceMode.Packet)
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, PlayerUtility.getWorldActionId(mc.world)));

        awaiting.put(bp, System.currentTimeMillis());

        if (sneak)
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        if (sprint)
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));

        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        return true;
    }

    public static boolean canPlaceBlock(@NotNull BlockPos bp, Interact interact, boolean ignoreEntities) {
        if (awaiting.containsKey(bp)) return false;
        return getPlaceResult(bp, interact, ignoreEntities) != null;
    }

    public static float @Nullable [] getPlaceAngle(@NotNull BlockPos bp, Interact interact, boolean ignoreEntities) {
        BlockHitResult result = getPlaceResult(bp, interact, ignoreEntities);
        if (result != null) return calculateAngle(result.getPos());
        return null;
    }

    @Nullable
    public static BlockHitResult getPlaceResult(@NotNull BlockPos bp, Interact interact, boolean ignoreEntities) {
        if (!ignoreEntities)
            for (Entity entity : new ArrayList<>(mc.world.getNonSpectatingEntities(Entity.class, new Box(bp))))
                if (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrbEntity))
                    return null;

        if (!mc.world.getBlockState(bp).isReplaceable()) {
            return null;
        }

        if(interact == Interact.AirPlace) {
            return ExplosionUtility.rayCastBlock(new RaycastContext(InteractionUtility.getEyesPos(mc.player), bp.toCenterPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player), bp);
        }

        ArrayList<BlockPosWithFacing> supports = getSupportBlocks(bp);
        for (BlockPosWithFacing support : supports) {
            if (interact != Interact.Vanilla) {
                if (getStrictDirections(bp).isEmpty())
                    return null;
                if (!getStrictDirections(bp).contains(support.facing))
                    continue;
            }
            BlockHitResult result = null;
            if (interact != Interact.Legit) {
                Vec3d directionVec = new Vec3d(support.position.getX() + 0.5 + support.facing.getVector().getX() * 0.5, support.position.getY() + 0.5 + support.facing.getVector().getY() * 0.5, support.position.getZ() + 0.5 + support.facing.getVector().getZ() * 0.5);
                result = new BlockHitResult(directionVec, support.facing, support.position, false);
            } else {
                Vec3d p = getVisibleDirectionPoint(support.facing, support.position);
                if (p != null) {
                    return new BlockHitResult(p, support.facing, support.position, false);
                }
            }
            return result;
        }
        return null;
    }


    public static @NotNull ArrayList<BlockPosWithFacing> getSupportBlocks(@NotNull BlockPos bp) {
        ArrayList<BlockPosWithFacing> list = new ArrayList<>();

        if (mc.world.getBlockState(bp.add(0, -1, 0)).isSolid() || awaiting.containsKey(bp.add(0, -1, 0)))
            list.add(new BlockPosWithFacing(bp.add(0, -1, 0), Direction.UP));

        if (mc.world.getBlockState(bp.add(0, 1, 0)).isSolid() || awaiting.containsKey(bp.add(0, 1, 0)))
            list.add(new BlockPosWithFacing(bp.add(0, 1, 0), Direction.DOWN));

        if (mc.world.getBlockState(bp.add(-1, 0, 0)).isSolid() || awaiting.containsKey(bp.add(-1, 0, 0)))
            list.add(new BlockPosWithFacing(bp.add(-1, 0, 0), Direction.EAST));

        if (mc.world.getBlockState(bp.add(1, 0, 0)).isSolid() || awaiting.containsKey(bp.add(1, 0, 0)))
            list.add(new BlockPosWithFacing(bp.add(1, 0, 0), Direction.WEST));

        if (mc.world.getBlockState(bp.add(0, 0, 1)).isSolid() || awaiting.containsKey(bp.add(0, 0, 1)))
            list.add(new BlockPosWithFacing(bp.add(0, 0, 1), Direction.NORTH));

        if (mc.world.getBlockState(bp.add(0, 0, -1)).isSolid() || awaiting.containsKey(bp.add(0, 0, -1)))
            list.add(new BlockPosWithFacing(bp.add(0, 0, -1), Direction.SOUTH));

        return list;
    }

    public static @Nullable BlockPosWithFacing checkNearBlocks(@NotNull BlockPos blockPos) {
        if (mc.world.getBlockState(blockPos.add(0, -1, 0)).isSolid())
            return new BlockPosWithFacing(blockPos.add(0, -1, 0), Direction.UP);
        else if (mc.world.getBlockState(blockPos.add(-1, 0, 0)).isSolid())
            return new BlockPosWithFacing(blockPos.add(-1, 0, 0), Direction.EAST);
        else if (mc.world.getBlockState(blockPos.add(1, 0, 0)).isSolid())
            return new BlockPosWithFacing(blockPos.add(1, 0, 0), Direction.WEST);
        else if (mc.world.getBlockState(blockPos.add(0, 0, 1)).isSolid())
            return new BlockPosWithFacing(blockPos.add(0, 0, 1), Direction.NORTH);
        else if (mc.world.getBlockState(blockPos.add(0, 0, -1)).isSolid())
            return new BlockPosWithFacing(blockPos.add(0, 0, -1), Direction.SOUTH);
        return null;
    }

    public static float squaredDistanceFromEyes(@NotNull Vec3d vec) {
        double d0 = vec.x - mc.player.getX();
        double d1 = vec.z - mc.player.getZ();
        double d2 = vec.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        return (float) (d0 * d0 + d1 * d1 + d2 * d2);
    }

    public static @NotNull List<Direction> getStrictDirections(@NotNull BlockPos bp) {
        List<Direction> visibleSides = new ArrayList<>();
        Vec3d positionVector = bp.toCenterPos();

        double westDelta = getEyesPos(mc.player).x - (positionVector.add(0.5, 0, 0).x);
        double eastDelta = getEyesPos(mc.player).x - (positionVector.add(-0.5, 0, 0).x);
        double northDelta = getEyesPos(mc.player).z - (positionVector.add(0, 0, 0.5).z);
        double southDelta = getEyesPos(mc.player).z - (positionVector.add(0, 0, -0.5).z);
        double upDelta = getEyesPos(mc.player).y - (positionVector.add(0, 0.5, 0).y);
        double downDelta = getEyesPos(mc.player).y - (positionVector.add(0, -0.5, 0).y);

        if (westDelta > 0 && !mc.world.getBlockState(bp.west()).isReplaceable()) visibleSides.add(Direction.EAST);
        if (westDelta < 0 && !mc.world.getBlockState(bp.east()).isReplaceable()) visibleSides.add(Direction.WEST);
        if (eastDelta < 0 && !mc.world.getBlockState(bp.east()).isReplaceable()) visibleSides.add(Direction.WEST);
        if (eastDelta > 0 && !mc.world.getBlockState(bp.west()).isReplaceable()) visibleSides.add(Direction.EAST);

        if (northDelta > 0 && !mc.world.getBlockState(bp.north()).isReplaceable()) visibleSides.add(Direction.SOUTH);
        if (northDelta < 0 && !mc.world.getBlockState(bp.south()).isReplaceable()) visibleSides.add(Direction.NORTH);
        if (southDelta < 0 && !mc.world.getBlockState(bp.south()).isReplaceable()) visibleSides.add(Direction.NORTH);
        if (southDelta > 0 && !mc.world.getBlockState(bp.north()).isReplaceable()) visibleSides.add(Direction.SOUTH);

        if (upDelta > 0 && !mc.world.getBlockState(bp.down()).isReplaceable()) visibleSides.add(Direction.UP);
        if (upDelta < 0 && !mc.world.getBlockState(bp.up()).isReplaceable()) visibleSides.add(Direction.DOWN);
        if (downDelta < 0 && !mc.world.getBlockState(bp.up()).isReplaceable()) visibleSides.add(Direction.DOWN);
        if (downDelta > 0 && !mc.world.getBlockState(bp.down()).isReplaceable()) visibleSides.add(Direction.UP);

        return visibleSides;
    }

    public static @NotNull List<Direction> getStrictBlockDirections(@NotNull BlockPos bp) {
        List<Direction> visibleSides = new ArrayList<>();
        Vec3d pV = bp.toCenterPos();

        double westDelta = getEyesPos(mc.player).x - (pV.add(0.5, 0, 0).x);
        double eastDelta = getEyesPos(mc.player).x - (pV.add(-0.5, 0, 0).x);
        double northDelta = getEyesPos(mc.player).z - (pV.add(0, 0, 0.5).z);
        double southDelta = getEyesPos(mc.player).z - (pV.add(0, 0, -0.5).z);
        double upDelta = getEyesPos(mc.player).y - (pV.add(0, 0.5, 0).y);
        double downDelta = getEyesPos(mc.player).y - (pV.add(0, -0.5, 0).y);

        if(westDelta > 0 && mc.world.getBlockState(bp.east()).isReplaceable())
            visibleSides.add(Direction.EAST);

        if(eastDelta < 0 && mc.world.getBlockState(bp.west()).isReplaceable())
            visibleSides.add(Direction.WEST);

        if(northDelta > 0 && mc.world.getBlockState(bp.south()).isReplaceable())
            visibleSides.add(Direction.SOUTH);

        if(southDelta < 0 && mc.world.getBlockState(bp.north()).isReplaceable())
            visibleSides.add(Direction.NORTH);

        if(upDelta > 0 && mc.world.getBlockState(bp.up()).isReplaceable())
            visibleSides.add(Direction.UP);

        if(downDelta < 0 && mc.world.getBlockState(bp.down()).isReplaceable())
            visibleSides.add(Direction.DOWN);

        return visibleSides;
    }

    public static @Nullable BreakData getBreakData(BlockPos bp, Interact interact) {
        if (interact == Interact.Vanilla) return new BreakData(Direction.UP, bp.toCenterPos().add(0, 0.5, 0));
        if (interact == Interact.Strict) {
            float bestDistance = 999f;
            Direction bestDirection = Direction.UP;
            Vec3d bestVector = null;

            for (Direction dir : Direction.values()) {
                Vec3d directionVec = new Vec3d(bp.getX() + 0.5 + dir.getVector().getX() * 0.5, bp.getY() + 0.5 + dir.getVector().getY() * 0.5, bp.getZ() + 0.5 + dir.getVector().getZ() * 0.5);
                float distance = squaredDistanceFromEyes(directionVec);
                if (bestDistance > distance) {
                    bestDirection = dir;
                    bestVector = directionVec;
                    bestDistance = distance;
                }
            }

            if (bestVector == null) return null;
            return new BreakData(bestDirection, bestVector);
        }

        if (interact == Interact.Legit) {
            float bestDistance = 999f;
            BreakData bestData = null;
            for (float x = 0f; x <= 1f; x += 0.2f) {
                for (float y = 0f; y <= 1; y += 0.2f) {
                    for (float z = 0f; z <= 1; z += 0.2f) {
                        Vec3d point = new Vec3d(bp.getX() + x, bp.getY() + y, bp.getZ() + z);
                        BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                        if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
                            continue;
                        BlockHitResult result = ExplosionUtility.rayCastBlock(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player), bp);

                        if (squaredDistanceFromEyes(point) < bestDistance)
                            if (result != null && result.getType() == HitResult.Type.BLOCK)
                                bestData = new BreakData(result.getSide(), result.getPos());
                    }
                }
            }
            if (bestData == null) return null;
            if (bestData.vector == null || bestData.dir == null) return null;
            return bestData;
        }
        return null;
    }

    public static @Nullable Vec3d getVisibleDirectionPoint(Direction dir, BlockPos bp) {
        Box brutBox = null;
        if (dir == Direction.DOWN) brutBox = new Box(new Vec3d(0.1f, 0f, 0.1f), new Vec3d(0.9f, 0f, 0.9f));
        if (dir == Direction.NORTH) brutBox = new Box(new Vec3d(0.1f, 0.1f, 0f), new Vec3d(0.9f, 0.9f, 0f));
        if (dir == Direction.EAST) brutBox = new Box(new Vec3d(1f, 0.1f, 0.1f), new Vec3d(1f, 0.9f, 0.9f));
        if (dir == Direction.SOUTH) brutBox = new Box(new Vec3d(0.1f, 0.1f, 1f), new Vec3d(0.9f, 0.9f, 1f));
        if (dir == Direction.WEST) brutBox = new Box(new Vec3d(0f, 0.1f, 0.1f), new Vec3d(0f, 0.9f, 0.9f));
        if (dir == Direction.UP) brutBox = new Box(new Vec3d(0.1f, 1f, 0.1f), new Vec3d(0.9f, 1f, 0.9f));

        if (brutBox == null) {
            Command.sendMessage("err");
            return null;
        }

        if (brutBox.maxX - brutBox.minX == 0) {
            for (double y = brutBox.minY; y < brutBox.maxY; y += 0.1f) {
                for (double z = brutBox.minZ; z < brutBox.maxZ; z += 0.1f) {
                    Vec3d point = new Vec3d(bp.getX() + brutBox.minX, bp.getY() + y, bp.getZ() + z);
                    BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                    if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
                        continue;
                    return point;
                }
            }
        }
        if (brutBox.maxY - brutBox.minY == 0) {
            for (double x = brutBox.minX; x < brutBox.maxX; x += 0.1f) {
                for (double z = brutBox.minZ; z < brutBox.maxZ; z += 0.1f) {
                    Vec3d point = new Vec3d(bp.getX() + x, bp.getY() + brutBox.minY, bp.getZ() + z);
                    BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                    if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
                        continue;
                    return point;
                }

            }
        }
        if (brutBox.maxZ - brutBox.minZ == 0) {
            for (double x = brutBox.minX; x < brutBox.maxX; x += 0.1f) {
                for (double y = brutBox.minY; y < brutBox.maxY; y += 0.1f) {
                    Vec3d point = new Vec3d(bp.getX() + x, bp.getY() + y, bp.getZ() + brutBox.minZ);
                    BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                    if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
                        continue;
                    return point;

                }
            }
        }
        return null;
    }

    public static boolean needSneak(Block in) {
        return SHIFT_BLOCKS.contains(in);
    }

    public record BlockPosWithFacing(BlockPos position, Direction facing) {
    }

    public record BreakData(Direction dir, Vec3d vector) {
    }

    public enum PlaceMode {
        Packet,
        Normal
    }

    public enum Interact {
        Vanilla,
        Strict,
        Legit,
        AirPlace
    }
}
