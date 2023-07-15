package thunder.hack.modules.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Identifier;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.PreRender3DEvent;
import thunder.hack.events.impl.Render3DEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtil;
import thunder.hack.utility.render.Render2DEngine;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class HitParticles extends Module {
    public final Setting<ColorSetting> colorrr = new Setting<>("Color", new ColorSetting(0x8800FF00));
    public Setting<Boolean> selfp = new Setting("Self", false);
    public Setting<Integer> amount = new Setting<>("Amount", 2, 1, 5);
    public Setting<Integer> lifeTime = new Setting<>("LifeTime", 2, 1, 10);
    public Setting<Integer> speed = new Setting<>("Speed", 2, 1, 20);
    CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();
    public Setting<Integer> starsScale = new Setting<>("StarsScale", 3, 1, 10);

    private final Setting<Mode> mode = new Setting("Mode", Mode.Stars);

    public enum Mode {
        Orbiz, Stars
    }

    private final Setting<ColorMode> colorMode = new Setting("ColorMode", ColorMode.Sync);

    public enum ColorMode {
        Custom, Sync
    }

    private Identifier star = new Identifier("textures/star.png");

    public HitParticles() {
        super("HitParticles", "HitParticles", Category.RENDER);
    }

    @Override
    public void onUpdate() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!selfp.getValue() && player == mc.player) continue;
            if (player.hurtTime > 0) {
                Color c = colorMode.getValue() == ColorMode.Sync ? HudEditor.getColor((int) MathUtil.random(1,228)) : colorrr.getValue().getColorObject();
                for(int i = 0; i < amount.getValue(); i++){
                    particles.add(new Particle(player.getX(), MathUtil.random((float) (player.getY() + player.getHeight()), (float) player.getY()), player.getZ(), c));
                }
            }
            particles.removeIf(particle -> System.currentTimeMillis() - particle.getTime() > lifeTime.getValue() * 1000);
        }
    }


    @Subscribe
    public void onPreRender3D(PreRender3DEvent event) {
        RenderSystem.enableDepthTest();
        if (mc.player != null && mc.world != null) {
            for (Particle particle : particles) {
                particle.render(event.getMatrixStack());
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

        public Particle(double x, double y, double z,Color color) {
            this.x = x;
            this.y = y;
            this.z = z;
            motionX = MathUtil.random(-(float) speed.getValue() / 100f, (float) speed.getValue() / 100f);
            motionY = MathUtil.random(-(float) speed.getValue() / 100f, (float) speed.getValue() / 100f);
            motionZ = MathUtil.random(-(float) speed.getValue() / 100f, (float) speed.getValue() / 100f);
            time = System.currentTimeMillis();
            this.color = color;
        }


        public long getTime() {
            return time;
        }

        public void update() {
            double sp = Math.sqrt(motionX * motionX + motionZ * motionZ) * 1;
            x += motionX;
            y += motionY;

            if (posBlock(x, y, z)) {
                motionY = -motionY / 1.1;
            } else {
                if (posBlock(x, y, z) || posBlock(x, y, z) || posBlock(x, y, z) || posBlock(x - sp, y, z - sp)
                        || posBlock(x + sp, y, z + sp) || posBlock(x + sp, y, z - sp) || posBlock(x - sp, y, z + sp)
                        || posBlock(x + sp, y, z) || posBlock(x - sp, y, z) || posBlock(x, y, z + sp) || posBlock(x, y, z - sp)
                        || posBlock(x - sp, y, z - sp) || posBlock(x + sp, y, z + sp) || posBlock(x + sp, y, z - sp)
                        || posBlock(x - sp, y, z + sp) || posBlock(x + sp, y, z) || posBlock(x - sp, y, z) || posBlock(x, y, z + sp)
                        || posBlock(x, y, z - sp) || posBlock(x - sp, y, z - sp) || posBlock(x + sp, y, z + sp) || posBlock(x + sp, y, z - sp)
                        || posBlock(x - sp, y, z + sp) || posBlock(x + sp, y, z) || posBlock(x - sp, y, z) || posBlock(x, y, z + sp)
                        || posBlock(x, y, z - sp)) {
                    motionX = -motionX;
                    motionZ = -motionZ;
                }
            }
            z += motionZ;
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

            if(mode.getValue() == Mode.Orbiz) {
                drawOrbiz(matrixStack, 0.0f, 0.7,color);
                drawOrbiz(matrixStack, 0.1f, 1.4,color);
                drawOrbiz(matrixStack, 0.2f, 2.3, color);
            } else {
                drawStar(matrixStack,color);
            }

            matrixStack.scale(0.8f, 0.8f, 0.8f);
            matrixStack.pop();
        }

        private boolean posBlock(double x, double y, double z) {
            return (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() != Blocks.AIR && mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() != Blocks.WATER && mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() != Blocks.LAVA);
        }
    }

    public void drawOrbiz(MatrixStack matrices,float z, final double r, Color c) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 20; i++) {
            final double x2 = Math.sin(((i * 18 * Math.PI) / 180)) * r;
            final double y2 = Math.cos(((i * 18 * Math.PI) / 180)) * r;
            bufferBuilder.vertex(matrix, (float) (x2), (float) (y2), z).color(c.getRed() / 255f,c.getGreen() / 255f,c.getBlue() / 255f,0.4f).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public void drawStar(MatrixStack matrices, Color c) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0,star);
        RenderSystem.setShaderColor(c.getRed() / 255f,c.getGreen() / 255f,c.getBlue() / 255f,c.getAlpha() / 255f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(mc.player.age * 2));
        Render2DEngine.renderTexture(matrices,0, 0,starsScale.getValue(),starsScale.getValue(),0,0,256,256,256,256);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f,1f,1f,1f);
    }

}
