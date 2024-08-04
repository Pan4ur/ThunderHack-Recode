package thunder.hack.features.modules.misc;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class UnfocusedCPU extends Module {
    public UnfocusedCPU() {
        super("UnfocusedCPU", Module.Category.MISC);
    }
    private final Setting<Integer> fps = new Setting("Fps", 15, 0, 100);
    private int maxFps;

    @Override
    public void onEnable() {
        maxFps = mc.options.getMaxFps().getValue();
    }

    @Override
    public void onUpdate() {
        if(!mc.isWindowFocused() && !fullNullCheck()){
            mc.getWindow().setFramerateLimit(fps.getValue());
        }
        else {
            mc.getWindow().setFramerateLimit(maxFps);
        }
    }
}
