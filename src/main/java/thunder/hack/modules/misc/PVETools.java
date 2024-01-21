package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CarrotsBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.util.hit.BlockHitResult;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.gui.notification.Notification;
import thunder.hack.gui.notification.NotificationManager;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.Notifications;
import thunder.hack.setting.Setting;

import static thunder.hack.modules.client.MainSettings.isRu;

public class PVETools extends Module {
    public PVETools() {
        super("PVETools", Category.MISC);
    }

    // Crops
    private final Setting<Boolean> autoHoe = new Setting<>("AutoHoe", false);
    private final Setting<Boolean> cladHelper = new Setting<>("CladHelper", false);
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

    @EventHandler
    public void onSync(EventSync e) {
    }

    // ПИЗДЕЦ НЕ ТРОГАЙТЕ МОДУЛЬ!
}