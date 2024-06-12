package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.ScoreboardObjective;
import thunder.hack.cmd.Command;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import static thunder.hack.ThunderHack.asyncManager;
import static thunder.hack.modules.client.ClientSettings.isRu;

public class RctCommand extends Command {
    public RctCommand(){
        super("rct");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            String[] allowedServers = {"funtime", "spookytime"};
            String serverAddress = mc.player.networkHandler.getServerInfo().address.toLowerCase();
            for (String allowedServer : allowedServers) {
                if (serverAddress.contains(allowedServer)) {
                    rct()
                    return SINGLE_SUCCESS;
                }
            }
            sendMessage(isRu()? "Rct работает только на фанике и спуки" : "Rct works only on funtime and spookytime");
            return SINGLE_SUCCESS;
        });

        builder.then(literal("pohuy").executes(context -> {
            rct()
            return SINGLE_SUCCESS;
        }));
    }

    private void rct() {
        mc.player.networkHandler.sendCommand("hub");
        asyncManager.run(() -> {
            ScoreboardObjective objective = mc.player.getScoreboard().getObjectives().stream().findFirst().orElse(null);
            if (objective != null) {
                mc.player.networkHandler.sendCommand("an" + objective.getDisplayName().getString().substring(10));
            }
        }, 1500);
    }
}
