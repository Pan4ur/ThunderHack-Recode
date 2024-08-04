package thunder.hack.core.hooks;

import thunder.hack.core.manager.client.ModuleManager;

public class ModuleShutdownHook extends Thread {
    @Override
    public void run() {
        if (ModuleManager.unHook.isEnabled())
            ModuleManager.unHook.disable();
    }
}
