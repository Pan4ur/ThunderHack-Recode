package thunder.hack.core.impl;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import thunder.hack.core.IManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProxyManager implements IManager {

    private final List<ThProxy> proxies = new ArrayList<>();
    private ThProxy activeProxy;

    public boolean isActive() {
        return getActiveProxy() != null;
    }

    public ThProxy getActiveProxy() {
        return activeProxy;
    }

    public void addProxy(ThProxy p) {
        proxies.add(p);
    }

    public void removeProxy(ThProxy p) {
        proxies.remove(p);
    }

    public List<ThProxy> getProxies() {
        return proxies;
    }

    public void setActiveProxy(ThProxy proxy) {
        activeProxy = proxy;
    }

    public void onLoad() {
        try {
            File file = new File(ConfigManager.CONFIG_FOLDER_NAME + "/misc/proxies.txt");

            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    while (reader.ready()) {
                        String[] line = reader.readLine().split(":");

                        String name = line[0];
                        String ip = line[1];
                        String port = line[2];
                        String login = line[3];
                        String password = line[4];
                        String active = line[5];

                        int p = 80;

                        try {
                            p = Integer.parseInt(port);
                        } catch (Exception e) {
                            LogUtils.getLogger().warn(e.getMessage());
                        }

                        ThProxy proxy = new ProxyManager.ThProxy(name, ip, p, login, password);
                        addProxy(proxy);

                        if (Objects.equals(active, "true"))
                            setActiveProxy(proxy);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveProxies() {
        File file = new File(ConfigManager.CONFIG_FOLDER_NAME + "/misc/proxies.txt");
        try {
            new File(ConfigManager.CONFIG_FOLDER_NAME).mkdirs();
            file.createNewFile();
        } catch (Exception ignored) {
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (ThProxy proxy : proxies)
                writer.write(proxy.getName() + ":" + proxy.getIp() + ":" + proxy.getPort() + ":" + proxy.getL() + ":" + proxy.getP() + ":" + (getActiveProxy() == proxy) + "\n");
        } catch (Exception ignored) {
        }
    }

    public static class ThProxy {
        private String name, ip, l, p;
        private int port;

        public ThProxy(String name, String ip, int port, String l, String p) {
            this.ip = ip;
            this.l = l;
            this.p = p;
            this.name = name;
            this.port = port;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getL() {
            return l;
        }

        public void setL(String l) {
            this.l = l;
        }

        public String getP() {
            return p;
        }

        public void setP(String p) {
            this.p = p;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
