package thunder.hack.cmd.impl;

import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.core.WayPointManager;
import thunder.hack.modules.client.MainSettings;

public class WayPointCommand extends Command {


    public WayPointCommand() {
        super("waypoint");
    }

    @Override
    public void execute(String[] args) {
        if (args[0] == null) {
            Command.sendMessage(usage());
        }
        if (args[0].equals("list")) {
            if(MainSettings.language.getValue() == MainSettings.Language.RU) {
                sendMessage("Метки:");
            } else {
                sendMessage("WayPoints:");
            }
            sendMessage(" ");
            Thunderhack.wayPointManager.getWayPoints().forEach(wp -> sendMessage(wp.name() + "X: " + wp.x() + " Y: " + wp.y() + " Z: " + wp.z()));
        }
        if (args[0].equals("remove")) {
            if (Thunderhack.wayPointManager.getWayPointByName(args[1]) != null) {
                WayPointManager.WayPoint wp = Thunderhack.wayPointManager.getWayPointByName(args[1]);
                Thunderhack.wayPointManager.removeWayPoint(wp);

                if(MainSettings.language.getValue() == MainSettings.Language.RU) {
                    sendMessage("Удалена метка " + wp.name());
                } else {
                    sendMessage("Removed waypoint " + wp.name());
                }

            } else {
                if(MainSettings.language.getValue() == MainSettings.Language.RU) {
                    sendMessage("Не существует метки с именем " + args[1]);
                } else {
                    sendMessage("Waypoint with name " + args[1] + " is not exists");
                }
            }
        }
        if (args.length >= 4) {
            if (args[0].equals("add")) {
                String x = args[1];
                String y = args[2];
                String z = args[3];
                String name = args[4];

                WayPointManager.WayPoint wp = new WayPointManager.WayPoint (Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z), name);
                Thunderhack.wayPointManager.addWayPoint(wp);

                if(MainSettings.language.getValue() == MainSettings.Language.RU) {
                    sendMessage("Добавлена метка " + name + " с координатами x: " + x + " y: " + y + " z: " + z);
                } else {
                    sendMessage("Added waypoint " + name + " with coords x: " + x + " y: " + y + " z: " + z);
                }
            } else {
                sendMessage(usage());
            }
        } else if(args.length == 3){
            if (args[0].equals("add")) {
                String name = args[1];
                WayPointManager.WayPoint wp = new WayPointManager.WayPoint((int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ(), name);
                Thunderhack.wayPointManager.addWayPoint(wp);
                if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                    sendMessage("Добавлена метка " + name + " с координатами x: " + ((int) mc.player.getX()) + " y: " + ((int) mc.player.getY()) + " z: " + ((int) mc.player.getZ()));
                } else {
                    sendMessage("Added waypoint " + name + " with coords x: " + ((int) mc.player.getX()) + " y: " + ((int) mc.player.getY()) + " z: " + ((int) mc.player.getZ()));
                }
            } else {
                sendMessage(usage());
            }
        } else {
            sendMessage(usage());
        }
    }

    String usage() {
        return "waypoint add/remove/list (waypoint add x y z name), (waypoint remove name)";
    }
}
