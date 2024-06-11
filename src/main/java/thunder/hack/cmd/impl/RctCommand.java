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
            if(!mc.player.networkHandler.getServerInfo().address.equals("mc.funtime.su") && !mc.player.networkHandler.getServerInfo().address.equals("spookytime.net")){
                sendMessage(isRu() ? "Rct работает только на фанике и спуки" : "Rct works only on funtime and spookytime");
                return SINGLE_SUCCESS;
            }
            asyncManager.run(() -> {
                mc.player.networkHandler.sendCommand("hub");
                try {Thread.sleep(1500);} catch (InterruptedException e) {throw new RuntimeException(e);}
                mc.player.networkHandler.sendCommand("an" + ((ScoreboardObjective) mc.player.getScoreboard().getObjectives().toArray()[0]).getDisplayName().getString().substring(10));
            });
            return SINGLE_SUCCESS;
        });
    }

}
