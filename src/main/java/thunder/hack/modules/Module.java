package thunder.hack.modules;

import thunder.hack.Thunderhack;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.events.impl.Render3DEvent;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.ThSoundPack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Module  {
    private final String description;
    public static MinecraftClient mc = MinecraftClient.getInstance();
    private final Category category;
    public Setting<Boolean> enabled = new Setting<>("Enabled", false);
    public String displayName;
    public Setting<Bind> bind = new Setting<>("Keybind", new Bind(-1,false,false));
    public Setting<Boolean> drawn = new Setting<>("Drawn", true);


    public Module(String name, String description, Category category) {
        this.displayName = name;
        this.description = description;
        this.category = category;
    }

    public Module(String name, Category category) {
        this.displayName = name;
        this.description = name;
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

    public void onRender2D(Render2DEvent event) {
    }

    public void onRender3D(Render3DEvent event) {
    }

    public void onUnload() {
    }

    public String getDisplayInfo() {
        return null;
    }

    public boolean isOn() {
        return this.enabled.getValue();
    }

    public boolean isOff() {
        return !this.enabled.getValue();
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            this.enable();
        } else {
            this.disable();
        }
    }

    public void enable() {
        this.enabled.setValue(true);
        this.onEnable();
        if (this.isOn()) {
            Thunderhack.EVENT_BUS.register(this);
        }
        if(fullNullCheck()) return;
        if ((!Objects.equals(this.getDisplayName(), "ClickGui")) && (!Objects.equals(this.getDisplayName(), "ThunderGui"))) {
            if(MainSettings.language.getValue() == MainSettings.Language.RU) {
                Thunderhack.notificationManager.publicity(this.getDisplayName(), "Модуль включен!", 2, Notification.Type.ENABLED);
            } else {
                Thunderhack.notificationManager.publicity(this.getDisplayName(), "Was Enabled!", 2, Notification.Type.ENABLED);
            }
            mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.ENABLE_SOUNDEVENT, SoundCategory.BLOCKS, 1f, 1f);
        }
    }

    public void disable() {
        try {
            Thunderhack.EVENT_BUS.unregister(this);
        } catch (Exception e){
        }
 
        if(fullNullCheck()) return;

        this.enabled.setValue(false);
        this.onDisable();
        if ((!Objects.equals(this.getDisplayName(), "ClickGui")) && (!Objects.equals(this.getDisplayName(), "ThunderGui"))) {
            if(MainSettings.language.getValue() == MainSettings.Language.RU) {
                Thunderhack.notificationManager.publicity(this.getDisplayName(), "Модуль выключен!", 2, Notification.Type.DISABLED);
            } else {
                Thunderhack.notificationManager.publicity(this.getDisplayName(), "Was Disabled!", 2, Notification.Type.DISABLED);
            }
            mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.DISABLE_SOUNDEVENT, SoundCategory.BLOCKS, 1f, 1f);
        }
    }

    public void toggle() {
        this.setEnabled(!this.isEnabled());
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isDrawn() {
        return this.drawn.getValue();
    }

    public void setDrawn(boolean drawn) {
        this.drawn.setValue(drawn);
    }

    public Category getCategory() {
        return this.category;
    }


    public Bind getBind() {
        return this.bind.getValue();
    }

    public void setBind(int key, boolean mouse,boolean hold) {
        this.bind.setValue(new Bind(key, mouse,hold));
    }

    public boolean listening() {
        return this.isOn();
    }

    public String getFullArrayString() {
        return this.getDisplayName() + Formatting.GRAY + (this.getDisplayInfo() != null ? " [" + Formatting.WHITE + this.getDisplayInfo() + Formatting.GRAY + "]" : "");
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }

    public String getName() {
        return this.getDisplayName();
    }


    public List<Setting> getSettings() {
        ArrayList<Setting> settingList = new ArrayList<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    settingList.add((Setting) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Field field : this.getClass().getSuperclass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);

                try {
                    settingList.add((Setting) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Field field : this.getClass().getSuperclass().getSuperclass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);

                try {
                    settingList.add((Setting) field.get(this));
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

    public Setting getSettingByName(String name) {
        for (Setting setting : getSettings()) {
            if (!setting.getName().equalsIgnoreCase(name)) continue;
            return setting;
        }
        return null;
    }

    public void onThread() {
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

