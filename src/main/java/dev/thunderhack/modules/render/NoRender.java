package dev.thunderhack.modules.render;

import dev.thunderhack.event.events.EventSync;
import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.modules.Module;
import dev.thunderhack.notification.Notification;
import dev.thunderhack.setting.Setting;
import meteordevelopment.orbit.EventHandler;
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
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.event.events.ParticleEvent;

import java.util.ArrayList;
import java.util.List;

import static dev.thunderhack.modules.client.MainSettings.isRu;

public class NoRender extends Module {
    public NoRender() {
        super("NoRender", Category.RENDER);
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
    public Setting<Boolean> antiPlayerCollision = new Setting<>("AntiPlayerCollision", true);

    int potionCouter, xpCounter, arrowCounter, itemsCounter;

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof TitleS2CPacket && antiTitle.getValue()) {
            e.cancel();
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        for (Entity ent : ThunderHack.asyncManager.getAsyncEntities()) {
            if (ent instanceof PotionEntity) {
                potionCouter++;
                if (potions.getValue()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if (ent instanceof ExperienceBottleEntity) {
                xpCounter++;
                if (xp.getValue()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if (ent instanceof EndCrystalEntity) {
                if (crystals.getValue()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if (ent instanceof ArrowEntity) {
                arrowCounter++;
                if (arrows.getValue()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if (ent instanceof EggEntity) {
                if (eggs.getValue()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if (ent instanceof ItemEntity) {
                itemsCounter++;
                if (items.getValue()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
        }

        if (auto.getValue()) {
            if (arrowCounter > 64)
                ThunderHack.notificationManager.publicity("NoRender", isRu() ? "Превышен лимит стрел! Удаляю..." : "Arrows limit reached! Removing...", 3, Notification.Type.SUCCESS);

            if (itemsCounter > 16)
                ThunderHack.notificationManager.publicity("NoRender", isRu() ? "Превышен лимит вещей! Удаляю..." : "Item limit reached! Removing...", 3, Notification.Type.SUCCESS);

            if (xpCounter > 16)
                ThunderHack.notificationManager.publicity("NoRender", isRu() ? "Превышен лимит пузырьков опыта! Удаляю..." : "XP orbs limit reached! Removing...", 3, Notification.Type.SUCCESS);

            if (potionCouter > 8)
                ThunderHack.notificationManager.publicity("NoRender", isRu() ? "Превышен лимит зелий! Удаляю..." : "Potions limit reached! Removing...", 3, Notification.Type.SUCCESS);


            List<Integer> toRemove = new ArrayList<>();

            for (Entity ent  : ThunderHack.asyncManager.getAsyncEntities()) {
                if (ent instanceof ArrowEntity && arrowCounter > 64) toRemove.add(ent.getId());
                if (ent instanceof ItemEntity && itemsCounter > 16) toRemove.add(ent.getId());
                if (ent instanceof ExperienceBottleEntity && xpCounter > 16) toRemove.add(ent.getId());
                if (ent instanceof PotionEntity && potionCouter > 8) toRemove.add(ent.getId());
            }

            try {
                toRemove.forEach(id -> mc.world.removeEntity(id, Entity.RemovalReason.KILLED));
            } catch (Exception ignored) {
            }
        }

        arrowCounter = 0;
        itemsCounter = 0;
        potionCouter = 0;
        xpCounter = 0;
    }


    @EventHandler
    public void onParticle(ParticleEvent.AddParticle e) {
        if (elderGuardian.getValue() && e.particle instanceof ElderGuardianAppearanceParticle) e.setCancelled(true);
        else if (explosions.getValue() && e.particle instanceof ExplosionLargeParticle) e.setCancelled(true);
        else if (campFire.getValue() && e.particle instanceof CampfireSmokeParticle) e.setCancelled(true);
        else if (fireworks.getValue() && (e.particle instanceof FireworksSparkParticle.FireworkParticle || e.particle instanceof FireworksSparkParticle.Flash)) e.setCancelled(true);
    }
}
