package thunder.hack.modules.player;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.entry.RegistryEntry;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

import java.util.Map;

public class PerfectDelay extends Module {
    public PerfectDelay() {
        super("PerfectDelay", Category.PLAYER);
    }

    private final Setting<HorseJump> horse = new Setting<>("Horse", HorseJump.Legit);
    private final Setting<Boolean> bow = new Setting<>("Bow", true);
    private final Setting<Boolean> crossbow = new Setting<>("Crossbow", true);
    private final Setting<Boolean> trident = new Setting<>("Trident", true);

    private float getEnchantLevel(ItemStack stack) {
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : EnchantmentHelper.getEnchantments(stack).getEnchantmentsMap()) {
            if (entry.getKey().equals(Enchantments.QUICK_CHARGE)) {
                return entry.getValue();
            }
        }
        return 0;
    }

    @Override
    public void onUpdate() {
        if (mc.player.getActiveItem().getItem() instanceof TridentItem && trident.getValue()) {
            if (mc.player.getItemUseTime() > 9)
                mc.interactionManager.stopUsingItem(mc.player);
        }

        if (mc.player.getActiveItem().getItem() instanceof CrossbowItem && crossbow.getValue()) {
            if (mc.player.getItemUseTime() >= 25 - (0.25 * getEnchantLevel(mc.player.getActiveItem()) * 20))
                mc.interactionManager.stopUsingItem(mc.player);
        }

        if (mc.player.getActiveItem().getItem() instanceof BowItem && bow.getValue()) {
            if (mc.player.getItemUseTime() > 19)
                mc.interactionManager.stopUsingItem(mc.player);
        }

        if (mc.player.getControllingVehicle() != null && mc.player.getControllingVehicle() instanceof HorseEntity && horse.is(HorseJump.Rage)) {
            ((IClientPlayerEntity) mc.player).setMountJumpStrength(1f);
        }

        if (mc.player.getControllingVehicle() != null && mc.player.getControllingVehicle() instanceof HorseEntity && horse.is(HorseJump.Legit) && mc.player.getMountJumpStrength() >= 1) {
            mc.options.jumpKey.setPressed(false);
        }
    }

    private enum HorseJump {
        Legit, Rage, Off
    }
}
