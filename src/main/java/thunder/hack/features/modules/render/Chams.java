package thunder.hack.features.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventHeldItemRenderer;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

public class Chams extends Module {
    public Chams() {
        super("Chams", Category.RENDER);
    }

    public final Setting<Boolean> handItems = new Setting<>("HandItems", false);
    private final Setting<ColorSetting> handItemsColor = new Setting<>("HandItemsColor", new ColorSetting(new Color(0x9317DE5D, true)), v -> handItems.getValue());

    public final Setting<Boolean> crystals = new Setting<>("Crystals", false);
    private final Setting<ColorSetting> crystalColor = new Setting<>("CrystalColor", new ColorSetting(new Color(0x932DD8E8, true)), v -> crystals.getValue());
    private final Setting<Boolean> staticCrystal = new Setting<>("StaticCrystal", true, v -> crystals.getValue());
    private final Setting<CMode> crystalMode = new Setting<>("CrystalMode", CMode.One, v -> crystals.getValue());

    public final Setting<Boolean> players = new Setting<>("Players", false);
    private final Setting<ColorSetting> playerColor = new Setting<>("PlayerColor", new ColorSetting(new Color(0x932DD8E8, true)), v -> players.getValue());
    private final Setting<ColorSetting> friendColor = new Setting<>("FriendColor", new ColorSetting(new Color(0x932DE830, true)), v -> players.getValue());
    private final Setting<Boolean> playerTexture = new Setting<>("PlayerTexture", true, v -> players.getValue());
    private final Setting<Boolean> simple = new Setting<>("Simple", false, v -> players.getValue());

    private final Setting<Boolean> alternativeBlending = new Setting<>("AlternativeBlending", true);

    private enum CMode {
        One, Two, Three
    }

    private final Identifier crystalTexture = Identifier.of("textures/entity/end_crystal/end_crystal.png");
    private static final float SINE_45_DEGREES = (float) Math.sin(0.7853981633974483);

