package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import baritone.api.BaritoneAPI;
import thunder.hack.cmd.args.WayPointArgumentType;
import thunder.hack.core.impl.WayPointManager;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.modules.client.ClientSettings.isRu;

public class GotoWaypointCommand extends Command {
    public GotoWaypointCommand(){
        super("goto");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("name", WayPointArgumentType.create()).executes(context -> {
            if(!ThunderHack.baritone) {
                sendMessage(isRu() ? "Баритон не найден (можешь скачать на https://meteorclient.com)" : "Baritone not found (you can download it on https://meteorclient.com)");
                return SINGLE_SUCCESS;
            }
            WayPointManager.WayPoint wp = context.getArgument("name", WayPointManager.WayPoint.class);
            if(!mc.world.getRegistryKey().getValue().getPath().equals(wp.getDimension())){
                sendMessage(isRu() ? "Метка в другом измерении" : "Waypoint in another dimension");
                return SINGLE_SUCCESS;
            }
            BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("goto " + wp.getX() + " " + wp.getY() + " " + wp.getZ());
            return SINGLE_SUCCESS;
        }));
    }
}
