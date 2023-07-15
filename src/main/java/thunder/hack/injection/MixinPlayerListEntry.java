package thunder.hack.injection;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.Thunderhack;
import thunder.hack.modules.client.OptifineCapes;
import thunder.hack.utility.OFCapesUtil;

import java.util.Map;

@Mixin(PlayerListEntry.class)
public class MixinPlayerListEntry {
    @Shadow
    @Final
    private GameProfile profile;
    @Shadow
    @Final
    private Map<MinecraftProfileTexture.Type, Identifier> textures;
    private boolean loadedCapeTexture = false;

    @Inject(method = "getCapeTexture", at = @At("HEAD"))
    private void getCapeTextureHook(CallbackInfoReturnable<Identifier> cir) {
        if(Thunderhack.moduleManager.get(OptifineCapes.class).isEnabled())
            getTexture();
    }

    @Inject(method = "getElytraTexture", at = @At("HEAD"))
    private void getElytraTextureHook(CallbackInfoReturnable<Identifier> cir) {
        if(Thunderhack.moduleManager.get(OptifineCapes.class).isEnabled())
            getTexture();
    }

    private void getTexture() {
        if (loadedCapeTexture) return;
        loadedCapeTexture = true;
        Map<MinecraftProfileTexture.Type, Identifier> textures = this.textures;
        OFCapesUtil.loadPlayerCape(this.profile, id -> {
            textures.put(MinecraftProfileTexture.Type.CAPE, id);
        });
    }
}
