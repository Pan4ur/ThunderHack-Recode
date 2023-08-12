package thunder.hack.core;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.EventTick;
import thunder.hack.modules.Module;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static thunder.hack.modules.Module.mc;

public class AsyncManager {

    private ClientService clientService = new ClientService();
    public static ExecutorService executor = Executors.newCachedThreadPool();

    private volatile Iterable<Entity> threadSafeEntityList = Collections.emptyList();
    private volatile List<AbstractClientPlayerEntity> threadSafePlayersList = Collections.emptyList();

    @EventHandler
    public void onTick(EventTick e){
        if(mc.world == null) return;
        threadSafeEntityList = mc.world.getEntities();
        threadSafePlayersList = mc.world.getPlayers();
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
    public void onSync(EventSync e){
        if(!clientService.isAlive()){
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
                try {
                    if (!Module.fullNullCheck()) {
                        for (Module module : Thunderhack.moduleManager.modules) {
                            if (module.isEnabled()) {
                                module.onThread();
                            }
                        }
                        Thread.sleep(20);
                    } else {
                        Thread.yield();
                    }
                } catch(Exception exception) {
                    exception.printStackTrace();
                    Command.sendMessage(exception.getMessage());
                }
            }
        }
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
}
