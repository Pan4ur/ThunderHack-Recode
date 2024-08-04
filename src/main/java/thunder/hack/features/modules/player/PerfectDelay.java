package thunder.hack.features.modules.player;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class PerfectDelay extends Module {
    public PerfectDelay() {
        super("PerfectDelay", Category.PLAYER);
    }

    private final Setting<HorseJump> horse = new Setting<>("Horse", HorseJump.Legit);
    private final Setting<Boolean> bow = new Setting<>("Bow", true);
    private final Setting<Boolean> crossbow = new Setting<>("Crossbow", true);
    private final Setting<Boolean> trident = new Setting<>("Trident", true);

    private float getEnchantLevel(ItemStack stack) {
        return EnchantmentHelper.getLevel(mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.QUICK_CHARGE).get(), stack);
    }

    @Override
    public void onUpdate() {
        if (mc.player.getActiveItem().getItem() instanceof TridentItem && trident.getValue()) {
            if (mc.player.getItemUseTime() > (ModuleManager.tridentBoost.isEnabled() ? ModuleManager.tridentBoost.cooldown.getValue() : 9))
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
