package thunder.hack.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static thunder.hack.utility.render.Render2DEngine.*;

public class HitParticles extends Module {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Stars);
    private final Setting<Physics> physics = new Setting<>("Physics", Physics.Fall);
    public final Setting<ColorSetting> colorrr = new Setting<>("Color", new ColorSetting(0x8800FF00));
    public Setting<Boolean> selfp = new Setting<>("Self", false);
    public Setting<Integer> amount = new Setting<>("Amount", 2, 1, 5);
    public Setting<Integer> lifeTime = new Setting<>("LifeTime", 2, 1, 10);
    public Setting<Integer> speed = new Setting<>("Speed", 2, 1, 20);
    CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();
    public Setting<Integer> starsScale = new Setting<>("Scale", 3, 1, 10, v -> mode.getValue() != Mode.Orbiz);
    private final Setting<ColorMode> colorMode = new Setting("ColorMode", ColorMode.Sync);

    public HitParticles() {
        super("HitParticles", Category.RENDER);
    }

    @Override
    public void onUpdate() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!selfp.getValue() && player == mc.player) continue;
            if (player.hurtTime > 0) {
                Color c = colorMode.getValue() == ColorMode.Sync ? HudEditor.getColor((int) MathUtility.random(1, 228)) : colorrr.getValue().getColorObject();
                for (int i = 0; i < amount.getValue(); i++) {
                    particles.add(new Particle(player.getX(), MathUtility.random((float) (player.getY() + player.getHeight()), (float) player.getY()), player.getZ(), c));
                }
            }
        }
        particles.removeIf(particle -> System.currentTimeMillis() - particle.getTime() > lifeTime.getValue() * 1000);
    }

    public void onPreRender3D(MatrixStack stack) {
        RenderSystem.enableDepthTest();
        if (mc.player != null && mc.world != null) {
            for (Particle particle : particles) {
                particle.render(stack);
            }
        }
        RenderSystem.disableDepthTest();
    }

    public class Particle {
        double x;
        double y;
        double z;
        double motionX;
        double motionY;
        double motionZ;
        long time;
        Color color;

        public Particle(double x, double y, double z, Color color) {
            this.x = x;
            this.y = y;
            this.z = z;
            motionX = MathUtility.random(-(float) speed.getValue() / 100f, (float) speed.getValue() / 100f);
            motionY = MathUtility.random(-(float) speed.getValue() / 100f, (float) speed.getValue() / 100f);
            motionZ = MathUtility.random(-(float) speed.getValue() / 100f, (float) speed.getValue() / 100f);
            time = System.currentTimeMillis();
            this.color = color;
        }

        public long getTime() {
            return time;
        }

        public void update() {
            double sp = Math.sqrt(motionX * motionX + motionZ * motionZ);
            x += motionX;
            y += motionY;
            z += motionZ;

            if (posBlock(x, y - starsScale.getValue() / 10f, z)) {
                motionY = -motionY / 1.1;
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

            if (physics.getValue() == Physics.Fall) motionY -= 0.0005f;
            motionX /= 1.005;
            motionZ /= 1.005;
            motionY /= 1.005;
        }

        public void render(MatrixStack matrixStack) {
            update();
            float scale = 0.07f;
            final double posX = x - mc.getEntityRenderDispatcher().camera.getPos().getX();
            final double posY = y - mc.getEntityRenderDispatcher().camera.getPos().getY();
            final double posZ = z - mc.getEntityRenderDispatcher().camera.getPos().getZ();

            matrixStack.push();
            matrixStack.translate(posX, posY, posZ);
            matrixStack.scale(-scale, -scale, -scale);

            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));

            switch (mode.getValue()) {
                case Orbiz -> {
                    drawOrbiz(matrixStack, 0.0f, 0.7, color);
                    drawOrbiz(matrixStack, 0.1f, 1.4, color);
                    drawOrbiz(matrixStack, 0.2f, 2.3, color);
                }
                case Stars -> drawStar(matrixStack, color, starsScale.getValue());
                case Hearts -> drawHeart(matrixStack, color, starsScale.getValue());
            }

            matrixStack.scale(0.8f, 0.8f, 0.8f);
            matrixStack.pop();
        }

        private boolean posBlock(double x, double y, double z) {
            return (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() != Blocks.AIR && mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() != Blocks.WATER && mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() != Blocks.LAVA);
        }
    }

    public enum Physics {
        Fall, Fly
    }

    public enum Mode {
        Orbiz, Stars, Hearts
    }

    public enum ColorMode {
        Custom, Sync
    }
}
