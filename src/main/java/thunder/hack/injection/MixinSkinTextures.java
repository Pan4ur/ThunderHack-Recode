package thunder.hack.injection;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.ModuleManager;
import thunder.hack.modules.client.Media;
import thunder.hack.utility.OptifineCapes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

@Mixin(SkinTextures.class)
public class MixinSkinTextures {
    private static final Identifier SUN_SKIN = new Identifier("textures/sunskin.png");

    @Final
    private GameProfile profile;
    @Final
    private Map<MinecraftProfileTexture.Type, Identifier> textures;

    private boolean loadedCapeTexture = false;

    @Inject(method = "capeTexture", at = @At("HEAD"))
    private void getCapeTextureHook(CallbackInfoReturnable<Identifier> cir) {
        if(ModuleManager.optifineCapes.isEnabled())
            getTexture();
    }

    @Inject(method = "elytraTexture", at = @At("HEAD"))
    private void getElytraTextureHook(CallbackInfoReturnable<Identifier> cir) {
        if(ModuleManager.optifineCapes.isEnabled())
            getTexture();
    }
    @Inject(method = "texture", at = @At("HEAD"), cancellable = true)
    public void getSkinTextureHook(CallbackInfoReturnable<Identifier> cir) {
        if(ModuleManager.media.isEnabled() && Media.skinProtect.getValue()){
            cir.setReturnValue(SUN_SKIN);
        }
    }

    @Unique
    private void getTexture() {
        if (loadedCapeTexture) return;
        loadedCapeTexture = true;

        Util.getMainWorkerExecutor().execute(() -> {
            try {
                URL capesList = new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/capes/capeBase.txt");
                BufferedReader in = new BufferedReader(new InputStreamReader(capesList.openStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String colune = inputLine.trim();
                    String name = colune.split(":")[0];
                    String cape = colune.split(":")[1];
                    if(Objects.equals(this.profile.getName(), name)) {
                        textures.put(MinecraftProfileTexture.Type.CAPE, new Identifier("textures/" + cape + ".png"));
                        return;
                    }
                }
            } catch (Exception ignored) {
            }
            Map<MinecraftProfileTexture.Type, Identifier> textures = this.textures;
            OptifineCapes.loadPlayerCape(this.profile, id -> {
                textures.put(MinecraftProfileTexture.Type.CAPE, id);
            });
        });
    }
}
