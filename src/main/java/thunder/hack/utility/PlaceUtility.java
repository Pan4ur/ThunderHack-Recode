package thunder.hack.utility;

import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import thunder.hack.core.PlaceManager;
import thunder.hack.utility.math.Placement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static thunder.hack.utility.Util.mc;

public class PlaceUtility {
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

    public static Placement place(Block block, BlockPos pos, boolean rotate, boolean strictDirection, boolean ignoreEntities) {
        Hand hand = null;
        int slot = -1;

        final ItemStack mainhandStack = mc.player.getMainHandStack();
        if (mainhandStack != ItemStack.EMPTY && mainhandStack.getItem() instanceof BlockItem) {
            final Block blockFromMainhandItem = ((BlockItem) mainhandStack.getItem()).getBlock();
            if (blockFromMainhandItem == block) {
                hand = Hand.MAIN_HAND;
                slot = mc.player.getInventory().selectedSlot;
            }
        }

        final ItemStack offhandStack = mc.player.getOffHandStack();
        if (offhandStack != ItemStack.EMPTY && offhandStack.getItem() instanceof BlockItem) {
            final Block blockFromOffhandItem = ((BlockItem) offhandStack.getItem()).getBlock();
            if (blockFromOffhandItem == block) {
                hand = Hand.OFF_HAND;
            }
        }

        if (hand == null) {
            for (int i = 0; i < 9; i++) {
                final ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem) {
                    final Block blockFromItem = ((BlockItem) stack.getItem()).getBlock();
                    if (blockFromItem == block) {
                        hand = Hand.MAIN_HAND;
                        slot = i;
                        break;
                    }
                }
            }
        }

        if (hand == null) return null;
        return place(pos, rotate, strictDirection, hand, slot,ignoreEntities);
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

    public static Placement place(BlockPos pos, boolean rotate, boolean strictDirection, Hand hand, int slot,boolean ignoreEntities) {
        if (!canPlaceBlock(pos,ignoreEntities)) return null;
        Direction side = getPlaceDirection(pos, strictDirection);
        if (side == null) {
            return null;
        }
        BlockPos neighbour = pos.offset(side);
        Direction opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour.getX() + 0.5, neighbour.getY() + 0.5, neighbour.getZ() + 0.5).add(new Vec3d(opposite.getUnitVector()).multiply(0.5));
        float[] angle = calculateAngle(hitVec);
        Placement placement = new Placement(neighbour, opposite, angle[0], angle[1], hand, rotate, slot);
        if (PlaceManager.add(new Action(placement))) {
            return placement;
        }
        return null;
    }

    public static float[] calcAngle(BlockPos pos, boolean strictDirection, boolean ignoreEntities){
        if (!canPlaceBlock(pos,ignoreEntities)) return null;
        Direction side = getPlaceDirection(pos, strictDirection);
        if (side == null) {
            return null;
        }
        BlockPos neighbour = pos.offset(side);
        Direction opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour.getX() + 0.5, neighbour.getY() + 0.5, neighbour.getZ() + 0.5).add(new Vec3d(opposite.getUnitVector()).multiply(0.5));
        return calculateAngle(hitVec);
    }

    public static Placement forcePlace(BlockPos pos, boolean strictDirection, Hand hand, int slot,boolean ignoreEntities) {
        if(!canPlaceBlock(pos,  strictDirection, ignoreEntities))return null;
        if(!mc.world.getBlockState(pos).isReplaceable()) return null;
        Direction side = Direction.DOWN;
        if(strictDirection) {
             side = getPlaceDirection(pos, true);
        }

        if (side == null) {
            return null;
        }
        BlockPos neighbour = pos.offset(side);
        Direction opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour.getX() + 0.5, neighbour.getY() + 0.5, neighbour.getZ() + 0.5).add(new Vec3d(opposite.getUnitVector()).multiply(0.5));
        float[] angle = calculateAngle(hitVec);
        Placement placement = new Placement(neighbour, opposite, angle[0], angle[1], hand, false, slot);
        placement.getAction().run();
        return placement;
    }


    public static boolean canPlaceBlock(BlockPos pos, boolean ignoreEntities) {
        return canPlaceBlock(pos, false, ignoreEntities);
    }

    public static boolean canPlaceBlock(BlockPos pos, boolean strictDirection, boolean ignoreEntities) {
        if (ghostBlocks.containsKey(pos)) {
            if (System.currentTimeMillis() - ghostBlocks.get(pos) > 500) {
                ghostBlocks.remove(pos);
            } else {
                return false;
            }
        }
        if (!mc.world.getBlockState(pos).isReplaceable()) return false;
        if (strictDirection) {
            if (getPlaceDirection(pos, true) == null) return false;
        }
        if (ignoreEntities) return true;
        return mc.world.canPlace(Blocks.DIRT.getDefaultState(), pos, ShapeContext.absent());
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

        return (!state.isAir() && state.getFluidState().isEmpty());
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
        return validFacings.stream()
                .min(Comparator.comparing(facing ->
                        getEyesPos(mc.player).distanceTo(
                                new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                                        .add(new Vec3d(facing.getUnitVector()).multiply(0.5)))))
                .orElse(null);
    }

    public static Direction getBreakDirection(BlockPos pos, boolean strictDirection) {
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
        float yD = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
        float pD = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)));
        if (pD > 90F) {
            pD = 90F;
        } else if (pD < -90F) {
            pD = -90F;
        }
        return new float[]{yD, pD};
    }
}
