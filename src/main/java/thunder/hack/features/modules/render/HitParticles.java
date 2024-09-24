package thunder.hack.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static thunder.hack.utility.render.Render2DEngine.*;

public class HitParticles extends Module {
    public HitParticles() {
        super("HitParticles", Category.RENDER);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Stars);
    private final Setting<Physics> physics = new Setting<>("Physics", Physics.Fall);
    private final Setting<ColorSetting> colorrr = new Setting<>("Color", new ColorSetting(0x8800FF00));
    private final Setting<Boolean> onlySelf = new Setting<>("Self", false);
    private final Setting<Integer> amount = new Setting<>("Amount", 2, 1, 5);
    private final Setting<Integer> lifeTime = new Setting<>("LifeTime", 2, 1, 10);
    private final Setting<Integer> speed = new Setting<>("Speed", 2, 1, 20);
    private final Setting<Float> starsScale = new Setting<>("Scale", 3f, 1f, 10f, v -> mode.getValue() != Mode.Orbiz);
    private final Setting<ColorMode> colorMode = new Setting<>("ColorMode", ColorMode.Sync);
    private final Setting<ColorSetting> colorH = new Setting<>("HealColor", new ColorSetting(3142544), v -> mode.is(Mode.Text));
    private final Setting<ColorSetting> colorD = new Setting<>("DamageColor", new ColorSetting(15811379), v -> mode.is(Mode.Text));

    private final HashMap<Integer, Float> healthMap = new HashMap<>();
    private final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();

    @Override
    public void onUpdate() {
        particles.removeIf(Particle::update);

        if (mode.is(Mode.Text)) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity == null || mc.player.squaredDistanceTo(entity) > 256f || !entity.isAlive() || !(entity instanceof LivingEntity lent))
                    continue;

                Color c = colorMode.getValue() == ColorMode.Sync ? HudEditor.getColor((int) MathUtility.random(1, 228)) : colorrr.getValue().getColorObject();
                float health = lent.getHealth() + lent.getAbsorptionAmount();
                float lastHealth = healthMap.getOrDefault(entity.getId(), health);
                healthMap.put(entity.getId(), health);
                if (lastHealth == health)
                    continue;

