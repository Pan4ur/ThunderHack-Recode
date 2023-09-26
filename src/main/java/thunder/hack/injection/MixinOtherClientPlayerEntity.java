package thunder.hack.injection;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Unique;
import thunder.hack.core.ModuleManager;
import thunder.hack.modules.misc.FakePlayer;
import thunder.hack.utility.interfaces.IEntityLiving;
import thunder.hack.utility.interfaces.IOtherClientPlayerEntity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;

import static thunder.hack.modules.Module.mc;


@Mixin(OtherClientPlayerEntity.class)
public class MixinOtherClientPlayerEntity extends AbstractClientPlayerEntity implements IOtherClientPlayerEntity {

    @Unique
    private double backUpX, backUpY, backUpZ;

    public MixinOtherClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    public void resolve() {
        if((Object) this == FakePlayer.fakePlayer){
            backUpY = -999;
            return;
        }
        backUpX = getX();
        backUpY = getY();
        backUpZ = getZ();
        Vec3d from = new Vec3d( ((IEntityLiving) this).getPrevServerX(),  ((IEntityLiving) this).getPrevServerY(),  ((IEntityLiving) this).getPrevServerZ());
        Vec3d to = new Vec3d(serverX, serverY, serverZ);
        if (mc.player.squaredDistanceTo(from) > mc.player.squaredDistanceTo(to)) setPosition(to.x, to.y, to.z); else setPosition(from.x, from.y, from.z);
    }

    public void releaseResolver() {
        if (backUpY != -999) {
            setPosition(backUpX, backUpY, backUpZ);
            backUpY = -999;
        }
    }

}
