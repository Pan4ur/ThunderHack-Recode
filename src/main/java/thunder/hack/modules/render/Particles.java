package thunder.hack.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanParent;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static thunder.hack.utility.render.Render2DEngine.*;

public class Particles extends Module {

    public Particles() {
        super("Particles", Category.RENDER);
    }

    // Фабос, чекнул сурс - сдох отец, спастишь - сдохнет мать

    private final Setting<BooleanParent> FireFlies = new Setting<>("FireFlies", new BooleanParent(true));
    private final Setting<Integer> ffcount = new Setting<>("FFCount", 30, 20, 200).withParent(FireFlies);
    private final Setting<Float> ffsize = new Setting<>("FFSize", 1f, 0.1f, 2.0f).withParent(FireFlies);
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.SnowFlake);
    private final Setting<Integer> count = new Setting<>("Count", 100, 20, 800);
    private final Setting<Float> size = new Setting<>("Size", 1f, 0.1f, 6.0f);
    private final Setting<ColorMode> lmode = new Setting<>("ColorMode", ColorMode.Sync);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(3649978), v -> lmode.getValue() == ColorMode.Custom);

    public enum ColorMode {
        Custom, Sync
    }

    public enum Mode {
        Off, SnowFlake, Stars, Hearts, Dollars, Bloom;
    }


    private ArrayList<ParticleBase> fireFlies = new ArrayList<>();
    private ArrayList<ParticleBase> particles = new ArrayList<>();

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
            if (mode.getValue() != Mode.Off)
                particles.add(new ParticleBase(
                        (float) (mc.player.getX() + MathUtility.random(-64f, 64f)),
                        (float) (mc.player.getY() + MathUtility.random(2, 64f)),
                        (float) (mc.player.getZ() + MathUtility.random(-64f, 64f)),
                        MathUtility.random(-0.4f, 0.4f),
                        MathUtility.random(-0.1f, 0.1f),
                        MathUtility.random(-0.4f, 0.4f)));
        }
    }

    public void onPreRender3D(MatrixStack stack) {
        fireFlies.forEach(ParticleBase::render);
        particles.forEach(ParticleBase::render);
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
        public void render() {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderTexture(0, firefly);

            if (!trails.isEmpty()) {
                Camera camera = mc.gameRenderer.getCamera();
                for (Trails.Trail ctx : trails) {
                    RenderSystem.setShaderColor(ctx.color().getRed() / 255f, ctx.color().getGreen() / 255f, ctx.color().getBlue() / 255f, (float) (((float) age / (float) maxAge) * ctx.animation(mc.getTickDelta())));

                    Vec3d pos = ctx.interpolate(1f);
                    MatrixStack matrices = new MatrixStack();
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                    matrices.translate(pos.x, pos.y, pos.z);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

                    VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                    Matrix4f matrix = matrices.peek().getPositionMatrix();
                    RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                    BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                    bufferBuilder.vertex(matrix, 0, ffsize.getValue(), 0).texture(0f, 1f).next();
                    bufferBuilder.vertex(matrix, ffsize.getValue(), ffsize.getValue(), 0).texture(1f, 1f).next();
                    bufferBuilder.vertex(matrix, ffsize.getValue(), 0, 0).texture(1f, 0).next();
                    bufferBuilder.vertex(matrix, 0, 0, 0).texture(0, 0).next();
                    BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
                    immediate.draw();
                    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                }
            }
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
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
            motionY *= 0.9f;
            motionZ *= 0.9f;

            motionY -= 0.001f;

            return false;
        }

        public void render() {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            switch (mode.getValue()){
                case Off -> {
                    return;
                }
                case Bloom ->  RenderSystem.setShaderTexture(0, firefly);
                case SnowFlake ->  RenderSystem.setShaderTexture(0, snowflake);
                case Dollars ->  RenderSystem.setShaderTexture(0, dollar);
                case Hearts ->  RenderSystem.setShaderTexture(0, heart);
                case Stars ->  RenderSystem.setShaderTexture(0, star);
            }

            Camera camera = mc.gameRenderer.getCamera();

            Color color1 = lmode.getValue() == ColorMode.Sync ? HudEditor.getColor(age * 2) : color.getValue().getColorObject();

            RenderSystem.setShaderColor(color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, (float) age / (float) maxAge);

            Vec3d pos = Render3DEngine.interpolatePos(prevposX, prevposY, prevposZ, posX, posY, posZ);

            MatrixStack matrices = new MatrixStack();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            matrices.translate(pos.x, pos.y, pos.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            bufferBuilder.vertex(matrix, 0, -size.getValue(), 0).texture(0f, 1f).next();
            bufferBuilder.vertex(matrix, -size.getValue(), -size.getValue(), 0).texture(1f, 1f).next();
            bufferBuilder.vertex(matrix, -size.getValue(), 0, 0).texture(1f, 0).next();
            bufferBuilder.vertex(matrix, 0, 0, 0).texture(0, 0).next();
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            immediate.draw();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
        }
    }
}
