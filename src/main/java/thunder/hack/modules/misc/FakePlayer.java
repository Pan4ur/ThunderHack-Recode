package thunder.hack.modules.misc;

import com.mojang.authlib.GameProfile;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;

import java.util.UUID;

public class FakePlayer extends Module {
    private final Setting<Boolean> copyInventory = new Setting<>("CopyInventory", false);

    public static OtherClientPlayerEntity fakePlayer;

    public FakePlayer() {
        super("FakePlayer", "FakePlayer", Category.MISC);
    }


    @Override
    public void onEnable() {
        fakePlayer = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.fromString("66123666-6666-6666-6666-666666666600"), "Hell_Raider"));
        fakePlayer.copyPositionAndRotation(mc.player);

        if (copyInventory.getValue()) {
            fakePlayer.getInventory().clone(mc.player.getInventory());
        }
        mc.world.addEntity(22822854, fakePlayer);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUpdate(PlayerUpdateEvent e){
    }


    @Override
    public void onDisable() {
        if (fakePlayer == null) return;

        fakePlayer.kill();
        fakePlayer.setRemoved(Entity.RemovalReason.KILLED);
        fakePlayer.onRemoved();
        fakePlayer = null;
    }
}
