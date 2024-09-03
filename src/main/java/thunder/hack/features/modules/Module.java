package thunder.hack.features.modules;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.CommandManager;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.gui.notification.Notification;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.client.Windows;
import thunder.hack.features.modules.client.UnHook;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;

import java.lang.reflect.Field;
import java.util.*;

import static thunder.hack.ThunderHack.LOGGER;
import static thunder.hack.core.Managers.NOTIFICATION;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public abstract class Module {
    private final Setting<Bind> bind = new Setting<>("Keybind", new Bind(-1, false, false));
    private final Setting<Boolean> drawn = new Setting<>("Drawn", true);
    private final Setting<Boolean> enabled = new Setting<>("Enabled", false);

    private final String description;
    private final Category category;
    private final String displayName;

    private final List<String> ignoreSoundList = Arrays.asList(
            "ClickGui",
            "ThunderGui",
            "HudEditor"
    );

    public static final MinecraftClient mc = MinecraftClient.getInstance();

    public Module(@NotNull String name, @NotNull Category category) {
        this.displayName = name;
        this.description = "descriptions." + category.getName().toLowerCase() + "." + name.toLowerCase();
        this.category = category;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onLogin() {
    }

    public void onLogout() {
    }

    public void onUpdate() {
    }

    public void onRender2D(DrawContext event) {
    }


    public void onRender3D(MatrixStack event) {
    }

    public void onUnload() {
    }

    public boolean isToggleable() {
        return true;
    }

    protected void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;

        mc.getNetworkHandler().sendPacket(packet);
    }

    protected void sendPacketSilent(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;
        ThunderHack.core.silentPackets.add(packet);
        mc.getNetworkHandler().sendPacket(packet);
    }

    protected void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        if (mc.getNetworkHandler() == null || mc.world == null) return;
        try (PendingUpdateManager pendingUpdateManager = mc.world.getPendingUpdateManager().incrementSequence();) {
            int i = pendingUpdateManager.getSequence();
            mc.getNetworkHandler().sendPacket(packetCreator.predict(i));
        }
    }

    public String getDisplayInfo() {
        return null;
    }

    public boolean isOn() {
        return enabled.getValue();
    }

    public boolean isOff() {
        return !enabled.getValue();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.setValue(enabled);
    }

    public void onThread() {
    }

    public void enable() {
        if (!(this instanceof UnHook))
            enabled.setValue(true);

        if (!fullNullCheck() || (this instanceof UnHook) || (this instanceof Windows))
            onEnable();

        if (isOn()) ThunderHack.EVENT_BUS.subscribe(this);
        if (fullNullCheck()) return;

        LogUtils.getLogger().info("[ThunderHack] enabled " + this.getName());
        Managers.MODULE.sortModules();

        if (!ignoreSoundList.contains(getDisplayName())) {
            NOTIFICATION.publicity(getDisplayName(), isRu() ? "Модуль включен!" : "Was Enabled!", 2, Notification.Type.ENABLED);
            Managers.SOUND.playEnable();
        }
    }

    public void disable(String reason) {
        sendMessage(reason);
        disable();
    }

    /**
     * @see #disable(String)
     */
    public void disable() {
        try {
            ThunderHack.EVENT_BUS.unsubscribe(this);
        } catch (Exception ignored) {
        }

        enabled.setValue(false);

        Managers.MODULE.sortModules();

        if (fullNullCheck()) return;

        onDisable();

        LOGGER.info("[ThunderHack] disabled {}", getName());

        if (!ignoreSoundList.contains(getDisplayName())) {
            NOTIFICATION.publicity(getDisplayName(), isRu() ? "Модуль выключен!" : "Was Disabled!", 2, Notification.Type.DISABLED);
            Managers.SOUND.playDisable();
        }
    }

    public void toggle() {
        if (enabled.getValue()) disable();
        else enable();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDrawn() {
        return drawn.getValue();
    }

    public void setDrawn(boolean d) {
        drawn.setValue(d);
    }

    public Category getCategory() {
        return category;
    }

    public Bind getBind() {
        return bind.getValue();
    }

    public void setBind(int key, boolean mouse, boolean hold) {
        setBind(new Bind(key, mouse, hold));
    }

    public void setBind(Bind b) {
        bind.setValue(b);
    }

    public boolean listening() {
        return isOn();
    }

    public String getFullArrayString() {
        return getDisplayName() + Formatting.GRAY + (getDisplayInfo() != null ? " [" + Formatting.WHITE + getDisplayInfo() + Formatting.GRAY + "]" : "");
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null || ModuleManager.unHook.isEnabled();
    }

    public String getName() {
        return getDisplayName();
    }

    public List<Setting<?>> getSettings() {
        ArrayList<Setting<?>> settingList = new ArrayList<>();
        Class<?> currentSuperclass = getClass();

        while (currentSuperclass != null) {
            for (Field field : currentSuperclass.getDeclaredFields()) {
                if (!Setting.class.isAssignableFrom(field.getType()))
                    continue;

                try {
                    field.setAccessible(true);
                    settingList.add((Setting<?>) field.get(this));
                } catch (IllegalAccessException error) {
                    LOGGER.warn(error.getMessage());
                }
            }

            currentSuperclass = currentSuperclass.getSuperclass();
        }

        settingList.forEach(s -> s.setModule(this));

        return settingList;
    }

    public boolean isEnabled() {
        return isOn();
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    public static void clickSlot(int id) {
        if (id == -1 || mc.interactionManager == null || mc.player == null) return;
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, 0, SlotActionType.PICKUP, mc.player);
    }

    public static void clickSlot(int id, SlotActionType type) {
        if (id == -1 || mc.interactionManager == null || mc.player == null) return;
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, 0, type, mc.player);
    }

    public static void clickSlot(int id, int button, SlotActionType type) {
        if (id == -1 || mc.interactionManager == null || mc.player == null) return;
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, button, type, mc.player);
    }

    public void sendMessage(String message) {
        if (fullNullCheck() || !ClientSettings.clientMessages.getValue() || ModuleManager.unHook.isEnabled()) return;
        if (mc.isOnThread()) {
            mc.player.sendMessage(Text.of(CommandManager.getClientMessage() + " " + Formatting.GRAY + "[" + Formatting.DARK_PURPLE + getDisplayName() + Formatting.GRAY + "] " + message));
        } else {
            mc.executeSync(() ->
                mc.player.sendMessage(Text.of(CommandManager.getClientMessage() + " " + Formatting.GRAY + "[" + Formatting.DARK_PURPLE + getDisplayName() + Formatting.GRAY + "] " + message))
            );
        }
    }

    public void sendChatMessage(String message) {
        if (fullNullCheck()) return;
        mc.getNetworkHandler().sendChatMessage(message);
    }

    public void sendChatCommand(String command) {
        if (fullNullCheck()) return;

        mc.getNetworkHandler().sendChatCommand(command);
    }

    public void debug(String message) {
        if (fullNullCheck() || !ClientSettings.debug.getValue()) return;
        if (mc.isOnThread()) {
            mc.player.sendMessage(Text.of(CommandManager.getClientMessage() + " " + Formatting.GRAY + "[" + Formatting.DARK_PURPLE + getDisplayName() + Formatting.GRAY + "] [\uD83D\uDD27] " + message));
        } else {
            mc.executeSync(() -> {
                mc.player.sendMessage(Text.of(CommandManager.getClientMessage() + " " + Formatting.GRAY + "[" + Formatting.DARK_PURPLE + getDisplayName() + Formatting.GRAY + "] [\uD83D\uDD27] " + message));
            });
        }
    }

    public boolean isKeyPressed(int button) {
        if (button == -1 || ModuleManager.unHook.isEnabled())
            return false;

        if (Managers.MODULE.activeMouseKeys.contains(button)) {
            Managers.MODULE.activeMouseKeys.clear();
            return true;
        }

        if (button < 10) // check
            return false;

        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), button);
    }

    public boolean isKeyPressed(Setting<Bind> bind) {
        if (bind.getValue().getKey() == -1 || ModuleManager.unHook.isEnabled())
            return false;
        return isKeyPressed(bind.getValue().getKey());
    }

    public @Nullable Setting<?> getSettingByName(String name) {
        for (Setting<?> setting : getSettings()) {
            if (!setting.getName().equalsIgnoreCase(name)) continue;
            return setting;
        }

        return null;
    }

    public static class Category {
        private final String name;
        private static final Map<String, Category> CATEGORIES = new LinkedHashMap<>();

        // Predefined categories as static instances
        public static final Category COMBAT = new Category("Combat");
        public static final Category MISC = new Category("Misc");
        public static final Category RENDER = new Category("Render");
        public static final Category MOVEMENT = new Category("Movement");
        public static final Category PLAYER = new Category("Player");
        public static final Category CLIENT = new Category("Client");
        public static final Category HUD = new Category("HUD");

        // Initialize predefined categories
        static {
            CATEGORIES.put("Combat", COMBAT);
            CATEGORIES.put("Misc", MISC);
            CATEGORIES.put("Render", RENDER);
            CATEGORIES.put("Movement", MOVEMENT);
            CATEGORIES.put("Player", PLAYER);
            CATEGORIES.put("Client", CLIENT);
            CATEGORIES.put("HUD", HUD);
        }

        // Private constructor to control creation
        private Category(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        // Static method to get or create a category
        public static Category getCategory(String name) {
            return CATEGORIES.computeIfAbsent(name, Category::new);
        }

        public static Collection<Category> values() {
            return CATEGORIES.values();
        }

        public static boolean isCustomCategory(Category category) {
            Set<String> predefinedCategoryNames = Set.of("Combat", "Misc", "Render", "Movement", "Player", "Client", "HUD");

            return !predefinedCategoryNames.contains(category.getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Category category = (Category) o;
            return Objects.equals(name, category.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}