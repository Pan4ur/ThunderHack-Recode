package thunder.hack.modules.misc;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class FakePlayer extends Module {

    public FakePlayer() {
        super("FakePlayer", "FakePlayer", Category.MISC);
    }
    public Setting<Boolean> copyInventory = new Setting<>("CopyInventory", false);

    OtherClientPlayerEntity fakePlayer;

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
        if(fakePlayer == null) return;
        fakePlayer.kill();
        fakePlayer.setRemoved(Entity.RemovalReason.KILLED);
        fakePlayer.onRemoved();
    }
}
