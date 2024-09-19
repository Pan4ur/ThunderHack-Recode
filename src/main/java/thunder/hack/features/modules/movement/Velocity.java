package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.injection.accesors.IExplosionS2CPacket;
import thunder.hack.injection.accesors.ISPacketEntityVelocity;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

//TY <3
//https://github.com/SkidderMC/FDPClient/blob/main/src/main/java/net/ccbluex/liquidbounce/features/module/modules/combat/velocitys/vanilla/JumpVelocity.kt

public class Velocity extends Module {
    public Velocity() {
        super("Velocity", Category.MOVEMENT);
    }

    public Setting<Boolean> onlyAura = new Setting<>("OnlyDuringAura", false);
    public Setting<Boolean> pauseInWater = new Setting<>("PauseInLiquids", false);
    public Setting<Boolean> explosions = new Setting<>("Explosions", true);
    public Setting<Boolean> cc = new Setting<>("PauseOnFlag", false);
    public Setting<Boolean> fire = new Setting<>("PauseOnFire", false);
    private final Setting<modeEn> mode = new Setting<>("Mode", modeEn.Matrix);
    public Setting<Float> vertical = new Setting<>("Vertical", 0.0f, 0.0f, 100.0f, v -> mode.getValue() == modeEn.Custom);
    private final Setting<jumpModeEn> jumpMode = new Setting<>("JumpMode", jumpModeEn.Jump, v -> mode.getValue() == modeEn.Jump);
    public Setting<Float> horizontal = new Setting<>("Horizontal", 0.0f, 0.0f, 100.0f, v -> mode.getValue() == modeEn.Custom || mode.getValue() == modeEn.Jump);
    public Setting<Float> motion = new Setting<>("Motion", .42f, 0.4f, 0.5f, v -> mode.getValue() == modeEn.Jump);
    public Setting<Boolean> fail = new Setting<>("SmartFail", true, v -> mode.getValue() == modeEn.Jump);
    public Setting<Float> failRate = new Setting<>("FailRate", 0.3f, 0.0f, 1.0f, v -> mode.getValue() == modeEn.Jump && fail.getValue());
    public Setting<Float> jumpRate = new Setting<>("FailJumpRate", 0.25f, 0.0f, 1.0f, v -> mode.getValue() == modeEn.Jump && fail.getValue());

