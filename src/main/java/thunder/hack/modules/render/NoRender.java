package thunder.hack.modules.render;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.ParticleEvent;
import thunder.hack.modules.Module;
import thunder.hack.notification.Notification;
import thunder.hack.notification.NotificationManager;
import thunder.hack.setting.Setting;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.ElderGuardianAppearanceParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;

public class NoRender extends Module {
    public NoRender() {
        super("NoRender", "NoRender", Category.RENDER);
    }

    public Setting<Boolean> auto = new Setting<>("Auto", false);
    public Setting<Boolean> potions = new Setting<>("Potions", false);
    public Setting<Boolean> xp = new Setting<>("XP", false);
    public Setting<Boolean> arrows = new Setting<>("Arrows", false);
    public Setting<Boolean> eggs = new Setting<>("Eggs", false);

    public Setting<Boolean> elderGuardian = new Setting<>("Guardian", false);
    public Setting<Boolean> explosions = new Setting<>("Explosions", false);
    public Setting<Boolean> campFire = new Setting<>("CampFire", false);
    public Setting<Boolean> fireworks = new Setting<>("Fireworks", false);
    public static Setting<Boolean> armor = new Setting<>("Armor", false);
    public static Setting<Boolean> bossbar = new Setting<>("Bossbar", false);
    public static Setting<Boolean> hurtCam = new Setting<>("HurtCam", false);
    public static Setting<Boolean> fireOverlay = new Setting<>("FireOverlay", false);
    public static Setting<Boolean> waterOverlay = new Setting<>("WaterOverlay", false);
    public static Setting<Boolean> blockOverlay = new Setting<>("BlockOverlay", false);
    public static Setting<Boolean> nausea = new Setting<>("Nausea", false);
    public static Setting<Boolean> blindness = new Setting<>("Blindness", false);
    public static Setting<Boolean> fog = new Setting<>("Fog", false);
    public static Setting<Boolean> darkness = new Setting<>("Darkness", false);
    public Setting<Boolean> items = new Setting<>("Items", false);
    public Setting<Boolean> crystals = new Setting<>("Crystals", false);
    public static Setting<Boolean> fireEntity = new Setting<>("FireEntity", true);
    public Setting<Boolean> antiTitle = new Setting<>("AntiTitle", false);


    int potionCouter, xpCounter, arrowCounter, itemsCounter;


    @Subscribe
    public void onPacketReceive(PacketEvent.Receive e){
        if(e.getPacket() instanceof TitleS2CPacket && antiTitle.getValue()){
            e.cancel();
        }
    }

    @Subscribe
    public void onSync(EventSync e){
        for(Entity ent : mc.world.getEntities()){
            if(ent instanceof PotionEntity){
                potionCouter++;
                if(potions.getValue())
                    mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if(ent instanceof ExperienceBottleEntity){
                xpCounter++;
                if(xp.getValue())
                    mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if(ent instanceof EndCrystalEntity){
                if(crystals.getValue())
                    mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if(ent instanceof ArrowEntity){
                arrowCounter++;
                if(arrows.getValue())
                    mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if(ent instanceof EggEntity){
                if(eggs.getValue())
                    mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if(ent instanceof ItemEntity){
                itemsCounter++;
                if(items.getValue())
                    mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
        }
        if(auto.getValue()){


            if(arrowCounter > 64){
                Thunderhack.notificationManager.publicity("NoRender","Превышен лимит стрел! Удаляю...",3, Notification.Type.SUCCESS);
            }
            if(itemsCounter > 16){
                Thunderhack.notificationManager.publicity("NoRender","Превышен лимит вещей! Удаляю...",3, Notification.Type.SUCCESS);
            }
            if(xpCounter > 16){
                Thunderhack.notificationManager.publicity("NoRender","Превышен лимит пузырьков опыта! Удаляю...",3, Notification.Type.SUCCESS);
            }
            if(potionCouter > 8){
                Thunderhack.notificationManager.publicity("NoRender","Превышен лимит зелий! Удаляю...",3, Notification.Type.SUCCESS);
            }

            for(Entity ent : mc.world.getEntities()){
                if(ent instanceof ArrowEntity && arrowCounter > 64){
                    mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
                }
                if(ent instanceof ItemEntity && itemsCounter > 16){
                    mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
                }
                if(ent instanceof ExperienceBottleEntity && xpCounter > 16){
                    mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
                }
                if(ent instanceof PotionEntity && potionCouter > 8){
                    mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
                }
            }
        }
        arrowCounter = 0;
        itemsCounter = 0;
        potionCouter = 0;
        xpCounter = 0;
    }



    @Subscribe
    public void onParticle(ParticleEvent.AddParticle event) {
        if (elderGuardian.getValue() && event.particle instanceof ElderGuardianAppearanceParticle) {
            event.setCancelled(true);
        } else if (explosions.getValue() && event.particle instanceof ExplosionLargeParticle) {
            event.setCancelled(true);
        } else if (campFire.getValue() && event.particle instanceof CampfireSmokeParticle) {
            event.setCancelled(true);
        } else if (fireworks.getValue() && (event.particle instanceof FireworksSparkParticle.FireworkParticle || event.particle instanceof FireworksSparkParticle.Flash)) {
            event.setCancelled(true);
        }
    }
}
