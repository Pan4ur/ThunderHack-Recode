package dev.thunderhack.cmd.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.cmd.Command;
import dev.thunderhack.cmd.args.WayPointArgumentType;
import dev.thunderhack.core.WayPointManager;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static dev.thunderhack.modules.client.MainSettings.isRu;

public class WayPointCommand extends Command {
    public WayPointCommand() {
        super("waypoint", "waypoints");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("list").executes(context -> {
            sendMessage(isRu() ? "Метки:" : "WayPoints:");
            sendMessage(" ");

            ThunderHack.wayPointManager.getWayPoints().forEach(wp -> sendMessage(wp.name() + "X: " + wp.x() + " Y: " + wp.y() + " Z: " + wp.z() + " Server: " + wp.server()));

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("remove").then(arg("name", WayPointArgumentType.create()).executes(context -> {
            WayPointManager.WayPoint wp = context.getArgument("name", WayPointManager.WayPoint.class);

            ThunderHack.wayPointManager.removeWayPoint(wp);

            sendMessage((isRu() ? "Удалена метка " : "Removed waypoint ") + wp.name());

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("add").then(arg("name", StringArgumentType.word()).executes(context -> {
            String name = context.getArgument("name", String.class);
            WayPointManager.WayPoint wp = new WayPointManager.WayPoint((int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ(), name, (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address));
            ThunderHack.wayPointManager.addWayPoint(wp);

            if (isRu())
                sendMessage("Добавлена метка " + name + " с координатами x: " + ((int) mc.player.getX()) + " y: " + ((int) mc.player.getY()) + " z: " + ((int) mc.player.getZ()));
            else
                sendMessage("Added waypoint " + name + " with coords x: " + ((int) mc.player.getX()) + " y: " + ((int) mc.player.getY()) + " z: " + ((int) mc.player.getZ()));

            return SINGLE_SUCCESS;
        }).then(arg("x", IntegerArgumentType.integer())
                .then(arg("y", IntegerArgumentType.integer())
                        .then(arg("z", IntegerArgumentType.integer()).executes(context -> {
                            String name = context.getArgument("name", String.class);
                            BlockPos pos = new BlockPos(context.getArgument("x", Integer.class), context.getArgument("y", Integer.class), context.getArgument("z", Integer.class));

                            WayPointManager.WayPoint wp = new WayPointManager.WayPoint(pos.getX(), pos.getY(), pos.getZ(), name, (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address));
                            ThunderHack.wayPointManager.addWayPoint(wp);

                            if (isRu())
                                sendMessage("Добавлена метка " + name + " с координатами x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ());
                            else
                                sendMessage("Added waypoint " + name + " with coords x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ());

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
