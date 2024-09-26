package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.MovementUtility;

public class AutoFlyme extends Module {
    public AutoFlyme() {
        super("AutoFlyme", Category.MISC);
    }

    public final Setting<Boolean> instantSpeed = new Setting<>("InstantSpeed", true);
    public final Setting<Boolean> hover = new Setting<>("hover", false);
    public final Setting<Boolean> useTimer = new Setting<>("UseTimer", false);

    public Setting<Float> hoverY = new Setting<>("hoverY", 0.228f, 0.0f, 1.0f, v -> hover.getValue());
    public Setting<Float> speed = new Setting<>("speed", 1.05f, 0.0f, 8f, v -> hover.getValue());

    //фаннигейм перешел на матрикс, и теперь можно летать со скоростью 582 км/ч :skull:
    private final Timer timer = new Timer();

    @Override
    public void onEnable() {
        if (!mc.player.getAbilities().flying) {
            mc.player.networkHandler.sendChatCommand("flyme");
        }
    }

    @Override
    public void onDisable() {
        ThunderHack.TICK_TIMER = 1.f;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof GameMessageS2CPacket) {
            final GameMessageS2CPacket packet = e.getPacket();
            if ((packet.content().getString().contains("Вы атаковали игрока") || packet.content().getString().contains("Возможность летать была удалена")) && timer.passedMs(1000)) {
                mc.player.networkHandler.sendChatCommand("flyme");
                mc.player.networkHandler.sendChatCommand("flyme");
                timer.reset();
            }
        }
    }

    @Override
    public void onUpdate() {
        if (useTimer.getValue()) ThunderHack.TICK_TIMER = 1.088f;
        if (!mc.player.getAbilities().flying && timer.passedMs(1000) && !mc.player.isOnGround() && mc.player.input.jumping) {
            mc.player.networkHandler.sendChatCommand("flyme");
            timer.reset();
        }
        if (!mc.options.jumpKey.isPressed() && hover.getValue() && mc.player.getAbilities().flying && mc.player.getAbilities().flying && !mc.player.isOnGround() && !mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, -hoverY.getValue(), 0.0)).iterator().hasNext()) {
            mc.player.setVelocity(mc.player.getVelocity().x, -0.05, mc.player.getVelocity().z);
        }
    }

    @EventHandler
    public void onUpdateWalkingPlayer(final EventSync event) {
        if (!instantSpeed.getValue() || !mc.player.getAbilities().flying) return;
        final double[] dir = MovementUtility.isMoving() ? MovementUtility.forward(speed.getValue()) : new double[]{0, 0};
        mc.player.setVelocity(dir[0], mc.player.getVelocity().y, dir[1]);
    }
}
