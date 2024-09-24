package thunder.hack.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4d;
import thunder.hack.core.Managers;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.injection.accesors.IAreaEffectCloudEntity;
import thunder.hack.injection.accesors.IBeaconBlockEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

import static thunder.hack.utility.render.animation.AnimationUtility.fast;

public class ESP extends Module {
    public ESP() {
        super("ESP", Category.RENDER);
    }

    private final Setting<Boolean> lingeringPotions = new Setting<>("LingeringPotions", false);
    private final Setting<Boolean> tntFuse = new Setting<>("TNTFuse", false);
    private final Setting<Float> tntrange = new Setting<>("TNTRange", 8.0f, 0f, 8f);
    private final Setting<ColorSetting> tntFuseText = new Setting<>("TNTFuseText", new ColorSetting(new Color(-1)), v -> tntFuse.getValue());
    private final Setting<Boolean> tntRadius = new Setting<>("TNTRadius", false);
    private final Setting<ColorSetting> tntRadiusColor = new Setting<>("TNTSphereColor", new ColorSetting(new Color(-1)), v -> tntRadius.getValue());
    private final Setting<Boolean> beaconRadius = new Setting<>("BeaconRadius", false);
    private final Setting<Boolean> keepY = new Setting<>("KeepY", false, v -> beaconRadius.getValue());
    private final Setting<ColorSetting> sphereColor = new Setting<>("SphereColor", new ColorSetting(new Color(-1)), v -> beaconRadius.getValue());
    private final Setting<ColorSetting> beakonColor = new Setting<>("BeakonColor", new ColorSetting(new Color(-1)), v -> beaconRadius.getValue());
    private final Setting<Boolean> burrow = new Setting<>("Burrow", false);
    private final Setting<ColorSetting> burrowTextColor = new Setting<>("BurrowTextColor", new ColorSetting(new Color(-1)), v -> burrow.getValue());
    private final Setting<ColorSetting> burrowColor = new Setting<>("BurrowColor", new ColorSetting(new Color(-1)), v -> burrow.getValue());
    private final Setting<Boolean> pearls = new Setting<>("Pearls", false);
    private final Setting<Boolean> dizorentRadius = new Setting<>("DizorentRadius", true);
    private final Setting<ColorSetting> dizorentColor = new Setting<>("DizorentColor", new ColorSetting(new Color(0xB300F1CC, true)), v -> dizorentRadius.getValue());

    private final Setting<SettingGroup> boxEsp = new Setting<>("Box", new SettingGroup(false, 0));
    private final Setting<Boolean> players = new Setting<>("Players", true).addToGroup(boxEsp);
    private final Setting<Boolean> friends = new Setting<>("Friends", true).addToGroup(boxEsp);
    private final Setting<Boolean> crystals = new Setting<>("Crystals", true).addToGroup(boxEsp);
    private final Setting<Boolean> creatures = new Setting<>("Creatures", false).addToGroup(boxEsp);
    private final Setting<Boolean> monsters = new Setting<>("Monsters", false).addToGroup(boxEsp);
    private final Setting<Boolean> ambients = new Setting<>("Ambients", false).addToGroup(boxEsp);
    private final Setting<Boolean> others = new Setting<>("Others", false).addToGroup(boxEsp);
    private final Setting<Boolean> outline = new Setting<>("Outline", true).addToGroup(boxEsp);
    private final Setting<Colors> colorMode = new Setting<>("ColorMode", Colors.SyncColor).addToGroup(boxEsp);
    private final Setting<Boolean> renderHealth = new Setting<>("renderHealth", true).addToGroup(boxEsp);

