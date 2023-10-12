package dev.thunderhack.modules.misc;

import dev.thunderhack.event.events.EventPostSync;
import dev.thunderhack.event.events.EventSync;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import meteordevelopment.orbit.EventHandler;

public class PVETools extends Module {
    public PVETools() {
        super("PVETools", Category.MISC);
    }

    // Crops
    private final Setting<Boolean> autoHoe = new Setting<>("AutoHoe", false);
    private final Setting<Boolean> autoLand = new Setting<>("AutoLand", false);
    private final Setting<Boolean> autoBoneMeal = new Setting<>("AutoBoneMeal", false);
    private final Setting<Boolean> Harvester = new Setting<>("Harvester", false);

    // Sheeps
    private final Setting<Boolean> SheepPaint = new Setting<>("SheepPaint", false);
    private final Setting<Boolean> SheepShear = new Setting<>("SheepShear", false);


    @EventHandler
    public void rotateAction(EventSync e) {
    }

    @EventHandler
    public void postRotateAction(EventPostSync e) {
    }
}