    public void renderCrystal(EndCrystalEntity endCrystalEntity, float f, float g, MatrixStack matrixStack, int i, ModelPart core, ModelPart frame) {
        RenderSystem.enableBlend();
        if (alternativeBlending.getValue())
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        else RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        BufferBuilder buffer;

        if (crystalMode.getValue() != CMode.One) {
            if (crystalMode.getValue() == CMode.Three) {
                RenderSystem.setShaderTexture(0, crystalTexture);
            } else {
                RenderSystem.setShaderTexture(0, TextureStorage.crystalTexture2);
            }
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        }

        matrixStack.push();
        float h = staticCrystal.getValue() ? -1.4f : EndCrystalEntityRenderer.getYOffset(endCrystalEntity, g);
        float j = ((float) endCrystalEntity.endCrystalAge + g) * 3.0f;
        matrixStack.push();
        RenderSystem.setShaderColor(crystalColor.getValue().getGlRed(), crystalColor.getValue().getGlGreen(), crystalColor.getValue().getGlBlue(), crystalColor.getValue().getGlAlpha());
        matrixStack.scale(2.0f, 2.0f, 2.0f);
        matrixStack.translate(0.0f, -0.5f, 0.0f);
        int k = OverlayTexture.DEFAULT_UV;
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        matrixStack.translate(0.0f, 1.5f + h / 2.0f, 0.0f);
        matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        frame.render(matrixStack, buffer, i, k);
        matrixStack.scale(0.875f, 0.875f, 0.875f);
        matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        frame.render(matrixStack, buffer, i, k);
        matrixStack.scale(0.875f, 0.875f, 0.875f);
        matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        core.render(matrixStack, buffer, i, k);
        matrixStack.pop();
        matrixStack.pop();
        Render2DEngine.endBuilding(buffer);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

    public void renderPlayer(PlayerEntity pe, float f, float g, MatrixStack matrixStack, int i, EntityModel model, CallbackInfo ci, Runnable post) {
        RenderSystem.enableBlend();
        if (alternativeBlending.getValue())
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        else RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        BufferBuilder buffer;

        if (!simple.getValue()) {
            RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity) pe).getSkinTextures().texture());
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        }

        float n;
        Direction direction;
        Entity entity;
        matrixStack.push();

        if (Managers.FRIEND.isFriend(pe)) {
            RenderSystem.setShaderColor(friendColor.getValue().getGlRed(), friendColor.getValue().getGlGreen(), friendColor.getValue().getGlBlue(), friendColor.getValue().getGlAlpha());
        } else {
            RenderSystem.setShaderColor(playerColor.getValue().getGlRed(), playerColor.getValue().getGlGreen(), playerColor.getValue().getGlBlue(), playerColor.getValue().getGlAlpha());
        }

        model.handSwingProgress = pe.getHandSwingProgress(g);
        model.riding = pe.hasVehicle();
        model.child = false;
        float h = MathHelper.lerpAngleDegrees(g, pe.prevBodyYaw, pe.bodyYaw);
        float j = MathHelper.lerpAngleDegrees(g, pe.prevHeadYaw, pe.headYaw);
        float k = j - h;
        if (pe.hasVehicle() && (entity = pe.getVehicle()) instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity) entity;
            h = MathHelper.lerpAngleDegrees(g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
            k = j - h;
            float l = MathHelper.wrapDegrees(k);
            if (l < -85.0f) {
                l = -85.0f;
            }
            if (l >= 85.0f) {
                l = 85.0f;
            }
            h = j - l;
            if (l * l > 2500.0f) {
                h += l * 0.2f;
            }
            k = j - h;
        }
        float m = MathHelper.lerp(g, pe.prevPitch, pe.getPitch());
        if (LivingEntityRenderer.shouldFlipUpsideDown(pe)) {
            m *= -1.0f;
            k *= -1.0f;
        }
        if (pe.isInPose(EntityPose.SLEEPING) && (direction = pe.getSleepingDirection()) != null) {
            n = pe.getEyeHeight(EntityPose.STANDING) - 0.1f;
            matrixStack.translate((float) (-direction.getOffsetX()) * n, 0.0f, (float) (-direction.getOffsetZ()) * n);
        }
        float l = pe.age + g;

        setupTransforms1(pe, matrixStack, l, h, g);
        matrixStack.scale(-1.0f, -1.0f, 1.0f);

        matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
        matrixStack.translate(0.0f, -1.501f, 0.0f);

        n = 0.0f;
        float o = 0.0f;
        if (!pe.hasVehicle() && pe.isAlive()) {
            n = pe.limbAnimator.getSpeed(g);
            o = pe.limbAnimator.getPos(g);
            if (pe.isBaby())
                o *= 3.0f;

            if (n > 1.0f)
                n = 1.0f;
        }
        model.animateModel(pe, o, n, g);
        model.setAngles(pe, o, n, l, k, m);
        int p = LivingEntityRenderer.getOverlay(pe, 0);
        model.render(matrixStack, buffer, i, p);
        Render2DEngine.endBuilding(buffer);
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        matrixStack.pop();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableDepthTest();
        if (!playerTexture.getValue()) {
            ci.cancel();
            post.run();
        }
    }

    public void setupTransforms1(PlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h) {
        float j = abstractClientPlayerEntity.getLeaningPitch(h);
        float k = abstractClientPlayerEntity.getPitch(h);
        float l;
        float m;
        if (abstractClientPlayerEntity.isFallFlying()) {
            setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
            l = (float) abstractClientPlayerEntity.getFallFlyingTicks() + h;
            m = MathHelper.clamp(l * l / 100.0F, 0.0F, 1.0F);
            if (!abstractClientPlayerEntity.isUsingRiptide()) {
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m * (-90.0F - k)));
            }

            Vec3d vec3d = abstractClientPlayerEntity.getRotationVec(h);
            Vec3d vec3d2 = abstractClientPlayerEntity.getVelocity();
            double d = vec3d2.horizontalLengthSquared();
            double e = vec3d.horizontalLengthSquared();
            if (d > 0.0 && e > 0.0) {
                double n = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                double o = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) (Math.signum(o) * Math.acos(n))));
            }
        } else if (j > 0.0F) {
            setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
            l = abstractClientPlayerEntity.isTouchingWater() ? -90.0F - k : -90.0F;
            m = MathHelper.lerp(j, 0.0F, l);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
            if (abstractClientPlayerEntity.isInSwimmingPose()) {
                matrixStack.translate(0.0F, -1.0F, 0.3F);
            }
        } else {
            setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
        }
    }

    private void setupTransforms(PlayerEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
        if (!entity.isInPose(EntityPose.SLEEPING)) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - bodyYaw));
        }

        if (entity.deathTime > 0) {
            float f = ((float) entity.deathTime + tickDelta - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }

            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 90.0F));
        } else if (entity.isUsingRiptide()) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F - entity.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(((float) entity.age + tickDelta) * -75.0F));
        } else if (entity.isInPose(EntityPose.SLEEPING)) {
            Direction direction = entity.getSleepingDirection();
            float g = direction != null ? getYaw(direction) : bodyYaw;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270.0F));
        }
    }

    private static float getYaw(Direction direction) {
        return switch (direction) {
            case NORTH -> 270.0f;
            case SOUTH -> 90.0f;
            case EAST -> 180.0f;
            default -> 0.0f;
        };
    }

    @EventHandler
    public void onRenderHands(EventHeldItemRenderer e) {
        if (handItems.getValue())
            RenderSystem.setShaderColor(handItemsColor.getValue().getRed() / 255f, handItemsColor.getValue().getGreen() / 255f, handItemsColor.getValue().getBlue() / 255f, handItemsColor.getValue().getAlpha() / 255f);
    }
}