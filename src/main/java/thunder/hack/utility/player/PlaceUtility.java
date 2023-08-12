package thunder.hack.utility.player;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import thunder.hack.core.PlaceManager;
import thunder.hack.utility.math.Placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

import static thunder.hack.modules.Module.mc;

public final class PlaceUtility {
    public static ConcurrentHashMap<BlockPos, Long> ghostBlocks = new ConcurrentHashMap<>();

    public static final Vec3d[] multiPoint = new Vec3d[]{
            // z
            new Vec3d(0.05, 0.05, 0),
            new Vec3d(0.05, 0.95, 0),
            new Vec3d(0.95, 0.05, 0),
            new Vec3d(0.95, 0.95, 0),
            new Vec3d(0.5, 0.5, 0),
            new Vec3d(0.05, 0.05, 1),
            new Vec3d(0.05, 0.95, 1),
            new Vec3d(0.95, 0.05, 1),
            new Vec3d(0.95, 0.95, 1),
            new Vec3d(0.5, 0.5, 1),
            // y
            new Vec3d(0.05, 0, 0.05),
            new Vec3d(0.05, 0, 0.95),
            new Vec3d(0.95, 0, 0.05),
            new Vec3d(0.95, 0, 0.95),
            new Vec3d(0.5, 0, 0.5),
            new Vec3d(0.05, 1, 0.05),
            new Vec3d(0.05, 1, 0.95),
            new Vec3d(0.95, 1, 0.05),
            new Vec3d(0.95, 1, 0.95),
            new Vec3d(0.5, 1, 0.5),
            // x
            new Vec3d(0, 0.05, 0.05),
            new Vec3d(0, 0.95, 0.05),
            new Vec3d(0, 0.05, 0.95),
            new Vec3d(0, 0.95, 0.95),
            new Vec3d(0, 0.5, 0.5),
            new Vec3d(1, 0.05, 0.05),
            new Vec3d(1, 0.95, 0.05),
            new Vec3d(1, 0.05, 0.95),
            new Vec3d(1, 0.95, 0.95),
            new Vec3d(1, 0.5, 0.5)
    };

    public static final Vec3d[] fastMultiPoint = new Vec3d[]{
            new Vec3d(0.05, 0.05, 0.05),
            new Vec3d(0.05, 0.95, 0.05),
            new Vec3d(0.05, 0.05, 0.95),
            new Vec3d(0.95, 0.05, 0.05),
            new Vec3d(0.95, 0.95, 0.05),
            new Vec3d(0.05, 0.95, 0.95),
            new Vec3d(0.95, 0.95, 0.95),
            new Vec3d(0.95, 0.05, 0.95)
    };

    public static final Vec3d[] halfMultiPoint = new Vec3d[]{
            // z
            new Vec3d(0.05, 0.05, 0),
            new Vec3d(0.05, 0.45, 0),
            new Vec3d(0.95, 0.05, 0),
            new Vec3d(0.95, 0.45, 0),
            new Vec3d(0.5, 0.24, 0),
            new Vec3d(0.05, 0.05, 1),
            new Vec3d(0.05, 0.45, 1),
            new Vec3d(0.95, 0.05, 1),
            new Vec3d(0.95, 0.45, 1),
            new Vec3d(0.5, 0.24, 1),
            // y
            new Vec3d(0.05, 0, 0.05),
            new Vec3d(0.05, 0, 0.95),
            new Vec3d(0.95, 0, 0.05),
            new Vec3d(0.95, 0, 0.95),
            new Vec3d(0.5, 0, 0.5),
            new Vec3d(0.05, 0.4, 0.05),
            new Vec3d(0.05, 0.4, 0.95),
            new Vec3d(0.95, 0.4, 0.05),
            new Vec3d(0.95, 0.4, 0.95),
            new Vec3d(0.5, 0.4, 0.5),
            // x
            new Vec3d(0, 0.05, 0.05),
            new Vec3d(0, 0.45, 0.05),
            new Vec3d(0, 0.05, 0.95),
            new Vec3d(0, 0.45, 0.95),
            new Vec3d(0, 0.24, 0.5),
            new Vec3d(1, 0.05, 0.05),
            new Vec3d(1, 0.45, 0.05),
            new Vec3d(1, 0.05, 0.95),
            new Vec3d(1, 0.45, 0.95),
            new Vec3d(1, 0.24, 0.5)
    };

