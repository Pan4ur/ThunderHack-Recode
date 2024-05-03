package thunder.hack.injection.accesors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Explosion.class)
public interface IExplosion {
    @Mutable
    @Accessor("x")
    void setX(double x);

    @Mutable
    @Accessor("y")
    void setY(double y);

    @Mutable
    @Accessor("z")
    void setZ(double z);

    @Mutable
    @Accessor("entity")
    void setEntity(Entity entity);

    @Mutable
    @Accessor("world")
    void setWorld(World world);

    @Mutable
    @Accessor("world")
    World getWorld();

    @Mutable
    @Accessor("damageSource")
    DamageSource getDamageSource();
}
