package thunder.hack.modules.misc;

import com.mojang.authlib.GameProfile;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;

import java.util.UUID;

public class FakePlayer extends Module {
    private final Setting<Boolean> copyInventory = new Setting<>("Copy Inventory", false);

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
        mc.world.addPlayer(22822854, fakePlayer);
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