    public static float[] rotationToDirection(Direction facing) {
        switch (facing) {
            case DOWN -> {
                return new float[]{mc.player.getYaw(), 90.0f};
            }
            case UP -> {
                return new float[]{mc.player.getYaw(), -90.0f};
            }
            case NORTH -> {
                return new float[]{180.0f, 0.0f};
            }
            case SOUTH -> {
                return new float[]{0.0f, 0.0f};
            }
            case WEST -> {
                return new float[]{90.0f, 0.0f};
            }
        }
        return new float[]{270.0f, 0.0f};
    }

    public static boolean canSee(Entity entity) {
        Vec3d entityEyes = getEyesPos(entity);
        Vec3d entityPos = entity.getPos();
        return canSee(entityEyes, entityPos);
    }

    public static boolean canSee(Vec3d entityEyes, Vec3d entityPos) {
        Vec3d playerEyes = getEyesPos(mc.player);

        if (mc.world.raycast(new RaycastContext(playerEyes, entityEyes, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS) {
            return true;
        }

        if (playerEyes.getY() > entityPos.getY()) {
            if (mc.world.raycast(new RaycastContext(playerEyes, entityPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS) {
                return true;
            }
        }

        return false;
    }

    public static boolean place(BlockPos pos, boolean rotate, boolean strictDirection, Hand hand, int slot, boolean ignoreEntities, PlaceMode mode) {
        if (!canPlaceBlock(pos, strictDirection, ignoreEntities)) return false;
        Direction side = getPlaceDirection(pos, strictDirection);
        if (side == null) return false;
        BlockPos neighbour = pos.offset(side);
        Direction opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour.getX() + 0.5, neighbour.getY() + 0.5, neighbour.getZ() + 0.5).add(new Vec3d(opposite.getUnitVector()).multiply(0.5));
        float[] angle = calculateAngle(hitVec);
        PlaceManager.add(new Placement(neighbour, opposite, angle[0], angle[1], hand, rotate, slot, mode));
        return true;
    }

    public static boolean place(BlockPos pos, boolean rotate, boolean strictDirection, Hand hand, int slot, boolean ignoreEntities) {
        return place(pos, rotate, strictDirection, hand, slot, ignoreEntities, PlaceMode.All);
    }

    public static float[] calcAngle(BlockPos pos, boolean strictDirection, boolean ignoreEntities) {
        if (!canPlaceBlock(pos, strictDirection, !ignoreEntities)) {
            return null;
        }
        Direction side = getPlaceDirection(pos, strictDirection);

        if (side == null) {
            return null;
        }

        BlockPos neighbour = pos.offset(side);
        Direction opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour.getX() + 0.5, neighbour.getY() + 0.5, neighbour.getZ() + 0.5).add(new Vec3d(opposite.getUnitVector()).multiply(0.5));
        return calculateAngle(hitVec);
    }

    public static boolean forcePlace(BlockPos pos, boolean strictDirection, Hand hand, int slot, boolean ignoreEntities, PlaceMode mode) {
        if (!canPlaceBlock(pos, strictDirection, !ignoreEntities)) return false;
        if (!mc.world.getBlockState(pos).isReplaceable()) return false;
        Direction side = Direction.DOWN;
        if (strictDirection) side = getPlaceDirection(pos, true);
        if (side == null) return false;
        BlockPos neighbour = pos.offset(side);
        Direction opposite = side.getOpposite();
        new Placement(neighbour, opposite, 0, 0, hand, false, slot, mode).getAction().run();
        ghostBlocks.put(pos, System.currentTimeMillis());
        return true;
    }

    public static boolean forcePlace(BlockPos pos, boolean strictDirection, Hand hand, int slot, boolean ignoreEntities) {
        return forcePlace(pos, strictDirection, hand, slot, ignoreEntities, PlaceMode.All);
    }

    public static boolean canPlaceBlock(BlockPos pos, boolean strictDirection) {
        return canPlaceBlock(pos, strictDirection, true);
    }

    public static boolean canPlaceBlock(BlockPos pos, boolean strictDirection, boolean checkEntities) {
        if(pos == null) return false;
        if (!mc.world.getBlockState(pos).isReplaceable()) {
            return false;
        }
        if (checkEntities) {
            for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
                if (entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity) continue;
                return false;
            }
        }
        return getPlaceDirection(pos, strictDirection) != null;
    }

    public static boolean canClick(BlockPos pos) {
        if (ghostBlocks.containsKey(pos)) {
            if (System.currentTimeMillis() - ghostBlocks.get(pos) > 500) {
                ghostBlocks.remove(pos);
            } else {
                return true;
            }
        }

        BlockState state = mc.world.getBlockState(pos);

        return (!state.isReplaceable() && state.getFluidState().isEmpty());
    }

    public static Direction getPlaceDirection(BlockPos pos, boolean strictDirection) {
        ArrayList<Direction> validFacings = new ArrayList<>();
        for (Direction side : Direction.values()) {
            BlockPos neighbour = pos.offset(side);

            if (!canClick(neighbour)) continue;

            if (strictDirection) {
                Vec3d eyePos = getEyesPos(mc.player);
                Vec3d blockCenter = new Vec3d(neighbour.getX() + 0.5, neighbour.getY() + 0.5, neighbour.getZ() + 0.5);
                BlockState blockState = mc.world.getBlockState(neighbour);
                boolean isFullBox = blockState.getBlock() == Blocks.AIR || blockState.isFullCube(mc.world, neighbour);
                ArrayList<Direction> validAxis = new ArrayList<>();
                validAxis.addAll(checkAxis(eyePos.x - blockCenter.x, Direction.WEST, Direction.EAST, !isFullBox));
                validAxis.addAll(checkAxis(eyePos.y - blockCenter.y, Direction.DOWN, Direction.UP, true));
                validAxis.addAll(checkAxis(eyePos.z - blockCenter.z, Direction.NORTH, Direction.SOUTH, !isFullBox));
                if (!validAxis.contains(side.getOpposite())) continue;
            }
            validFacings.add(side);
        }
        return validFacings.stream().min(Comparator.comparing(facing -> getEyesPos(mc.player).distanceTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).add(new Vec3d(facing.getUnitVector()).multiply(0.5))))).orElse(null);
    }

    public static Direction getBreakDirection(BlockPos pos, boolean strictDirection) {
        if (!strictDirection) {
            return Direction.UP;
        }
        ArrayList<Direction> validFacings = new ArrayList<>();
        validFacings.addAll(Arrays.asList(Direction.values()));
        return validFacings.stream().min(Comparator.comparing(facing -> getEyesPos(mc.player).distanceTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).add(new Vec3d(facing.getUnitVector()).multiply(0.5))))).orElse(null);
    }

    public static ArrayList<Direction> checkAxis(double diff, Direction negativeSide, Direction positiveSide, boolean bothIfInRange) {
        ArrayList<Direction> valid = new ArrayList<>();
        if (diff < -0.5) {
            valid.add(negativeSide);
        }
        if (diff > 0.5) {
            valid.add(positiveSide);
        }
        if (bothIfInRange) {
            if (!valid.contains(negativeSide)) valid.add(negativeSide);
            if (!valid.contains(positiveSide)) valid.add(positiveSide);
        }
        return valid;
    }

    public static Vec3d getEyesPos(Entity entity) {
        return entity.getPos().add(0, entity.getEyeHeight(entity.getPose()), 0);
    }

    public static float[] calculateAngle(Vec3d to) {
        return calculateAngle(getEyesPos(mc.player), to);
    }

    public static float[] calculateAngle(Vec3d from, Vec3d to) {
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));
        float yD = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
        float pD = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)));
        if (pD > 90F) {
            pD = 90F;
        } else if (pD < -90F) {
            pD = -90F;
        }
        return new float[]{yD, pD};
    }

    public static float[] getRotationForPlace(BlockPos bp, boolean strictDirection) {
        if (!mc.world.getBlockState(bp).isReplaceable()) return null;

        ArrayList<BlockPosWithFacing> neighbours = new ArrayList<>();
        neighbours.add(new BlockPosWithFacing(bp.up(), Direction.DOWN));
        neighbours.add(new BlockPosWithFacing(bp.west(), Direction.EAST));
        neighbours.add(new BlockPosWithFacing(bp.east(), Direction.WEST));
        neighbours.add(new BlockPosWithFacing(bp.north(), Direction.SOUTH));
        neighbours.add(new BlockPosWithFacing(bp.south(), Direction.NORTH));
        neighbours.add(new BlockPosWithFacing(bp.down(), Direction.UP));

        for (BlockPosWithFacing bp2 : neighbours) {
            if (mc.world.getBlockState(bp2.position).isReplaceable()) continue;
            for (Vec3d point : getDirectionPoints(bp2.facing)) {
                Vec3d p = new Vec3d(bp2.position.getX() + point.getX(), bp2.position.getY() + point.getY(), bp2.position.getZ() + point.getZ());
                BlockHitResult result = mc.world.raycast(new RaycastContext(getEyesPos(mc.player), p, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ((mc.player))));
                if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(bp2.position) && result.getSide().equals(bp2.facing)) {
                    return calculateAngle(p);
                }
            }
        }
        return null;
    }

    public static boolean placeBlock(BlockPos bp, boolean strictDirection) {
        if (!mc.world.getBlockState(bp).isReplaceable()) return false;

        if (strictDirection) {
            ArrayList<BlockPosWithFacing> neighbours = new ArrayList<>();
            neighbours.add(new BlockPosWithFacing(bp.up(), Direction.DOWN));
            neighbours.add(new BlockPosWithFacing(bp.west(), Direction.EAST));
            neighbours.add(new BlockPosWithFacing(bp.east(), Direction.WEST));
            neighbours.add(new BlockPosWithFacing(bp.north(), Direction.SOUTH));
            neighbours.add(new BlockPosWithFacing(bp.south(), Direction.NORTH));
            neighbours.add(new BlockPosWithFacing(bp.down(), Direction.UP));

            for (BlockPosWithFacing bp2 : neighbours) {
                if (mc.world.getBlockState(bp2.position).isReplaceable()) continue;
                for (Vec3d point : getDirectionPoints(bp2.facing)) {
                    Vec3d p = new Vec3d(bp2.position.getX() + point.getX(), bp2.position.getY() + point.getY(), bp2.position.getZ() + point.getZ());
                    BlockHitResult result = mc.world.raycast(new RaycastContext(getEyesPos(mc.player), p, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ((mc.player))));
                    if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(bp2.position)) {
                        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, PlayerUtility.getWorldActionId(mc.world)));
                        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                        break;
                    }
                }
            }
        } else {
            BlockPos bp2 = bp.down();
            Vec3d p = new Vec3d(bp2.getX() + 0.5, bp2.getY() + 1f, bp2.getZ() + 0.5f);
            BlockHitResult result = new BlockHitResult(p, Direction.UP, bp2, false);
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, PlayerUtility.getWorldActionId(mc.world)));
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        return false;
    }

    public static Vec3d[] getDirectionPoints(Direction dir) {
        if (dir == Direction.DOWN) {
            return new Vec3d[]{
                    new Vec3d(0.05, 0.05, 0.05),
                    new Vec3d(0.95, 0.05, 0.05),
                    new Vec3d(0.95, 0.05, 0.95),
                    new Vec3d(0.05, 0.05, 0.95),
                    new Vec3d(0.5, 0.05, 0.05),
                    new Vec3d(0.5, 0.05, 0.5),
                    new Vec3d(0.05, 0.05, 0.5)
            };
        } else if (dir == Direction.NORTH) {
            return new Vec3d[]{
                    new Vec3d(0.05, 0.05, 0.05),
                    new Vec3d(0.05, 0.95, 0.05),
                    new Vec3d(0.95, 0.95, 0.05),
                    new Vec3d(0.95, 0.05, 0.05),
                    new Vec3d(0.05, 0.5, 0.05),
                    new Vec3d(0.5, 0.5, 0.05),
                    new Vec3d(0.5, 0.05, 0.05)
            };
        } else if (dir == Direction.EAST) {
            return new Vec3d[]{
                    new Vec3d(0.95, 0.05, 0.05),
                    new Vec3d(0.95, 0.95, 0.05),
                    new Vec3d(0.95, 0.95, 0.95),
                    new Vec3d(0.95, 0.05, 0.95),
                    new Vec3d(0.5, 0.05, 0),
                    new Vec3d(0.5, 0.5, 0),
                    new Vec3d(0.5, 0.5, 0.5),
                    new Vec3d(0.5, 0.05, 0.5)
            };
        } else if (dir == Direction.SOUTH) {
            return new Vec3d[]{
                    new Vec3d(0.05, 0.05, 1),
                    new Vec3d(0.95, 0.05, 1),
                    new Vec3d(0.05, 0.95, 1),
                    new Vec3d(0.95, 0.95, 1),

            };
        } else if (dir == Direction.WEST) {
            return new Vec3d[]{
                    new Vec3d(0.05, 0.05, 0.05),
                    new Vec3d(0.05, 0.05, 0.95),
                    new Vec3d(0.05, 0.95, 0.95),
                    new Vec3d(0.05, 0.95, 0.05),
                    new Vec3d(0.05, 0.05, 0.5),
                    new Vec3d(0.05, 0.5, 0.5),
                    new Vec3d(0.05, 0.5, 0.05)
            };
        } else {
            return new Vec3d[]{
                    new Vec3d(0.05, 0.95, 0.05),
                    new Vec3d(0.95, 0.95, 0.05),
                    new Vec3d(0.95, 0.95, 0.95),
                    new Vec3d(0.05, 0.95, 0.95),
                    new Vec3d(0.5, 0.95, 0.05),
                    new Vec3d(0.5, 0.95, 0.5),
                    new Vec3d(0.05, 0.95, 0.5)
            };
        }
    }

    public record BlockPosWithFacing(BlockPos position, Direction facing) {
    }

    public enum PlaceMode {
        Packet,
        Normal,
        All
    }
}
