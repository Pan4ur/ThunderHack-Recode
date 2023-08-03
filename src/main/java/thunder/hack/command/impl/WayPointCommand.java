package thunder.hack.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.util.math.BlockPos;
import thunder.hack.Thunderhack;
import thunder.hack.command.Command;
import thunder.hack.core.WayPointManager;
import thunder.hack.modules.client.MainSettings;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class WayPointCommand extends Command {
    public WayPointCommand() {
        super("waypoint");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("list").executes(context -> {
            if (MainSettings.language.getValue() == MainSettings.Language.RU) sendMessage("Метки:");
            else sendMessage("WayPoints:");

            sendMessage(" ");
            Thunderhack.wayPointManager.getWayPoints().forEach(wp -> sendMessage(wp.name() + "X: " + wp.x() + " Y: " + wp.y() + " Z: " + wp.z()));

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("remove").then(arg("name", StringArgumentType.word())).executes(context -> {
            String name = context.getArgument("name", String.class);

            WayPointManager.WayPoint wp = Thunderhack.wayPointManager.getWayPointByName(name);
            if (wp != null) {
                Thunderhack.wayPointManager.removeWayPoint(wp);

                if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                    sendMessage("Удалена метка " + wp.name());
                } else {
                    sendMessage("Removed waypoint " + wp.name());
                }
            } else {
                if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                    sendMessage("Не существует метки с именем " + name);
                } else {
                    sendMessage("Waypoint with name " + name + " is not exists");
                }
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("add").then(arg("name", StringArgumentType.word())).executes(context -> {
            String name = context.getArgument("name", String.class);
            WayPointManager.WayPoint wp = new WayPointManager.WayPoint((int) MC.player.getX(), (int) MC.player.getY(), (int) MC.player.getZ(), name);
            Thunderhack.wayPointManager.addWayPoint(wp);

            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                sendMessage("Добавлена метка " + name + " с координатами x: " + ((int) MC.player.getX()) + " y: " + ((int) MC.player.getY()) + " z: " + ((int) MC.player.getZ()));
            } else {
                sendMessage("Added waypoint " + name + " with coords x: " + ((int) MC.player.getX()) + " y: " + ((int) MC.player.getY()) + " z: " + ((int) MC.player.getZ()));
            }

            return SINGLE_SUCCESS;
        }).then(arg("blockPos", BlockPosArgumentType.blockPos())).executes(context -> {
            String name = context.getArgument("name", String.class);
            BlockPos pos = context.getArgument("blockPos", BlockPos.class);

            WayPointManager.WayPoint wp = new WayPointManager.WayPoint(pos.getX(), pos.getY(), pos.getZ(), name);
            Thunderhack.wayPointManager.addWayPoint(wp);

            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                sendMessage("Добавлена метка " + name + " с координатами x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ());
            } else {
                sendMessage("Added waypoint " + name + " with coords x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ());
            }

            return SINGLE_SUCCESS;
        }));

        builder.executes(context -> {
            sendMessage(usage());

            return SINGLE_SUCCESS;
        });
    }

    String usage() {
        return "waypoint add/remove/list (waypoint add x y z name), (waypoint remove name)";
    }
}
