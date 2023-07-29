package thunder.hack.utility.render.shaders;

import thunder.hack.utility.interfaces.IShaderEffect;

import static thunder.hack.modules.Module.mc;

public class ShaderManager {
    public static final OutlineProgram OUTLINE = OutlineProgram.create(managedShaderEffect -> {
        if(managedShaderEffect.getShader() != null) {
            ((IShaderEffect) managedShaderEffect.getShader()).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) managedShaderEffect.getShader()).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        }
    });

    public static final SmokeProgram SMOKE = SmokeProgram.create(managedShaderEffect -> {
        if(managedShaderEffect.getShader() != null) {
            ((IShaderEffect) managedShaderEffect.getShader()).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) managedShaderEffect.getShader()).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        }
    });

    public static final GradientProgram GRADIENT = GradientProgram.create(managedShaderEffect -> {
        if(managedShaderEffect.getShader() != null) {
            ((IShaderEffect) managedShaderEffect.getShader()).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) managedShaderEffect.getShader()).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        }
    });


    public static boolean fullNullCheck(){
        if(GRADIENT == null || SMOKE == null || OUTLINE == null) return true;
        return  false;
    }
}
