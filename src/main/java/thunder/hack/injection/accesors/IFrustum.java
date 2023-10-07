package thunder.hack.injection.accesors;

import org.joml.FrustumIntersection;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.Frustum;

@Mixin(Frustum.class)
public interface IFrustum {
    @Accessor
    FrustumIntersection getFrustumIntersection();

    @Accessor("x")
    double getX();

    @Accessor("x")
    void setX(double x);

    @Accessor("y")
    double getY();

    @Accessor("y")
    void setY(double y);

    @Accessor("z")
    double getZ();

    @Accessor("z")
    void setZ(double z);
}
