package thunder.hack.modules.player;

import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.TridentItem;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class PerfectDelay extends Module {
    public PerfectDelay() {
        super("PerfectDelay", Category.PLAYER);
    }

    private final Setting<Boolean> horse = new Setting<>("Horse", true);
    private final Setting<Boolean> bow = new Setting<>("Bow", true);
    private final Setting<Boolean> crossbow = new Setting<>("Crossbow", true);
    private final Setting<Boolean> trident = new Setting<>("Trident", true);

    @Override
    public void onUpdate() {
        if (mc.player.getActiveItem().getItem() instanceof TridentItem && trident.getValue()) {
            if (mc.player.getItemUseTime() > 9)
                mc.interactionManager.stopUsingItem(mc.player);
        }

        if (mc.player.getActiveItem().getItem() instanceof CrossbowItem && crossbow.getValue()) {
            if (mc.player.getItemUseTime() > 24)
                mc.interactionManager.stopUsingItem(mc.player);
        }

        if (mc.player.getActiveItem().getItem() instanceof BowItem && bow.getValue()) {
            if (mc.player.getItemUseTime() > 19)
                mc.interactionManager.stopUsingItem(mc.player);
        }

        if (mc.player.getControllingVehicle() != null && mc.player.getControllingVehicle() instanceof HorseEntity && horse.getValue()) {
            ((IClientPlayerEntity) mc.player).setMountJumpStrength(1f);
        }
    }
}
