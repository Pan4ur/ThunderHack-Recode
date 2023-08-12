package thunder.hack.modules.combat;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtil;
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
        super("AntiBot", "Убирает ботов", Category.COMBAT);
    }


    @EventHandler
    public void onSync(EventSync e) {
        if (!onlyAura.getValue()) {
            for (PlayerEntity player : AntiBot.mc.world.getPlayers()) {
                if (mode.getValue() == Mode.MotionCheck) {
                    if (player != null) {
                        double speed = (player.getX() - player.prevX) * (player.getX() - player.prevX) + (player.getZ() - player.prevZ) * (player.getZ() - player.prevZ);
                        if (player != mc.player && speed > 0.5 && MathUtil.getDistanceSq(player) <= Aura.attackRange.getPow2Value() && !bots.contains(player)) {
                            if (!bots.contains(player)) {
                                Command.sendMessage(player.getName().getString() + " is a bot!");
                                ++botsNumber;
                                bots.add(player);
                            }
                        }
                    }
                } else {
                    if (!player.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName().getString()).getBytes(StandardCharsets.UTF_8))) && player instanceof OtherClientPlayerEntity) {
                        if (!bots.contains(player)) {
                            Command.sendMessage(player.getName().getString() + " is a bot!");
                            ++botsNumber;
                            bots.add(player);
                        }
                    }
                    if (!player.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName().getString()).getBytes(StandardCharsets.UTF_8))) && player.isInvisible() && player instanceof OtherClientPlayerEntity) {
                        if (!bots.contains(player)) {
                            Command.sendMessage(player.getName().getString() + " is a bot!");
                            ++botsNumber;
                            bots.add(player);
                        }
                    }
                }
            }
        } else {
            if (Aura.target != null) {
                if (Aura.target instanceof PlayerEntity) {
                    if (mode.getValue() == Mode.MotionCheck) {
                        double speed = (Aura.target.getX() - Aura.target.prevX) * (Aura.target.getX() - Aura.target.prevX) + (Aura.target.getZ() - Aura.target.prevZ) * (Aura.target.getZ() - Aura.target.prevZ);
                        if (speed > 0.5 && !bots.contains(Aura.target)) {
                            if (ticks >= checkticks.getValue()) {
                                Command.sendMessage(Aura.target.getName().getString() + " is a bot!");
                                ++botsNumber;
                                bots.add((PlayerEntity) Aura.target);
                            }
                            ticks++;
                        }
                    } else {
                        if (!Aura.target.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + Aura.target.getName().getString()).getBytes(StandardCharsets.UTF_8))) && Aura.target instanceof OtherClientPlayerEntity) {
                            Command.sendMessage(Aura.target.getName().getString() + " is a bot!");
                            ++botsNumber;
                            bots.add((PlayerEntity) Aura.target);
                        }
                        if (!Aura.target.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + Aura.target.getName().getString()).getBytes(StandardCharsets.UTF_8))) && Aura.target.isInvisible() && Aura.target instanceof OtherClientPlayerEntity) {
                            Command.sendMessage(Aura.target.getName().getString() + " is a bot!");
                            ++botsNumber;
                            bots.add((PlayerEntity) Aura.target);
                        }
                    }

                }
            }
        }


        for (PlayerEntity bot : bots) {
            if (remove.getValue()) {
                try {
                    mc.world.removeEntity(bot.getId(), Entity.RemovalReason.KILLED);
                } catch (Exception ignored) {

                }
            }
        }
        if (timer.passedMs(10000)) {
            bots.clear();
            botsNumber = 0;
            timer.reset();
            ticks = 0;
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