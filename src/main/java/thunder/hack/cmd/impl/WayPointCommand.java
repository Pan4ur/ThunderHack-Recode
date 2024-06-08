package thunder.hack.cmd.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.WayPointArgumentType;
import thunder.hack.core.impl.WayPointManager;
import thunder.hack.modules.client.ClientSettings;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.modules.Module.mc;

public class WayPointCommand extends Command {
    public WayPointCommand() {
        super("waypoint", "waypoints");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("list").executes(context -> {
            if (ClientSettings.language.getValue() == ClientSettings.Language.RU) sendMessage("Метки:");
            else sendMessage("WayPoints:");

            sendMessage(" ");
            ThunderHack.wayPointManager.getWayPoints().forEach(wp -> sendMessage(wp.getName() + " X: " + wp.getX() + " Y: " + wp.getY() + " Z: " + wp.getZ() + " Server: " + wp.getServer() + " Dimension: " + wp.getDimension()));

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("remove").then(arg("name", WayPointArgumentType.create()).executes(context -> {
            WayPointManager.WayPoint wp = context.getArgument("name", WayPointManager.WayPoint.class);

            ThunderHack.wayPointManager.removeWayPoint(wp);

            if (ClientSettings.language.getValue() == ClientSettings.Language.RU) {
                sendMessage("Удалена метка " + wp.getName());
            } else {
                sendMessage("Removed waypoint " + wp.getName());
            }

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("add").then(arg("name", StringArgumentType.word()).executes(context -> {
            String name = context.getArgument("name", String.class);
            WayPointManager.WayPoint wp = new WayPointManager.WayPoint((int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ(), name, (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address), mc.world.getRegistryKey().getValue().getPath());
            ThunderHack.wayPointManager.addWayPoint(wp);

            if (ClientSettings.language.getValue() == ClientSettings.Language.RU) {
                sendMessage("Добавлена метка " + name + " с координатами x: " + ((int) mc.player.getX()) + " y: " + ((int) mc.player.getY()) + " z: " + ((int) mc.player.getZ()));
            } else {
                sendMessage("Added waypoint " + name + " with coords x: " + ((int) mc.player.getX()) + " y: " + ((int) mc.player.getY()) + " z: " + ((int) mc.player.getZ()));
            }

            return SINGLE_SUCCESS;
        }).then(arg("x", IntegerArgumentType.integer())
                .then(arg("y", IntegerArgumentType.integer())
                        .then(arg("z", IntegerArgumentType.integer()).executes(context -> {
                            String name = context.getArgument("name", String.class);
                            BlockPos pos = new BlockPos(context.getArgument("x", Integer.class), context.getArgument("y", Integer.class), context.getArgument("z", Integer.class));

                            WayPointManager.WayPoint wp = new WayPointManager.WayPoint(pos.getX(), pos.getY(), pos.getZ(), name, (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address), mc.world.getRegistryKey().getValue().getPath());
                            ThunderHack.wayPointManager.addWayPoint(wp);

                            if (ClientSettings.language.getValue() == ClientSettings.Language.RU) {
                                sendMessage("Добавлена метка " + name + " с координатами x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ());
                            } else {
                                sendMessage("Added waypoint " + name + " with coords x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ());
                            }

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
