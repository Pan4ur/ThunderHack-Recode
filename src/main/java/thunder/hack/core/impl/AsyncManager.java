package thunder.hack.core.impl;

import com.google.common.collect.Lists;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.core.IManager;
import thunder.hack.events.impl.EventPostTick;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.EventTick;
import thunder.hack.gui.clickui.normal.ClickUI;
import thunder.hack.gui.clickui.small.SmallClickUI;
import thunder.hack.gui.mainmenu.MainMenuScreen;
import thunder.hack.modules.Module;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.shaders.MainMenuProgram;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncManager implements IManager {
    private ClientService clientService = new ClientService();
    public static ExecutorService executor = Executors.newCachedThreadPool();

    private static final Timer updateTimer = new Timer();

    private volatile Iterable<Entity> threadSafeEntityList = Collections.emptyList();
    private volatile List<AbstractClientPlayerEntity> threadSafePlayersList = Collections.emptyList();
    public final AtomicBoolean ticking = new AtomicBoolean(false);

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPostTick(EventPostTick e) {
        if (mc.world == null) return;

        threadSafeEntityList = Lists.newArrayList(mc.world.getEntities());
        threadSafePlayersList = Lists.newArrayList(mc.world.getPlayers());
        ticking.set(false);
    }

    public Iterable<Entity> getAsyncEntities() {
        return threadSafeEntityList;
    }

    public List<AbstractClientPlayerEntity> getAsyncPlayers() {
        return threadSafePlayersList;
    }

    public AsyncManager() {
        clientService.setName("ThunderHack-AsyncProcessor");
        clientService.setDaemon(true);
        clientService.start();
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (!clientService.isAlive()) {
            clientService = new ClientService();
            clientService.setName("ThunderHack-AsyncProcessor");
            clientService.setDaemon(true);
            clientService.start();
        }
    }

    public static class ClientService extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if(updateTimer.passedMs(16)) {
                    if (mc != null && (mc.currentScreen instanceof MainMenuScreen || mc.currentScreen instanceof ClickUI || mc.currentScreen instanceof SmallClickUI)) {
                        MainMenuProgram.increaseTime();
                    }
                    updateTimer.reset();
                }

                try {
                    if (!Module.fullNullCheck()) {
                        for (Module module : ThunderHack.moduleManager.modules) {
                            if (module.isEnabled()) {
                                module.onThread();
                            }
                        }
                        Thread.sleep(100);
                    } else Thread.yield();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Command.sendMessage(exception.getMessage());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTick(EventTick e) {
        ticking.set(true);
    }

    public void run(Runnable runnable, long delay) {
        executor.execute(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runnable.run();
        });
    }

    public void run(Runnable r) {
        executor.execute(r);
    }
}