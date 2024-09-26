package thunder.hack.utility.interfaces;

import net.minecraft.util.math.BlockPos;
import thunder.hack.features.modules.render.Trails;

import java.util.List;

public interface IEntity {
    List<Trails.Trail> getTrails();

    BlockPos thunderHack_Recode$getVelocityBP();
}
