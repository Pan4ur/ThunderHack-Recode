package dev.thunderhack.modules.combat;

import dev.thunderhack.event.events.EventSync;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.Timer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class AntiBot extends Module {
    public static ArrayList<PlayerEntity> bots = new ArrayList<>();
    public Setting<Boolean> remove = new Setting<>("Remove", false);
    public Setting<Boolean> onlyAura = new Setting<>("OnlyAura", true);
    private final Setting<Mode> mode = new Setting("Mode", Mode.MotionCheck);
    public Setting<Integer> checkticks = new Setting("checkTicks", 3, 0, 10, v -> mode.getValue() == Mode.MotionCheck);
    private final Timer timer = new Timer();
    private int botsNumber = 0;
    private int ticks = 0;

    public AntiBot() {
        super("AntiBot", Category.COMBAT);
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (!onlyAura.getValue())
            for (PlayerEntity player : AntiBot.mc.world.getPlayers())
                isABot(player);
        else if (Aura.target instanceof PlayerEntity ent)
            isABot(ent);

        for (PlayerEntity bot : bots) {
            if (remove.getValue()) {
                try {
                    mc.world.removeEntity(bot.getId(), Entity.RemovalReason.KILLED);
                } catch (Exception ignored) {}
            }
        }

        if (timer.passedMs(10000)) {
            bots.clear();
            botsNumber = 0;
            ticks = 0;
            timer.reset();
        }
    }

    private void isABot(PlayerEntity ent){
        if (mode.getValue() == Mode.MotionCheck) {
            double speed = (ent.getX() - ent.prevX) * (ent.getX() - ent.prevX) + (ent.getZ() - ent.prevZ) * (ent.getZ() - ent.prevZ);
            if (speed > 0.5 && !bots.contains(ent)) {
                if (ticks >= checkticks.getValue()) {
                    sendMessage(ent.getName().getString() + " is a bot!");
                    ++botsNumber;
                    bots.add(ent);
                }
                ticks++;
            }
        } else {
            if (!ent.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + ent.getName().getString()).getBytes(StandardCharsets.UTF_8))) && ent instanceof OtherClientPlayerEntity) {
                sendMessage(ent.getName().getString() + " is a bot!");
                ++botsNumber;
                bots.add(ent);
            }
            if (!ent.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + ent.getName().getString()).getBytes(StandardCharsets.UTF_8))) && ent.isInvisible() && ent instanceof OtherClientPlayerEntity) {
                sendMessage(ent.getName().getString() + " is a bot!");
                ++botsNumber;
                bots.add(ent);
            }
        }
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(botsNumber);
    }

    public enum Mode {
        UUIDCheck, MotionCheck
    }
}