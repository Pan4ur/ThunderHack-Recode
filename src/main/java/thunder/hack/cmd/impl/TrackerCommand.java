package thunder.hack.cmd.impl;

import net.minecraft.util.Formatting;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.gui.clickui.ClickUI;
import thunder.hack.modules.Module;
import thunder.hack.modules.misc.Tracker;

public class TrackerCommand extends Command {
    public TrackerCommand() {
        super("tracker");
    }

    @Override
    public void execute(String[] commands) {
        if(Thunderhack.moduleManager.get(Tracker.class).isEnabled()){
            Thunderhack.moduleManager.get(Tracker.class).sendTrack();
        }
    }
}
