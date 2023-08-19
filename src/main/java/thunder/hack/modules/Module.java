package thunder.hack.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import thunder.hack.Thunderhack;
import thunder.hack.core.CommandManager;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.ThSoundPack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Module {
    public final Setting<Boolean> enabled = new Setting<>("Enabled", false);
    private final Setting<Bind> bind = new Setting<>("Keybind", new Bind(-1, false, false));
    private final Setting<Boolean> drawn = new Setting<>("Drawn", true);

    private final String displayName;
    private final String description;
    private final Category category;

    public static final MinecraftClient mc = MinecraftClient.getInstance();

    public Module(String name, String description, Category category) {
        this.displayName = name;
        this.description = description;
        this.category = category;
    }

    public Module(String name, Category category) {
        this.displayName = description = name;
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

    public void onThread() {
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

    protected void sendMessage(String message) {
        if (mc.player == null) return;
        mc.player.sendMessage(Text.of(CommandManager.getClientMessage() + " " + Formatting.GRAY +  "[" + Formatting.DARK_PURPLE + getDisplayName() + Formatting.GRAY + "] " + message));
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

    public void enable() {
        enabled.setValue(true);
        onEnable();

        if (isOn())
            Thunderhack.EVENT_BUS.subscribe(this);

        if (fullNullCheck()) return;
        if ((!Objects.equals(displayName, "ClickGui")) && (!Objects.equals(displayName, "ThunderGui"))) {
            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                Thunderhack.notificationManager.publicity(displayName, "Модуль включен!", 2, Notification.Type.ENABLED);
            } else {
                Thunderhack.notificationManager.publicity(displayName, "Was Enabled!", 2, Notification.Type.ENABLED);
            }
            mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.ENABLE_SOUNDEVENT, SoundCategory.BLOCKS, 1f, 1f);
        }
    }

    public void disable(String reason) {
        sendMessage(reason);
        disable();
    }


    @Deprecated
    public void disable() {
        try {
            Thunderhack.EVENT_BUS.unsubscribe(this);
        } catch (Exception ignored) {
        }

        if (fullNullCheck()) return;

        enabled.setValue(false);
        onDisable();
        if ((!Objects.equals(getDisplayName(), "ClickGui")) && (!Objects.equals(getDisplayName(), "ThunderGui"))) {
            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                Thunderhack.notificationManager.publicity(getDisplayName(), "Модуль выключен!", 2, Notification.Type.DISABLED);
            } else {
                Thunderhack.notificationManager.publicity(getDisplayName(), "Was Disabled!", 2, Notification.Type.DISABLED);
            }
            mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.DISABLE_SOUNDEVENT, SoundCategory.BLOCKS, 1f, 1f);
        }
    }

    public void toggle() {
        if (enabled.getValue()) {
            disable();
        } else {
            enable();
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isDrawn() {
        return drawn.getValue();
    }

    public void setDrawn(boolean drawn) {
        this.drawn.setValue(drawn);
    }

    public Category getCategory() {
        return this.category;
    }

    public Bind getBind() {
        return bind.getValue();
    }

    public void setBind(int key, boolean mouse, boolean hold) {
        bind.setValue(new Bind(key, mouse, hold));
    }

    public void setBind(Bind bind) {
        this.bind.setValue(bind);
    }

    public boolean listening() {
        return this.isOn();
    }

    public String getFullArrayString() {
        return displayName + Formatting.GRAY + (getDisplayInfo() != null ? " [" + Formatting.WHITE + getDisplayInfo() + Formatting.GRAY + "]" : "");
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null || mc.getNetworkHandler() == null;
    }

    public String getName() {
        return displayName;
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
        return this.isOn();
    }

    public boolean isDisabled() {
        return !this.isEnabled();
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

        Category(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}

