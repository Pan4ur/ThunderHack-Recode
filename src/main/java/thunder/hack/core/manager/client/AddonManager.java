package thunder.hack.core.manager.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import thunder.hack.ThunderHack;
import thunder.hack.api.IAddon;
import thunder.hack.core.manager.IManager;
import thunder.hack.core.Managers;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddonManager implements IManager {
    private int totalAddons = 0;
    private final List<IAddon> addons = new ArrayList<>();

    public void incrementAddonCount() {
        totalAddons++;
    }

    public int getTotalAddons() {
        return totalAddons;
    }

    public void addAddon(IAddon addon) {
        addons.add(addon);
    }

    public List<IAddon> getAddons() {
        return addons;
    }

    public void initAddons() {
        LogUtils.getLogger().info("Starting addon initialization.");

        for (EntrypointContainer<IAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("thunderhack", IAddon.class)) {
            IAddon addon = entrypoint.getEntrypoint();

            try {
                LogUtils.getLogger().info("Initializing addon: " + addon.getClass().getName());
                LogUtils.getLogger().debug("Addon class loader: " + addon.getClass().getClassLoader());
                addon.onInitialize();
                LogUtils.getLogger().info("Addon initialized successfully: " + addon.getClass().getName());

                incrementAddonCount();
                LogUtils.getLogger().debug("Addon count incremented.");

                addAddon(addon);
                LogUtils.getLogger().debug("Addon added to manager.");
                ThunderHack.EVENT_BUS.registerLambdaFactory(addon.getPackage(), (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

                // Register Modules
                if (addon.getModules() != null)
                    addon.getModules().stream().filter(Objects::nonNull).forEach(module -> {
                        try {
                            LogUtils.getLogger().info("Registering module: " + module.getClass().getName());
                            LogUtils.getLogger().debug("Module class loader: " + module.getClass().getClassLoader());
                            Managers.MODULE.registerModule(module);
                            LogUtils.getLogger().info("Module registered successfully: " + module.getClass().getName());
                        } catch (Exception e) {
                            LogUtils.getLogger().error("Error registering module: " + module.getClass().getName(), e);
                        }
                    });

                // Register Commands
                if (addon.getCommands() != null)
                    addon.getCommands().stream().filter(Objects::nonNull).forEach(command -> {
                        try {
                            LogUtils.getLogger().info("Registering command: " + command.getClass().getName());
                            LogUtils.getLogger().debug("Command class loader: " + command.getClass().getClassLoader());
                            Managers.COMMAND.registerCommand(command);
                            LogUtils.getLogger().info("Command registered successfully: " + command.getClass().getName());
                        } catch (Exception e) {
                            LogUtils.getLogger().error("Error registering command: " + command.getClass().getName(), e);
                        }
                    });

                // Register HUD Elements
                if (addon.getHudElements() != null)
                    addon.getHudElements().stream().filter(Objects::nonNull).forEach(hudElement -> {
                        try {
                            LogUtils.getLogger().info("Registering HUD element: " + hudElement.getClass().getName());
                            LogUtils.getLogger().debug("HUD element class loader: " + hudElement.getClass().getClassLoader());
                            Managers.MODULE.registerHudElement(hudElement);
                            LogUtils.getLogger().info("HUD element registered successfully: " + hudElement.getClass().getName());
                        } catch (Exception e) {
                            LogUtils.getLogger().error("Error registering HUD element: " + hudElement.getClass().getName(), e);
                        }
                    });

            } catch (Exception e) {
                LogUtils.getLogger().error("Error initializing addon: " + addon.getClass().getName(), e);
            }
        }

        LogUtils.getLogger().info("Addon initialization complete.");
    }

    public void shutDown() {
        for (IAddon addon : getAddons()) {
            try {
                addon.onShutdown();
            } catch (Exception e) {
                LogUtils.getLogger().error("Error running addon onShutdown method: " + addon.getClass().getName(), e);
            }
        }
    }
}