    private final Setting<SettingGroup> boxColors = new Setting<>("BoxColors", new SettingGroup(false, 0));
    private final Setting<ColorSetting> playersC = new Setting<>("PlayersC", new ColorSetting(new Color(0xFF9200))).addToGroup(boxColors);
    private final Setting<ColorSetting> friendsC = new Setting<>("FriendsC", new ColorSetting(new Color(0x30FF00))).addToGroup(boxColors);
    private final Setting<ColorSetting> crystalsC = new Setting<>("CrystalsC", new ColorSetting(new Color(0x00BBFF))).addToGroup(boxColors);
    private final Setting<ColorSetting> creaturesC = new Setting<>("CreaturesC", new ColorSetting(new Color(0xA0A4A6))).addToGroup(boxColors);
    private final Setting<ColorSetting> monstersC = new Setting<>("MonstersC", new ColorSetting(new Color(0xFF0000))).addToGroup(boxColors);
    private final Setting<ColorSetting> ambientsC = new Setting<>("AmbientsC", new ColorSetting(new Color(0x7B00FF))).addToGroup(boxColors);
    private final Setting<ColorSetting> othersC = new Setting<>("OthersC", new ColorSetting(new Color(0xFF0062))).addToGroup(boxColors);
    public final Setting<ColorSetting> healthB = new Setting<>("healthB", new ColorSetting(new Color(0xff1100))).addToGroup(boxColors);
    public final Setting<ColorSetting> healthU = new Setting<>("healthU", new ColorSetting(new Color(0x2fff00))).addToGroup(boxColors);

    private float dizorentAnimation = 0f;

