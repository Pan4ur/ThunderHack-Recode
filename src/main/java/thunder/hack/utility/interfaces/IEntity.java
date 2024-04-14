package thunder.hack.utility.interfaces;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import thunder.hack.modules.render.Trails;

import java.util.List;

public interface IEntity {
    List<Trails.Trail> getTrails();

    BlockPos thunderHack_Recode$getVelocityBP();
}
