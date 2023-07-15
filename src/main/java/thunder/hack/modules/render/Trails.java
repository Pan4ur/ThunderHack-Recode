package thunder.hack.modules.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import thunder.hack.events.impl.PreRender3DEvent;
import thunder.hack.events.impl.Render3DEvent;
import thunder.hack.utility.interfaces.IEntity;
import thunder.hack.modules.Module;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Trails extends Module {
    public Trails() {
        super("Trails", "Trails", Category.RENDER);
    }


    public final Setting<Boolean> pearls = new Setting("Pearls", false);
    public final Setting<Boolean> xp = new Setting("Xp", false);
    public final Setting<Boolean> arrows = new Setting("Arrows", false);
    public final Setting<Boolean> players = new Setting("Players", false);
   // private final Setting<cMode> cmode = new Setting<>("ColorMode", cMode.Rainbow));

    Iterable<Entity> asyncEntities = new ArrayList<>();

    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x8800FF00));
    public Setting<Float> down = new Setting("Down", 0.5F, 0.0F, 2.0F);
    public Setting<Float> width = new Setting("Height", 1.3F, 0.1F, 2.0F);

    @Subscribe
    public void onPreRender3D(PreRender3DEvent event) {
        for (Entity en : asyncEntities) {
            if (en instanceof EnderPearlEntity && pearls.getValue()) {
                calcTrajectory(en);
            }
        }

        for (Entity en : asyncEntities) {
            if (en instanceof ArrowEntity && arrows.getValue()) {
                calcTrajectory(en);
            }
        }

        for (Entity en : asyncEntities) {
            if (en instanceof ExperienceBottleEntity && xp.getValue()) {
                calcTrajectory(en);
            }
        }

        if(players.getValue()){
            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (entity == mc.player && mc.options.getPerspective().isFirstPerson()) continue;

                float alpha = color.getValue().getAlpha() / 255f;


                if (((IEntity) entity).getTrails().size() > 0) {
                    event.getMatrixStack().push();
                    RenderSystem.disableCull();
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferBuilder = tessellator.getBuffer();

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthFunc(GL11.GL_LEQUAL);

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i < ((IEntity) entity).getTrails().size(); i++) {
                        Trail ctx = ((IEntity) entity).getTrails().get(i);
                        Vec3d pos = ctx.interpolate(mc.getTickDelta());
                        bufferBuilder.vertex(event.getMatrixStack().peek().getPositionMatrix(), (float) pos.x, (float) pos.y + down.getValue(), (float) pos.z).color(Render2DEngine.injectAlpha(((IEntity) entity).getTrails().get(i).color(), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                        bufferBuilder.vertex(event.getMatrixStack().peek().getPositionMatrix(), (float) pos.x, (float) pos.y + width.getValue()  + down.getValue(), (float) pos.z).color(Render2DEngine.injectAlpha(((IEntity) entity).getTrails().get(i).color(), (int) ((alpha * ctx.animation(mc.getTickDelta())) * 255)).getRGB()).next();
                    }

                    tessellator.draw();
                    Render3DEngine.cleanup();
                    RenderSystem.enableCull();
                    RenderSystem.disableDepthTest();
                    event.getMatrixStack().pop();
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        asyncEntities = mc.world.getEntities();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if(player.getPos().getZ() != player.prevZ || player.getPos().getX() != player.prevX){
                ((IEntity)player).getTrails().add(new Trail(new Vec3d(player.prevX,player.prevY,player.prevZ), player.getPos(), color.getValue().getColorObject()));
            }
            ((IEntity)player).getTrails().removeIf(Trail::update);
        }
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

    public HashMap<Entity, Vec3d> lastPoss = new HashMap<>();
    public HashMap<Entity, Integer> i1 = new HashMap<>();

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
            if(e instanceof ArrowEntity){
                motionY -= 0.05000000074505806;
            } else {
                motionY -= 0.03f;
            }
            Vec3d pos = new Vec3d(x, y, z);

            if (mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)) != null) {
                if (mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.ENTITY) {
                    break;
                }
                if (mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.BLOCK) {
                    break;
                }
            }

            if (y <= 0) break;
            if (e.getVelocity().x == 0 && e.getVelocity().y == 0 && e.getVelocity().z == 0) continue;
            lastPoss.put(e, new Vec3d(lastPos.x, lastPos.y, lastPos.z));
            Render3DEngine.drawLine((float) lastPos.x, (float) lastPos.y, (float) lastPos.z,(float) x, (float) y, (float) z,Render2DEngine.astolfo(i, 300, 0.5f, 10),2);
            i1.put(e, i);
        }

    }
}
