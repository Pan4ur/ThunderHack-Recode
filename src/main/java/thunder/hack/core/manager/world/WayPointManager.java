package thunder.hack.core.manager.world;

import thunder.hack.core.manager.IManager;
import thunder.hack.core.manager.client.ConfigManager;

import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class WayPointManager implements IManager {
    private static CopyOnWriteArrayList<WayPoint> wayPoints = new CopyOnWriteArrayList<>();

    public void addWayPoint(WayPoint wp) {
        if (!wayPoints.contains(wp))
            wayPoints.add(wp);
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
                        String dimension = line.length == 6 ? line[5] : "overworld";

                        addWayPoint(new WayPoint(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z), name, server, dimension));
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
                writer.write(wayPoint.x + ":" + wayPoint.y + ":" + wayPoint.z + ":" + wayPoint.name + ":" + wayPoint.server + ":" + wayPoint.dimension + "\n");
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
        for (WayPoint wayPoint : getWayPoints())
            if (wayPoint.name.equalsIgnoreCase(name))
                return wayPoint;
        return null;
    }


    public static class WayPoint {

        private int x, y, z;
        private String name, server, dimension;

        public WayPoint(int x, int y, int z, String name, String server, String dimension) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.name = name;
            this.server = server;
            this.dimension = dimension;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getZ() {
            return z;
        }

        public void setZ(int z) {
            this.z = z;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public String getDimension() {
            return dimension;
        }

        public void setDimension(String dimension) {
            this.dimension = dimension;
        }
    }
}