                particles.add(new Particle((float) lent.getX(), MathUtility.random((float) (lent.getY() + lent.getHeight()), (float) lent.getY()), (float) lent.getZ(), c,
                        MathUtility.random(0, 180), MathUtility.random(10f, 60f), health - lastHealth));
            }
            return;
        }

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (onlySelf.getValue() && player != mc.player)
                continue;
            if (player.hurtTime > 0) {
                Color c = colorMode.getValue() == ColorMode.Sync ? HudEditor.getColor((int) MathUtility.random(1, 228)) : colorrr.getValue().getColorObject();
                for (int i = 0; i < amount.getValue(); i++) {
                    particles.add(new Particle((float) player.getX(), MathUtility.random((float) (player.getY() + player.getHeight()), (float) player.getY()), (float) player.getZ(), c, MathUtility.random(0, 180), MathUtility.random(10f, 60f), 0));
                }
            }
        }
    }

    public void onRender3D(MatrixStack stack) {
        RenderSystem.disableDepthTest();
        if (mc.player != null && mc.world != null) {
            for (Particle particle : particles) {
                particle.render(stack);
            }
        }
        RenderSystem.enableDepthTest();
    }

    public class Particle {
        float x;
        float y;
        float z;

        float px;
        float py;
        float pz;

        float motionX;
        float motionY;
        float motionZ;

        float rotationAngle;
        float rotationSpeed;
        float health;

        long time;
        Color color;

        public Particle(float x, float y, float z, Color color, float rotationAngle, float rotationSpeed, float health) {
            this.x = x;
            this.y = y;
            this.z = z;
            px = x;
            py = y;
            pz = z;
            motionX = MathUtility.random(-(float) speed.getValue() / 50f, (float) speed.getValue() / 50f);
            motionY = MathUtility.random(-(float) speed.getValue() / 50f, (float) speed.getValue() / 50f);
            motionZ = MathUtility.random(-(float) speed.getValue() / 50f, (float) speed.getValue() / 50f);
            time = System.currentTimeMillis();
            this.color = color;
            this.rotationAngle = rotationAngle;
            this.rotationSpeed = rotationSpeed;
            this.health = health;
        }

        public long getTime() {
            return time;
        }

        public boolean update() {
            double sp = Math.sqrt(motionX * motionX + motionZ * motionZ);
            px = x;
            py = y;
            pz = z;

            x += motionX;
            y += motionY;
            z += motionZ;

            if (posBlock(x, y - starsScale.getValue() / 10f, z)) {
                motionY = -motionY / 1.1f;
                motionX = motionX / 1.1f;
                motionZ = motionZ / 1.1f;
            } else {
                if (posBlock(x - sp, y, z - sp)
                        || posBlock(x + sp, y, z + sp)
                        || posBlock(x + sp, y, z - sp)
                        || posBlock(x - sp, y, z + sp)
                        || posBlock(x + sp, y, z)
                        || posBlock(x - sp, y, z)
                        || posBlock(x, y, z + sp)
                        || posBlock(x, y, z - sp)
                ) {
                    motionX = -motionX;
                    motionZ = -motionZ;
                }
            }

            if (physics.getValue() == Physics.Fall)
                motionY -= 0.035f;

            motionX /= 1.005f;
            motionZ /= 1.005f;
            motionY /= 1.005f;

            return System.currentTimeMillis() - getTime() > lifeTime.getValue() * 1000;
        }

        public void render(MatrixStack matrixStack) {
            float size = starsScale.getValue();
            float scale = mode.is(Mode.Text) ? 0.025f * size : 0.07f;

            final double posX = Render2DEngine.interpolate(px, x, Render3DEngine.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getX();
            final double posY = Render2DEngine.interpolate(py, y, Render3DEngine.getTickDelta()) + 0.1 - mc.getEntityRenderDispatcher().camera.getPos().getY();
            final double posZ = Render2DEngine.interpolate(pz, z, Render3DEngine.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getZ();

            matrixStack.push();
            matrixStack.translate(posX, posY, posZ);

            matrixStack.scale(scale, scale, scale);

            matrixStack.translate(size / 2, size / 2, size / 2);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));

            if (mode.is(Mode.Text))
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
            else
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationAngle += (float) (AnimationUtility.deltaTime() * rotationSpeed)));

            matrixStack.translate(-size / 2, -size / 2, -size / 2);

            switch (mode.getValue()) {
                case Orbiz -> {
                    drawOrbiz(matrixStack, 0.0f, 0.3, color);
                    drawOrbiz(matrixStack, -0.1f, 0.5, color);
                    drawOrbiz(matrixStack, -0.2f, 0.7, color);
                }
                case Stars -> drawStar(matrixStack, color, size);
                case Hearts -> drawHeart(matrixStack, color, size);
                case Bloom -> drawBloom(matrixStack, color, size);
                case Text ->
                        FontRenderers.sf_medium.drawCenteredString(matrixStack, MathUtility.round2(health) + " ", 0, 0, (health > 0 ? colorH.getValue() : colorD.getValue()).getColorObject());
            }

            matrixStack.scale(0.8f, 0.8f, 0.8f);
            matrixStack.pop();
        }

        private boolean posBlock(double x, double y, double z) {
            Block b = mc.world.getBlockState(BlockPos.ofFloored(x, y, z)).getBlock();
            return (!(b instanceof AirBlock) && b != Blocks.WATER && b != Blocks.LAVA);
        }
    }

    public enum Physics {
        Fall, Fly
    }

    public enum Mode {
        Orbiz, Stars, Hearts, Bloom, Text
    }

    public enum ColorMode {
        Custom, Sync
    }
}
