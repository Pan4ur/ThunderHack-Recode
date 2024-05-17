package thunder.hack.modules.client;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventSetting;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class Baritone extends Module {
    public Baritone() {
        super("Baritone", Category.CLIENT);
    }
    public final Setting<Boolean> pauseInGUI = new Setting<>("PauseInGUI", false);
    public final Setting<Boolean> allowBreak = new Setting<>("allowBreak", true);
    public final Setting<Boolean> allowPlace = new Setting<>("allowPlace", true);
    public final Setting<Boolean> allowSprint = new Setting<>("allowSprint", true);

    @EventHandler
    public void onSettingChange(EventSetting e) {
        if (e.getSetting() == allowBreak) {
            mc.player.networkHandler.sendChatMessage("#allowBreak " + allowBreak.getValue().toString());
        } else if (e.getSetting() == allowPlace) {
            mc.player.networkHandler.sendChatMessage("#allowPlace " + allowPlace.getValue().toString());
        } else if (e.getSetting() == allowSprint) {
            mc.player.networkHandler.sendChatMessage("#allowSprint " + allowSprint.getValue().toString());
        }
    }
}
