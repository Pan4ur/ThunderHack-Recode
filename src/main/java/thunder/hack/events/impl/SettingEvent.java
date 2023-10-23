package thunder.hack.events.impl;

import thunder.hack.events.Event;
import thunder.hack.setting.Setting;

public class SettingEvent extends Event {
    final Setting<?> setting;

    public SettingEvent(Setting<?> setting){
        this.setting = setting;
    }

    public Setting<?> getSetting() {
        return setting;
    }
}
