package thunder.hack.api;

import thunder.hack.modules.Module;

import java.util.List;

public interface IAddon {
    void onInitialize();
    List<Module> getModules();
}