package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.modules.misc.FakePlayer;
import thunder.hack.modules.render.NameTags;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public final class AntiBot extends Module {
    public static ArrayList<PlayerEntity> bots = new ArrayList<>();
    public Setting<Boolean> remove = new Setting<>("Remove", false);
    public Setting<Boolean> onlyAura = new Setting<>("OnlyAura", true);
    private final Setting<Mode> mode = new Setting("Mode", Mode.UUIDCheck);
    public Setting<Integer> checkticks = new Setting("checkTicks", 3, 0, 10, v -> mode.getValue() == Mode.MotionCheck);
    private final Timer timer = new Timer();
    private int botsNumber = 0;
    private int ticks = 0;


    public AntiBot() {
        super("AntiBot", Category.COMBAT);
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (!onlyAura.getValue()) mc.world.getPlayers().forEach(this::isABot);
        else if (Aura.target instanceof PlayerEntity ent) isABot(ent);

        bots.forEach(b -> {
            if (remove.getValue())
                try {
                    mc.world.removeEntity(b.getId(), Entity.RemovalReason.KILLED);
                } catch (Exception ignored) {
                }
        });

        if (timer.passedMs(10000)) {
            bots.clear();
            botsNumber = 0;
            ticks = 0;
            timer.reset();
        }
    }

    private void isABot(PlayerEntity ent) {
        if (bots.contains(ent))
            return;

        switch (mode.getValue()) {
            case UUIDCheck -> {
                if (!ent.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + ent.getName().getString()).getBytes(StandardCharsets.UTF_8))) && ent instanceof OtherClientPlayerEntity
                        && (FakePlayer.fakePlayer == null || ent.getId() != FakePlayer.fakePlayer.getId())
                        && !ent.getName().getString().contains("-")) {
                    sendMessage(ent.getName().getString() + " is a bot!");
                    ++botsNumber;
                    bots.add(ent);
                }
            }
            case MotionCheck -> {
                double speed = (ent.getX() - ent.prevX) * (ent.getX() - ent.prevX) + (ent.getZ() - ent.prevZ) * (ent.getZ() - ent.prevZ);
                if (speed > 0.5 && !bots.contains(ent)) {
                    if (ticks >= checkticks.getValue()) {
                        sendMessage(ent.getName().getString() + " is a bot!");
                        ++botsNumber;
                        bots.add(ent);
                    }
                    ticks++;
                }
            }
            case ZeroPing -> {
                if (NameTags.getEntityPing(ent) <= 0) {
                    sendMessage(ent.getName().getString() + " is a bot!");
                    ++botsNumber;
                    bots.add(ent);
                }
            }
        }
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(botsNumber);
    }

    public enum Mode {
        UUIDCheck, MotionCheck, ZeroPing
    }
}