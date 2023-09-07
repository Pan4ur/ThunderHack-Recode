package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.PushEvent;
import thunder.hack.injection.accesors.IExplosionS2CPacket;
import thunder.hack.injection.accesors.ISPacketEntityVelocity;
import thunder.hack.modules.Module;
import thunder.hack.modules.combat.Aura;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.*;

public class Velocity extends Module {

    /*
    TY <3
    https://github.com/SkidderMC/FDPClient/blob/main/src/main/java/net/ccbluex/liquidbounce/features/module/modules/combat/velocitys/vanilla/JumpVelocity.kt
     */

    public Setting<Boolean> onlyAura = new Setting<>("OnlyAura", false);
    public Setting<Boolean> autoDisable = new Setting<>("DisableOnVerify", false);
    public static Setting<Boolean> noPush = new Setting<>("NoPush", false);
    private final Setting<modeEn> mode = new Setting<>("Mode", modeEn.Matrix);
    public Setting<Float> vertical = new Setting<>("Vertical", 0.0f, 0.0f, 100.0f, v -> mode.getValue() == modeEn.Custom);
    private final Setting<jumpModeEn> jumpMode = new Setting<>("JumpMode", jumpModeEn.Jump, v -> mode.getValue() == modeEn.Jump);
    public Setting<Float> horizontal = new Setting<>("Horizontal", 0.0f, 0.0f, 100.0f, v -> mode.getValue() == modeEn.Custom || mode.getValue() == modeEn.Jump);
    public Setting<Float> motion = new Setting<>("Motion", .42f, 0.4f, 0.5f, v -> mode.getValue() == modeEn.Jump);
    public Setting<Boolean> fail = new Setting<>("SmartFail", true, v -> mode.getValue() == modeEn.Jump);
    public Setting<Float> failRate = new Setting<>("FailRate", 0.3f, 0.0f, 1.0f, v -> mode.getValue() == modeEn.Jump && fail.getValue());
    public Setting<Float> jumpRate = new Setting<>("FailJumpRate", 0.25f, 0.0f, 1.0f, v -> mode.getValue() == modeEn.Jump && fail.getValue());

    private boolean doJump, failJump, skip, flag;
    private int grimTicks = 0;


    public Velocity() {
        super("Velocity", "акэбэшка", Module.Category.MOVEMENT);
    }


    @Override
    public void onDisable() {
        // Blocks.ICE.slipperiness = 0.98f;
        // Blocks.PACKED_ICE.slipperiness = 0.98f;
        //Blocks.FROSTED_ICE.slipperiness = 0.98f;
    }


    @EventHandler
    public void onPacketReceived(PacketEvent.Receive event) {

        if (fullNullCheck()) return;

        if (event.getPacket() instanceof GameMessageS2CPacket && autoDisable.getValue()) {
            String text = ((GameMessageS2CPacket) event.getPacket()).content().getString();
            if (text.contains("Тебя проверяют на чит АКБ, ник хелпера - ")) {
                disable();
            }
        }

        if (event.getPacket() instanceof EntityStatusS2CPacket pac  && (pac = event.getPacket()).getStatus() == 31 && pac.getEntity(Velocity.mc.world) instanceof FishingBobberEntity) {
            FishingBobberEntity fishHook = (FishingBobberEntity) pac.getEntity(Velocity.mc.world) ;
            if (fishHook.getHookedEntity() == Velocity.mc.player) {
                event.setCancelled(true);
            }
        }

        if (event.getPacket() instanceof ExplosionS2CPacket) {
            ExplosionS2CPacket velocity_ = event.getPacket();
            if (mode.getValue() == modeEn.Custom) {
                ((IExplosionS2CPacket)velocity_).setMotionX(((IExplosionS2CPacket)velocity_).getMotionX() * horizontal.getValue() / 100f);
                ((IExplosionS2CPacket)velocity_).setMotionZ(((IExplosionS2CPacket)velocity_).getMotionZ() * horizontal.getValue() / 100f);
                ((IExplosionS2CPacket)velocity_).setMotionY(((IExplosionS2CPacket)velocity_).getMotionY() * vertical.getValue() / 100f);
            } else if (mode.getValue() == modeEn.Cancel) {
                ((IExplosionS2CPacket)velocity_).setMotionX(0);
                ((IExplosionS2CPacket)velocity_).setMotionY(0);
                ((IExplosionS2CPacket)velocity_).setMotionZ(0);
            }
        }

        if(mode.getValue() == modeEn.OldGrim){
            if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket ) {
                EntityVelocityUpdateS2CPacket  var4 = event.getPacket();
                if (var4.getId() == mc.player.getId()) {
                    event.cancel();
                    grimTicks = 6;
                }
            }
            if (event.getPacket() instanceof PlayPingS2CPacket && grimTicks > 0) {
                event.cancel();
                grimTicks--;
            }
        }

