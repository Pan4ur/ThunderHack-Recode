package thunder.hack.core;

import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class WayPointManager {

    private static CopyOnWriteArrayList<WayPoint> wayPoints = new CopyOnWriteArrayList<>();

    public void addWayPoint(WayPoint macro) {
        if (!wayPoints.contains(macro)) {
            wayPoints.add(macro);
        }
    }

    public void onLoad() {
        wayPoints = new CopyOnWriteArrayList<>();
        try {
            File file = new File("ThunderHackRecode/misc/waypoints.txt");

            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    while (reader.ready()) {
                        String[] line = reader.readLine().split(":");
                        String x = line[0];
                        String y = line[1];
                        String z = line[2];
                        String name = line[3];

                        addWayPoint(new WayPoint(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z), name));
                    }

                }
            }
        } catch (Exception ignored) {
        }
    }

    public void saveWayPoints() {
        File file = new File("ThunderHackRecode/misc/waypoints.txt");
        try {
            new File("ThunderHackRecode").mkdirs();
            file.createNewFile();
        } catch (Exception ignored) {

        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (WayPoint macro : wayPoints) {
                writer.write(macro.x + ":" + macro.y + ":" + macro.z + ":" + macro.name + "\n");
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

    public record WayPoint(int x, int y, int z, String name) {
    }
}
