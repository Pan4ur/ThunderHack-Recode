package thunder.hack.api;

import thunder.hack.cmd.Command;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.Module;

import java.util.List;

public interface IAddon {
    void onInitialize();

    List<Module> getModules();

    List<Command> getCommands();

    List<HudElement> getHudElements();

    String getPackage();

    String getName();

    String getAuthor();

    String getRepo();
}