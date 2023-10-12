package dev.thunderhack.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.item.SwordItem;
import net.minecraft.resource.ResourceFactory;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.modules.combat.Aura;
import dev.thunderhack.modules.player.NoEntityTrace;
import dev.thunderhack.modules.render.NoRender;
import dev.thunderhack.utils.math.FrameRateCounter;
import dev.thunderhack.utils.render.MSAAFramebuffer;
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
import dev.thunderhack.utils.render.Render3DEngine;
import dev.thunderhack.utils.render.BlockAnimationUtility;
import dev.thunderhack.utils.render.shaders.GlProgram;

import java.util.List;
import java.util.function.Consumer;

import static dev.thunderhack.modules.Module.mc;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Shadow
    public abstract void render(float tickDelta, long startTime, boolean tick);

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
        MSAAFramebuffer.use(() -> {
            ThunderHack.moduleManager.onRender3D(matrix);
            BlockAnimationUtility.onRender(matrix);
            Render3DEngine.onRender3D(matrix); // <- не двигать
        });
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V", shift = At.Shift.AFTER))
    public void postRender3dHook(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        ThunderHack.moduleManager.onPostRender3D(matrix);
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurtHook(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (ModuleManager.noRender.isEnabled() && NoRender.hurtCam.getValue()) {
            ci.cancel();
        }
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float applyCameraTransformationsMathHelperLerpProxy(float delta, float first, float second) {
        if (ModuleManager.noRender.isEnabled() && NoRender.nausea.getValue()) return 0;
        return MathHelper.lerp(delta, first, second);
    }

    @Inject(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"), cancellable = true)
    private void onUpdateTargetedEntity(float tickDelta, CallbackInfo info) {
        if (ModuleManager.noEntityTrace.isEnabled() && (mc.player.getMainHandStack().getItem() instanceof PickaxeItem || !NoEntityTrace.ponly.getValue()) && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            if (mc.player.getMainHandStack().getItem() instanceof SwordItem && NoEntityTrace.noSword.getValue()) return;
            mc.getProfiler().pop();
            info.cancel();
        }
        if (ModuleManager.aura.isEnabled() && Aura.target != null && mc.player.distanceTo(Aura.target) <= Aura.attackRange.getValue() && Aura.mode.getValue() != Aura.Mode.None) {
            mc.getProfiler().pop();
            info.cancel();
            // TODO vector from aura
            mc.crosshairTarget = new EntityHitResult(Aura.target);
        }
    }

    @Inject(method = "loadPrograms", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    void loadAllTheShaders(ResourceFactory factory, CallbackInfo ci, List<ShaderStage> stages, List<Pair<ShaderProgram, Consumer<ShaderProgram>>> shadersToLoad) {
        GlProgram.forEachProgram(loader -> shadersToLoad.add(new Pair<>(loader.getLeft().apply(factory), loader.getRight())));
    }

    @Inject(at = @At("TAIL"), method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D", cancellable = true)
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
}
