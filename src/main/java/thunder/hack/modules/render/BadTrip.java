package thunder.hack.modules.render;


import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class BadTrip extends Module{
    public BadTrip() {
        super("Bad Trip", Category.RENDER);
    }
    public final Setting<Integer> speed = new Setting<>("Speed", 500, 1, 1000);
    public final Setting<Float> factor = new Setting<>("Factor", 0.5f, 0f, 1f);
}
