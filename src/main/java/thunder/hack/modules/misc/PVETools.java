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
    String CladHelperCoords;
    String string;

    @EventHandler
    public void rotateAction(EventSync e) {
    }

    @EventHandler
    public void postRotateAction(EventPostSync e) {
    }
    @Override
    public void onEnable() {
        if(mc.player.getMainHandStack().getItem().toString().equals("filled_map")) {
            if (cladHelper.getValue()) {
                string = "";
                CladHelperCoords = mc.player.getMainHandStack().getNbt().toString();
                for (int i = CladHelperCoords.indexOf("x"); i < CladHelperCoords.indexOf("]") - 2; i++) {
                    string += CladHelperCoords.charAt(i);
                }
                disable("Found! Coords: " + string);
            }
        }
        else{
            disable(isRu() ? "Возьми карту в руки!" : "Get map in hand!");
        }
    }
    @EventHandler
    public void onSync(EventSync e) {
        if (autoHoe.getValue()) {
            if (mc.crosshairTarget != null && mc.crosshairTarget instanceof BlockHitResult bhr) {
                if (mc.world.getBlockState(bhr.getBlockPos()).getBlock() instanceof CropBlock block && block.getAge(mc.world.getBlockState(bhr.getBlockPos())) == 7) {
                    mc.options.attackKey.setPressed(true);
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPostSync(EventPostSync event) {
        mc.options.attackKey.setPressed(false);
    }
}