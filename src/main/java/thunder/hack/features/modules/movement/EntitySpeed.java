package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventPlayerTravel;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class EntitySpeed extends Module {
    public EntitySpeed() {
        super("EntitySpeed", Category.MOVEMENT);
    }

    private final Setting<Boolean> accelerate = new Setting<>("Accelerate", true);
    private final Setting<Float> accelerateFactor = new Setting<>("AccelerateFactor", 9f, 0f, 20f, v -> accelerate.getValue());
    private final Setting<Boolean> stopunloaded = new Setting<>("StopUnloaded", true);
    private final Setting<Float> speed = new Setting<>("Speed", 0.77f, 0.1f, 5f);
    private final Setting<Float> timer = new Setting<>("Timer", 1f, 0.1f, 5f);
    private final Setting<Float> jitter = new Setting<>("Jitter", 0.05f, 0.0f, 0.5f);

    private int ticks;
    private float acceleration;

    @Override
    public void onEnable() {
        ticks = 0;
        acceleration = 0f;
    }

    @Override
    public void onDisable() {
        ThunderHack.TICK_TIMER = 1f;
    }

    @EventHandler
    public void onPlayerTravel(@NotNull EventPlayerTravel ev) {
        if (!ev.isPre()) return;
        if (fullNullCheck()) return;

        Entity entity = mc.player.getControllingVehicle();

        if (entity == null) return;
        if ((!mc.world.isChunkLoaded((int) entity.getPos().getX() >> 4, (int) entity.getPos().getZ() >> 4) || entity.getPos().getY() < -60) && stopunloaded.getValue())
            return;

        if (entity.horizontalCollision || mc.player.horizontalCollision)
            acceleration = 0;

        if (timer.getValue() != 1.0f)
            ThunderHack.TICK_TIMER = timer.getValue();

        double[] motion = MovementUtility.forward(getSpeed());
        double predictedX = entity.getX() + motion[0];
        double predictedZ = entity.getZ() + motion[1];

        if ((!mc.world.isChunkLoaded((int) predictedX >> 4, (int) predictedZ >> 4) || entity.getPos().getY() < -60) && stopunloaded.getValue())
            return;

        if (MovementUtility.isMoving()) entity.setVelocity(motion[0], entity.getVelocity().getY(), motion[1]);
        else entity.setVelocity(0, entity.getVelocity().getY(), 0);

        if (ticks++ > 50)
            ticks = 0;

        ev.cancel();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket)
            acceleration = 0;
    }

    private float getSpeed() {
        float baseSpeed = Math.min((acceleration = (acceleration + (20f - accelerateFactor.getValue()) / speed.getValue())) / 100.0F, speed.getValue());
        if (!accelerate.getValue())
            baseSpeed = speed.getValue();
        baseSpeed += (ticks > 25 ? jitter.getValue() : 0);
        return baseSpeed;
    }
}
