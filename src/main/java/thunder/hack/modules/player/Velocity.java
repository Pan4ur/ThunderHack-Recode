package thunder.hack.modules.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.PushEvent;
import thunder.hack.injection.accesors.IExplosionS2CPacket;
import thunder.hack.injection.accesors.ISPacketEntityVelocity;
import thunder.hack.modules.Module;
import thunder.hack.modules.combat.Aura;
import thunder.hack.notification.Notification;
import thunder.hack.notification.NotificationManager;
import thunder.hack.setting.Setting;
import thunder.hack.utility.MovementUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.*;

public class Velocity extends Module {
    public Setting<Boolean> onlyAura = new Setting<>("OnlyAura", false);
    public Setting<Boolean> autoDisable = new Setting<>("DisableOnVerify", false);
    public Setting<Boolean> noPush = new Setting<>("NoPush", false);
    private final Setting<modeEn> mode = new Setting<>("Mode", modeEn.Matrix);
    public Setting<Boolean> mairixC = new Setting<>("MatrixCancel", true,v-> mode.getValue() == modeEn.Cancel);
    public Setting<Float> horizontal = new Setting<>("Horizontal", 0.0f, 0.0f, 100.0f, v -> mode.getValue() == modeEn.Custom);
    public Setting<Float> vertical = new Setting<>("Vertical", 0.0f, 0.0f, 100.0f, v -> mode.getValue() == modeEn.Custom);
    private boolean flag;
    public static int flags;
    private int grimTicks = 0;


    public Velocity() {
        super("Velocity", "акэбэшка", Module.Category.MOVEMENT);
    }


    @Override
    public void onUpdate() {
        if (mairixC.getValue()) {
            if(flags >= 90){
                Thunderhack.notificationManager.publicity("Velocity","Выключен из-за большой вероятности кика!",3, Notification.Type.INFO);
                disable();
            }
        }
    }

    @Override
    public void onDisable() {
        flags = 0;
        // Blocks.ICE.slipperiness = 0.98f;
        // Blocks.PACKED_ICE.slipperiness = 0.98f;
         //Blocks.FROSTED_ICE.slipperiness = 0.98f;
    }

    @Override
    public String getDisplayInfo(){
        return flags + "";
    }

