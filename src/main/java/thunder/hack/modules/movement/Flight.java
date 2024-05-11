package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Formatting;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

import java.text.Format;

import static thunder.hack.modules.client.ClientSettings.isRu;

public class Flight extends Module {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Vanilla);
    private final Setting<Float> hSpeed = new Setting<>("Horizontal", 1f, 0.0f, 10.0f, v -> !mode.is(Mode.StormBreak));
    private final Setting<Float> vSpeed = new Setting<>("Vertical", 0.78F, 0.0F, 5F, v -> !mode.is(Mode.StormBreak));
    private final Setting<Float> boostValue = new Setting<>("Boost", 1f, 0.1F, 1f, v -> mode.is(Mode.StormBreak));
    private final Setting<Boolean> autoToggle = new Setting<>("AutoToggle", false, v -> mode.is(Mode.MatrixJump));

    private double prevX, prevY, prevZ;
    public boolean onPosLook = false;
    private int flyTicks = 0;

    public Flight() {
        super("Flight", Module.Category.MOVEMENT);
    }

    @EventHandler
    public void onEventSync(EventSync event) {
        switch (mode.getValue()) {
            case Vanilla -> {
                if (MovementUtility.isMoving()) {
                    final double[] dir = MovementUtility.forward(hSpeed.getValue());
                    mc.player.setVelocity(dir[0], 0, dir[1]);
                } else mc.player.setVelocity(0, 0, 0);

                if (mc.options.jumpKey.isPressed())
                    mc.player.setVelocity(mc.player.getVelocity().add(0, vSpeed.getValue(), 0));
                if (mc.options.sneakKey.isPressed())
                    mc.player.setVelocity(mc.player.getVelocity().add(0, -vSpeed.getValue(), 0));
            }

            case AirJump -> {
                if (MovementUtility.isMoving() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(0.5, 0.0, 0.5).offset(0.0, -1.0, 0.0)).iterator().hasNext()) {
                    mc.player.setOnGround(true);
                    mc.player.jump();
                }
            }

            case MatrixGlide -> {
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                    flyTicks = 5;
                } else if (flyTicks > 0) {
                    if (MovementUtility.isMoving()) {
                        final double[] dir = MovementUtility.forward(hSpeed.getValue());
                        mc.player.setVelocity(dir[0], -0.04, dir[1]);
                    } else mc.player.setVelocity(0, -0.04, 0);
                    flyTicks--;
                }
            }

            case StormBreak -> {
                if(mc.player.age % 60 == 0)
                    sendMessage(Formatting.RED + (isRu() ? "В этом режиме нужно ломать блоки!" : "In this mode you need to break blocks!"));
            }
        }
    }

    @Override
    public void onUpdate() {
        if (mode.getValue() != Mode.MatrixJump) return;
        mc.player.getAbilities().flying = false;
        mc.player.setVelocity(0.0, 0.0, 0.0);

        if (mc.options.jumpKey.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0, vSpeed.getValue(), 0));
        if (mc.options.sneakKey.isPressed())
            mc.player.setVelocity(mc.player.getVelocity().add(0, -vSpeed.getValue(), 0));

        final double[] dir = MovementUtility.forward(hSpeed.getValue());
        mc.player.setVelocity(dir[0], mc.player.getVelocity().getY(), dir[1]);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (mode.getValue() == Mode.MatrixJump) {
            if (fullNullCheck()) return;
            if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
                onPosLook = true;
                prevX = mc.player.getVelocity().getX();
                prevY = mc.player.getVelocity().getY();
                prevZ = mc.player.getVelocity().getZ();
            }
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (mode.getValue() == Mode.MatrixJump) {
            if (e.getPacket() instanceof PlayerMoveC2SPacket.Full) {
                if (onPosLook) {
                    mc.player.setVelocity(prevX, prevY, prevZ);
                    onPosLook = false;
                    if (autoToggle.getValue()) disable();
                }
            }
        }

        if (mode.is(Mode.StormBreak) && e.getPacket() instanceof PlayerActionC2SPacket pac && (pac.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK
                || pac.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK && mc.world.getBlockState(pac.getPos()).isReplaceable())) {
            final double[] dir = MovementUtility.forward(2.0f * boostValue.getValue());
            mc.player.setVelocity(dir[0], 3f * boostValue.getValue(), dir[1]);
        }
    }

    private enum Mode {
        Vanilla, MatrixJump, AirJump, MatrixGlide, StormBreak
    }
}