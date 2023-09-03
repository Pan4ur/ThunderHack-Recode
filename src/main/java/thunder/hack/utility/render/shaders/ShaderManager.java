package thunder.hack.utility.render.shaders;

import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.util.Identifier;
import thunder.hack.utility.interfaces.IShaderEffect;

import static thunder.hack.modules.Module.mc;

public class ShaderManager {
    public static ManagedShaderEffect OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/outline.json"), managedShaderEffect -> {
        ((IShaderEffect) managedShaderEffect.getShaderEffect()).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
        ((IShaderEffect) managedShaderEffect.getShaderEffect()).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
    });

    public static ManagedShaderEffect SMOKE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/smoke.json"), managedShaderEffect -> {
        ((IShaderEffect) managedShaderEffect.getShaderEffect()).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
        ((IShaderEffect) managedShaderEffect.getShaderEffect()).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
    });

    public static ManagedShaderEffect GRADIENT = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/gradient.json"), managedShaderEffect -> {
        ((IShaderEffect) managedShaderEffect.getShaderEffect()).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
        ((IShaderEffect) managedShaderEffect.getShaderEffect()).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
    });

    public static boolean fullNullCheck(){
        if(GRADIENT == null || SMOKE == null || OUTLINE == null) return true;
        return  false;
    }

    public static void reload(){
        OUTLINE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/outline.json"), managedShaderEffect -> {
            ((IShaderEffect) managedShaderEffect.getShaderEffect()).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) managedShaderEffect.getShaderEffect()).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        SMOKE = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/smoke.json"), managedShaderEffect -> {
            ((IShaderEffect) managedShaderEffect.getShaderEffect()).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) managedShaderEffect.getShaderEffect()).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });

        GRADIENT = ShaderEffectManager.getInstance().manage(new Identifier("minecraft", "shaders/post/gradient.json"), managedShaderEffect -> {
            ((IShaderEffect) managedShaderEffect.getShaderEffect()).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect) managedShaderEffect.getShaderEffect()).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
        });
    }
}
