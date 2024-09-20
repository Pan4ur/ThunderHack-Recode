package thunder.hack.features.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.interfaces.IEntity;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static thunder.hack.utility.render.Render2DEngine.applyOpacity;

public class Trails extends Module {
    public Trails() {
        super("Trails", Category.RENDER);
    }

    private final Setting<Boolean> xp = new Setting<>("Xp", false);
    private final Setting<Particles> pearls = new Setting<>("Pearls", Particles.Particles);
    private final Setting<Particles> arrows = new Setting<>("Arrows", Particles.Particles);
    private final Setting<Players> players = new Setting<>("Players", Players.Particles);
    private final Setting<Boolean> onlySelf = new Setting<>("OnlySelf", false, v -> players.getValue() != Players.None);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x8800FF00));
    private final Setting<Float> down = new Setting<>("Down", 0.5F, 0.0F, 2.0F);
    private final Setting<Float> width = new Setting<>("Height", 1.3F, 0.1F, 2.0F);
    private final Setting<Integer> speed = new Setting<>("Speed", 2, 1, 20, v -> players.is(Players.Particles) || arrows.is(Particles.Particles) || pearls.is(Particles.Particles));
    private final Setting<HitParticles.Mode> mode = new Setting<>("Mode", HitParticles.Mode.Stars);
    private final Setting<HitParticles.Physics> physics = new Setting<>("Physics", HitParticles.Physics.Fall, v -> players.is(Players.Particles) || arrows.is(Particles.Particles) || pearls.is(Particles.Particles));
    private final Setting<Integer> starsScale = new Setting<>("Scale", 3, 1, 10, v -> players.is(Players.Particles) || arrows.is(Particles.Particles) || pearls.is(Particles.Particles));
    private final Setting<Integer> amount = new Setting<>("Amount", 2, 1, 5, v -> players.is(Players.Particles) || arrows.is(Particles.Particles) || pearls.is(Particles.Particles));
    private final Setting<Integer> lifeTime = new Setting<>("LifeTime", 2, 1, 10, v -> players.is(Players.Particles) || arrows.is(Particles.Particles) || pearls.is(Particles.Particles));
    private final Setting<Mode> lmode = new Setting<>("ColorMode", Mode.Sync);
    private final Setting<ColorSetting> lcolor = new Setting<>("Color2", new ColorSetting(0x2250b4b4), v -> lmode.getValue() == Mode.Custom);

    private List<Particle> particles = new ArrayList<>();

    public void onRender3D(MatrixStack stack) {
        for (Entity en : Managers.ASYNC.getAsyncEntities()) {
            if (en instanceof EnderPearlEntity && pearls.is(Particles.Trail))
                calcTrajectory(en);

            if (en instanceof ArrowEntity && arrows.is(Particles.Trail))
                calcTrajectory(en);

            if (en instanceof ExperienceBottleEntity && xp.getValue())
                calcTrajectory(en);
        }
        if (players.getValue() == Players.Trail) {
            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (entity != mc.player && onlySelf.getValue())
                    continue;

                float alpha = color.getValue().getAlpha() / 255f;
                if (!((IEntity) entity).getTrails().isEmpty()) {
                    stack.push();
                    RenderSystem.disableCull();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthFunc(GL11.GL_LEQUAL);

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

                    for (int i = 0; i < ((IEntity) entity).getTrails().size(); i++) {
                        Trail ctx = ((IEntity) entity).getTrails().get(i);
                        Vec3d pos = ctx.interpolate(Render3DEngine.getTickDelta());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + down.getValue(), (float) pos.z).color(Render2DEngine.injectAlpha(((IEntity) entity).getTrails().get(i).color(), (int) ((alpha * ctx.animation(Render3DEngine.getTickDelta())) * 255)).getRGB());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + width.getValue() + down.getValue(), (float) pos.z).color(Render2DEngine.injectAlpha(((IEntity) entity).getTrails().get(i).color(), (int) ((alpha * ctx.animation(Render3DEngine.getTickDelta())) * 255)).getRGB());
                    }

                    Render2DEngine.endBuilding(bufferBuilder);

                    Render3DEngine.endRender();
                    RenderSystem.enableCull();
                    RenderSystem.disableDepthTest();
                    stack.pop();
                }
            }
        } else if (players.getValue() == Players.Tail) {
            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (entity != mc.player && onlySelf.getValue())
                    continue;
                float alpha = color.getValue().getAlpha();
                Camera camera = mc.gameRenderer.getCamera();
                stack.push();
                RenderSystem.setShaderTexture(0, TextureStorage.firefly);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
                BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

                int size = ((IEntity) entity).getTrails().size();
                if (!((IEntity) entity).getTrails().isEmpty()) {
                    for (int i = 0; i < size; i++) {
                        Trail ctx = ((IEntity) entity).getTrails().get(i);
                        MatrixStack matrices = new MatrixStack();
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

                        Vec3d pos = ctx.interpolate(Render3DEngine.getTickDelta());
                        matrices.translate(pos.x, pos.y + 0.9f, pos.z);

                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                        Matrix4f matrix = matrices.peek().getPositionMatrix();

                        Color col = ctx.color();

                        float sc = 0.6f;

                        float colorFactor = (float) i / (float) size;
                        col = Render2DEngine.interpolateColorC(col, Color.WHITE, (float) Math.pow(1f - colorFactor, 2f));
                        float animPow = (float) ctx.animation(Render3DEngine.getTickDelta());
                        int animatedAlpha = (int) (alpha * animPow);

                        bufferBuilder.vertex(matrix, -sc, sc, 0).texture(0f, 1f).color(Render2DEngine.injectAlpha(col, animatedAlpha).getRGB());
                        bufferBuilder.vertex(matrix, sc, sc, 0).texture(1f, 1f).color(Render2DEngine.injectAlpha(col, animatedAlpha).getRGB());
                        bufferBuilder.vertex(matrix, sc, -sc, 0).texture(1f, 0).color(Render2DEngine.injectAlpha(col, animatedAlpha).getRGB());
                        bufferBuilder.vertex(matrix, -sc, -sc, 0).texture(0, 0).color(Render2DEngine.injectAlpha(col, animatedAlpha).getRGB());
                    }
                }
                Render2DEngine.endBuilding(bufferBuilder);
                RenderSystem.depthMask(true);
                RenderSystem.disableDepthTest();
                RenderSystem.disableBlend();
                stack.pop();
            }
        } else if (players.getValue() == Players.Cute) {
            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (entity != mc.player && onlySelf.getValue())
                    continue;

                float alpha = color.getValue().getAlpha() / 255f;
                if (!((IEntity) entity).getTrails().isEmpty()) {
                    stack.push();
                    RenderSystem.disableCull();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthFunc(GL11.GL_LEQUAL);

                    float step = (float) (mc.player.getBoundingBox().getLengthY() / 5f);

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);


                    for (int i = 0; i < ((IEntity) entity).getTrails().size(); i++) {
                        Trail ctx = ((IEntity) entity).getTrails().get(i);
                        Vec3d pos = ctx.interpolate(Render3DEngine.getTickDelta());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(69, 221, 255), (int) ((alpha * ctx.animation(Render3DEngine.getTickDelta())) * 255)).getRGB());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(69, 221, 255), (int) ((alpha * ctx.animation(Render3DEngine.getTickDelta())) * 255)).getRGB());
                    }
                    Render2DEngine.endBuilding(bufferBuilder);

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i < ((IEntity) entity).getTrails().size(); i++) {
                        Trail ctx = ((IEntity) entity).getTrails().get(i);
                        Vec3d pos = ctx.interpolate(Render3DEngine.getTickDelta());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(248, 139, 160), (int) ((alpha * ctx.animation(Render3DEngine.getTickDelta())) * 255)).getRGB());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 2, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(248, 139, 160), (int) ((alpha * ctx.animation(Render3DEngine.getTickDelta())) * 255)).getRGB());
                    }
                    Render2DEngine.endBuilding(bufferBuilder);

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i < ((IEntity) entity).getTrails().size(); i++) {
                        Trail ctx = ((IEntity) entity).getTrails().get(i);
                        Vec3d pos = ctx.interpolate(Render3DEngine.getTickDelta());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 2, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(255, 255, 255), (int) ((alpha * ctx.animation(Render3DEngine.getTickDelta())) * 255)).getRGB());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 3, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(255, 255, 255), (int) ((alpha * ctx.animation(Render3DEngine.getTickDelta())) * 255)).getRGB());
                    }
                    Render2DEngine.endBuilding(bufferBuilder);

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i < ((IEntity) entity).getTrails().size(); i++) {
                        Trail ctx = ((IEntity) entity).getTrails().get(i);
                        Vec3d pos = ctx.interpolate(Render3DEngine.getTickDelta());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 3, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(248, 139, 160), (int) ((alpha * ctx.animation(Render3DEngine.getTickDelta())) * 255)).getRGB());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 4, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(248, 139, 160), (int) ((alpha * ctx.animation(Render3DEngine.getTickDelta())) * 255)).getRGB());
                    }
                    Render2DEngine.endBuilding(bufferBuilder);

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i < ((IEntity) entity).getTrails().size(); i++) {
                        Trail ctx = ((IEntity) entity).getTrails().get(i);
                        Vec3d pos = ctx.interpolate(Render3DEngine.getTickDelta());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 4, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(69, 221, 255), (int) ((alpha * ctx.animation(Render3DEngine.getTickDelta())) * 255)).getRGB());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 5, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(69, 221, 255), (int) ((alpha * ctx.animation(Render3DEngine.getTickDelta())) * 255)).getRGB());
                    }
                    Render2DEngine.endBuilding(bufferBuilder);

                    Render3DEngine.endRender();
                    RenderSystem.enableCull();
                    RenderSystem.disableDepthTest();
                    stack.pop();
                }
            }
        }

        if (!particles.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);

            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);

            switch (mode.getValue()) {
                case Stars -> RenderSystem.setShaderTexture(0, TextureStorage.star);
                case Bloom -> RenderSystem.setShaderTexture(0, TextureStorage.firefly);
                case Hearts -> RenderSystem.setShaderTexture(0, TextureStorage.heart);
                default -> {
                    return;
                }
            }

            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            if (mc.player != null && mc.world != null)
                particles.forEach(p -> p.render(stack, bufferBuilder));

            Render2DEngine.endBuilding(bufferBuilder);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    @Override
    public void onUpdate() {
        Color c = lmode.getValue() == Mode.Sync ? HudEditor.getColor(mc.player.age % 360) : lcolor.getValue().getColorObject();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player.getPos().getZ() != player.prevZ || player.getPos().getX() != player.prevX && (!onlySelf.getValue())) {
                ((IEntity) player).getTrails().add(new Trail(new Vec3d(player.prevX, player.prevY, player.prevZ), player.getPos(), c));
                if (players.is(Players.Particles)) {
                    for (int i = 0; i < amount.getValue(); i++) {
                        particles.add(new Particle(player.getX(), MathUtility.random((float) (player.getY() + player.getHeight()), (float) player.getY()), player.getZ(), c));
                    }
                }
            }
            ((IEntity) player).getTrails().removeIf(Trail::update);

        }

        for (Entity en : Managers.ASYNC.getAsyncEntities()) {
            if (en instanceof ArrowEntity ae && (ae.prevY != ae.getY()) && arrows.is(Particles.Particles))
                for (int i = 0; i < 5; i++)
                    particles.add(new Particle(en.getX(), en.getY(), en.getZ(), HudEditor.getColor(mc.player.age)));

            if (en instanceof EnderPearlEntity && pearls.is(Particles.Particles))
                for (int i = 0; i < 5; i++)
                    particles.add(new Particle(en.getX(), en.getY(), en.getZ(), HudEditor.getColor(mc.player.age)));
        }

        if (Managers.PLAYER.currentPlayerSpeed != 0) {
            ((IEntity) mc.player).getTrails().add(new Trail(new Vec3d(mc.player.prevX, mc.player.prevY, mc.player.prevZ), mc.player.getPos(), c));
            if (players.is(Players.Particles)) {
                for (int i = 0; i < amount.getValue(); i++) {
                    particles.add(new Particle(mc.player.getX(), MathUtility.random((float) (mc.player.getY() + mc.player.getHeight()), (float) mc.player.getY()), mc.player.getZ(), c));
                }
            }
        }
        particles.removeIf(particle -> System.currentTimeMillis() - particle.time > 1000f * lifeTime.getValue());
    }

    public static class Trail {
        private final Vec3d from;
        private final Vec3d to;
        private final Color color;
        private int ticks, prevTicks;

        public Trail(Vec3d from, Vec3d to, Color color) {
            this.from = from;
            this.to = to;
            this.ticks = 10;
            this.color = color;
        }

        public Vec3d interpolate(float pt) {
            double x = from.x + ((to.x - from.x) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getX();
            double y = from.y + ((to.y - from.y) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getY();
            double z = from.z + ((to.z - from.z) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getZ();
            return new Vec3d(x, y, z);
        }

        public double animation(float pt) {
            return (this.prevTicks + (this.ticks - this.prevTicks) * pt) / 10.;
        }

        public boolean update() {
            this.prevTicks = this.ticks;
            return this.ticks-- <= 0;
        }

        public Color color() {
            return color;
        }
    }

    private void calcTrajectory(Entity e) {
        double motionX = e.getVelocity().x;
        double motionY = e.getVelocity().y;
        double motionZ = e.getVelocity().z;
        double x = e.getX();
        double y = e.getY();
        double z = e.getZ();
        Vec3d lastPos = new Vec3d(x, y, z);
        for (int i = 0; i < 300; i++) {
            lastPos = new Vec3d(x, y, z);
            x += motionX;
            y += motionY;
            z += motionZ;
            if (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() == Blocks.WATER) {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }
            if (e instanceof ArrowEntity) {
                motionY -= 0.05000000074505806;
            } else {
                motionY -= 0.03f;
            }
            Vec3d pos = new Vec3d(x, y, z);

            if (mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)) != null) {
                if (mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.ENTITY)
                    break;
                if (mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.BLOCK)
                    break;
            }

            if (y <= -65) break;
            if (e.getVelocity().x == 0 && e.getVelocity().y == 0 && e.getVelocity().z == 0) continue;

            int alpha = (int) MathUtility.clamp((255f * (i / 8f)), 0, 255);

            Render3DEngine.drawLine(lastPos, pos, lmode.getValue() == Mode.Sync ? Render2DEngine.injectAlpha(HudEditor.getColor(i * 5), alpha) : Render2DEngine.injectAlpha(lcolor.getValue().getColorObject(), alpha));
        }
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
            motionX = MathUtility.random(-(float) speed.getValue() / 200f, (float) speed.getValue() / 200f);
            motionY = MathUtility.random(-(float) speed.getValue() / 200f, (float) speed.getValue() / 200f);
            motionZ = MathUtility.random(-(float) speed.getValue() / 200f, (float) speed.getValue() / 200f);
            time = System.currentTimeMillis();
            this.color = color;
        }

        public void update() {
            double sp = starsScale.getValue() / 10f;
            x += motionX;
            y += motionY;
            z += motionZ;

            if (posBlock(x, y - starsScale.getValue() / 10f, z)) {
                motionY = -motionY / 1.1;
            } else {
                if (posBlock(x, y, z)
                        || posBlock(x - sp, y, z - sp)
                        || posBlock(x + sp, y, z + sp)
                        || posBlock(x + sp, y, z - sp)
                        || posBlock(x - sp, y, z + sp)
                        || posBlock(x + sp, y, z)
                        || posBlock(x - sp, y, z)
                        || posBlock(x, y, z - sp)
                        || posBlock(x, y, z + sp)
                ) {
                    motionX = -motionX;
                    motionZ = -motionZ;
                }
            }

            if (physics.getValue() == HitParticles.Physics.Fall) motionY -= 0.0005f;
            motionX /= 1.005;
            motionZ /= 1.005;
            motionY /= 1.005;
        }

        public void render(MatrixStack matrixStack, BufferBuilder bufferBuilder) {
            update();
            float scale = starsScale.getValue() / 10f;
            final double posX = x - mc.getEntityRenderDispatcher().camera.getPos().getX();
            final double posY = y - mc.getEntityRenderDispatcher().camera.getPos().getY();
            final double posZ = z - mc.getEntityRenderDispatcher().camera.getPos().getZ();

            Camera camera = mc.gameRenderer.getCamera();

            MatrixStack matrices = new MatrixStack();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            matrices.translate(posX, posY, posZ);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

            Matrix4f matrix = matrices.peek().getPositionMatrix();

            float colorAnim = (float) (System.currentTimeMillis() - time) / (1000f * lifeTime.getValue());

            Color c = lmode.getValue() == Mode.Sync ? HudEditor.getColor((int) (360 * colorAnim)) : lcolor.getValue().getColorObject();

            bufferBuilder.vertex(matrix, -scale / 2, scale, 0).texture(0, 1).color(applyOpacity(c, 1f - colorAnim).getRGB());
            bufferBuilder.vertex(matrix, scale, scale, 0).texture(1, 1).color(applyOpacity(c, 1f - colorAnim).getRGB());
            bufferBuilder.vertex(matrix, scale, -scale / 2, 0).texture(1, 0).color(applyOpacity(c, 1f - colorAnim).getRGB());
            bufferBuilder.vertex(matrix, -scale / 2, -scale / 2, 0).texture(0, 0).color(applyOpacity(c, 1f - colorAnim).getRGB());
        }

        private boolean posBlock(double x, double y, double z) {
            Block b = mc.world.getBlockState(BlockPos.ofFloored(x, y, z)).getBlock();
            return b != Blocks.AIR && b != Blocks.WATER && b != Blocks.LAVA;
        }
    }

    private enum Mode {
        Custom, Sync
    }

    private enum Players {
        Trail, Particles, Cute, Tail, None
    }

    private enum Particles {
        Trail, Particles, None
    }
}