    public void onRender3D(MatrixStack stack) {
        if(mc.options.hudHidden) return;
        if (lingeringPotions.getValue()) {
            for (Entity ent : mc.world.getEntities()) {
                if (ent instanceof AreaEffectCloudEntity aece) {
                    double x = aece.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
                    double y = aece.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
                    double z = aece.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();

                    float middle = aece.getRadius();

                    stack.push();
                    stack.translate(x, y, z);

                    Render3DEngine.setupRender();
                    RenderSystem.disableDepthTest();
                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

                    for (int i = 0; i <= 360; i += 6) {
                        double v = Math.sin(Math.toRadians(i));
                        double u = Math.cos(Math.toRadians(i));
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * middle, (float) 0, (float) v * middle).color(Render2DEngine.injectAlpha(new Color(getAreaCloudColor(aece)), 100).getRGB());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), 0, 0, 0).color(Render2DEngine.injectAlpha(new Color(getAreaCloudColor(aece)), 0).getRGB());
                    }
                    Render2DEngine.endBuilding(bufferBuilder);

                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    for (int i = 0; i <= 360; i += 6) {
                        double v = Math.sin(Math.toRadians(i));
                        double u = Math.cos(Math.toRadians(i));
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * middle, (float) 0, (float) v * middle).color(Render2DEngine.injectAlpha(new Color(getAreaCloudColor(aece)), 255).getRGB());
                        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * (middle - 0.04f), (float) 0, (float) v * (middle - 0.04f)).color(Render2DEngine.injectAlpha(new Color(getAreaCloudColor(aece)), 255).getRGB());
                    }
                    Render2DEngine.endBuilding(bufferBuilder);

                    Render3DEngine.endRender();
                    RenderSystem.enableDepthTest();
                    stack.translate(-x, -y, -z);
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
                    FontRenderers.modules.drawCenteredString(matrices, String.format("%.1f", ((aece.getRadius() * 10) - 5f)), 0, -10f, -1);
                    RenderSystem.disableBlend();
                    RenderSystem.enableDepthTest();
                }
            }
        }

        if (dizorentRadius.getValue()) {
            dizorentAnimation = fast(dizorentAnimation, mc.player.getMainHandStack().getItem() == Items.ENDER_EYE ? 10 : 0, 15f);

            if (mc.player.getMainHandStack().getItem() == Items.ENDER_EYE) {
                double x = Render2DEngine.interpolate(mc.player.prevX, mc.player.getX(), Render3DEngine.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getX();
                double y = Render2DEngine.interpolate(mc.player.prevY, mc.player.getY(), Render3DEngine.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getY();
                double z = Render2DEngine.interpolate(mc.player.prevZ, mc.player.getZ(), Render3DEngine.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getZ();


                stack.push();
                stack.translate(x, y, z);

                Render3DEngine.setupRender();
                RenderSystem.disableDepthTest();
                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

                for (int i = 0; i <= 360; i += 6) {
                    double v = Math.sin(Math.toRadians(i));
                    double u = Math.cos(Math.toRadians(i));
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * dizorentAnimation, (float) 0, (float) v * dizorentAnimation).color(Render2DEngine.injectAlpha(new Color(dizorentColor.getValue().getColor()), 100).getRGB());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), 0, 0, 0).color(Render2DEngine.injectAlpha(new Color(dizorentColor.getValue().getColor()), 0).getRGB());
                }
                Render2DEngine.endBuilding(bufferBuilder);

                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                for (int i = 0; i <= 360; i += 6) {
                    double v = Math.sin(Math.toRadians(i));
                    double u = Math.cos(Math.toRadians(i));
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * dizorentAnimation, (float) 0, (float) v * dizorentAnimation).color(Render2DEngine.injectAlpha(new Color(dizorentColor.getValue().getColor()), 255).getRGB());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) u * (dizorentAnimation - 0.04f), (float) 0, (float) v * (dizorentAnimation - 0.04f)).color(Render2DEngine.injectAlpha(new Color(dizorentColor.getValue().getColor()), 255).getRGB());
                }
                Render2DEngine.endBuilding(bufferBuilder);

                Render3DEngine.endRender();
                RenderSystem.enableDepthTest();
                stack.translate(-x, -y, -z);
                stack.pop();

                for (PlayerEntity pl : Managers.ASYNC.getAsyncPlayers()) {
                    if (mc.player.squaredDistanceTo(pl.getPos()) > 100 || pl == mc.player)
                        continue;
                    Render3DEngine.drawTargetEsp(stack, pl);
                }
            }
        }

        if (beaconRadius.getValue()) {
            for (BlockEntity be : StorageEsp.getBlockEntities()) {
                if (be instanceof BeaconBlockEntity bbe) {
                    double x = be.getPos().getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
                    double y = be.getPos().getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
                    double z = be.getPos().getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();

                    Render3DEngine.drawBoxOutline(new Box(be.getPos()), beakonColor.getValue().getColorObject(), 2);
                    float range = ((IBeaconBlockEntity) bbe).getLevel() * 10 + 11;

                    boolean ky = keepY.getValue();

                    stack.push();
                    stack.translate(x,  ky ? -10 : y, z);
                    Render3DEngine.drawCylinder(stack, range, ky ? 20 : 256, 20, ky ? 5 : 20, sphereColor.getValue().getColor());
                    stack.translate(-x, ky ? 10 : y, -z);
                    stack.pop();
                }
            }
        }

        if (burrow.getValue()) {
            for (PlayerEntity pl : mc.world.getPlayers()) {
                BlockPos blockPos = BlockPos.ofFloored(pl.getPos().add(0,0.15f,0));
                Block block = mc.world.getBlockState(blockPos).getBlock();

                double x = blockPos.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
                double y = blockPos.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
                double z = blockPos.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();

                if (block == Blocks.OBSIDIAN
                        || block == Blocks.CRYING_OBSIDIAN
                        || block == Blocks.ANVIL
                        || block == Blocks.PLAYER_HEAD
                        || block == Blocks.SKELETON_SKULL
                        || block == Blocks.WITHER_SKELETON_SKULL) {
                    Render3DEngine.drawBoxOutline(new Box(blockPos), burrowColor.getValue().getColorObject(), 2);
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
                    FontRenderers.modules.drawCenteredString(matrices, "BURROW", 0, -5, burrowTextColor.getValue().getColor());
                    RenderSystem.disableBlend();
                    RenderSystem.enableDepthTest();
                }
            }
        }

        if (tntFuse.getValue() || tntRadius.getValue()) {
            for (Entity ent : mc.world.getEntities()) {
                if (ent instanceof TntEntity tnt) {
                    double x = tnt.prevX + (tnt.getPos().getX() - tnt.prevX) * Render3DEngine.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
                    double y = tnt.prevY + (tnt.getPos().getY() - tnt.prevY) * Render3DEngine.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
                    double z = tnt.prevZ + (tnt.getPos().getZ() - tnt.prevZ) * Render3DEngine.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();

                    if (tntFuse.getValue()) {
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
                        FontRenderers.modules.drawCenteredString(matrices, String.format("%.1f", ((float) tnt.getFuse() / 20f)) + "s", 0, -5, tntFuseText.getValue().getColor());
                        RenderSystem.disableBlend();
                        RenderSystem.enableDepthTest();
                    }

                    if (tntRadius.getValue()) {
                        stack.push();
                        stack.translate(x, y, z);
                        Render3DEngine.drawSphere(stack, tntrange.getValue(), 20, 20, tntRadiusColor.getValue().getColor());
                        stack.translate(-x, -y, -z);
                        stack.pop();
                    }
                }
            }
        }
    }

    public void onRender2D(DrawContext context) {
        if(mc.options.hudHidden) return;
        if (pearls.getValue()) {
            for (Entity ent : mc.world.getEntities()) {
                if (ent instanceof EnderPearlEntity pearl) {
                    float xOffset = mc.getWindow().getScaledWidth() / 2f;
                    float yOffset = mc.getWindow().getScaledHeight() / 2f;

                    float xPos = (float) (pearl.prevX + (pearl.getPos().getX() - pearl.prevX) * Render3DEngine.getTickDelta());
                    float zPos = (float) (pearl.prevZ + (pearl.getPos().getZ() - pearl.prevZ) * Render3DEngine.getTickDelta());

                    float yaw = getRotations(new Vec2f(xPos, zPos)) - mc.player.getYaw();
                    context.getMatrices().translate(xOffset, yOffset, 0.0F);
                    context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
                    context.getMatrices().translate(-xOffset, -yOffset, 0.0F);
                    Render2DEngine.drawTracerPointer(context.getMatrices(), xOffset, yOffset - 50, 12.5f, 0.5f, 3.63f, true, true, HudEditor.getColor(1).getRGB());
                    context.getMatrices().translate(xOffset, yOffset, 0.0F);
                    context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
                    context.getMatrices().translate(-xOffset, -yOffset, 0.0F);
                    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                    FontRenderers.modules.drawCenteredString(context.getMatrices(), String.format("%.1f", mc.player.distanceTo(pearl)) + "m", (float) (Math.sin(Math.toRadians(yaw)) * 50f) + xOffset, (float) (yOffset - (Math.cos(Math.toRadians(yaw)) * 50f)) - 20, -1);
                }
            }
        }

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        Render2DEngine.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Entity ent : mc.world.getEntities())
            if (shouldRender(ent))
                drawBox(bufferBuilder, ent, matrix);

        Render2DEngine.endBuilding(bufferBuilder);
        Render2DEngine.endRender();
    }

    public boolean shouldRender(Entity entity) {
        if (entity == null)
            return false;

        if (mc.player == null)
            return false;

        if (entity instanceof PlayerEntity) {
            if (entity == mc.player)
                return false;
            if (Managers.FRIEND.isFriend((PlayerEntity) entity))
                return friends.getValue();
            return players.getValue();
        }

        if (entity instanceof EndCrystalEntity)
            return crystals.getValue();

        return switch (entity.getType().getSpawnGroup()) {
            case CREATURE, WATER_CREATURE -> creatures.getValue();
            case MONSTER -> monsters.getValue();
            case AMBIENT, WATER_AMBIENT -> ambients.getValue();
            default -> others.getValue();
        };
    }

    public Color getEntityColor(Entity entity) {
        if (entity == null)
            return new Color(-1);

        if (entity instanceof PlayerEntity) {
            if (Managers.FRIEND.isFriend((PlayerEntity) entity))
                return friendsC.getValue().getColorObject();
            return playersC.getValue().getColorObject();
        }

        if (entity instanceof EndCrystalEntity)
            return crystalsC.getValue().getColorObject();

        return switch (entity.getType().getSpawnGroup()) {
            case CREATURE, WATER_CREATURE -> creaturesC.getValue().getColorObject();
            case MONSTER -> monstersC.getValue().getColorObject();
            case AMBIENT, WATER_AMBIENT -> ambientsC.getValue().getColorObject();
            default -> othersC.getValue().getColorObject();
        };
    }

    public void drawBox(BufferBuilder bufferBuilder, @NotNull Entity ent, Matrix4f matrix) {
        Vec3d[] vectors = getVectors(ent);

        Color col = getEntityColor(ent);

        Vector4d position = null;
        for (Vec3d vector : vectors) {
            vector = Render3DEngine.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
            if (vector.z > 0 && vector.z < 1) {
                if (position == null) position = new Vector4d(vector.x, vector.y, vector.z, 0);
                position.x = Math.min(vector.x, position.x);
                position.y = Math.min(vector.y, position.y);
                position.z = Math.max(vector.x, position.z);
                position.w = Math.max(vector.y, position.w);
            }
        }


        if (position != null) {
            double posX = position.x;
            double posY = position.y;
            double endPosX = position.z;
            double endPosY = position.w;

            if(outline.getValue()) {
                Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) (posX - 1F), (float) posY, (float) (posX + 0.5), (float) (endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
                Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) (posX - 1F), (float) (posY - 0.5), (float) (endPosX + 0.5), (float) (posY + 0.5 + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
                Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) (endPosX - 0.5 - 0.5), (float) posY, (float) (endPosX + 0.5), (float) (endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
                Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) (posX - 1), (float) (endPosY - 0.5 - 0.5), (float) (endPosX + 0.5), (float) (endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
            }

            switch(colorMode.getValue()) {
                case Custom -> {
                    Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) (posX - 0.5f), (float) posY, (float) (posX + 0.5 - 0.5), (float) endPosY, col, col, col, col);
                    Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) posX, (float) (endPosY - 0.5f), (float) endPosX, (float) endPosY, col, col, col, col);
                    Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) (posX - 0.5), (float) posY, (float) endPosX, (float) (posY + 0.5), col, col, col, col);
                    Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) (endPosX - 0.5), (float) posY, (float) endPosX, (float) endPosY, col, col, col, col);
                }
                case SyncColor -> {
                    Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) (posX - 0.5f), (float) posY, (float) (posX + 0.5 - 0.5), (float) endPosY, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(0), HudEditor.getColor(270));
                    Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) posX, (float) (endPosY - 0.5f), (float) endPosX, (float) endPosY, HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(180), HudEditor.getColor(0));
                    Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) (posX - 0.5), (float) posY, (float) endPosX, (float) (posY + 0.5), HudEditor.getColor(180), HudEditor.getColor(90), HudEditor.getColor(90), HudEditor.getColor(180));
                    Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) (endPosX - 0.5), (float) posY, (float) endPosX, (float) endPosY, HudEditor.getColor(90), HudEditor.getColor(270), HudEditor.getColor(270), HudEditor.getColor(90));
                }
            }


            if(ent instanceof LivingEntity lent && lent.getHealth() != 0 && renderHealth.getValue()) {
                Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) (posX - 5), (float) posY, (float) posX - 3, (float) endPosY, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
                switch(colorMode.getValue()) {
                    case Custom -> Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) (posX - 5), (float) (endPosY + (posY - endPosY) * lent.getHealth() / lent.getMaxHealth()), (float) posX - 3, (float) endPosY, healthB.getValue().getColorObject(), healthB.getValue().getColorObject(), healthU.getValue().getColorObject(), healthU.getValue().getColorObject());
                    case SyncColor ->  Render2DEngine.setRectPoints(bufferBuilder, matrix, (float) (posX - 5), (float) (endPosY + (posY - endPosY) * lent.getHealth() / lent.getMaxHealth()), (float) posX - 3, (float) endPosY, HudEditor.getColor(90), HudEditor.getColor(90), HudEditor.getColor(270), HudEditor.getColor(270));
                }
            } }
    }

    @NotNull
    private static Vec3d[] getVectors(@NotNull Entity ent) {
        double x = ent.prevX + (ent.getX() - ent.prevX) * Render3DEngine.getTickDelta();
        double y = ent.prevY + (ent.getY() - ent.prevY) * Render3DEngine.getTickDelta();
        double z = ent.prevZ + (ent.getZ() - ent.prevZ) * Render3DEngine.getTickDelta();
        Box axisAlignedBB2 = ent.getBoundingBox();
        Box axisAlignedBB = new Box(axisAlignedBB2.minX - ent.getX() + x - 0.05, axisAlignedBB2.minY - ent.getY() + y, axisAlignedBB2.minZ - ent.getZ() + z - 0.05, axisAlignedBB2.maxX - ent.getX() + x + 0.05, axisAlignedBB2.maxY - ent.getY() + y + 0.15, axisAlignedBB2.maxZ - ent.getZ() + z + 0.05);
        return new Vec3d[]{new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)};
    }

    private int getAreaCloudColor(AreaEffectCloudEntity ent) {
        ParticleEffect particleEffect = ent.getParticleType();
        if (particleEffect instanceof EntityEffectParticleEffect effect) {
            return ((IAreaEffectCloudEntity)ent).getPotionContentsComponent().getColor();
        }
        return -1;
    }

    public static float getRotations(Vec2f vec) {
        if (mc.player == null) return 0;
        double x = vec.x - mc.player.getPos().x;
        double z = vec.y - mc.player.getPos().z;
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }

    public enum Colors {
        SyncColor, Custom
    }
}
