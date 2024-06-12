package thunder.hack.modules;

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
import thunder.hack.core.impl.CommandManager;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.notification.Notification;
import thunder.hack.injection.accesors.IClientWorldMixin;
import thunder.hack.modules.client.ClientSettings;
import thunder.hack.modules.client.Windows;
import thunder.hack.modules.misc.UnHook;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static thunder.hack.modules.client.ClientSettings.isRu;

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

    private final List<String> ignoredModules = Arrays.asList(
            "ClickGui",
            "ClientSettings",
            "Rotations"
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
        try (PendingUpdateManager pendingUpdateManager = ((IClientWorldMixin) mc.world).getPendingUpdateManager().incrementSequence();) {
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
        if (ignoredModules.contains(getDisplayName())) {
            enabled.setValue(false);
            return;
        }


        LogUtils.getLogger().info("[ThunderHack] enabled " + this.getName());
        ThunderHack.moduleManager.sortModules();

        if (!ignoreSoundList.contains(getDisplayName())) {
            ThunderHack.notificationManager.publicity(getDisplayName(), isRu() ? "Модуль включен!" : "Was Enabled!", 2, Notification.Type.ENABLED);
            ThunderHack.soundManager.playEnable();
        }
    }

    public void disable(String reason) {
        sendMessage(reason);
        disable();
    }

    @Deprecated
    public void disable() {
        try {
            ThunderHack.EVENT_BUS.unsubscribe(this);
        } catch (Exception ignored) {
        }

        enabled.setValue(false);

        ThunderHack.moduleManager.sortModules();

        if (fullNullCheck()) return;
        if (ignoredModules.contains(getDisplayName())) enabled.setValue(false);

        onDisable();

        LogUtils.getLogger().info("[ThunderHack] disabled " + getName());

        if (!ignoreSoundList.contains(getDisplayName())) {
            ThunderHack.notificationManager.publicity(getDisplayName(), isRu() ? "Модуль выключен!" : "Was Disabled!", 2, Notification.Type.DISABLED);
            ThunderHack.soundManager.playDisable();
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

        for (Field field : getClass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    settingList.add((Setting<?>) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Field field : getClass().getSuperclass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);

                try {
                    settingList.add((Setting<?>) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Field field : getClass().getSuperclass().getSuperclass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);

                try {
                    settingList.add((Setting<?>) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            for (Field field : getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredFields()) {
                if (Setting.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);

                    try {
                        settingList.add((Setting<?>) field.get(this));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception ignored) {
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
        mc.player.sendMessage(Text.of(CommandManager.getClientMessage() + " " + Formatting.GRAY + "[" + Formatting.DARK_PURPLE + getDisplayName() + Formatting.GRAY + "] " + message));
    }

    public void debug(String message) {
        if (fullNullCheck() || !ClientSettings.debug.getValue()) return;
        mc.player.sendMessage(Text.of(CommandManager.getClientMessage() + " " + Formatting.GRAY + "[" + Formatting.DARK_PURPLE + getDisplayName() + Formatting.GRAY + "] [\uD83D\uDD27] " + message));
    }

    public boolean isKeyPressed(int button) {
        if (button == -1 || ModuleManager.unHook.isEnabled())
            return false;

        if (ThunderHack.moduleManager.activeMouseKeys.contains(button)) {
            ThunderHack.moduleManager.activeMouseKeys.clear();
            return true;
        }

        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), button);
    }

    public boolean isKeyPressed(Setting<Bind> bind) {
        if (bind.getValue().getKey() == -1 || ModuleManager.unHook.isEnabled())
            return false;
        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), bind.getValue().getKey());
    }

    public @Nullable Setting<?> getSettingByName(String name) {
        for (Setting<?> setting : getSettings()) {
            if (!setting.getName().equalsIgnoreCase(name)) continue;
            return setting;
        }

        return null;
    }

    public enum Category {
        COMBAT("Combat"),
        MISC("Misc"),
        RENDER("Render"),
        MOVEMENT("Movement"),
        PLAYER("Player"),
        CLIENT("Client"),
        HUD("HUD");

        private final String name;

        Category(String n) {
            name = n;
        }

        public String getName() {
            return name;
        }
    }
}