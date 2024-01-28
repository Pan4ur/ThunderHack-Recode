package thunder.hack.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;

import java.util.ArrayList;
import java.util.List;

import static thunder.hack.modules.client.MainSettings.isRu;

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
    public Setting<Boolean> vignette = new Setting<>("Vignette", true);
    public Setting<Boolean> portal = new Setting<>("Portal", true);
    public Setting<Boolean> explosions = new Setting<>("Explosions", false);
    public Setting<Boolean> campFire = new Setting<>("CampFire", false);
    public Setting<Boolean> fireworks = new Setting<>("Fireworks", false);
    public static Setting<Boolean> armor = new Setting<>("Armor", false);
    public static Setting<Boolean> bossbar = new Setting<>("Bossbar", false);
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
    public Setting<Boolean> breakParticles = new Setting<>("BreakParticles", true);
    public Setting<Boolean> antiTitle = new Setting<>("AntiTitle", false);
    public Setting<Boolean> antiPlayerCollision = new Setting<>("AntiPlayerCollision", true);
    public Setting<NoScoreBoard> noScoreBoard = new Setting<>("NoScoreBoard", NoScoreBoard.None);
    public Setting<Float> sbX = new Setting<>("BoardX", 1.0f, 0.0f, 10.0f, v-> noScoreBoard.getValue() == NoScoreBoard.Position);
    public Setting<Float> sbY = new Setting<>("BoardY", 1.0f, 0.0f, 10.0f, v-> noScoreBoard.getValue() == NoScoreBoard.Position);

    public enum NoScoreBoard {
        Off, Position, None
    }

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
}
