package thunder.hack.utility.render.shaders;

import thunder.hack.utility.interfaces.IShaderEffect;

import static thunder.hack.utility.Util.mc;

public class ShaderManager {
    public static final OutlineProgram OUTLINE = OutlineProgram.create(managedShaderEffect -> {
        ((IShaderEffect) managedShaderEffect.getShader()).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
        ((IShaderEffect) managedShaderEffect.getShader()).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
    });

    public static final SmokeProgram SMOKE = SmokeProgram.create(managedShaderEffect -> {
        ((IShaderEffect) managedShaderEffect.getShader()).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
        ((IShaderEffect) managedShaderEffect.getShader()).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
    });

    public static final GradientProgram GRADIENT = GradientProgram.create(managedShaderEffect -> {
        ((IShaderEffect) managedShaderEffect.getShader()).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
        ((IShaderEffect) managedShaderEffect.getShader()).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
    });
}
