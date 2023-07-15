package thunder.hack.utility;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BlockPosWithFacing {
    private final BlockPos bp;
    private final Direction facing;

    public BlockPosWithFacing(BlockPos blockPos, Direction enumFacing) {
        this.bp = blockPos;
        this.facing = enumFacing;
    }

    public BlockPos getPosition() {
        return this.bp;
    }

    public Direction getFacing() {
        return this.facing;
    }

}
