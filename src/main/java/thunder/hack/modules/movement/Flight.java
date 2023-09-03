package thunder.hack.modules.movement;


import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class Flight extends Module {
    public boolean pendingFlagApplyPacket = false;
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Vanilla);
    public Setting<Float> speed = new Setting<>("Speed", 0.1f, 0.0f, 10.0f, v -> mode.getValue() == Mode.Vanilla);
    public Setting<Float> vspeedValue = new Setting<>("Vertical", 0.78F, 0.0F, 5F, v -> mode.getValue() == Mode.MatrixJump);
    public Setting<Boolean> autoToggle = new Setting<>("AutoToggle", false, v -> mode.getValue() == Mode.MatrixJump);
    private double lastMotionX = 0.0;
    private double lastMotionY = 0.0;
    private double lastMotionZ = 0.0;


    public Flight() {
        super("Flight", "Makes you fly.", Module.Category.MOVEMENT);
    }

    @EventHandler
    public void onEventSync(EventSync event) {
        if (mode.getValue() == Mode.Vanilla) {
            if (MovementUtility.isMoving()) {
                final double[] dir = MovementUtility.forward(this.speed.getValue());
                mc.player.setVelocity(dir[0],0,dir[1]);
            } else {
                mc.player.setVelocity(0,0,0);
            }
            if (mc.options.jumpKey.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0,speed.getValue(),0));
            if (mc.options.sneakKey.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0,-speed.getValue(),0));

        } else if (mode.getValue() == Mode.AirJump) {
            if (MovementUtility.isMoving() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(0.5, 0.0, 0.5).offset(0.0, -1.0, 0.0)).iterator().hasNext()) {
                mc.player.setOnGround(true);
                mc.player.jump();
            }
        }
    }

    @Override
    public void onUpdate() {
        if (mode.getValue() != Mode.MatrixJump) {
            return;
        }
        mc.player.getAbilities().flying = false;
        mc.player.setVelocity(0.0, 0.0, 0.0);

        if (mc.options.jumpKey.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0,vspeedValue.getValue(),0));

        if (mc.options.sneakKey.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0,-vspeedValue.getValue(),0));

        final double[] dir = MovementUtility.forward(speed.getValue());
        mc.player.setVelocity(dir[0],mc.player.getVelocity().getY(),dir[1]);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (mode.getValue() != Mode.MatrixJump) {
            return;
        }
        if (fullNullCheck()) {
            return;
        }
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            pendingFlagApplyPacket = true;
            lastMotionX = mc.player.getVelocity().getX();
            lastMotionY = mc.player.getVelocity().getY();
            lastMotionZ = mc.player.getVelocity().getZ();
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (mode.getValue() == Mode.MatrixJump) {

            if (e.getPacket() instanceof PlayerMoveC2SPacket.Full) {
                if (pendingFlagApplyPacket) {
                    mc.player.setVelocity(lastMotionX,lastMotionY,lastMotionZ);
                    pendingFlagApplyPacket = false;
                    if (autoToggle.getValue())
                        disable();
                }
            }
        }
    }

    private enum Mode {
        Vanilla, MatrixJump, AirJump
    }
}