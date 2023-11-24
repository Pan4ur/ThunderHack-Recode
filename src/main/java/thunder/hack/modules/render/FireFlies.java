package thunder.hack.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.interfaces.IEntity;
import thunder.hack.utility.math.MathUtility;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static thunder.hack.utility.render.Render2DEngine.firefly;

public class FireFlies extends Module {

    public FireFlies() {
        super("FireFlies", Category.RENDER);
    }

    private final Setting<Integer> count = new Setting<>("Count", 20, 1, 200);
    private final Setting<Float> size = new Setting<>("Size", 1f, 0.1f, 6.0f);
    private final Setting<Mode> lmode = new Setting<>("ColorMode", Mode.Sync);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(3649978), v -> lmode.getValue() == Mode.Custom);

    public enum Mode {
        Custom, Sync
    }

    private ArrayList<FireFly> fireFlies = new ArrayList<>();

    @Override
    public void onUpdate() {
        fireFlies.removeIf(FireFly::tick);

        if(fireFlies.size() < count.getValue()) {
            fireFlies.add( new FireFly(
                    (float) (mc.player.getX() + MathUtility.random(-25f, 25f)),
                    (float) (mc.player.getY() + MathUtility.random(2f, 5f)),
                    (float) (mc.player.getZ() + MathUtility.random(-25f, 25f)),
                    MathUtility.random(-0.2f, 0.2f),
                    MathUtility.random(-0.1f, 0.1f),
                    MathUtility.random(-0.2f, 0.2f)));
        }
    }

    public void onPreRender3D(MatrixStack stack) {
        fireFlies.forEach(FireFly::render);
    }

    public class FireFly {
        private List<Trails.Trail> trails = new ArrayList<>();

        private float prevposX, prevposY, prevposZ, posX, posY, posZ, motionX, motionY, motionZ;
        private int age, maxAge;

        public FireFly(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            age = (int) MathUtility.random(100, 300);
            maxAge = age;
        }

        public boolean tick() {

            if(mc.player.squaredDistanceTo(posX, posY, posZ) > 100) age-=4;
            else if(!mc.world.getBlockState(new BlockPos((int) posX, (int) posY, (int) posZ)).isAir()) age-=8;
            else age--;

            if(age < 0)
                return true;

            trails.removeIf(Trails.Trail::update);

            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            trails.add(new Trails.Trail(new Vec3d(prevposX, prevposY, prevposZ), new Vec3d(posX, posY, posZ), lmode.getValue() == Mode.Sync ? HudEditor.getColor(age * 10) : color.getValue().getColorObject()));

            motionX*=0.99f;
            motionY*=0.99f;
            motionZ*=0.99f;

            return false;
        }

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
                    matrices.translate(pos.x, pos.y,  pos.z);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

                    VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                    Matrix4f matrix = matrices.peek().getPositionMatrix();
                    RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                    BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                    bufferBuilder.vertex(matrix, 0, size.getValue(), 0).texture(0f, 1f).next();
                    bufferBuilder.vertex(matrix, size.getValue(), size.getValue(), 0).texture(1f, 1f).next();
                    bufferBuilder.vertex(matrix, size.getValue(), 0, 0).texture(1f, 0).next();
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
}
