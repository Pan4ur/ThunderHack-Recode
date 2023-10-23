package thunder.hack.cmd.impl;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.NotNull;
import thunder.hack.cmd.Command;
import thunder.hack.injection.accesors.IMinecraftClient;

import java.util.Optional;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class LoginCommand extends Command {
    public LoginCommand() {
        super("login");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("name", StringArgumentType.word()).executes(context -> {
            login(context.getArgument("name", String.class));
            sendMessage("Аккаунт изменен на: " + mc.getSession().getUsername());

            return SINGLE_SUCCESS;
        }));

        builder.executes(context -> {
            sendMessage("Использование: .login <nickname>");

            return SINGLE_SUCCESS;
        });
    }

    public void login(String name) {
        try {
            setSession(new Session(name, Uuids.getOfflinePlayerUuid(name), "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));
        } catch (Exception exception) {
            sendMessage("Неверное имя! " + exception);
        }
    }


    public void setSession(Session session) {
        IMinecraftClient mca = (IMinecraftClient) mc;
        mca.setSessionT(session);
        mc.getGameProfile().getProperties().clear();
        UserApiService apiService;
        apiService = UserApiService.OFFLINE;
        mca.setUserApiService(apiService);
        mca.setSocialInteractionsManagerT(new SocialInteractionsManager(mc, apiService));
        mca.setProfileKeys(ProfileKeys.create(apiService, session, mc.runDirectory.toPath()));
        mca.setAbuseReportContextT(AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), apiService));
    }
}
