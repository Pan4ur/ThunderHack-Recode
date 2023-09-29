package thunder.hack.utility.world;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

import static thunder.hack.modules.Module.mc;

public final class HoleUtility {
    public static final Vec3i[] VECTOR_PATTERN = {
            new Vec3i(0, 0, 1),
            new Vec3i(0, 0, -1),
            new Vec3i(1, 0, 0),
            new Vec3i(-1, 0, 0)
    };

    public static @NotNull List<BlockPos> getSurroundPoses(BlockPos from) {
        List<BlockPos> surroundPoses = new ArrayList<>();
        if (mc.world == null) return surroundPoses;

        getHolePoses(from).forEach(checkPos -> {
            for (Vec3i pattern : VECTOR_PATTERN) {
                BlockPos newPos = checkPos.add(pattern);

                if (!mc.world.getBlockState(newPos).isReplaceable())
                    surroundPoses.add(newPos);
            }
        });

        return surroundPoses;
    }

    public static @NotNull List<BlockPos> getHolePoses(BlockPos from) {
        final List<BlockPos> checkQuad = findQuadPoses(from);
        final List<BlockPos> checkDouble = findDoublePoses(from);

        if (checkDouble != null && checkDouble.size() == 2) return checkDouble;
        if (checkQuad != null && checkQuad.size() == 4) return checkQuad;

        return List.of(from);
    }

    public static @Nullable List<BlockPos> findQuadPoses(BlockPos checkFrom) {
        if (mc.world == null) return null;
        final List<BlockPos> quadPoses = new ArrayList<>();

        quadPoses.add(checkFrom);

        if (mc.world.getBlockState(checkFrom.add(1, 0, 1)).isReplaceable()) {
            quadPoses.add(checkFrom.add(1, 0, 1));
            quadPoses.add(checkFrom.add(1, 0, 0));
            quadPoses.add(checkFrom.add(0, 0, 1));
        } else if (mc.world.getBlockState(checkFrom.add(1, 0, -1)).isReplaceable()) {
            quadPoses.add(checkFrom.add(1, 0, -1));
            quadPoses.add(checkFrom.add(1, 0, 0));
            quadPoses.add(checkFrom.add(0, 0, -1));
        } else if (mc.world.getBlockState(checkFrom.add(-1, 0, 1)).isReplaceable()) {
            quadPoses.add(checkFrom.add(-1, 0, 1));
            quadPoses.add(checkFrom.add(-1, 0, 0));
            quadPoses.add(checkFrom.add(0, 0, 1));
        } else if (mc.world.getBlockState(checkFrom.add(-1, 0, -1)).isReplaceable()) {
            quadPoses.add(checkFrom.add(-1, 0, -1));
            quadPoses.add(checkFrom.add(-1, 0, 0));
            quadPoses.add(checkFrom.add(0, 0, -1));
        }

        return quadPoses;
    }

    public static @Nullable @Unmodifiable List<BlockPos> findDoublePoses(BlockPos checkFrom) {
        if (mc.world == null) return null;

        for (Vec3i vec : VECTOR_PATTERN) {
            if (mc.world.getBlockState(checkFrom.add(vec)).isReplaceable())
                return List.of(checkFrom, checkFrom.add(vec));
        }

        return null;
    }

    public static boolean isHole(BlockPos pos) {
        return validIndestructible(pos) || validBedrock(pos)
                || validTwoBlockIndestructibleXZ(pos) || validTwoBlockBedrockXZ(pos)
                || validQuadIndestructible(pos) || validQuadBedrock(pos);
    }

    public static boolean validIndestructible(@NotNull BlockPos pos) {
        return !validBedrock(pos)
                && (isIndestructible(pos.add(0, -1, 0)) || isBedrock(pos.add(0, -1, 0)))
                && (isIndestructible(pos.add(1, 0, 0)) || isBedrock(pos.add(1, 0, 0)))
                && (isIndestructible(pos.add(-1, 0, 0)) || isBedrock(pos.add(-1, 0, 0)))
                && (isIndestructible(pos.add(0, 0, 1)) || isBedrock(pos.add(0, 0, 1)))
                && (isIndestructible(pos.add(0, 0, -1)) || isBedrock(pos.add(0, 0, -1)))
                && isAir(pos)
                && isAir(pos.add(0, 1, 0))
                && isAir(pos.add(0, 2, 0));
    }

    public static boolean validBedrock(@NotNull BlockPos pos) {
        return isBedrock(pos.add(0, -1, 0))
                && isBedrock(pos.add(1, 0, 0))
                && isBedrock(pos.add(-1, 0, 0))
                && isBedrock(pos.add(0, 0, 1))
                && isBedrock(pos.add(0, 0, -1))
                && isAir(pos)
                && isAir(pos.add(0, 1, 0))
                && isAir(pos.add(0, 2, 0));
    }

    public static boolean validTwoBlockBedrockXZ(@NotNull BlockPos pos) {
        if (!isAir(pos)) return false;
        Vec3i addVec = getTwoBlocksDirection(pos);

        // If addVec not found -> hole incorrect
        if (addVec == null)
            return false;

        BlockPos[] checkPoses = new BlockPos[]{pos, pos.add(addVec)};
        // Check surround poses of checkPoses
        for (BlockPos checkPos : checkPoses) {
            BlockPos downPos = checkPos.down();
            if (!isBedrock(downPos))
                return false;

            for (Vec3i vec : VECTOR_PATTERN) {
                BlockPos reducedPos = checkPos.add(vec);
                if (!isBedrock(reducedPos) && !reducedPos.equals(pos) && !reducedPos.equals(pos.add(addVec)))
                    return false;
            }
        }

        return true;
    }

