package dev.thunderhack.event.events;

import dev.thunderhack.event.Event;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class EventPlaceBlock extends Event {
    private BlockPos blockPos;
    private Block block;

    public EventPlaceBlock(BlockPos blockPos, Block block) {
        this.blockPos = blockPos;
        this.block = block;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public Block getBlock() {
        return block;
    }
}
