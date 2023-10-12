package dev.thunderhack.event.events;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import dev.thunderhack.event.Event;

public class EventSetBlockState extends Event {
    private final BlockPos pos;
    private final BlockState state;

    public EventSetBlockState(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }
}
