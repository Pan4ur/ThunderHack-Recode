package thunder.hack.features.modules.client;

import thunder.hack.gui.windows.WindowsScreen;
import thunder.hack.gui.windows.impl.*;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.PositionSetting;

public class Windows extends Module {
    public Windows() {
        super("Windows", Category.CLIENT);
    }

    // todo size
    public final Setting<PositionSetting> macroPos = new Setting<>("macroPos", new PositionSetting(0.3f, 0.3f), v -> false);
    public final Setting<PositionSetting> configPos = new Setting<>("configPos", new PositionSetting(0.35f, 0.35f), v -> false);
    public final Setting<PositionSetting> friendPos = new Setting<>("friendPos", new PositionSetting(0.4f, 0.4f), v -> false);
    public final Setting<PositionSetting> waypointPos = new Setting<>("waypointPos", new PositionSetting(0.45f, 0.45f), v -> false);
    public final Setting<PositionSetting> proxyPos = new Setting<>("proxyPos", new PositionSetting(0.5f, 0.5f), v -> false);

    @Override
    public void onEnable() {
        mc.setScreen(new WindowsScreen(
                MacroWindow.get(macroPos.getValue().getX() * mc.getWindow().getScaledWidth(), macroPos.getValue().getY() * mc.getWindow().getScaledHeight(), macroPos),
                ConfigWindow.get(configPos.getValue().getX() * mc.getWindow().getScaledWidth(), configPos.getValue().getY() * mc.getWindow().getScaledHeight(), configPos),
                FriendsWindow.get(friendPos.getValue().getX() * mc.getWindow().getScaledWidth(), friendPos.getValue().getY() * mc.getWindow().getScaledHeight(), friendPos),
                WaypointWindow.get(waypointPos.getValue().getX() * mc.getWindow().getScaledWidth(), waypointPos.getValue().getY() * mc.getWindow().getScaledHeight(), waypointPos),
                ProxyWindow.get(proxyPos.getValue().getX() * mc.getWindow().getScaledWidth(), proxyPos.getValue().getY() * mc.getWindow().getScaledHeight(), proxyPos)
        ));
        disable();
    }
}
