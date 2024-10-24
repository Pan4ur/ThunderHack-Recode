package thunder.hack.features.modules.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.events.impl.PacketEvent;

import static thunder.hack.features.modules.client.ClientSettings.isRu;
import static thunder.hack.features.modules.combat.Criticals.getEntity;

public class Religion extends Module {
    public Religion() { super("Religion", Category.CLIENT); }

    public final Setting<YourReligion> ReligionSetting = new Setting<>("YourReligion", YourReligion.Christianity, v -> true);

    public enum YourReligion { Christianity, Islam, Satanism, Atheism }
    public int sheepHits = 0;

    @EventHandler
    @SuppressWarnings("unused")
    private void onPacketSend(PacketEvent.@NotNull Send e) {
        if (!(e.getPacket() instanceof PlayerInteractEntityC2SPacket pac)) return;

        Entity entity = getEntity(pac);
        if (entity == null) return;

        if ((entity instanceof PigEntity || entity instanceof ZoglinEntity) && ReligionSetting.is(YourReligion.Islam)) e.cancel();

        if (entity instanceof PlayerEntity) {
            if (ReligionSetting.is(YourReligion.Christianity) && Managers.FRIEND.isFriend(entity.getName().getString())) {
                sendMessage(isRu() ? "Люби ближнего твоего, как самого себя!" : "Love your neighbour as yourself!");
                e.cancel();
            } else if (ReligionSetting.is(YourReligion.Christianity)) {
                sendMessage(isRu() ? "Не убивай!" : "Do not kill!");
                e.cancel();
            }
        }

        if ((entity instanceof SheepEntity) && ReligionSetting.is(YourReligion.Satanism)) {
            sheepHits++;
            sendMessage(isRu() ? String.format("Сатана хочет больше! Ударов по овцам: %d", sheepHits) : String.format("Satan needs more! Times you hit a sheep: %d", sheepHits));
        }

    }
}
