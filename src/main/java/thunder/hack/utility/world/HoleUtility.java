package thunder.hack.utility.world;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static thunder.hack.features.modules.Module.mc;

public final class HoleUtility {
    public static final Vec3i[] VECTOR_PATTERN = {
            new Vec3i(0, 0, 1),
            new Vec3i(0, 0, -1),
            new Vec3i(1, 0, 0),
            new Vec3i(-1, 0, 0)
    };

    public static @NotNull List<BlockPos> getHolePoses(@NotNull Vec3d from) {
        List<BlockPos> positions = new ArrayList<>();

        double decimalX = from.getX() - Math.floor(from.getX());
        double decimalZ = from.getZ() - Math.floor(from.getZ());
        int offX = calcOffset(decimalX);
        int offZ = calcOffset(decimalZ);
        positions.add(getPos(from));
        for (int x = 0; x <= Math.abs(offX); ++x) {
            for (int z = 0; z <= Math.abs(offZ); ++z) {
                int properX = x * offX;
                int properZ = z * offZ;
                positions.add(Objects.requireNonNull(getPos(from)).add(properX, 0, properZ));
            }
        }

        return positions;
    }

    public static @NotNull List<BlockPos> getSurroundPoses(@NotNull Vec3d from) {
        final BlockPos fromPos = BlockPos.ofFloored(from);
        final ArrayList<BlockPos> tempOffsets = new ArrayList<>();

        final double decimalX = Math.abs(from.getX()) - Math.floor(Math.abs(from.getX()));
        final double decimalZ = Math.abs(from.getZ()) - Math.floor(Math.abs(from.getZ()));
        final int lengthXPos = calcLength(decimalX, false);
        final int lengthXNeg = calcLength(decimalX, true);
        final int lengthZPos = calcLength(decimalZ, false);
        final int lengthZNeg = calcLength(decimalZ, true);

        for (int x = 1; x < lengthXPos + 1; ++x) {
            tempOffsets.add(addToPlayer(fromPos, x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(fromPos, x, 0.0, -(1 + lengthZNeg)));
        }
        for (int x = 0; x <= lengthXNeg; ++x) {
            tempOffsets.add(addToPlayer(fromPos, -x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(fromPos, -x, 0.0, -(1 + lengthZNeg)));
        }
        for (int z = 1; z < lengthZPos + 1; ++z) {
            tempOffsets.add(addToPlayer(fromPos, 1 + lengthXPos, 0.0, z));
            tempOffsets.add(addToPlayer(fromPos, -(1 + lengthXNeg), 0.0, z));
        }
        for (int z = 0; z <= lengthZNeg; ++z) {
            tempOffsets.add(addToPlayer(fromPos, 1 + lengthXPos, 0.0, -z));
            tempOffsets.add(addToPlayer(fromPos, -(1 + lengthXNeg), 0.0, -z));
        }

        return tempOffsets;
    }

    private static @NotNull BlockPos getPos(@NotNull Vec3d from) {
        return BlockPos.ofFloored(from.getX(), from.getY() - Math.floor(from.getY()) > 0.8 ? Math.floor(from.getY()) + 1.0 : Math.floor(from.getY()), from.getZ());
    }

    public static int calcOffset(double dec) {
        return dec >= 0.7 ? 1 : (dec <= 0.3 ? -1 : 0);
    }

    public static int calcLength(double decimal, boolean negative) {
        if (negative) return decimal <= 0.3 ? 1 : 0;
        return decimal >= 0.7 ? 1 : 0;
    }

    public static BlockPos addToPlayer(@NotNull BlockPos playerPos, double x, double y, double z) {
        if (playerPos.getX() < 0) x = -x;
        if (playerPos.getY() < 0) y = -y;
        if (playerPos.getZ() < 0) z = -z;
        return playerPos.add(BlockPos.ofFloored(x, y, z));
    }

    public static boolean isHole(BlockPos pos) {
        return isSingleHole(pos)
                || validTwoBlockIndestructible(pos) || validTwoBlockBedrock(pos)
                || validQuadIndestructible(pos) || validQuadBedrock(pos);
    }

    public static boolean isSingleHole(BlockPos pos) {
        return validIndestructible(pos) || validBedrock(pos);
    }

    public static boolean validIndestructible(@NotNull BlockPos pos) {
        return !validBedrock(pos)
                && (isIndestructible(pos.add(0, -1, 0)) || isBedrock(pos.add(0, -1, 0)))
                && (isIndestructible(pos.add(1, 0, 0)) || isBedrock(pos.add(1, 0, 0)))
                && (isIndestructible(pos.add(-1, 0, 0)) || isBedrock(pos.add(-1, 0, 0)))
                && (isIndestructible(pos.add(0, 0, 1)) || isBedrock(pos.add(0, 0, 1)))
                && (isIndestructible(pos.add(0, 0, -1)) || isBedrock(pos.add(0, 0, -1)))
                && isReplaceable(pos)
                && isReplaceable(pos.add(0, 1, 0))
                && isReplaceable(pos.add(0, 2, 0));
    }

    public static boolean validBedrock(@NotNull BlockPos pos) {
        return isBedrock(pos.add(0, -1, 0))
                && isBedrock(pos.add(1, 0, 0))
                && isBedrock(pos.add(-1, 0, 0))
                && isBedrock(pos.add(0, 0, 1))
                && isBedrock(pos.add(0, 0, -1))
                && isReplaceable(pos)
                && isReplaceable(pos.add(0, 1, 0))
                && isReplaceable(pos.add(0, 2, 0));
    }

    public static boolean validTwoBlockBedrock(@NotNull BlockPos pos) {
        if (!isReplaceable(pos)) return false;
        Vec3i addVec = getTwoBlocksDirection(pos);

        // If addVec not found -> hole incorrect
        if (addVec == null)
            return false;

        BlockPos[] checkPoses = new BlockPos[]{pos, pos.add(addVec)};
        // Check surround poses of checkPoses
        for (BlockPos checkPos : checkPoses) {

            if(!isReplaceable(checkPos.add(0, 1, 0)) || !isReplaceable(checkPos.add(0, 2, 0)))
                return false;

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

    public static boolean validTwoBlockIndestructible(@NotNull BlockPos pos) {
        if (!isReplaceable(pos)) return false;
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

            if(!isReplaceable(checkPos.add(0, 1, 0)) || !isReplaceable(checkPos.add(0, 2, 0)))
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
            if (isReplaceable(pos.add(vec)))
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

            if(!isReplaceable(checkPos.add(0, 1, 0)) || !isReplaceable(checkPos.add(0, 2, 0)))
                return false;

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
            if (!isBedrock(downPos))
                return false;

            if(!isReplaceable(checkPos.add(0, 1, 0)) || !isReplaceable(checkPos.add(0, 2, 0)))
                return false;

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

        if (!isReplaceable(pos))
            return null;

        if (isReplaceable(pos.add(1, 0, 0)) && isReplaceable(pos.add(0, 0, 1)) && isReplaceable(pos.add(1, 0, 1))) {
            dirList.add(pos.add(1, 0, 0));
            dirList.add(pos.add(0, 0, 1));
            dirList.add(pos.add(1, 0, 1));
        }
        if (isReplaceable(pos.add(-1, 0, 0)) && isReplaceable(pos.add(0, 0, -1)) && isReplaceable(pos.add(-1, 0, -1))) {
            dirList.add(pos.add(-1, 0, 0));
            dirList.add(pos.add(0, 0, -1));
            dirList.add(pos.add(-1, 0, -1));
        }
        if (isReplaceable(pos.add(1, 0, 0)) && isReplaceable(pos.add(0, 0, -1)) && isReplaceable(pos.add(1, 0, -1))) {
            dirList.add(pos.add(1, 0, 0));
            dirList.add(pos.add(0, 0, -1));
            dirList.add(pos.add(1, 0, -1));
        }
        if (isReplaceable(pos.add(-1, 0, 0)) && isReplaceable(pos.add(0, 0, 1)) && isReplaceable(pos.add(-1, 0, 1))) {
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

        Block block = mc.world.getBlockState(bp).getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.NETHERITE_BLOCK
                || block == Blocks.CRYING_OBSIDIAN || block == Blocks.RESPAWN_ANCHOR;
    }

    private static boolean isBedrock(BlockPos bp) {
        if (mc.world == null) return false;

        return mc.world.getBlockState(bp).getBlock() == Blocks.BEDROCK;
    }

    private static boolean isReplaceable(BlockPos bp) {
        if (mc.world == null) return false;

        return mc.world.getBlockState(bp).isReplaceable();
    }
}
