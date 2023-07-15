package thunder.hack.cmd.impl;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.UserApiService;
import net.fabricmc.fabric.mixin.networking.client.accessor.MinecraftClientAccessor;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.report.AbuseReportContext;
import net.minecraft.client.report.ReporterEnvironment;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.Session;
import net.minecraft.util.Uuids;
import thunder.hack.cmd.Command;
import thunder.hack.injection.accesors.IMinecraftClient;

import java.util.Optional;

public class LoginCommand extends Command {

    public LoginCommand() {
        super("login");
    }

    public void login(String name) {
        try {
            setSession(new Session(name, Uuids.getOfflinePlayerUuid(name).toString(), "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));
        } catch (Exception exception) {
            Command.sendMessage("Неверное имя! " + exception);
        }
    }


    public void setSession(Session session) {
        IMinecraftClient mca = (IMinecraftClient) mc;
        mca.setSessionT(session);
        mc.getSessionProperties().clear();
        UserApiService apiService;
        apiService = UserApiService.OFFLINE;
        mca.setUserApiService(apiService);
        mca.setSocialInteractionsManagerT(new SocialInteractionsManager(mc, apiService));
        mca.setProfileKeys(ProfileKeys.create(apiService, session, mc.runDirectory.toPath()));
        mca.setAbuseReportContextT(AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), apiService));
    }

    @Override
    public void execute(String[] var1) {
        try {
            login(var1[0]);
            Command.sendMessage("Аккаунт изменен на: " + mc.getSession().getUsername());
        } catch (Exception exception) {
            Command.sendMessage("Использование: .login nick");
        }
    }
}
