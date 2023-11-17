package thunder.hack.core.impl;

import thunder.hack.core.IManager;

import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class WayPointManager implements IManager {
    private static CopyOnWriteArrayList<WayPoint> wayPoints = new CopyOnWriteArrayList<>();

    public void addWayPoint(WayPoint macro) {
        if (!wayPoints.contains(macro)) {
            wayPoints.add(macro);
        }
    }

    public void onLoad() {
        wayPoints = new CopyOnWriteArrayList<>();
        try {
            File file = new File(ConfigManager.CONFIG_FOLDER_NAME + "/misc/waypoints.txt");

            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    while (reader.ready()) {
                        String[] line = reader.readLine().split(":");
                        String x = line[0];
                        String y = line[1];
                        String z = line[2];
                        String name = line[3];
                        String server = line[4];

                        addWayPoint(new WayPoint(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z), name, server));
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveWayPoints() {
        File file = new File(ConfigManager.CONFIG_FOLDER_NAME + "/misc/waypoints.txt");
        try {
            new File(ConfigManager.CONFIG_FOLDER_NAME).mkdirs();
            file.createNewFile();
        } catch (Exception ignored) {
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (WayPoint wayPoint : wayPoints) {
                writer.write(wayPoint.x + ":" + wayPoint.y + ":" + wayPoint.z + ":" + wayPoint.name + ":" + wayPoint.server + "\n");
            }
        } catch (Exception ignored) {
        }
    }

    public void removeWayPoint(WayPoint macro) {
        wayPoints.remove(macro);
    }

    public CopyOnWriteArrayList<WayPoint> getWayPoints() {
        return wayPoints;
    }

    public WayPoint getWayPointByName(String name) {
        for (WayPoint wayPoint : getWayPoints()) {
            if (wayPoint.name.equalsIgnoreCase(name)) {
                return wayPoint;
            }
        }
        return null;
    }

    public record WayPoint(int x, int y, int z, String name, String server) {
    }
}
