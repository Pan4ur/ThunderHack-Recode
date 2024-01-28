package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IPlayerPositionLookS2CPacket;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import thunder.hack.utility.player.MovementUtility;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PacketFly extends Module {
    public PacketFly() {
        super("PacketFly", Category.MOVEMENT);
    }

    public Setting<Boolean> limit = new Setting<>("Limit", true);
    public Setting<Boolean> antiKick = new Setting<>("AntiKick", true);

    public Setting<Float> speed = new Setting<>("Speed", 1.0f, 0.0f, 3.0f);
    public Setting<Float> timer = new Setting<>("Timer", 1f, 0.1f, 2f);
    public Setting<Integer> increaseTicks = new Setting<>("IncreaseTicks", 1, 1, 20);
    public Setting<Float> factor = new Setting<>("Factor", 1f, 1f, 10f);
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Fast);
    private final Setting<Phase> phase = new Setting<>("Phase", Phase.Full);
    private final Setting<Type> type = new Setting<>("Type", Type.Preserve);
    private int teleportId = -1;
    private final ArrayList<PlayerMoveC2SPacket> movePackets = new ArrayList<>();
    private final Random rnd = new Random();
    private final ConcurrentHashMap<Integer,Teleport> teleports = new ConcurrentHashMap<>();
    private int ticks = 0;
    private int factorTicks = 0;

    private boolean flip = false;

    @Override
    public void onEnable() {
        teleportId = -1;
        if (fullNullCheck() && mc.player != null) {
            ticks = 0;
            teleportId = 0;
            movePackets.clear();
            teleports.clear();
        }
        factorTicks = 0;
    }

    @Override
    public void onDisable() {
        ThunderHack.TICK_TIMER = (1.0f);
    }

    public boolean getTickCounter(int n) {
        ++ticks;
        if (ticks >= n) {
            ticks = 0;
            return true;
        }
        return false;
    }

    private int getWorldBorder() {
        if (mc.isInSingleplayer()) {
            return 1;
        }
        int n = rnd.nextInt(29000000);
        if (rnd.nextBoolean()) {
            return n;
        }
        return -n;
    }

    public Vec3d getVectorByMode(@NotNull Vec3d vec3d, Vec3d vec3d2) {
        Vec3d vec3d3 = vec3d.add(vec3d2);
        switch (type.getValue()) {
            case Preserve -> vec3d3 = vec3d3.add(getWorldBorder(), 0.0, getWorldBorder());
            case Up -> vec3d3 = vec3d3.add(0.0, 1337.0, 0.0);
            case Down -> vec3d3 = vec3d3.add(0.0, -1337.0, 0.0);
            case Bounds -> vec3d3 = new Vec3d(vec3d3.x, mc.player.getY() <= 10.0 ? 255.0 : 1.0, vec3d3.z);
        }
        return vec3d3;
    }

    public void sendPackets(double x, double y, double z, boolean confirm) {
        Vec3d vec3d = new Vec3d(x, y, z);
        Vec3d vec3d2 = mc.player.getPos().add(vec3d);
        Vec3d vec3d3 = getVectorByMode(vec3d, vec3d2);

        PlayerMoveC2SPacket packet1 =  new PlayerMoveC2SPacket.PositionAndOnGround(vec3d2.x, vec3d2.y, vec3d2.z, mc.player.isOnGround());
        movePackets.add(packet1);
        sendPacket(packet1);

        PlayerMoveC2SPacket packet2 = new PlayerMoveC2SPacket.PositionAndOnGround(vec3d3.x, vec3d3.y, vec3d3.z, mc.player.isOnGround());
        movePackets.add(packet2);
        sendPacket(packet2);
        if (confirm) {
            sendPacket(new TeleportConfirmC2SPacket(++teleportId));
            teleports.put(teleportId, new Teleport(vec3d2.x, vec3d2.y, vec3d2.z, System.currentTimeMillis()));
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (fullNullCheck()) return;
        if (mc.player != null && event.getPacket() instanceof PlayerPositionLookS2CPacket pac) {
            Teleport teleport = teleports.remove(pac.getTeleportId());
            if (
                    mc.player.isAlive()
                    && mc.world.isChunkLoaded((int) mc.player.getX() >> 4, (int) mc.player.getZ() >> 4)
                    && !(mc.currentScreen instanceof DownloadingTerrainScreen)
                    && mode.getValue() != Mode.Rubber
                    && teleport != null
                    && teleport.x == pac.getX()
                    && teleport.y == pac.getY()
                    && teleport.z == pac.getZ()
            ) {
                event.cancel();
                return;
            }
            ((IPlayerPositionLookS2CPacket) pac).setYaw(mc.player.getYaw());
            ((IPlayerPositionLookS2CPacket) pac).setPitch(mc.player.getPitch());
            teleportId = pac.getTeleportId();
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.@NotNull Send event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            if (movePackets.contains((PlayerMoveC2SPacket) event.getPacket())) {
                movePackets.remove((PlayerMoveC2SPacket) event.getPacket());
                return;
            }
            event.cancel();
        }
    }

    @Override
    public void onUpdate() {
        teleports.entrySet().removeIf(PacketFly::Method4295);
    }

    private static boolean Method4295(Object o) {
        return System.currentTimeMillis() - ((Teleport) ((Map.Entry) o).getValue()).time > TimeUnit.SECONDS.toMillis(30L);
    }

    @EventHandler
    public void onMove(@NotNull EventMove event) {
        if (!event.isCancelled()) {
            if (mode.getValue() != Mode.Rubber && teleportId == 0) {
                return;
            }
            event.cancel();
            event.setX(mc.player.getVelocity().x);
            event.setY(mc.player.getVelocity().y);
            event.setZ(mc.player.getVelocity().z);
            if (phase.getValue() != Phase.Off && (phase.getValue() == Phase.Semi || mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.0625, -0.0625, -0.0625)).iterator().hasNext())) {
                mc.player.noClip = true;
            }
        }
    }

    @EventHandler
    public void onSync(EventSync eventPlayerUpdateWalking) {
        if (timer.getValue() != 1.0) {
            ThunderHack.TICK_TIMER = timer.getValue();
        }
        mc.player.setVelocity(0.0, 0.0, 0.0);
        if (mode.getValue() != Mode.Rubber && teleportId == 0) {
            if (getTickCounter(4)) {
                sendPackets(0.0, 0.0, 0.0, false);
            }
            return;
        }
        boolean bl = mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.0625, -0.0625, -0.0625)).iterator().hasNext();
        double d = 0.0;
        d = mc.player.input.jumping && (bl || !MovementUtility.isMoving()) ? ((antiKick.getValue()) && !bl ? getTickCounter(mode.getValue() == Mode.Rubber ? 10 : 20) ? -0.032 : 0.062 : 0.062) : (mc.player.input.sneaking ? -0.062 : (!bl ? (getTickCounter(4) ? ((antiKick.getValue()) ? -0.04 : 0.0) : 0.0) : 0.0));
        if (phase.getValue() == Phase.Full && bl && MovementUtility.isMoving() && d != 0.0) {
            d = mc.player.input.jumping ? (d /= 2.5) : (d /= 1.5);
        }
        double[] dArray = MovementUtility.forward(phase.getValue() == Phase.Full && bl ? 0.034444444444444444 : (double) (speed.getValue()) * 0.26);

        int factorInt = 1;
        if (mode.getValue() == Mode.Factor && mc.player.age % increaseTicks.getValue() == 0) {
            factorInt = (int) Math.floor(factor.getValue());
            factorTicks++;
            if (factorTicks > (int) (20D / ((factor.getValue() - (double) factorInt) * 20D))) {
                factorInt += 1;
                factorTicks = 0;
            }
        }

        for (int i = 1; i <= factorInt; ++i) {
            if (mode.getValue() == Mode.Limit) {
                if (mc.player.age % 2 == 0) {
                    if (flip && d >= 0.0) {
                        flip = false;
                        d = -0.032;
                    }
                    mc.player.setVelocity(dArray[0] * (double) i,d * (double) i,dArray[1] * (double) i);
                    sendPackets(mc.player.getVelocity().x, mc.player.getVelocity().y, mc.player.getVelocity().z, !limit.getValue());
                    continue;
                }
                if (!(d < 0.0)) continue;
                flip = true;
                continue;
            }
            mc.player.setVelocity(dArray[0] * (double) i,d * (double) i,dArray[1] * (double) i);
            sendPackets(mc.player.getVelocity().x, mc.player.getVelocity().y, mc.player.getVelocity().z, mode.getValue() != Mode.Rubber);
        }
    }

    public enum Mode {
        Fast, Factor, Rubber, Limit
    }

    public enum Phase {
        Full, Off, Semi
    }

    public enum Type {
        Preserve, Up, Down, Bounds
    }

    public record Teleport(double x, double y, double z, long time){}
}
