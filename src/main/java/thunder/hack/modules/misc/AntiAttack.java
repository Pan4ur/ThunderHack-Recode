package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

import static thunder.hack.modules.combat.Criticals.getEntity;

public final class AntiAttack extends Module {
    private final Setting<Boolean> friend = new Setting<>("Friend", true);
    private final Setting<Boolean> zoglin = new Setting<>("Zoglin", true);
    private final Setting<Boolean> villager = new Setting<>("Villager", false);

    public AntiAttack() {
        super("AntiAttack", Category.PLAYER);
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPacketSend(PacketEvent.@NotNull Send e) {
        if (e.getPacket() instanceof PlayerInteractEntityC2SPacket pac) {
            Entity entity = getEntity(pac);
            if (entity == null) return;
            if (ThunderHack.friendManager.isFriend(entity.getName().getString()) && friend.getValue())
                e.cancel();
            if (entity instanceof ZombifiedPiglinEntity && zoglin.getValue())
                e.cancel();
            if(entity instanceof VillagerEntity && villager.getValue()){
                e.cancel();
            }
        }
    }
}
