package thunder.hack.modules.misc;

import com.google.common.eventbus.Subscribe;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.modules.combat.AutoCrystal;
import thunder.hack.setting.Setting;

public class PVETools extends Module {
    public PVETools() {
        super("PVETools", Category.MISC);
    }


    // Crops
    private final Setting<Boolean> AutoHoe = new Setting<>("AutoHoe", false);
    private final Setting<Boolean> autoLand = new Setting<>("AutoLand", false);
    private final Setting<Boolean> autoBoneMeal = new Setting<>("AutoBoneMeal", false);
    private final Setting<Boolean> Harvester = new Setting<>("Harvester", false);

    // Sheeps
    private final Setting<Boolean> SheepPaint = new Setting<>("SheepPaint", false);
    private final Setting<Boolean> SheepShear = new Setting<>("SheepShear", false);


    @Subscribe
    public void rotateAction(EventSync e){

    }

    @Subscribe
    public void postRotateAction(EventPostSync e){

    }

}
