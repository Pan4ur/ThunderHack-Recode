package thunder.hack.injection;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.resource.ResourceFactory;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.modules.combat.Aura;
import thunder.hack.modules.player.NoEntityTrace;
import thunder.hack.modules.render.NoRender;
import thunder.hack.utility.math.FrameRateCounter;
import thunder.hack.utility.render.MSAAFramebuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gl.ShaderStage;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.BlockAnimationUtility;
import thunder.hack.utility.render.shaders.GlProgram;

import java.util.List;
import java.util.function.Consumer;

import static thunder.hack.modules.Module.mc;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Shadow
    public abstract void render(float tickDelta, long startTime, boolean tick);

    @Shadow
    private float zoom;

    @Shadow
    private float zoomX;

    @Shadow
    private float zoomY;

    @Shadow
    private float viewDistance;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", shift = At.Shift.BEFORE), method = "render")
    void postHudRenderHook(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        FrameRateCounter.INSTANCE.recordFrame();
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    void render3dHook(float tickDelta, long limitTime, @NotNull MatrixStack matrix, CallbackInfo ci) {
        Render3DEngine.lastProjMat.set(RenderSystem.getProjectionMatrix());
        Render3DEngine.lastModMat.set(RenderSystem.getModelViewMatrix());
        Render3DEngine.lastWorldSpaceMatrix.set(matrix.peek().getPositionMatrix());
        ThunderHack.moduleManager.onPreRender3D(matrix);
        MSAAFramebuffer.use(false, () -> {
            ThunderHack.moduleManager.onRender3D(matrix);
            BlockAnimationUtility.onRender(matrix);
            Render3DEngine.onRender3D(matrix); // <- не двигать
        });
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V", shift = At.Shift.AFTER))
    public void postRender3dHook(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        ThunderHack.shaderManager.renderShaders();
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float applyCameraTransformationsMathHelperLerpProxy(float delta, float first, float second) {
        if (ModuleManager.noRender.isEnabled() && NoRender.nausea.getValue()) return 0;
        return MathHelper.lerp(delta, first, second);
    }

    @Inject(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"), cancellable = true)
    private void onUpdateTargetedEntity(float tickDelta, CallbackInfo info) {
        if (ModuleManager.noEntityTrace.isEnabled() && (mc.player.getMainHandStack().getItem() instanceof PickaxeItem || !NoEntityTrace.ponly.getValue()) && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            if (mc.cameraEntity instanceof EndCrystalEntity && NoEntityTrace.ignoreCrystals.getValue()) return;
            if (mc.player.getMainHandStack().getItem() instanceof SwordItem && NoEntityTrace.noSword.getValue()) return;
            mc.getProfiler().pop();
            info.cancel();
        }
        if (ModuleManager.aura.isEnabled() && Aura.target != null && mc.player.distanceTo(Aura.target) <= ModuleManager.aura.attackRange.getValue() && ModuleManager.aura.rotationMode.getValue() != Aura.Rotation.None) {
            mc.getProfiler().pop();
            info.cancel();
            //add vector from aura
            mc.crosshairTarget = new EntityHitResult(Aura.target);
        }

        if(ModuleManager.freeCam.isEnabled())
            mc.crosshairTarget = ThunderHack.playerManager.getRtxTarget(ModuleManager.freeCam.getFakeYaw(), ModuleManager.freeCam.getFakePitch(), ModuleManager.freeCam.getFakeX(), ModuleManager.freeCam.getFakeY(), ModuleManager.freeCam.getFakeZ());
    }

    @Inject(method = "loadPrograms", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    void loadAllTheShaders(ResourceFactory factory, CallbackInfo ci, List<ShaderStage> stages, List<Pair<ShaderProgram, Consumer<ShaderProgram>>> shadersToLoad) {
        GlProgram.forEachProgram(loader -> shadersToLoad.add(new Pair<>(loader.getLeft().apply(factory), loader.getRight())));
    }

    @Inject(method = "getBasicProjectionMatrix",at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrixHook(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        if(ModuleManager.aspectRatio.isEnabled()) {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            if (zoom != 1.0f) {
                matrixStack.translate(zoomX, -zoomY, 0.0f);
                matrixStack.scale(zoom, zoom, 1.0f);
            }
            matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective((float)(fov * 0.01745329238474369), ModuleManager.aspectRatio.ratio.getValue(), 0.05f, viewDistance * 4.0f));
            cir.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }

    @Inject(method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D", at = @At("TAIL"), cancellable = true)
    public void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cb) {
        if (ModuleManager.fov.isEnabled()) {
            if (cb.getReturnValue() == 70
                    && !ModuleManager.fov.itemFov.getValue()
                    && mc.options.getPerspective() != Perspective.FIRST_PERSON) return;
            else if (ModuleManager.fov.itemFov.getValue() && cb.getReturnValue() == 70) {
                cb.setReturnValue(ModuleManager.fov.itemFovModifier.getValue().doubleValue());
                return;
            }

            if (mc.player.isSubmergedInWater()) return;
            cb.setReturnValue(ModuleManager.fov.fovModifier.getValue().doubleValue());
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void bobViewHook(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if(MainSettings.customBob.getValue()) {
            ThunderHack.core.bobView(matrices, tickDelta);
            ci.cancel();
        }
    }
}