    @Subscribe
    public void onPacketReceived(PacketEvent.Receive event) {

        if (fullNullCheck()) return;
        Entity entity;
        EntityStatusS2CPacket  packet;

        if (event.getPacket() instanceof GameMessageS2CPacket && autoDisable.getValue()) {
            String text = ((GameMessageS2CPacket) event.getPacket()).content().getString();
            if (text.contains("Тебя проверяют на чит АКБ, ник хелпера - ")) {
                toggle();
            }
        }

        if (event.getPacket() instanceof EntityStatusS2CPacket  && (packet = event.getPacket()).getStatus() == 31 && (entity = packet.getEntity(Velocity.mc.world)) instanceof FishingBobberEntity) {
            FishingBobberEntity fishHook = (FishingBobberEntity) entity;
            if (fishHook.getHookedEntity() == Velocity.mc.player) {
                event.setCancelled(true);
            }
        }

        if (event.getPacket() instanceof ExplosionS2CPacket) {
            ExplosionS2CPacket velocity_ = event.getPacket();
            if (mode.getValue() == modeEn.Custom) {
                ((IExplosionS2CPacket)velocity_).setMotionX(((IExplosionS2CPacket)velocity_).getMotionX() * this.horizontal.getValue() / 100f);
                ((IExplosionS2CPacket)velocity_).setMotionZ(((IExplosionS2CPacket)velocity_).getMotionZ() * this.horizontal.getValue() / 100f);
                ((IExplosionS2CPacket)velocity_).setMotionY(((IExplosionS2CPacket)velocity_).getMotionY() * this.vertical.getValue() / 100f);
            } else if (mode.getValue() == modeEn.Cancel) {
                flags += 10;
                ((IExplosionS2CPacket)velocity_).setMotionX(0);
                ((IExplosionS2CPacket)velocity_).setMotionY(0);
                ((IExplosionS2CPacket)velocity_).setMotionZ(0);
            }
        }


        if (onlyAura.getValue() && Thunderhack.moduleManager.get(Aura.class).isDisabled()) {
            return;
        }

        if (mode.getValue() == modeEn.Cancel && event.getPacket() instanceof EntityVelocityUpdateS2CPacket ) {
            EntityVelocityUpdateS2CPacket pac = event.getPacket();
            if (pac.getId() == mc.player.getId()) {
                if(MovementUtil.isMoving()){
                    flags += 3;
                } else {
                    flags += 6;
                }
                event.setCancelled(true);
                return;
            }
        }

        if (mode.getValue() == modeEn.Sunrise && event.getPacket() instanceof EntityVelocityUpdateS2CPacket ) {
            EntityVelocityUpdateS2CPacket pac = event.getPacket();
            if (pac.getId() == mc.player.getId()) {
                event.setCancelled(true);
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), -999.0, mc.player.getZ(), true));
                return;
            }
        }

        if (mode.getValue() == modeEn.Custom) {
            EntityVelocityUpdateS2CPacket  velocity;
            if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket  && (velocity = event.getPacket()).getId() == mc.player.getId()) {
                ((ISPacketEntityVelocity)velocity).setMotionX((int) ((float) velocity.getVelocityX() * this.horizontal.getValue() / 100f));
                ((ISPacketEntityVelocity)velocity).setMotionY((int) ((float) velocity.getVelocityY() * this.vertical.getValue() / 100f));
                ((ISPacketEntityVelocity)velocity).setMotionZ((int) ((float) velocity.getVelocityZ() * this.horizontal.getValue() / 100f));
            }
        }
        if (mode.getValue() == modeEn.Matrix) {
            if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket ) {
                EntityVelocityUpdateS2CPacket  var4 = event.getPacket();
                if (var4.getId() == mc.player.getId()) {
                    if (!flag) {
                        event.setCancelled(true);
                        flag = true;
                    } else {
                        flag = false;
                        ((ISPacketEntityVelocity)var4).setMotionX(((int) ((double) var4.getVelocityX() * -0.1)));
                        ((ISPacketEntityVelocity)var4).setMotionZ(((int) ((double) var4.getVelocityZ() * -0.1)));
                    }
                }
            }
        }
        if(mode.getValue() == modeEn.GrimAC){
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

        if(mode.getValue() == modeEn.Redirect){
            EntityVelocityUpdateS2CPacket  velocity;
            if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket  && (velocity = event.getPacket()).getId() == mc.player.getId()) {

                int vX =  velocity.getVelocityX();
                int vZ =  velocity.getVelocityZ();
                if (vX < 0) vX *= -1;
                if (vZ < 0) vZ *= -1;

                double[] motion = MovementUtil.forward((vX + vZ));


                ((ISPacketEntityVelocity) velocity).setMotionX((int) ((float) motion[0]));
                ((ISPacketEntityVelocity) velocity).setMotionY((int) (0));
                ((ISPacketEntityVelocity) velocity).setMotionZ((int) ((float) motion[1]));
            }
        }
    }

    @Subscribe
    public void onPreMotion(EventSync var1) {
        if (mode.getValue() == modeEn.Matrix) {
            if (mc.player.hurtTime > 0 && !mc.player.isOnGround()) {
                double var3 = mc.player.getYaw() * 0.017453292F;
                double var5 = Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
                mc.player.setVelocity(-Math.sin(var3) * var5,mc.player.getVelocity().y,Math.cos(var3) * var5);
                mc.player.setSprinting(mc.player.age % 2 != 0);
            }
        }
        if (grimTicks > 0) {
            grimTicks--;
        }
    }

    @Override
    public void onEnable() {
        grimTicks = 0;
    }

    @Subscribe
    public void onPush(PushEvent event) {
        if(noPush.getValue()) {
            event.setPushX(event.getPushX() * 0);
            event.setPushY(event.getPushY() * 0);
            event.setPushZ(event.getPushZ() * 0);
        }
    }


    public enum modeEn {
        Matrix, Cancel, Sunrise,Custom, Redirect,GrimAC
    }
}
