package thunder.hack.modules.movement;



import com.google.common.eventbus.Subscribe;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import thunder.hack.utility.MovementUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PacketFly extends Module {
    public PacketFly() {
        super("PacketFly", "PacketFly", Category.MOVEMENT);
    }

    public Setting<Boolean> limit = new Setting<>("Limit", true);
    public Setting<Boolean> antiKick = new Setting<>("AntiKick", true);

    public Setting<Float> speed = new Setting("Speed", 1.0f, 0.0f, 3.0f);
    public Setting<Float> timer = new Setting("Timer", 1f, 0.0f, 2f);
    public Setting<Integer> increaseTicks = new Setting("IncreaseTicks", 20, 1, 20);
    public Setting<Integer> factor = new Setting("Factor", 1, 1, 10);
    private final Setting<Mode> mode = new Setting("Mode", Mode.Fast);
    private final Setting<Phase> phase = new Setting("Phase", Phase.Full);
    private final Setting<Type> type = new Setting("Type", Type.Preserve);
    private int teleportId = -1;
    private final ArrayList<PlayerMoveC2SPacket> movePackets = new ArrayList<>();
    private final Random rnd = new Random();
    private final ConcurrentHashMap teleports = new ConcurrentHashMap();
    private int ticks = 0;
    private boolean flip = false;


    public static double[] getSpeedDirection(double d) {
        double d2 = mc.player.input.movementForward;
        double d3 = mc.player.input.movementSideways;
        float f = mc.player.getYaw();
        double[] dArray = new double[2];
        if (d2 == 0.0 && d3 == 0.0) {
            dArray[0] = 0.0;
            dArray[1] = 0.0;
        } else {
            if (d2 != 0.0) {
                if (d3 > 0.0) {
                    f += (float) (d2 > 0.0 ? -45 : 45);
                } else if (d3 < 0.0) {
                    f += (float) (d2 > 0.0 ? 45 : -45);
                }
                d3 = 0.0;
                if (d2 > 0.0) {
                    d2 = 1.0;
                } else if (d2 < 0.0) {
                    d2 = -1.0;
                }
            }
            double cos = Math.cos(Math.toRadians(f + 90.0f));
            double sin = Math.sin(Math.toRadians(f + 90.0f));
            dArray[0] = d2 * d * cos + d3 * d * sin;
            dArray[1] = d2 * d * sin - d3 * d * cos;
        }
        return dArray;
    }

    @Override
    public void onEnable() {
        teleportId = -1;
        if (fullNullCheck() && mc.player != null) {
            ticks = 0;
            teleportId = 0;
            movePackets.clear();
            teleports.clear();
        }

    }

    @Override
    public void onDisable() {
        Thunderhack.TICK_TIMER = (1.0f);
    }




    public boolean getTickCounter(int n) {
        ++ticks;
        if (ticks >= n) {
            ticks = 0;
            return true;
        }
        return false;
    }

    private int Method4291() {
        if (mc.isInSingleplayer()) {
            return 1;
        }
        int n = rnd.nextInt(29000000);
        if (rnd.nextBoolean()) {
            return n;
        }
        return -n;
    }

    public Vec3d getVectorByMode(Vec3d vec3d, Vec3d vec3d2) {
        Vec3d vec3d3 = vec3d.add(vec3d2);
        switch ((this.type.getValue())) {
            case Preserve -> {
                vec3d3 = vec3d3.add(this.Method4291(), 0.0, this.Method4291());
            }
            case Up -> {
                vec3d3 = vec3d3.add(0.0, 1337.0, 0.0);
            }
            case Down -> {
                vec3d3 = vec3d3.add(0.0, -1337.0, 0.0);
            }
            case Bounds -> {
                vec3d3 = new Vec3d(vec3d3.x, mc.player.getY() <= 10.0 ? 255.0 : 1.0, vec3d3.z);
            }
        }
        return vec3d3;
    }

    public void Method4293(double x, double y, double z, boolean confirm) {
        Vec3d vec3d = new Vec3d(x, y, z);
        Vec3d vec3d2 = mc.player.getPos().add(vec3d);
        Vec3d vec3d3 = this.getVectorByMode(vec3d, vec3d2);
        PlayerMoveC2SPacket packet1 =  new PlayerMoveC2SPacket.PositionAndOnGround(vec3d2.x, vec3d2.y, vec3d2.z, mc.player.isOnGround());
        movePackets.add(packet1);
        mc.player.networkHandler.sendPacket(packet1);
        PlayerMoveC2SPacket packet2 = new PlayerMoveC2SPacket.PositionAndOnGround(vec3d3.x, vec3d3.y, vec3d3.z, mc.player.isOnGround());
        movePackets.add(packet2);
        mc.player.networkHandler.sendPacket(packet2);
        if (confirm) {
            mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(++teleportId));
            teleports.put(teleportId, new Teleport(vec3d2.x, vec3d2.y, vec3d2.z, System.currentTimeMillis()));
        }
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive event) {
        if (fullNullCheck()) {
            return;
        }
        if (mc.player != null && event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket pac = event.getPacket();
            Teleport teleport = (Teleport) teleports.remove(pac.getTeleportId());
            if (
                    mc.player.isAlive()
                    && mc.world.isChunkLoaded((int) mc.player.getX(), (int) mc.player.getZ())
                    && !(mc.currentScreen instanceof DownloadingTerrainScreen)
                    && this.mode.getValue() != Mode.Rubber
                    && teleport != null
                    && teleport.getPosX() == pac.getX()
                    && teleport.getPosY() == pac.getY()
                    && teleport.getPosZ() == pac.getZ()
            ) {

                event.setCancelled(true);
                return;
            }
            //    yawBypass(sPacketPlayerPosLook,mc.player.getYaw());
            //  pitchBypass(sPacketPlayerPosLook,mc.player.getPitch());
            teleportId = pac.getTeleportId();
        }
    }


    @Subscribe
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            if (movePackets.contains((PlayerMoveC2SPacket) event.getPacket())) {
                movePackets.remove((PlayerMoveC2SPacket) event.getPacket());
                return;
            }
            event.setCancelled(true);
        }
    }

    @Override
    public void onUpdate() {
        this.teleports.entrySet().removeIf(PacketFly::Method4295);
    }

    private static boolean Method4295(Object o) {
        return System.currentTimeMillis() - ((Teleport) ((Map.Entry) o).getValue()).getTime() > TimeUnit.SECONDS.toMillis(30L);
    }

    @Subscribe
    public void onMove(EventMove event) {
        if (!event.isCancelled()) {
            if (this.mode.getValue() != Mode.Rubber && teleportId == 0) {
                return;
            }
            event.setCancelled(true);
            event.set_x(mc.player.getVelocity().x);
            event.set_y(mc.player.getVelocity().y);
            event.set_z(mc.player.getVelocity().z);
            if (this.phase.getValue() != Phase.Off && (phase.getValue() == Phase.Semi || mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.0625, -0.0625, -0.0625)).iterator().hasNext())) {
                mc.player.noClip = true;
            }
        }
    }

    @Subscribe
    public void onSync(EventSync eventPlayerUpdateWalking) {
        if (timer.getValue() != 1.0) {
            Thunderhack.TICK_TIMER = timer.getValue();
        }
        mc.player.setVelocity(0.0, 0.0, 0.0);
        if (this.mode.getValue() != Mode.Rubber && teleportId == 0) {
            if (getTickCounter(4)) {
                Method4293(0.0, 0.0, 0.0, false);
            }
            return;
        }
        boolean bl = mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.0625, -0.0625, -0.0625)).iterator().hasNext();
        double d = 0.0;
        d = mc.player.input.jumping && (bl || !MovementUtil.isMoving()) ? ((this.antiKick.getValue()) && !bl ? this.getTickCounter(this.mode.getValue() == Mode.Rubber ? 10 : 20) ? -0.032 : 0.062 : 0.062) : (mc.player.input.sneaking ? -0.062 : (!bl ? (getTickCounter(4) ? ((this.antiKick.getValue()) ? -0.04 : 0.0) : 0.0) : 0.0));
        if (this.phase.getValue() == Phase.Full && bl && MovementUtil.isMoving() && d != 0.0) {
            d = mc.player.input.jumping ? (d /= 2.5) : (d /= 1.5);
        }
        double[] dArray = getSpeedDirection(this.phase.getValue() == Phase.Full && bl ? 0.034444444444444444 : (double) (this.speed.getValue()) * 0.26);
        int n = 1;
        if (this.mode.getValue() == Mode.Factor && mc.player.age % this.increaseTicks.getValue() == 0) {
            n = this.factor.getValue();
        }
        for (int i = 1; i <= n; ++i) {
            if (this.mode.getValue() == Mode.Limit) {
                if (mc.player.age % 2 == 0) {
                    if (flip && d >= 0.0) {
                        flip = false;
                        d = -0.032;
                    }
                    mc.player.setVelocity(dArray[0] * (double) i,d * (double) i,dArray[1] * (double) i);
                    this.Method4293(mc.player.getVelocity().x, mc.player.getVelocity().y, mc.player.getVelocity().z, !this.limit.getValue());
                    continue;
                }
                if (!(d < 0.0)) continue;
                flip = true;
                continue;
            }
            mc.player.setVelocity(dArray[0] * (double) i,d * (double) i,dArray[1] * (double) i);
            this.Method4293(mc.player.getVelocity().x, mc.player.getVelocity().y, mc.player.getVelocity().z, this.mode.getValue() != Mode.Rubber);
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

    public static class Teleport {
        private final double posX;
        private final double posY;
        private final double posZ;
        private final long time;

        public Teleport(double x, double y, double z, long time) {
            this.posX = x;
            this.posY = y;
            this.posZ = z;
            this.time = time;
        }

        double getPosX() {
            return this.posX;
        }

        double getPosY() {
            return this.posY;
        }

        double getPosZ() {
            return this.posZ;
        }

        long getTime() {
            return this.time;
        }
    }
}
