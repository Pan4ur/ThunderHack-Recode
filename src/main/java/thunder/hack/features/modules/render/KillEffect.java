package thunder.hack.features.modules.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KillEffect extends Module {
    public KillEffect() {
        super("KillEffect", Category.RENDER);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Orthodox);
    private final Setting<Integer> speed = new Setting<>("Y Speed", 0, -10, 10, value -> mode.getValue() == Mode.Orthodox);
    public final Setting<Integer> volume = new Setting<>("Volume", 100, 0, 100);
    private final Setting<Boolean> playSound = new Setting<>("Play Sound", true, value -> mode.getValue() == Mode.Orthodox);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(255, 255, 0, 150)), value -> mode.getValue() == Mode.Orthodox);
    private final Setting<Boolean> mobs = new Setting<>("Mobs", false);

    private final Map<Entity, Long> renderEntities = new ConcurrentHashMap<>();
    private final Map<Entity, Long> lightingEntities = new ConcurrentHashMap<>();

    private enum Mode {
        Orthodox,
        FallingLava,
        LightningBolt
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (mc.world == null) return;

        switch (mode.getValue()) {
            case Orthodox -> renderEntities.forEach((entity, time) -> {
                if (System.currentTimeMillis() - time > 3000) {
                    renderEntities.remove(entity);
                } else {
                    Render3DEngine.drawLine(entity.getPos().add(0, calculateSpeed(), 0), entity.getPos().add(0, 3 + calculateSpeed(), 0), color.getValue().getColorObject());
                    Render3DEngine.drawLine(entity.getPos().add(1, 2.3 + calculateSpeed(), 0), entity.getPos().add(-1, 2.3 + calculateSpeed(), 0), color.getValue().getColorObject());
                    Render3DEngine.drawLine(entity.getPos().add(0.5, 1.2 + calculateSpeed(), 0), entity.getPos().add(-0.5, 0.8 + calculateSpeed(), 0), color.getValue().getColorObject());
                }
            });
            case FallingLava -> renderEntities.keySet().forEach(entity -> {
                for (int i = 0; i < entity.getHeight() * 10; i++) {
                    for (int j = 0; j < entity.getWidth() * 10; j++) {
                        for (int k = 0; k < entity.getWidth() * 10; k++) {
                            mc.world.addParticle(ParticleTypes.FALLING_LAVA, entity.getX() + j * 0.1, entity.getY() + i * 0.1, entity.getZ() + k * 0.1, 0, 0, 0);
                        }
                    }
                }

                renderEntities.remove(entity);
            });
            case LightningBolt -> renderEntities.forEach((entity, time) -> {
                LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
                lightningEntity.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());
                mc.world.addEntity(lightningEntity);
                renderEntities.remove(entity);
                lightingEntities.put(entity, System.currentTimeMillis());
            });
        }
    }

    @Override
    public void onUpdate() {
        Managers.ASYNC.getAsyncEntities().forEach(entity -> {
            if (!(entity instanceof PlayerEntity) && !mobs.getValue()) return;
            if (!(entity instanceof LivingEntity liv)) return;

            if (entity == mc.player || renderEntities.containsKey(entity) || lightingEntities.containsKey(entity))
                return;
            if (entity.isAlive() || liv.getHealth() != 0) return;

            if (playSound.getValue() && mode.getValue() == Mode.Orthodox)
                mc.world.playSound(mc.player, entity.getBlockPos(), Managers.SOUND.ORTHODOX_SOUNDEVENT, SoundCategory.BLOCKS, volume.getValue() / 100f, 1f);

            renderEntities.put(entity, System.currentTimeMillis());
        });

        if (!lightingEntities.isEmpty()) {
            lightingEntities.forEach((entity, time) -> {
                if (System.currentTimeMillis() - time > 5000) {
                    lightingEntities.remove(entity);
                }
            });
        }
    }

    private double calculateSpeed() {
        return (double) speed.getValue() / 100;
    }
}