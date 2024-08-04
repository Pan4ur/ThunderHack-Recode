package thunder.hack.features.modules.misc;

import io.netty.util.internal.ConcurrentSet;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import thunder.hack.events.impl.EventEntitySpawn;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

//~pasted~ Ported from 3arthh4ack
public class Tracker extends Module {
    public Tracker() {
        super("Tracker", Category.MISC);
    }

    protected final Setting<Boolean> only1v1 = new Setting<>("1v1-Only", true);

    protected final Set<BlockPos> placed = new ConcurrentSet<>();
    protected final AtomicInteger awaitingExp = new AtomicInteger();
    protected static final AtomicInteger crystals = new AtomicInteger();
    protected static final AtomicInteger exp = new AtomicInteger();
    protected static PlayerEntity trackedPlayer;
    protected boolean awaiting;
    protected int crystalStacks;
    protected int expStacks;

    @Override
    public void onEnable() {
        awaiting = false;
        trackedPlayer = null;
        awaitingExp.set(0);
        crystals.set(0);
        exp.set(0);
        crystalStacks = 0;
        expStacks = 0;
    }

    @Override
    public String getDisplayInfo() {
        return trackedPlayer == null ? null : trackedPlayer.getName().getString();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof GameMessageS2CPacket pac) {

            String s = pac.content().getString();
            if (!s.contains("<") && (s.contains("has accepted your duel request") || s.contains("Accepted the duel request from"))) {
                sendMessage(isRu() ? "Дуель принята! Обновляю цель..." : "Duel accepted! Resetting target...");
                trackedPlayer = null;
                awaitingExp.set(0);
                crystals.set(0);
                exp.set(0);
                crystalStacks = 0;
                expStacks = 0;
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EventEntitySpawn e) {
        if (e.getEntity() instanceof EndCrystalEntity) {
            if (!placed.remove(BlockPos.ofFloored(e.getEntity().getX(), e.getEntity().getY() - 1, e.getEntity().getZ()))) {
                crystals.incrementAndGet();
            }
        }
        if (e.getEntity() instanceof ExperienceBottleEntity) {
            if (awaitingExp.get() > 0) {
                if (mc.player.squaredDistanceTo(e.getEntity()) < 16) awaitingExp.decrementAndGet();
                else exp.incrementAndGet();
            } else exp.incrementAndGet();
        }
    }

    @Override
    public void onUpdate() {
        boolean found = false;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == null || player.equals(mc.player)) continue;

            if (found && only1v1.getValue()) {
                disable(isRu() ? "Ты не в дуели! Отключаю.." : "Disabled, you are not in a 1v1! Disabling...");
                return;
            }
            if (trackedPlayer == null)
                sendMessage(Formatting.LIGHT_PURPLE + (isRu() ? "Следим за " : "Now tracking ") + Formatting.DARK_PURPLE + player.getName().getString() + Formatting.LIGHT_PURPLE + "!");
            trackedPlayer = player;
            found = true;
        }

        if (trackedPlayer == null) return;

        int exp = this.exp.get() / 64;
        if (expStacks != exp) {
            expStacks = exp;
            if (isRu()) sendMessage(Formatting.DARK_PURPLE + trackedPlayer.getName().getString() + Formatting.LIGHT_PURPLE + " использовал " + Formatting.WHITE + exp + Formatting.LIGHT_PURPLE + (exp == 1 ? " стак" : " стаков") + " Пузырьков опыта!");
            else sendMessage(Formatting.DARK_PURPLE + trackedPlayer.getName().getString() + Formatting.LIGHT_PURPLE + " used " + Formatting.WHITE + exp + Formatting.LIGHT_PURPLE + (exp == 1 ? " stack" : " stacks") + " of XP Bottles!");
        }

        int crystals = this.crystals.get() / 64;
        if (crystalStacks != crystals) {
            crystalStacks = crystals;
            if (!isRu()) sendMessage(Formatting.DARK_PURPLE + trackedPlayer.getName().getString() + Formatting.LIGHT_PURPLE + " used " + Formatting.WHITE + crystals + Formatting.LIGHT_PURPLE + (crystals == 1 ? " stack" : " stacks") + " of Crystals!");
            else sendMessage(Formatting.DARK_PURPLE + trackedPlayer.getName().getString() + Formatting.LIGHT_PURPLE + " использовал " + Formatting.WHITE + crystals + Formatting.LIGHT_PURPLE + (crystals == 1 ? " стак" : " стаков") + " Кристаллов!");
        }
    }

    public void sendTrack() {
        if (trackedPlayer != null) {
            int c = crystals.get();
            int e = exp.get();
            StringBuilder builder;

            if (isRu()) builder = new StringBuilder().append(trackedPlayer.getName().getString()).append(Formatting.LIGHT_PURPLE).append(" использовал ").append(Formatting.WHITE).append(c).append(Formatting.LIGHT_PURPLE).append(" (").append(Formatting.WHITE);
            else builder = new StringBuilder().append(trackedPlayer.getName().getString()).append(Formatting.LIGHT_PURPLE).append(" has used ").append(Formatting.WHITE).append(c).append(Formatting.LIGHT_PURPLE).append(" (").append(Formatting.WHITE);

            if (c % 64 == 0) builder.append(c / 64);
            else builder.append(MathUtility.round(c / 64.0, 1));

            if (isRu()) builder.append(Formatting.LIGHT_PURPLE).append(") кристаллов и ").append(Formatting.WHITE).append(e).append(Formatting.LIGHT_PURPLE).append(" (").append(Formatting.WHITE);
            else builder.append(Formatting.LIGHT_PURPLE).append(") crystals and ").append(Formatting.WHITE).append(e).append(Formatting.LIGHT_PURPLE).append(" (").append(Formatting.WHITE);

            if (e % 64 == 0) builder.append(e / 64);
            else builder.append(MathUtility.round(e / 64.0, 1));

            if (isRu()) builder.append(Formatting.LIGHT_PURPLE).append(") пузырьков опыта.");
            else builder.append(Formatting.LIGHT_PURPLE).append(") bottles of experience.");

            sendMessage(builder.toString());
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof PlayerInteractItemC2SPacket) {
            if (mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE || mc.player.getOffHandStack().getItem() == Items.EXPERIENCE_BOTTLE) {
                awaitingExp.incrementAndGet();
            }
        }
        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket pac) {
            if (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL || mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) {
                placed.add(pac.getBlockHitResult().getBlockPos());
            }
        }
    }
}
