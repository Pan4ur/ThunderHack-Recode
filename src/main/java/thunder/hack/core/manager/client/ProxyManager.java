package thunder.hack.core.manager.client;

import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.handler.NetworkStateTransitions;
import thunder.hack.core.manager.IManager;

import java.io.*;
import java.net.InetSocketAddress;
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

                        if (Objects.equals(active, "true")) setActiveProxy(proxy);
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

    public void checkPing(ThProxy proxy) {
        long now = System.currentTimeMillis();
        proxy.setPing(-2);
        new Thread(() -> {
            try {
                NioEventLoopGroup group = new NioEventLoopGroup();

                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ch.pipeline().addLast(new FlowControlHandler())
                                        .addLast("timeout_proxy_checker", new ReadTimeoutHandler(8))
                                        .addLast("inbound_proxy_checker", new NetworkStateTransitions.InboundConfigurer())
                                        .addLast("outbound_proxy_checker", new NetworkStateTransitions.OutboundConfigurer())
                                        .addLast(new Socks5ProxyHandler(new InetSocketAddress(proxy.getIp(), proxy.getPort()), proxy.getL(), proxy.getP()))
                                        .addLast(new ProxyHandler());
                            }
                        });

                ChannelFuture future = bootstrap.connect("mcfunny.su", 25565).sync();
                future.await();
                if (future.isSuccess()) proxy.setPing((int) (System.currentTimeMillis() - now));
                else proxy.setPing(-1);

                group.shutdownGracefully();
            } catch (Exception e) {
                proxy.setPing(-1);
            }
        }).start();
    }

    private static class ProxyHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }

    public static class ThProxy {
        private String name, ip, l, p;
        private int port, ping;

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

        public void setPort(int port) {
            this.port = port;
        }

        public int getPort() {
            return port;
        }

        public void setPing(int ping) {
            this.ping = ping;
        }

        public int getPing() {
            return ping;
        }
    }
}
