package thunder.hack.modules.client;

import baritone.api.BaritoneAPI;
import meteordevelopment.orbit.EventHandler;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSetting;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

import static thunder.hack.modules.client.ClientSettings.isRu;

public final class BaritoneSettings extends Module {
    public BaritoneSettings() {
        super("BaritoneSettings", Category.CLIENT);
    }
    public final Setting<Boolean> allowBreakBlock = new Setting<>("AllowBreakBlock", true);
    public final Setting<Boolean> allowPlace = new Setting<>("AllowPlace", true);
    public final Setting<Boolean> allowSprint = new Setting<>("AllowSprint", true);
    public final Setting<Boolean> debug = new Setting<>("Debug", false);
    public final Setting<Boolean> enterPortal = new Setting<>("EnterPortal", false);

    @EventHandler
    public void onSettingChange(EventSetting e){
        if(!ThunderHack.baritone) {
            sendMessage(isRu() ? "Баритон не найден (можешь скачать на https://meteorclient.com)" : "Baritone not found (you can download it on https://meteorclient.com)");
            return;
        }
        BaritoneAPI.getSettings().allowBreak.value = allowBreakBlock.getValue();
        BaritoneAPI.getSettings().allowPlace.value = allowPlace.getValue();
        BaritoneAPI.getSettings().allowSprint.value = allowSprint.getValue();
        BaritoneAPI.getSettings().chatDebug.value = debug.getValue();
        BaritoneAPI.getSettings().enterPortal.value = enterPortal.getValue();
    }
}