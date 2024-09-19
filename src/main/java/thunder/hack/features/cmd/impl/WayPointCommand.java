package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.args.WayPointArgumentType;
import thunder.hack.core.manager.world.WayPointManager;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class WayPointCommand extends Command {
    public WayPointCommand() {
        super("waypoint", "waypoints");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("list").executes(context -> {
            sendMessage(isRu() ? "Метки:" : "Waypoints:");

            sendMessage(" ");
            Managers.WAYPOINT.getWayPoints().forEach(wp -> sendMessage(wp.getName() + " X: " + wp.getX() + " Y: " + wp.getY() + " Z: " + wp.getZ() + " Server: " + wp.getServer() + " Dimension: " + wp.getDimension()));

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("remove").then(arg("name", WayPointArgumentType.create()).executes(context -> {
            WayPointManager.WayPoint wp = context.getArgument("name", WayPointManager.WayPoint.class);
            Managers.WAYPOINT.removeWayPoint(wp);
            sendMessage(isRu() ? "Удалена метка " : "Deleted waypoint " + wp.getName());

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("add").then(arg("name", StringArgumentType.word()).executes(context -> {
            String name = context.getArgument("name", String.class);
            WayPointManager.WayPoint wp = new WayPointManager.WayPoint((int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ(), name, (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address), mc.world.getRegistryKey().getValue().getPath());
            Managers.WAYPOINT.addWayPoint(wp);

            sendMessage((isRu() ? "Добавлена метка " + name + " с координатами" : "Added waypoint " + name + " with coords") + " X: " + ((int) mc.player.getX()) + " Y: " + ((int) mc.player.getY()) + " Z: " + ((int) mc.player.getZ()));

            return SINGLE_SUCCESS;
        }).then(arg("x", IntegerArgumentType.integer())
                .then(arg("y", IntegerArgumentType.integer())
                        .then(arg("z", IntegerArgumentType.integer()).executes(context -> {
                            String name = context.getArgument("name", String.class);
                            BlockPos pos = new BlockPos(context.getArgument("x", Integer.class), context.getArgument("y", Integer.class), context.getArgument("z", Integer.class));

                            WayPointManager.WayPoint wp = new WayPointManager.WayPoint(pos.getX(), pos.getY(), pos.getZ(), name, (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address), mc.world.getRegistryKey().getValue().getPath());
                            Managers.WAYPOINT.addWayPoint(wp);

                            sendMessage((isRu() ? "Добавлена метка " + name + " с координатами X: " : "Added waypoint " + name + " with coords") + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ());

                            return SINGLE_SUCCESS;
                        }))))));

        builder.executes(context -> {
            sendMessage(usage());

            return SINGLE_SUCCESS;
        });
    }

    String usage() {
        return "waypoint add/remove/list (waypoint add x y z name), (waypoint remove name)";
    }
}
