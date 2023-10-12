package dev.thunderhack.modules;

import dev.thunderhack.notification.Notification;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.core.CommandManager;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.Bind;
import dev.thunderhack.utils.SoundUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.thunderhack.modules.client.MainSettings.isRu;

public abstract class Module {
    private final Setting<Bind> bind = new Setting<>("Keybind", new Bind(-1, false, false));
    private final Setting<Boolean> drawn = new Setting<>("Drawn", true);
    private final Setting<Boolean> enabled = new Setting<>("Enabled", false);

    private final String description;
    private final Category category;
    private final String displayName;

    public static MinecraftClient mc = MinecraftClient.getInstance();

    public Module(@NotNull String name, @NotNull Category category) {
        this.displayName = name;
        this.description = "descriptions." + category.getName().toLowerCase() + "." + name.toLowerCase();
        this.category = category;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onLoad() {
    }

    public void onTick() {
    }

    public void onLogin() {
    }

    public void onLogout() {
    }

    public void onUpdate() {
    }

    public void onRenderShaders(DrawContext context) {
    }

    public void onRender2D(DrawContext event) {
    }

    public void onRender3D(MatrixStack event) {
    }

    public void onPreRender3D(MatrixStack stack) {
    }

    public void onUnload() {
    }

    protected void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;

        mc.getNetworkHandler().sendPacket(packet);
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

    public void onPostRender3D(MatrixStack stack) {
    }

    public void enable() {
        this.enabled.setValue(true);

        if (!fullNullCheck()) onEnable();
        if (isOn()) ThunderHack.EVENT_BUS.subscribe(this);
        if (fullNullCheck()) return;

        if ((!Objects.equals(getDisplayName(), "ClickGui")) && (!Objects.equals(getDisplayName(), "ThunderGui"))) {
            ThunderHack.notificationManager.publicity(getDisplayName(), isRu() ? "Модуль включен!" : "Was Enabled!", 2, Notification.Type.ENABLED);

            mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundUtil.ENABLE_SOUNDEVENT, SoundCategory.BLOCKS, 1f, 1f);
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

        if (fullNullCheck()) return;
        onDisable();

        if ((!Objects.equals(getDisplayName(), "ClickGui")) && (!Objects.equals(getDisplayName(), "ThunderGui"))) {
            ThunderHack.notificationManager.publicity(getDisplayName(), isRu() ? "Модуль выключен!" : "Was Disabled!", 2, Notification.Type.DISABLED);

            mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundUtil.DISABLE_SOUNDEVENT, SoundCategory.BLOCKS, 1f, 1f);
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
        return mc.player == null || mc.world == null;
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

    public void sendMessage(String message) {
        if (fullNullCheck()) return;
        mc.player.sendMessage(Text.of(CommandManager.getClientMessage() + " " + Formatting.GRAY + "[" + Formatting.DARK_PURPLE + getDisplayName() + Formatting.GRAY + "] " + message));
    }

    public Setting<?> getSettingByName(String name) {
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
        CLIENT("ThunderHack"),
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