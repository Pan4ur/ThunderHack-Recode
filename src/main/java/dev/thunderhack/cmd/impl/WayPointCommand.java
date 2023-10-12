package dev.thunderhack.cmd.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.client.MainSettings;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import dev.thunderhack.cmd.Command;
import dev.thunderhack.cmd.args.WayPointArgumentType;
import dev.thunderhack.core.WayPointManager;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class WayPointCommand extends Command {
    public WayPointCommand() {
        super("waypoint", "waypoints");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("list").executes(context -> {
            if (MainSettings.language.getValue() == MainSettings.Language.RU) sendMessage("Метки:");
            else sendMessage("WayPoints:");

            sendMessage(" ");
            ThunderHack.wayPointManager.getWayPoints().forEach(wp -> sendMessage(wp.name() + "X: " + wp.x() + " Y: " + wp.y() + " Z: " + wp.z() + " Server: " + wp.server()));

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("remove").then(arg("name", WayPointArgumentType.create()).executes(context -> {
            WayPointManager.WayPoint wp = context.getArgument("name", WayPointManager.WayPoint.class);

            ThunderHack.wayPointManager.removeWayPoint(wp);

            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                sendMessage("Удалена метка " + wp.name());
            } else {
                sendMessage("Removed waypoint " + wp.name());
            }

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("add").then(arg("name", StringArgumentType.word()).executes(context -> {
            String name = context.getArgument("name", String.class);
            WayPointManager.WayPoint wp = new WayPointManager.WayPoint((int) MC.player.getX(), (int) MC.player.getY(), (int) MC.player.getZ(), name, (Module.mc.isInSingleplayer() ? "SinglePlayer" : Module.mc.getNetworkHandler().getServerInfo().address));
            ThunderHack.wayPointManager.addWayPoint(wp);

            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                sendMessage("Добавлена метка " + name + " с координатами x: " + ((int) MC.player.getX()) + " y: " + ((int) MC.player.getY()) + " z: " + ((int) MC.player.getZ()));
            } else {
                sendMessage("Added waypoint " + name + " with coords x: " + ((int) MC.player.getX()) + " y: " + ((int) MC.player.getY()) + " z: " + ((int) MC.player.getZ()));
            }

            return SINGLE_SUCCESS;
        }).then(arg("x", IntegerArgumentType.integer())
                .then(arg("y", IntegerArgumentType.integer())
                        .then(arg("z", IntegerArgumentType.integer()).executes(context -> {
                            String name = context.getArgument("name", String.class);
                            BlockPos pos = new BlockPos(context.getArgument("x", Integer.class), context.getArgument("y", Integer.class), context.getArgument("z", Integer.class));

                            WayPointManager.WayPoint wp = new WayPointManager.WayPoint(pos.getX(), pos.getY(), pos.getZ(), name, (Module.mc.isInSingleplayer() ? "SinglePlayer" : Module.mc.getNetworkHandler().getServerInfo().address));
                            ThunderHack.wayPointManager.addWayPoint(wp);

                            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                                sendMessage("Добавлена метка " + name + " с координатами x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ());
                            } else {
                                sendMessage("Added waypoint " + name + " with coords x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ());
                            }

                            return SINGLE_SUCCESS;
                        })))))); // macro remove. // eclip bedrock // waypoints

        builder.executes(context -> {
            sendMessage(usage());

            return SINGLE_SUCCESS;
        });
    }

    String usage() {
        return "waypoint add/remove/list (waypoint add x y z name), (waypoint remove name)";
    }
}
