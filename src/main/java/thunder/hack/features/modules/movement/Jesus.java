package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventCollision;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.Religion;
import thunder.hack.setting.Setting;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class Jesus extends Module {
    public Jesus() {
        super("Jesus", Category.MOVEMENT);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.SOLID);

    @EventHandler
    @SuppressWarnings("unused")
    public void onCollide(EventCollision e) {
        if (e.getState().getBlock() instanceof FluidBlock) {
            e.setState(mode.is(Mode.SOLID) ? Blocks.ENDER_CHEST.getDefaultState() : Blocks.OBSIDIAN.getDefaultState());
        }
    }

    @Override
    public void onEnable() {
        if (ModuleManager.religion.isOn() && !ModuleManager.religion.ReligionSetting.is(Religion.YourReligion.Christianity)) {
            ModuleManager.religion.sendMessage(isRu() ? "Ты не веришь в Иисуса!" : "You do not believe in Jesus!");
            disable();
        }
    }

    public enum Mode {
        SOLID, SOLID2
    }
}
