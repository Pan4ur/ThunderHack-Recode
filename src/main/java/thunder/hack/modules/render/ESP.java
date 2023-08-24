package thunder.hack.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.*;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.impl.RadarRewrite;
import thunder.hack.injection.accesors.IBeaconBlockEntity;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class ESP extends Module {

    public ESP() {
        super("ESP", Category.RENDER);
    }

    private final Setting<Boolean> lingeringPotions = new Setting<>("LingeringPotions", false);
    private final Setting<Boolean> tntFuse = new Setting<>("TNTFuse", false);
    private Setting<Float> tntrange = new Setting<>("TNTRange",8.0f,0f,8f);
    private final Setting<ColorSetting> tntFuseText = new Setting<>("TNTFuseText", new ColorSetting(new Color(-1)),v-> tntFuse.getValue());
    private final Setting<Boolean> tntRadius = new Setting<>("TNTRadius", false);
    private final Setting<ColorSetting> tntRadiusColor = new Setting<>("TNTSphereColor", new ColorSetting(new Color(-1)),v-> tntRadius.getValue());
    private final Setting<Boolean> beaconRadius = new Setting<>("BeaconRadius", false);
    private final Setting<ColorSetting> sphereColor = new Setting<>("SphereColor", new ColorSetting(new Color(-1)),v-> beaconRadius.getValue());
    private final Setting<ColorSetting> beakonColor = new Setting<>("BeakonColor", new ColorSetting(new Color(-1)),v-> beaconRadius.getValue());
    private final Setting<Boolean> burrow = new Setting<>("Burrow", false);
    private final Setting<ColorSetting> burrowTextColor = new Setting<>("BurrowTextColor", new ColorSetting(new Color(-1)),v-> burrow.getValue());
    private final Setting<ColorSetting> burrowColor = new Setting<>("BurrowColor", new ColorSetting(new Color(-1)),v-> burrow.getValue());
    private final Setting<Boolean> pearls = new Setting<>("Pearls", false);

    public void onRender3D(MatrixStack stack){
        if(lingeringPotions.getValue()){
            for(Entity ent : mc.world.getEntities()){
                if(ent instanceof AreaEffectCloudEntity aece){
                    double x = aece.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
                    double y = aece.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
                    double z = aece.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();

                    float middle = aece.getRadius();

                    stack.push();
                    stack.translate(x,y,z);


                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferBuilder = tessellator.getBuffer();
                    Render3DEngine.setup();
                    RenderSystem.disableDepthTest();
                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i <= 360; i += 6) {
                        double v = Math.sin(Math.toRadians(i));
                        double u = Math.cos(Math.toRadians(i));
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * middle, (float) 0, (float) v * middle).color(Render2DEngine.injectAlpha(new Color(aece.getColor()),100).getRGB()).next();
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), 0, 0, 0).color(Render2DEngine.injectAlpha(new Color(aece.getColor()),0).getRGB()).next();
                    }
                    tessellator.draw();


                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i <= 360; i += 6) {
                        double v = Math.sin(Math.toRadians(i));
                        double u = Math.cos(Math.toRadians(i));
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * middle, (float) 0, (float) v * middle).color(Render2DEngine.injectAlpha(new Color(aece.getColor()),255).getRGB()).next();
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * (middle - 0.04f), (float) 0, (float) v * (middle - 0.04f)).color(Render2DEngine.injectAlpha(new Color(aece.getColor()),255).getRGB()).next();
                    }
                    tessellator.draw();

                    Render3DEngine.cleanup();
                    RenderSystem.enableDepthTest();
                    stack.translate(-x,-y,-z);
                    stack.pop();

                    RenderSystem.disableDepthTest();
                    MatrixStack matrices = new MatrixStack();
                    Camera camera = mc.gameRenderer.getCamera();
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                    matrices.translate(x, y, z);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    matrices.translate(0, 0, 0);
                    matrices.scale(-0.05f, -0.05f, 0);
                    VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                    FontRenderers.modules.drawCenteredString(matrices, String.format("%.1f",((aece.getRadius() * 10) - 5f)), 0, -10f, -1);
                    immediate.draw();
                    RenderSystem.disableBlend();
                    RenderSystem.enableDepthTest();
                }
            }
        }

        if(beaconRadius.getValue()){
            for(BlockEntity be : StorageEsp.getBlockEntities()) {
                if(be instanceof BeaconBlockEntity bbe) {
                    double x = be.getPos().getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
                    double y = be.getPos().getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
                    double z = be.getPos().getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();

                    Render3DEngine.drawBoxOutline(new Box(be.getPos()),beakonColor.getValue().getColorObject(),2);
                    int level = ((IBeaconBlockEntity)bbe).getLevel();
                    float range = level == 1.0F ? 19.0F : (level == 2.0F ? 29.0F : (level == 3.0F ? 40.0F : (level == 4.0F ? 51.f : 0.0F)));

                    stack.push();
                    stack.translate(x, y, z);
                    Render3DEngine.drawSphere(stack, range, 20, 20,sphereColor.getValue().getColor());
                    stack.translate(-x, -y, -z);
                    stack.pop();
                }
            }
        }

        if(burrow.getValue()){
            for (PlayerEntity pl : mc.world.getPlayers()){
                BlockPos blockPos = BlockPos.ofFloored(pl.getPos());
                Block block = mc.world.getBlockState(blockPos).getBlock();

                double x = blockPos.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
                double y = blockPos.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
                double z = blockPos.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();

                if(block == Blocks.OBSIDIAN
                        || block == Blocks.CRYING_OBSIDIAN
                        || block == Blocks.ANVIL
                        || block == Blocks.PLAYER_HEAD
                        || block == Blocks.SKELETON_SKULL
                        || block == Blocks.WITHER_SKELETON_SKULL){
                    Render3DEngine.drawBoxOutline(new Box(blockPos),burrowColor.getValue().getColorObject(),2);
                    RenderSystem.disableDepthTest();
                    MatrixStack matrices = new MatrixStack();
                    Camera camera = mc.gameRenderer.getCamera();
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                    matrices.translate(x + 0.5f, y + 0.5f, z + 0.5f);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    matrices.translate(0, 0, 0);
                    matrices.scale(-0.025f, -0.025f, 0);
                    VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                    FontRenderers.modules.drawCenteredString(matrices, "BURROW", 0, -5, burrowTextColor.getValue().getColor());
                    immediate.draw();
                    RenderSystem.disableBlend();
                    RenderSystem.enableDepthTest();
                }
            }
        }

        if(tntFuse.getValue() || tntRadius.getValue()){
            for(Entity ent : mc.world.getEntities()) {
                if (ent instanceof TntEntity tnt) {
                    double x = tnt.prevX + (tnt.getPos().getX() - tnt.prevX) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
                    double y = tnt.prevY + (tnt.getPos().getY() - tnt.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
                    double z = tnt.prevZ + (tnt.getPos().getZ() - tnt.prevZ) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();

                    if(tntFuse.getValue()) {
                        RenderSystem.disableDepthTest();
                        MatrixStack matrices = new MatrixStack();
                        Camera camera = mc.gameRenderer.getCamera();
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                        matrices.translate(x, y + 0.5f, z);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        matrices.translate(0, 0, 0);
                        matrices.scale(-0.025f, -0.025f, 0);
                        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                        FontRenderers.modules.drawCenteredString(matrices, String.format("%.1f", ((float) tnt.getFuse() / 20f)) + "s", 0, -5, tntFuseText.getValue().getColor());
                        immediate.draw();
                        RenderSystem.disableBlend();
                        RenderSystem.enableDepthTest();
                    }

                    if(tntRadius.getValue()) {
                        stack.push();
                        stack.translate(x, y, z);
                        Render3DEngine.drawSphere(stack, tntrange.getValue(), 20, 20,tntRadiusColor.getValue().getColor());
                        stack.translate(-x, -y, -z);
                        stack.pop();
                    }
                }
            }
        }
    }

    public void onRender2D(DrawContext context){
        if(pearls.getValue()) {
            for (Entity ent : mc.world.getEntities()) {
                if (ent instanceof EnderPearlEntity pearl) {
                    float xOffset = mc.getWindow().getScaledWidth() / 2f;
                    float yOffset = mc.getWindow().getScaledHeight() / 2f;

                    float xPos = (float) (pearl.prevX + (pearl.getPos().getX() - pearl.prevX) * mc.getTickDelta());
                    float zPos = (float) (pearl.prevZ + (pearl.getPos().getZ() - pearl.prevZ) * mc.getTickDelta());

                    float yaw = getRotations(new Vec2f(xPos, zPos)) - mc.player.getYaw();
                    context.getMatrices().translate(xOffset, yOffset, 0.0F);
                    context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
                    context.getMatrices().translate(-xOffset, -yOffset, 0.0F);
                    RadarRewrite.drawTracerPointer(context.getMatrices(), xOffset, yOffset - 50, 12.5f, HudEditor.getColor(1).getRGB());
                    context.getMatrices().translate(xOffset, yOffset, 0.0F);
                    context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
                    context.getMatrices().translate(-xOffset, -yOffset, 0.0F);
                    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                    FontRenderers.modules.drawCenteredString(context.getMatrices(), String.format("%.1f",mc.player.distanceTo(pearl)) + "m", (float) (Math.sin(Math.toRadians(yaw)) * 50f) + xOffset, (float) (yOffset - (Math.cos(Math.toRadians(yaw)) * 50f)) - 20, -1);
                }
            }
        }
    }

    public static float getRotations(Vec2f vec) {
        if (mc.player == null) return 0;
        double x = vec.x - mc.player.getPos().x;
        double z = vec.y - mc.player.getPos().z;
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }
}