    public static boolean validTwoBlockIndestructibleXZ(@NotNull BlockPos pos) {
        if (!isAir(pos)) return false;
        Vec3i addVec = getTwoBlocksDirection(pos);

        // If addVec not found -> hole incorrect
        if (addVec == null)
            return false;

        BlockPos[] checkPoses = new BlockPos[]{pos, pos.add(addVec)};
        // Check surround poses of checkPoses
        boolean wasIndestrictible = false;
        for (BlockPos checkPos : checkPoses) {
            BlockPos downPos = checkPos.down();
            if (isIndestructible(downPos))
                wasIndestrictible = true;
            else if (!isBedrock(downPos))
                return false;

            for (Vec3i vec : VECTOR_PATTERN) {
                BlockPos reducedPos = checkPos.add(vec);

                if (isIndestructible(reducedPos)) {
                    wasIndestrictible = true;
                    continue;
                }
                if (!isBedrock(reducedPos) && !reducedPos.equals(pos) && !reducedPos.equals(pos.add(addVec)))
                    return false;
            }
        }

        return wasIndestrictible;
    }

    private static @Nullable Vec3i getTwoBlocksDirection(BlockPos pos) {
        // Try to get direction
        for (Vec3i vec : VECTOR_PATTERN) {
            if (isAir(pos.add(vec)))
                return vec;
        }

        return null;
    }

    public static boolean validQuadIndestructible(@NotNull BlockPos pos) {
        List<BlockPos> checkPoses = getQuadDirection(pos);
        // If checkPoses not found -> hole incorrect
        if (checkPoses == null)
            return false;

        boolean wasIndestrictible = false;
        for (BlockPos checkPos : checkPoses) {
            BlockPos downPos = checkPos.down();
            if (isIndestructible(downPos)) {
                wasIndestrictible = true;
            } else if (!isBedrock(downPos)) {
                return false;
            }

            for (Vec3i vec : VECTOR_PATTERN) {
                BlockPos reducedPos = checkPos.add(vec);

                if (isIndestructible(reducedPos)) {
                    wasIndestrictible = true;
                    continue;
                }
                if (!isBedrock(reducedPos) && !checkPoses.contains(reducedPos)) {
                    return false;
                }
            }
        }

        return wasIndestrictible;
    }

    public static boolean validQuadBedrock(@NotNull BlockPos pos) {
        List<BlockPos> checkPoses = getQuadDirection(pos);
        // If checkPoses not found -> hole incorrect
        if (checkPoses == null)
            return false;

        for (BlockPos checkPos : checkPoses) {
            BlockPos downPos = checkPos.down();
            if (!isBedrock(downPos)) {
                return false;
            }

            for (Vec3i vec : VECTOR_PATTERN) {
                BlockPos reducedPos = checkPos.add(vec);
                if (!isBedrock(reducedPos) && !checkPoses.contains(reducedPos)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static @Nullable List<BlockPos> getQuadDirection(@NotNull BlockPos pos) {
        // Try to get direction
        List<BlockPos> dirList = new ArrayList<>();
        dirList.add(pos);

        if(!isAir(pos))
            return null;

        if (isAir(pos.add(1, 0, 0)) && isAir(pos.add(0, 0, 1)) && isAir(pos.add(1, 0, 1))) {
            dirList.add(pos.add(1, 0, 0));
            dirList.add(pos.add(0, 0, 1));
            dirList.add(pos.add(1, 0, 1));
        }
        if (isAir(pos.add(-1, 0, 0)) && isAir(pos.add(0, 0, -1)) && isAir(pos.add(-1, 0, -1))) {
            dirList.add(pos.add(-1, 0, 0));
            dirList.add(pos.add(0, 0, -1));
            dirList.add(pos.add(-1, 0, -1));
        }
        if (isAir(pos.add(1, 0, 0)) && isAir(pos.add(0, 0, -1)) && isAir(pos.add(1, 0, -1))) {
            dirList.add(pos.add(1, 0, 0));
            dirList.add(pos.add(0, 0, -1));
            dirList.add(pos.add(1, 0, -1));
        }
        if (isAir(pos.add(-1, 0, 0)) && isAir(pos.add(0, 0, 1)) && isAir(pos.add(-1, 0, 1))) {
            dirList.add(pos.add(-1, 0, 0));
            dirList.add(pos.add(0, 0, 1));
            dirList.add(pos.add(-1, 0, 1));
        }

        if (dirList.size() != 4)
            return null;

        return dirList;
    }

    private static boolean isIndestructible(BlockPos bp) {
        if (mc.world == null) return false;

        return mc.world.getBlockState(bp).getBlock() == Blocks.OBSIDIAN
                || mc.world.getBlockState(bp).getBlock() == Blocks.NETHERITE_BLOCK
                || mc.world.getBlockState(bp).getBlock() == Blocks.CRYING_OBSIDIAN
                || mc.world.getBlockState(bp).getBlock() == Blocks.RESPAWN_ANCHOR;
    }

    private static boolean isBedrock(BlockPos bp) {
        if (mc.world == null) return false;

        return mc.world.getBlockState(bp).getBlock() == Blocks.BEDROCK;
    }

    private static boolean isAir(BlockPos bp) {
        if (mc.world == null) return false;

        return mc.world.isAir(bp);
    }
}
