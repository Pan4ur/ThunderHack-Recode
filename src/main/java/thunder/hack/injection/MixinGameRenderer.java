package thunder.hack.injection;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import thunder.hack.core.Managers;
import thunder.hack.utility.render.shaders.satin.impl.ReloadableShaderEffectManager;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.player.NoEntityTrace;
import thunder.hack.utility.math.FrameRateCounter;
import thunder.hack.utility.render.BlockAnimationUtility;
import thunder.hack.utility.render.Render3DEngine;

import static thunder.hack.features.modules.Module.mc;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow
    private float zoom;

    @Shadow
    private float zoomX;

    @Shadow
    private float zoomY;

    @Shadow
    private float viewDistance;

    @Shadow
    public abstract void tick();

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 1, shift = At.Shift.BEFORE), method = "render")
    void postHudRenderHook(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        FrameRateCounter.INSTANCE.recordFrame();
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    void render3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Module.fullNullCheck()) return;

        Camera camera = mc.gameRenderer.getCamera();
        MatrixStack matrixStack = new MatrixStack();
        RenderSystem.getModelViewStack().pushMatrix().mul(matrixStack.peek().getPositionMatrix());
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        RenderSystem.applyModelViewMatrix();

        Render3DEngine.lastProjMat.set(RenderSystem.getProjectionMatrix());
        Render3DEngine.lastModMat.set(RenderSystem.getModelViewMatrix());
        Render3DEngine.lastWorldSpaceMatrix.set(matrixStack.peek().getPositionMatrix());

        Managers.MODULE.onRender3D(matrixStack);
        BlockAnimationUtility.onRender(matrixStack);
        Render3DEngine.onRender3D(matrixStack); // <- не двигать

        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/render/Camera;FLorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    public void postRender3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Module.fullNullCheck()) return;
        Managers.SHADER.renderShaders();
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float renderWorldHook(float delta, float first, float second) {
        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.nausea.getValue()) return 0;
        return MathHelper.lerp(delta, first, second);
    }

    @Inject(method = "loadPrograms", at = @At(value = "RETURN"))
    private void loadSatinPrograms(ResourceFactory factory, CallbackInfo ci) {
        ReloadableShaderEffectManager.INSTANCE.reload(factory);
    }

    @Inject(method = "updateCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;findCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;"), cancellable = true)
    private void onUpdateTargetedEntity(float tickDelta, CallbackInfo info) {
        if (Module.fullNullCheck()) return;

        /*
        if (ModuleManager.aura.isEnabled() && Aura.target != null && mc.player.distanceTo(Aura.target) <= ModuleManager.aura.attackRange.getValue() && ModuleManager.aura.rotationMode.getValue() != Aura.Mode.None) {
            mc.getProfiler().pop();
            info.cancel();
            //add vector from aura
            mc.crosshairTarget = new EntityHitResult(Aura.target);
        }
         */

        if (ModuleManager.freeCam.isEnabled()) {
            mc.getProfiler().pop();
            info.cancel();
            mc.crosshairTarget = Managers.PLAYER.getRtxTarget(ModuleManager.freeCam.getFakeYaw(), ModuleManager.freeCam.getFakePitch(), ModuleManager.freeCam.getFakeX(), ModuleManager.freeCam.getFakeY(), ModuleManager.freeCam.getFakeZ());
        }
    }

    @Inject(method = "findCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void findCrosshairTargetHook(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta, CallbackInfoReturnable<HitResult> cir) {
        if (ModuleManager.noEntityTrace.isEnabled() && (mc.player.getMainHandStack().getItem() instanceof PickaxeItem || !NoEntityTrace.ponly.getValue())) {
            if (mc.player.getMainHandStack().getItem() instanceof SwordItem && NoEntityTrace.noSword.getValue()) return;
            double d = Math.max(blockInteractionRange, entityInteractionRange);
            Vec3d vec3d = camera.getCameraPosVec(tickDelta);
            HitResult hitResult = camera.raycast(d, tickDelta, false);
            cir.setReturnValue(ensureTargetInRangeCustom(hitResult, vec3d, blockInteractionRange));
        }
    }

    @Inject(method = "getBasicProjectionMatrix", at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrixHook(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        if (ModuleManager.aspectRatio.isEnabled()) {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            if (zoom != 1.0f) {
                matrixStack.translate(zoomX, -zoomY, 0.0f);
                matrixStack.scale(zoom, zoom, 1.0f);
            }
            matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective((float) (fov * 0.01745329238474369), ModuleManager.aspectRatio.ratio.getValue(), 0.05f, viewDistance * 4.0f));
            cir.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }

    @Inject(method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D", at = @At("TAIL"), cancellable = true)
    public void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cb) {
        if (ModuleManager.fov.isEnabled()) {
            if (cb.getReturnValue() == 70 && !ModuleManager.fov.itemFov.getValue() && mc.options.getPerspective() != Perspective.FIRST_PERSON)
                return;

            else if (ModuleManager.fov.itemFov.getValue() && cb.getReturnValue() == 70) {
                cb.setReturnValue(ModuleManager.fov.itemFovModifier.getValue().doubleValue());
                return;
            }

            if (mc.player.isSubmergedInWater())
                return;

            cb.setReturnValue(ModuleManager.fov.fovModifier.getValue().doubleValue());
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void bobViewHook(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (Module.fullNullCheck()) return;
        if (ModuleManager.noBob.isEnabled()) {
            ModuleManager.noBob.bobView(matrices, tickDelta);
            ci.cancel();
            return;
        }
        if (ClientSettings.customBob.getValue()) {
            ThunderHack.core.bobView(matrices, tickDelta);
            ci.cancel();
        }
    }

    @Unique
    private HitResult ensureTargetInRangeCustom(HitResult hitResult, Vec3d cameraPos, double interactionRange) {
        Vec3d vec3d = hitResult.getPos();
        if (!vec3d.isInRange(cameraPos, interactionRange)) {
            Vec3d vec3d2 = hitResult.getPos();
            Direction direction = Direction.getFacing(vec3d2.x - cameraPos.x, vec3d2.y - cameraPos.y, vec3d2.z - cameraPos.z);
            return BlockHitResult.createMissed(vec3d2, direction, BlockPos.ofFloored(vec3d2));
        } else {
            return hitResult;
        }
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void showFloatingItemHook(ItemStack floatingItem, CallbackInfo info) {
        if (ModuleManager.totemAnimation.isEnabled()) {
            ModuleManager.totemAnimation.showFloatingItem(floatingItem);
            info.cancel();
        }
    }

    @Inject(method = "renderFloatingItem", at = @At("HEAD"), cancellable = true)
    private void renderFloatingItemHook(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (ModuleManager.totemAnimation.isEnabled()) {
            ModuleManager.totemAnimation.renderFloatingItem(tickDelta);
            ci.cancel();
        }
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurtHook(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.hurtCam.getValue())
            ci.cancel();
    }
}
