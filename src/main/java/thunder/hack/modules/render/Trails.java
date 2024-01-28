package thunder.hack.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
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
import thunder.hack.ThunderHack;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.interfaces.IEntity;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static thunder.hack.utility.render.Render2DEngine.*;

public class Trails extends Module {
    public Trails() {
        super("Trails", Category.RENDER);
    }

    private final Setting<Boolean> pearls = new Setting<>("Pearls", false);
    private final Setting<Boolean> xp = new Setting<>("Xp", false);
    private final Setting<Boolean> arrows = new Setting<>("Arrows", false);
    private final Setting<Players> players = new Setting<>("Players", Players.Particles);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x8800FF00));
    private final Setting<Float> down = new Setting<>("Down", 0.5F, 0.0F, 2.0F);
    private final Setting<Float> width = new Setting<>("Height", 1.3F, 0.1F, 2.0F);
    private final Setting<Integer> speed = new Setting<>("Speed", 2, 1, 20, v-> players.getValue() == Players.Particles);
    private final Setting<HitParticles.Mode> mode = new Setting<>("Mode", HitParticles.Mode.Stars, v-> players.getValue() == Players.Particles);
    private final Setting<HitParticles.Physics> physics = new Setting<>("Physics", HitParticles.Physics.Fall, v-> players.getValue() == Players.Particles);
    private final Setting<Integer> starsScale = new Setting<>("Scale", 3, 1, 10, v-> players.getValue() == Players.Particles);
    private final Setting<Integer> amount = new Setting<>("Amount", 2, 1, 5, v-> players.getValue() == Players.Particles);
    private final Setting<Integer> lifeTime = new Setting<>("LifeTime", 2, 1, 10, v-> players.getValue() == Players.Particles);
    private final Setting<Mode> lmode = new Setting<>("ColorMode", Mode.Sync);
    private final Setting<ColorSetting> lcolor = new Setting<>("Color2", new ColorSetting(0x2250b4b4), v -> lmode.getValue() == Mode.Custom);

    private List<Particle> particles = new ArrayList<>();

    public void onRender3D(MatrixStack stack) {
        for (Entity en : ThunderHack.asyncManager.getAsyncEntities()) {
            if (en instanceof EnderPearlEntity && pearls.getValue())
                calcTrajectory(en);

            if (en instanceof ArrowEntity && arrows.getValue())
                calcTrajectory(en);

            if (en instanceof ExperienceBottleEntity && xp.getValue())
                calcTrajectory(en);
        }
    }

    public void onPreRender3D(MatrixStack stack) {
        if (players.getValue() == Players.Trail) {
            for (PlayerEntity entity : mc.world.getPlayers()) {
                float alpha = color.getValue().getAlpha() / 255f;
                if (!((IEntity) entity).thunderHack_Recode$getTrails().isEmpty()) {
                    stack.push();
                    RenderSystem.disableCull();
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferBuilder = tessellator.getBuffer();

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthFunc(GL11.GL_LEQUAL);

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i < ((IEntity) entity).thunderHack_Recode$getTrails().size(); i++) {
                        Trail ctx = ((IEntity) entity).thunderHack_Recode$getTrails().get(i);
                        Vec3d pos = ctx.interpolate(mc.getTickDelta());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + down.getValue(), (float) pos.z).color(Render2DEngine.injectAlpha(((IEntity) entity).thunderHack_Recode$getTrails().get(i).color(), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + width.getValue() + down.getValue(), (float) pos.z).color(Render2DEngine.injectAlpha(((IEntity) entity).thunderHack_Recode$getTrails().get(i).color(), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                    }

                    tessellator.draw();
                    Render3DEngine.cleanup();
                    RenderSystem.enableCull();
                    RenderSystem.disableDepthTest();
                    stack.pop();
                }
            }
        } else if (players.getValue() == Players.Particles) {
            RenderSystem.enableDepthTest();
            if (mc.player != null && mc.world != null)
                particles.forEach(p -> p.render(stack));
            RenderSystem.disableDepthTest();
        } else if(players.getValue() == Players.Cute){
            for (PlayerEntity entity : mc.world.getPlayers()) {
                float alpha = color.getValue().getAlpha() / 255f;
                if (!((IEntity) entity).thunderHack_Recode$getTrails().isEmpty()) {
                    stack.push();
                    RenderSystem.disableCull();
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferBuilder = tessellator.getBuffer();

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthFunc(GL11.GL_LEQUAL);

                    float step = (float) (mc.player.getBoundingBox().getLengthY() / 5f);

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i < ((IEntity) entity).thunderHack_Recode$getTrails().size(); i++) {
                        Trail ctx = ((IEntity) entity).thunderHack_Recode$getTrails().get(i);
                        Vec3d pos = ctx.interpolate(mc.getTickDelta());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(69, 221, 255), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(69, 221, 255), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                    }
                    tessellator.draw();

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i < ((IEntity) entity).thunderHack_Recode$getTrails().size(); i++) {
                        Trail ctx = ((IEntity) entity).thunderHack_Recode$getTrails().get(i);
                        Vec3d pos = ctx.interpolate(mc.getTickDelta());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(248, 139, 160), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 2, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(248, 139, 160), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                    }
                    tessellator.draw();

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i < ((IEntity) entity).thunderHack_Recode$getTrails().size(); i++) {
                        Trail ctx = ((IEntity) entity).thunderHack_Recode$getTrails().get(i);
                        Vec3d pos = ctx.interpolate(mc.getTickDelta());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 2, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(255, 255, 255), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 3, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(255, 255, 255), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                    }
                    tessellator.draw();

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i < ((IEntity) entity).thunderHack_Recode$getTrails().size(); i++) {
                        Trail ctx = ((IEntity) entity).thunderHack_Recode$getTrails().get(i);
                        Vec3d pos = ctx.interpolate(mc.getTickDelta());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 3, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(248, 139, 160), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 4, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(248, 139, 160), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                    }
                    tessellator.draw();

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i < ((IEntity) entity).thunderHack_Recode$getTrails().size(); i++) {
                        Trail ctx = ((IEntity) entity).thunderHack_Recode$getTrails().get(i);
                        Vec3d pos = ctx.interpolate(mc.getTickDelta());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 4, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(69, 221, 255), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) pos.x, (float) pos.y + step * 5, (float) pos.z).color(Render2DEngine.injectAlpha(new Color(69, 221, 255), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                    }
                    tessellator.draw();

                    Render3DEngine.cleanup();
                    RenderSystem.enableCull();
                    RenderSystem.disableDepthTest();
                    stack.pop();
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        Color c = lmode.getValue() == Mode.Sync ? HudEditor.getColor((int) MathUtility.random(1, 228)) : lcolor.getValue().getColorObject();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player.getPos().getZ() != player.prevZ || player.getPos().getX() != player.prevX) {
                ((IEntity) player).thunderHack_Recode$getTrails().add(new Trail(new Vec3d(player.prevX, player.prevY, player.prevZ), player.getPos(), color.getValue().getColorObject()));
                for (int i = 0; i < amount.getValue(); i++) {
                    particles.add(new Particle(player.getX(), MathUtility.random((float) (player.getY() + player.getHeight()), (float) player.getY()), player.getZ(), c));
                }
            }
            ((IEntity) player).thunderHack_Recode$getTrails().removeIf(Trail::update);
        }

        if (ThunderHack.playerManager.currentPlayerSpeed != 0) {
            ((IEntity) mc.player).thunderHack_Recode$getTrails().add(new Trail(new Vec3d(mc.player.prevX, mc.player.prevY, mc.player.prevZ), mc.player.getPos(), color.getValue().getColorObject()));
            for (int i = 0; i < amount.getValue(); i++) {
                particles.add(new Particle(mc.player.getX(), MathUtility.random((float) (mc.player.getY() + mc.player.getHeight()), (float) mc.player.getY()), mc.player.getZ(), c));
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

            int alpha = (int) MathUtility.clamp((255f * (i / 8f)), 0 ,255);

            Render3DEngine.drawLine((float) lastPos.x, (float) lastPos.y, (float) lastPos.z, (float) x, (float) y, (float) z, lmode.getValue() == Mode.Sync ? Render2DEngine.injectAlpha(HudEditor.getColor(i * 5), alpha) : Render2DEngine.injectAlpha(lcolor.getValue().getColorObject(), alpha), 2);
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
            double sp = starsScale.getValue() / 10f;
            x += motionX;
            y += motionY;
            z += motionZ;

            if (posBlock(x, y - starsScale.getValue() / 10f, z)) {
                motionY = -motionY / 1.1;
            } else {
                if (    posBlock(x, y, z)
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
                case Stars -> drawStar(matrixStack, color, starsScale.getValue());
                case Orbiz ->  {
                    drawOrbiz(matrixStack, 0.0f, 0.7, color);
                    drawOrbiz(matrixStack, 0.1f, 1.4, color);
                    drawOrbiz(matrixStack, 0.2f, 2.3, color);
                }
                default -> drawHeart(matrixStack, color, starsScale.getValue());
            }

            matrixStack.scale(0.8f, 0.8f, 0.8f);
            matrixStack.pop();
        }

        private boolean posBlock(double x, double y, double z) {
            return (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() != Blocks.AIR && mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() != Blocks.WATER && mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() != Blocks.LAVA);
        }
    }

    private enum Mode {
        Custom, Sync
    }

    private enum Players {
        Trail, Particles, Cute, None
    }
}