        if (onlyAura.getValue() && Thunderhack.moduleManager.get(Aura.class).isDisabled()) return;

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket pac) {
            if (pac.getId() == mc.player.getId()) {
                if(mode.getValue() == modeEn.Matrix) {
                    if (!flag) {
                        event.setCancelled(true);
                        flag = true;
                    } else {
                        flag = false;
                        ((ISPacketEntityVelocity) pac).setMotionX(((int) ((double) pac.getVelocityX() * -0.1)));
                        ((ISPacketEntityVelocity) pac).setMotionZ(((int) ((double) pac.getVelocityZ() * -0.1)));
                    }
                } else if(mode.getValue() == modeEn.Redirect){
                    int vX =  pac.getVelocityX();
                    int vZ =  pac.getVelocityZ();
                    if (vX < 0) vX *= -1;
                    if (vZ < 0) vZ *= -1;

                    double[] motion = MovementUtility.forward((vX + vZ));

                    ((ISPacketEntityVelocity) pac).setMotionX((int) (motion[0]));
                    ((ISPacketEntityVelocity) pac).setMotionY(0);
                    ((ISPacketEntityVelocity) pac).setMotionZ((int) (motion[1]));
                } else if (mode.getValue() == modeEn.Custom) {
                    ((ISPacketEntityVelocity)pac).setMotionX((int) ((float) pac.getVelocityX() * horizontal.getValue() / 100f));
                    ((ISPacketEntityVelocity)pac).setMotionY((int) ((float) pac.getVelocityY() * vertical.getValue() / 100f));
                    ((ISPacketEntityVelocity)pac).setMotionZ((int) ((float) pac.getVelocityZ() * horizontal.getValue() / 100f));
                } else if (mode.getValue() == modeEn.Sunrise) {
                    event.setCancelled(true);
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), -999.0, mc.player.getZ(), true));
                } else if (mode.getValue() == modeEn.Cancel) {
                    event.setCancelled(true);
                } else if(mode.getValue() == modeEn.Jump && mc.player.isOnGround()){
                    ((ISPacketEntityVelocity)pac).setMotionX((int) ((float) pac.getVelocityX() * horizontal.getValue() / 100f));
                    ((ISPacketEntityVelocity)pac).setMotionZ((int) ((float) pac.getVelocityZ() * horizontal.getValue() / 100f));
                }
            }
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (mode.getValue() == modeEn.Matrix) {
            if (mc.player.hurtTime > 0 && !mc.player.isOnGround()) {
                double var3 = mc.player.getYaw() * 0.017453292F;
                double var5 = Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
                mc.player.setVelocity(-Math.sin(var3) * var5,mc.player.getVelocity().y,Math.cos(var3) * var5);
                mc.player.setSprinting(mc.player.age % 2 != 0);
            }
        }
        if(mode.getValue() == modeEn.Jump){
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
                switch (jumpMode.getValue()){
                    case Jump -> mc.player.jump();
                    case Motion -> mc.player.setVelocity(mc.player.getVelocity().getX(), motion.getValue(),mc.player.getVelocity().getZ());
                    case Both -> {
                        mc.player.jump();
                        mc.player.setVelocity(mc.player.getVelocity().getX(), motion.getValue(),mc.player.getVelocity().getZ());
                    }
                }
            }
        }
        if (grimTicks > 0)
            grimTicks--;
    }

    @Override
    public void onEnable() {
        grimTicks = 0;
    }

    @EventHandler
    public void onPush(PushEvent event) {
        if(noPush.getValue()) {
            event.setPushX(event.getPushX() * 0);
            event.setPushY(event.getPushY() * 0);
            event.setPushZ(event.getPushZ() * 0);
        }
    }

    public enum modeEn {
        Matrix, Cancel, Sunrise, Custom, Redirect, OldGrim, Jump
    }

    public enum jumpModeEn {
        Motion, Jump, Both
    }
}
