package thunder.hack.utility.interfaces;

import net.minecraft.util.math.Vec3d;
import thunder.hack.features.modules.combat.Aura;

import java.util.List;

public interface IEntityLiving {
    double getPrevServerX();

    double getPrevServerY();

    double getPrevServerZ();

    List<Aura.Position> getPositionHistory();
}
