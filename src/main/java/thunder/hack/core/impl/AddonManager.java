package thunder.hack.core.impl;

import thunder.hack.api.IAddon;
import thunder.hack.core.IManager;

import java.util.ArrayList;
import java.util.List;

public class AddonManager implements IManager {
    private static int totalAddons = 0;
    private static final List<IAddon> addons = new ArrayList<>();

    public static void incrementAddonCount() {
        totalAddons++;
    }

    public static int getTotalAddons() {
        return totalAddons;
    }

    public static void addAddon(IAddon addon) {
        addons.add(addon);
    }

    public static List<IAddon> getAddons() {
        return addons;
    }
}