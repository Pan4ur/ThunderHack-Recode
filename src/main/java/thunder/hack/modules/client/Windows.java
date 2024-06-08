package thunder.hack.modules.client;

import thunder.hack.gui.windows.WindowsScreen;
import thunder.hack.gui.windows.impl.ConfigWindow;
import thunder.hack.gui.windows.impl.FriendsWindow;
import thunder.hack.gui.windows.impl.MacroWindow;
import thunder.hack.gui.windows.impl.WaypointWindow;
import thunder.hack.modules.Module;

public class Windows extends Module {
    public Windows() {
        super("Windows", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        mc.setScreen(new WindowsScreen(
                MacroWindow.get(),
                ConfigWindow.get(),
                FriendsWindow.get(),
                WaypointWindow.get()
        ));
        disable();
    }
}
