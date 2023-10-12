package dev.thunderhack.event.events;

import net.minecraft.util.math.BlockPos;
import dev.thunderhack.event.Event;

public class EventBreakBlock extends Event {
    private BlockPos bp;

    public EventBreakBlock(BlockPos bp) {
        this.bp = bp;
    }

    public BlockPos getPos() {
        return bp;
    }
}
