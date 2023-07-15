package thunder.hack.injection.accesors;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.report.AbuseReportContext;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.Session;
import net.minecraft.network.encryption.SignatureVerifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface IMinecraftClient {

    @Accessor("itemUseCooldown")
    int getUseCooldown();

    @Accessor("itemUseCooldown")
    void setUseCooldown(int val);

    @Invoker("doItemUse")
    void idoItemUse();

    @Mutable
    @Accessor("profileKeys")
    void setProfileKeys(ProfileKeys keys);


    @Mutable
    @Accessor("session")
    void setSessionT(Session session);

    @Mutable
    @Accessor
    void setUserApiService(UserApiService apiService);

    @Mutable
    @Accessor("socialInteractionsManager")
    void setSocialInteractionsManagerT(SocialInteractionsManager socialInteractionsManager);

    @Mutable
    @Accessor("abuseReportContext")
    void setAbuseReportContextT(AbuseReportContext abuseReportContext);
}
