package thunder.hack.features.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Фабос, чекнул сурс - сдох отец, спастишь - сдохнет мать
public class Particles extends Module {
    public Particles() {
        super("Particles", Category.RENDER);
    }

    private final Setting<BooleanSettingGroup> FireFlies = new Setting<>("FireFlies", new BooleanSettingGroup(true));
    private final Setting<Integer> ffcount = new Setting<>("FFCount", 30, 20, 200).addToGroup(FireFlies);
    private final Setting<Float> ffsize = new Setting<>("FFSize", 1f, 0.1f, 2.0f).addToGroup(FireFlies);
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.SnowFlake);
    private final Setting<Integer> count = new Setting<>("Count", 100, 20, 800);
    private final Setting<Float> size = new Setting<>("Size", 1f, 0.1f, 6.0f);
    private final Setting<ColorMode> lmode = new Setting<>("ColorMode", ColorMode.Sync);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(3649978), v -> lmode.getValue() == ColorMode.Custom);
    private final Setting<Physics> physics = new Setting<>("Physics", Physics.Fly, v -> mode.getValue() != Mode.Off);

    private final ArrayList<ParticleBase> fireFlies = new ArrayList<>();
    private final ArrayList<ParticleBase> particles = new ArrayList<>();

    @Override
    public void onUpdate() {
        fireFlies.removeIf(ParticleBase::tick);
        particles.removeIf(ParticleBase::tick);

        for (int i = fireFlies.size(); i < ffcount.getValue(); i++) {
            if (FireFlies.getValue().isEnabled())
                fireFlies.add(new FireFly(
                        (float) (mc.player.getX() + MathUtility.random(-25f, 25f)),
                        (float) (mc.player.getY() + MathUtility.random(2f, 15f)),
                        (float) (mc.player.getZ() + MathUtility.random(-25f, 25f)),
                        MathUtility.random(-0.2f, 0.2f),
                        MathUtility.random(-0.1f, 0.1f),
                        MathUtility.random(-0.2f, 0.2f)));
        }

        for (int j = particles.size(); j < count.getValue(); j++) {
            boolean drop = physics.getValue() == Physics.Drop;
            if (mode.getValue() != Mode.Off)
                particles.add(new ParticleBase(
                        (float) (mc.player.getX() + MathUtility.random(-48f, 48f)),
                        (float) (mc.player.getY() + MathUtility.random(2, 48f)),
                        (float) (mc.player.getZ() + MathUtility.random(-48f, 48f)),
                        drop ? 0 : MathUtility.random(-0.4f, 0.4f),
                        drop ? MathUtility.random(-0.2f, -0.05f) : MathUtility.random(-0.1f, 0.1f),
                        drop ? 0 : MathUtility.random(-0.4f, 0.4f)));
        }
    }

    public void onRender3D(MatrixStack stack) {
        if (FireFlies.getValue().isEnabled()) {
            stack.push();
            RenderSystem.setShaderTexture(0, TextureStorage.firefly);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            fireFlies.forEach(p -> p.render(bufferBuilder));
            Render2DEngine.endBuilding(bufferBuilder);
            RenderSystem.depthMask(true);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            stack.pop();
        }

        if (mode.getValue() != Mode.Off) {
            stack.push();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            particles.forEach(p -> p.render(bufferBuilder));
            Render2DEngine.endBuilding(bufferBuilder);
            RenderSystem.depthMask(true);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            stack.pop();
        }
    }

    public class FireFly extends ParticleBase {
        private final List<Trails.Trail> trails = new ArrayList<>();

        public FireFly(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
            super(posX, posY, posZ, motionX, motionY, motionZ);
        }

        @Override
        public boolean tick() {

            if (mc.player.squaredDistanceTo(posX, posY, posZ) > 100) age -= 4;
            else if (!mc.world.getBlockState(new BlockPos((int) posX, (int) posY, (int) posZ)).isAir()) age -= 8;
            else age--;

            if (age < 0)
                return true;

            trails.removeIf(Trails.Trail::update);

            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            trails.add(new Trails.Trail(new Vec3d(prevposX, prevposY, prevposZ), new Vec3d(posX, posY, posZ), lmode.getValue() == ColorMode.Sync ? HudEditor.getColor(age * 10) : color.getValue().getColorObject()));

            motionX *= 0.99f;
            motionY *= 0.99f;
            motionZ *= 0.99f;

            return false;
        }

        @Override
        public void render(BufferBuilder bufferBuilder) {
            RenderSystem.setShaderTexture(0, TextureStorage.firefly);
            if (!trails.isEmpty()) {
                Camera camera = mc.gameRenderer.getCamera();
                for (Trails.Trail ctx : trails) {
                    Vec3d pos = ctx.interpolate(1f);
                    MatrixStack matrices = new MatrixStack();
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                    matrices.translate(pos.x, pos.y, pos.z);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    Matrix4f matrix = matrices.peek().getPositionMatrix();

                    bufferBuilder.vertex(matrix, 0, -ffsize.getValue(), 0).texture(0f, 1f).color(Render2DEngine.injectAlpha(ctx.color(), (int) (255 * ((float) age / (float) maxAge) * ctx.animation(Render3DEngine.getTickDelta()))).getRGB());
                    bufferBuilder.vertex(matrix, -ffsize.getValue(), -ffsize.getValue(), 0).texture(1f, 1f).color(Render2DEngine.injectAlpha(ctx.color(), (int) (255 * ((float) age / (float) maxAge) * ctx.animation(Render3DEngine.getTickDelta()))).getRGB());
                    bufferBuilder.vertex(matrix, -ffsize.getValue(), 0, 0).texture(1f, 0).color(Render2DEngine.injectAlpha(ctx.color(), (int) (255 * ((float) age / (float) maxAge) * ctx.animation(Render3DEngine.getTickDelta()))).getRGB());
                    bufferBuilder.vertex(matrix, 0, 0, 0).texture(0, 0).color(Render2DEngine.injectAlpha(ctx.color(), (int) (255 * ((float) age / (float) maxAge) * ctx.animation(Render3DEngine.getTickDelta()))).getRGB());
                }
            }
        }
    }

    public class ParticleBase {

        protected float prevposX, prevposY, prevposZ, posX, posY, posZ, motionX, motionY, motionZ;
        protected int age, maxAge;

        public ParticleBase(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            age = (int) MathUtility.random(100, 300);
            maxAge = age;
        }

        public boolean tick() {
            if (mc.player.squaredDistanceTo(posX, posY, posZ) > 4096) age -= 8;
            else age--;

            if (age < 0)
                return true;

            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            motionX *= 0.9f;
            if (physics.getValue() == Physics.Fly)
                motionY *= 0.9f;
            motionZ *= 0.9f;

            motionY -= 0.001f;

            return false;
        }

        public void render(BufferBuilder bufferBuilder) {
            switch (mode.getValue()) {
                case Bloom -> RenderSystem.setShaderTexture(0, TextureStorage.firefly);
                case SnowFlake -> RenderSystem.setShaderTexture(0, TextureStorage.snowflake);
                case Dollars -> RenderSystem.setShaderTexture(0, TextureStorage.dollar);
                case Hearts -> RenderSystem.setShaderTexture(0, TextureStorage.heart);
                case Stars -> RenderSystem.setShaderTexture(0, TextureStorage.star);
            }

            Camera camera = mc.gameRenderer.getCamera();
            Color color1 = lmode.getValue() == ColorMode.Sync ? HudEditor.getColor(age * 2) : color.getValue().getColorObject();
            Vec3d pos = Render3DEngine.interpolatePos(prevposX, prevposY, prevposZ, posX, posY, posZ);

            MatrixStack matrices = new MatrixStack();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            matrices.translate(pos.x, pos.y, pos.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

            Matrix4f matrix1 = matrices.peek().getPositionMatrix();

            bufferBuilder.vertex(matrix1, 0, -size.getValue(), 0).texture(0f, 1f).color(Render2DEngine.injectAlpha(color1, (int) (255 * ((float) age / (float) maxAge))).getRGB());
            bufferBuilder.vertex(matrix1, -size.getValue(), -size.getValue(), 0).texture(1f, 1f).color(Render2DEngine.injectAlpha(color1, (int) (255 * ((float) age / (float) maxAge))).getRGB());
            bufferBuilder.vertex(matrix1, -size.getValue(), 0, 0).texture(1f, 0).color(Render2DEngine.injectAlpha(color1, (int) (255 * ((float) age / (float) maxAge))).getRGB());
            bufferBuilder.vertex(matrix1, 0, 0, 0).texture(0, 0).color(Render2DEngine.injectAlpha(color1, (int) (255 * ((float) age / (float) maxAge))).getRGB());
        }
    }

    public enum ColorMode {
        Custom, Sync
    }

    public enum Mode {
        Off, SnowFlake, Stars, Hearts, Dollars, Bloom;
    }

    public enum Physics {
        Drop, Fly
    }
}
