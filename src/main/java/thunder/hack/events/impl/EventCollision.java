package thunder.hack.events.impl;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import thunder.hack.events.Event;

public class EventCollision extends Event {

    private VoxelShape shape;
    private BlockState bs;
    private BlockPos bp;


    public EventCollision(VoxelShape shape, BlockState bs, BlockPos bp){
        this.shape = shape;
        this.bs = bs;
        this.bp = bp;
    }

    public VoxelShape getShape(){
        return shape;
    }

    public void setShape(VoxelShape vs){
        shape = vs;
    }

    public BlockPos getPos(){
        return bp;
    }

    public BlockState getState(){
        return bs;
    }
}