    private boolean doJump, failJump, skip, flag;
    private int grimTicks, ccCooldown;


    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck()) return;

        if (mc.player != null && (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isInLava()) && pauseInWater.getValue())
            return;

        if (mc.player != null && mc.player.isOnFire() && fire.getValue() && (mc.player.hurtTime > 0)) {
            return;
        }

        if (ccCooldown > 0) {
            ccCooldown--;
            return;
        }

        // MAIN VELOCITY
        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket pac) {
            if (pac.getId() == mc.player.getId() && (!onlyAura.getValue() || ModuleManager.aura.isEnabled())) {
                switch (mode.getValue()) {
                    case Matrix -> {
                        if (!flag) {
                            e.cancel();
                            flag = true;
                        } else {
                            flag = false;
                            ((ISPacketEntityVelocity) pac).setMotionX(((int) (pac.getVelocityX() * -0.1)));
                            ((ISPacketEntityVelocity) pac).setMotionZ(((int) (pac.getVelocityZ() * -0.1)));
                        }
                    }
                    case Redirect -> {
                        double vX = Math.abs(pac.getVelocityX());
                        double vZ = Math.abs(pac.getVelocityZ());
                        double[] motion = MovementUtility.forward((vX + vZ));
                        ((ISPacketEntityVelocity) pac).setMotionX((int) (motion[0]));
                        ((ISPacketEntityVelocity) pac).setMotionY(0);
                        ((ISPacketEntityVelocity) pac).setMotionZ((int) (motion[1]));
                    }
                    case Custom -> {
                        ((ISPacketEntityVelocity) pac).setMotionX((int) ((float) pac.getVelocityX() * horizontal.getValue() / 100f));
                        ((ISPacketEntityVelocity) pac).setMotionY((int) ((float) pac.getVelocityY() * vertical.getValue() / 100f));
                        ((ISPacketEntityVelocity) pac).setMotionZ((int) ((float) pac.getVelocityZ() * horizontal.getValue() / 100f));
                    }
                    case Sunrise -> {
                        e.cancel();
                        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), -999.0, mc.player.getZ(), true));
                    }
                    case Cancel -> e.cancel();
                    case Jump -> {
                        ((ISPacketEntityVelocity) pac).setMotionX((int) ((float) pac.getVelocityX() * horizontal.getValue() / 100f));
                        ((ISPacketEntityVelocity) pac).setMotionZ((int) ((float) pac.getVelocityZ() * horizontal.getValue() / 100f));
                    }
                    case OldGrim -> {
                        e.cancel();
                        grimTicks = 6;
                    }
                    case GrimNew -> {
                        e.cancel();
                        flag = true;
                    }
                }
            }
        }

        // EXPLOSION
        if (e.getPacket() instanceof ExplosionS2CPacket explosion && explosions.getValue()) {
            switch (mode.getValue()) {
                case Cancel -> {
                    ((IExplosionS2CPacket) explosion).setMotionX(0);
                    ((IExplosionS2CPacket) explosion).setMotionY(0);
                    ((IExplosionS2CPacket) explosion).setMotionZ(0);
                }
                case Custom -> {
                    ((IExplosionS2CPacket) explosion).setMotionX(((IExplosionS2CPacket) explosion).getMotionX() * horizontal.getValue() / 100f);
                    ((IExplosionS2CPacket) explosion).setMotionZ(((IExplosionS2CPacket) explosion).getMotionZ() * horizontal.getValue() / 100f);
                    ((IExplosionS2CPacket) explosion).setMotionY(((IExplosionS2CPacket) explosion).getMotionY() * vertical.getValue() / 100f);
                }
                case GrimNew -> {
                    ((IExplosionS2CPacket) explosion).setMotionX(0);
                    ((IExplosionS2CPacket) explosion).setMotionY(0);
                    ((IExplosionS2CPacket) explosion).setMotionZ(0);
                    flag = true;
                }
            }
        }

        // PING
        if (mode.getValue() == modeEn.OldGrim) {
            if (e.getPacket() instanceof CommonPingS2CPacket && grimTicks > 0) {
                e.cancel();
                grimTicks--;
            }
        }

        // LAGBACK
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            if (cc.getValue() || mode.getValue() == modeEn.GrimNew)
                ccCooldown = 5;
        }
    }


    @Override
    public void onUpdate() {
        if (mc.player != null && (mc.player.isTouchingWater() || mc.player.isSubmergedInWater()) && pauseInWater.getValue())
            return;

        switch (mode.getValue()) {
            case Matrix -> {
                if (mc.player.hurtTime > 0 && !mc.player.isOnGround()) {
                    double var3 = mc.player.getYaw() * 0.017453292F;
                    double var5 = Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
                    mc.player.setVelocity(-Math.sin(var3) * var5, mc.player.getVelocity().y, Math.cos(var3) * var5);
                    mc.player.setSprinting(mc.player.age % 2 != 0);
                }
            }
            case Jump -> {
                if ((failJump || mc.player.hurtTime > 6) && mc.player.isOnGround()) {
                    if (failJump) failJump = false;
                    if (!doJump) skip = true;
                    if (Math.random() <= failRate.getValue() && fail.getValue()) {
                        if (Math.random() <= jumpRate.getValue()) {
                            doJump = true;
                            failJump = true;
                        } else {
                            doJump = false;
                            failJump = false;
                        }
                    } else {
                        doJump = true;
                        failJump = false;
                    }
                    if (skip) {
                        skip = false;
                        return;
                    }
                    switch (jumpMode.getValue()) {
                        case Jump -> mc.player.jump();
                        case Motion ->
                                mc.player.setVelocity(mc.player.getVelocity().getX(), motion.getValue(), mc.player.getVelocity().getZ());
                        case Both -> {
                            mc.player.jump();
                            mc.player.setVelocity(mc.player.getVelocity().getX(), motion.getValue(), mc.player.getVelocity().getZ());
                        }
                    }
                }
            }
            case GrimNew -> {
                if (flag) {
                    if (ccCooldown <= 0) {
                        sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), ((IClientPlayerEntity) mc.player).getLastYaw(), ((IClientPlayerEntity) mc.player).getLastPitch(), mc.player.isOnGround()));
                        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, BlockPos.ofFloored(mc.player.getPos()), Direction.DOWN));
                    }
                    flag = false;
                }
            }
        }
        if (grimTicks > 0)
            grimTicks--;
    }

    private boolean isValidMotion(double motion, double min, double max) {
        return Math.abs(motion) > min && Math.abs(motion) < max;
    }

    @Override
    public void onEnable() {
        grimTicks = 0;
    }

    public enum modeEn {
        Matrix, Cancel, Sunrise, Custom, Redirect, OldGrim, Jump, GrimNew
    }

    public enum jumpModeEn {
        Motion, Jump, Both
    }
}
