package thunder.hack.features.modules.client;

import baritone.api.BaritoneAPI;
import meteordevelopment.orbit.EventHandler;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSetting;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public final class BaritoneSettings extends Module {
    public BaritoneSettings() {
        super("BaritoneSettings", Category.CLIENT);
    }

    public final Setting<Boolean> allowBreakBlock = new Setting<>("AllowBreakBlock", true);
    public final Setting<Boolean> allowPlace = new Setting<>("AllowPlace", true);
    public final Setting<Boolean> allowSprint = new Setting<>("AllowSprint", true);
    public final Setting<Boolean> debug = new Setting<>("Debug", false);
    public final Setting<Boolean> enterPortal = new Setting<>("EnterPortal", false);
    public final Setting<Boolean> desktopNotifications = new Setting<>("DesktopNotifications", false);

    @EventHandler
    public void onSettingChange(EventSetting e) {
        if (!ThunderHack.baritone) {
            sendMessage(isRu() ? "Баритон не найден (можешь скачать на https://meteorclient.com)" : "Baritone not found (you can download it at https://meteorclient.com)");
            return;
        }
        BaritoneAPI.getSettings().allowBreak.value = allowBreakBlock.getValue();
        BaritoneAPI.getSettings().allowPlace.value = allowPlace.getValue();
        BaritoneAPI.getSettings().allowSprint.value = allowSprint.getValue();
        BaritoneAPI.getSettings().chatDebug.value = debug.getValue();
        BaritoneAPI.getSettings().enterPortal.value = enterPortal.getValue();
        BaritoneAPI.getSettings().desktopNotifications.value = desktopNotifications.getValue();
    }

    @Override
    public boolean isToggleable() {
        return false;
    }
}