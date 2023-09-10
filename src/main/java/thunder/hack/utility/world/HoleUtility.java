package thunder.hack.utility.world;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        if (checkQuad != null && checkQuad.size() == 4) return checkQuad;
        if (checkDouble != null && checkDouble.size() == 2) return checkDouble;

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

    public static @Nullable List<BlockPos> findDoublePoses(BlockPos checkFrom) {
        if (mc.world == null) return null;
        final List<BlockPos> doublePoses = new ArrayList<>();

        doublePoses.add(checkFrom);

        if (mc.world.getBlockState(checkFrom.add(1, 0, 0)).isReplaceable())
            doublePoses.add(checkFrom.add(1, 0, 0));
        else if (mc.world.getBlockState(checkFrom.add(-1, 0, 0)).isReplaceable())
            doublePoses.add(checkFrom.add(-1, 0, 0));
        else if (mc.world.getBlockState(checkFrom.add(0, 0, 1)).isReplaceable())
            doublePoses.add(checkFrom.add(0, 0, 1));
        else if (mc.world.getBlockState(checkFrom.add(0, 0, -1)).isReplaceable())
            doublePoses.add(checkFrom.add(0, 0, -1));


        return doublePoses;
    }

    public static boolean isHole(BlockPos pos) {
        return validIndestructible(pos) || validBedrock(pos)
                || validTwoBlockIndestructibleXZ(pos) || validTwoBlockIndestructibleXZ1(pos)
                || validTwoBlockBedrockXZ(pos) || validTwoBlockBedrockXZ1(pos)
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

    public static boolean validTwoBlockIndestructibleXZ(@NotNull BlockPos pos) {
        return (isIndestructible(pos.down()) || isBedrock(pos.down()))
                && (isIndestructible(pos.west()) || isBedrock(pos.west()))
                && (isIndestructible(pos.south()) || isBedrock(pos.south()))
                && (isIndestructible(pos.north()) || isBedrock(pos.north()))
                && isAir(pos)
                && isAir(pos.up())
                && isAir(pos.up(2))
                && (isIndestructible(pos.east().down()) || isBedrock(pos.east().down()))
                && (isIndestructible(pos.east(2)) || isBedrock(pos.east(2)))
                && (isIndestructible(pos.east().south()) || isBedrock(pos.east().south()))
                && (isIndestructible(pos.east().north()) || isBedrock(pos.east().north()))
                && isAir(pos.east())
                && isAir(pos.east().up())
                && isAir(pos.east().up(2));
    }

    public static boolean validTwoBlockIndestructibleXZ1(@NotNull BlockPos pos) {
        return (isIndestructible(pos.down()) || isBedrock(pos.down()))
                && (isIndestructible(pos.west()) || isBedrock(pos.west()))
                && (isIndestructible(pos.east()) || isBedrock(pos.east()))
                && (isIndestructible(pos.north()) || isBedrock(pos.north()))
                && isAir(pos)
                && isAir(pos.up())
                && isAir(pos.up(2))
                && (isIndestructible(pos.south().down()) || isBedrock(pos.south().down()))
                && (isIndestructible(pos.south(2)) || isBedrock(pos.south(2)))
                && (isIndestructible(pos.south().east()) || isBedrock(pos.south().east()))
                && (isIndestructible(pos.south().west()) || isBedrock(pos.south().west()))
                && isAir(pos.south())
                && isAir(pos.south().up())
                && isAir(pos.south().up(2));
    }

    public static boolean validQuadIndestructible(@NotNull BlockPos pos) {
        return ((isIndestructible(pos.down()) || isBedrock(pos.down())) && (isAir(pos)) && isAir(pos.up()) && isAir(pos.up(2)))
                && ((isIndestructible(pos.south().down()) || isBedrock(pos.south().down())) && (isAir(pos.south())) && isAir(pos.south().up()) && isAir(pos.south().up(2)))
                && ((isIndestructible(pos.east().down()) || isBedrock(pos.east().down())) && (isAir(pos.east())) && isAir(pos.east().up()) && isAir(pos.east().up(2)))
                && ((isIndestructible(pos.south().east().down()) || isBedrock(pos.south().east().down())) && (isAir(pos.south().east())) && isAir(pos.south().east().up()) && isAir(pos.south().east().up(2)))
                && ((isIndestructible(pos.north()) || isBedrock(pos.north())) && (isIndestructible(pos.west()) || isBedrock(pos.west())))
                && ((isIndestructible(pos.east().north()) || isBedrock(pos.east().north())) && (isIndestructible(pos.east().east()) || isBedrock(pos.east().east())))
                && ((isIndestructible(pos.south().west()) || isBedrock(pos.south().west())) && (isIndestructible(pos.south().south()) || isBedrock(pos.south().south())))
                && ((isIndestructible(pos.east().south().south()) || isBedrock(pos.east().south().south())) && (isIndestructible(pos.east().south().east()) || isBedrock(pos.east().south().east())));
    }

    public static boolean validQuadBedrock(@NotNull BlockPos pos) {
        return ((isBedrock(pos.down())) && (isAir(pos)) && isAir(pos.up()) && isAir(pos.up(2)))
                && ((isBedrock(pos.south().down())) && (isAir(pos.south())) && isAir(pos.south().up()) && isAir(pos.south().up(2)))
                && ((isBedrock(pos.east().down())) && (isAir(pos.east())) && isAir(pos.east().up()) && isAir(pos.east().up(2)))
                && ((isBedrock(pos.south().east().down())) && (isAir(pos.south().east())) && isAir(pos.south().east().up()) && isAir(pos.south().east().up(2)))
                && (isBedrock(pos.north()) && isBedrock(pos.west()))
                && (isBedrock(pos.east().north()) && isBedrock(pos.east().east()))
                && (isBedrock(pos.south().west()) && isBedrock(pos.south().south()))
                && (isBedrock(pos.east().south().south()) && isBedrock(pos.east().south().east()));
    }

    public static boolean validTwoBlockBedrockXZ(@NotNull BlockPos pos) {
        return isBedrock(pos.down())
                && isBedrock(pos.west())
                && isBedrock(pos.south())
                && isBedrock(pos.north())
                && isAir(pos)
                && isAir(pos.up())
                && isAir(pos.up(2))
                && isBedrock(pos.east().down())
                && isBedrock(pos.east(2))
                && isBedrock(pos.east().south())
                && isBedrock(pos.east().north())
                && isAir(pos.east())
                && isAir(pos.east().up())
                && isAir(pos.east().up(2));
    }

    public static boolean validTwoBlockBedrockXZ1(@NotNull BlockPos pos) {
        return isBedrock(pos.down())
                && isBedrock(pos.west())
                && isBedrock(pos.east())
                && isBedrock(pos.north())
                && isAir(pos)
                && isAir(pos.up())
                && isAir(pos.up(2))
                && isBedrock(pos.south().down())
                && isBedrock(pos.south(2))
                && isBedrock(pos.south().east())
                && isBedrock(pos.south().west())
                && isAir(pos.south())
                && isAir(pos.south().up())
                && isAir(pos.south().up(2));
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
