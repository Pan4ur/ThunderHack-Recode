package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MovementType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.impl.UseTridentEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class TridentBoost extends Module {
    public TridentBoost() {
        super("TridentBoost", Category.MOVEMENT);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Motion);
    private final Setting<Float> factor = new Setting<>("Factor", 1f, 0.1f, 20f);
    public final Setting<Integer> cooldown = new Setting<>("Cooldown", 10, 0, 20);
    public final Setting<Boolean> anyWeather = new Setting<>("AnyWeather", true);

    private enum Mode {
        Motion, Factor
    }

    @EventHandler
    public void onUseTrident(UseTridentEvent e) {
        if (mc.player.getItemUseTime() >= cooldown.getValue()) {
            float j = EnchantmentHelper.getTridentSpinAttackStrength(mc.player.getActiveItem(), mc.player);
            if (anyWeather.getValue() || mc.player.isTouchingWaterOrRain()) {
                if (j > 0) {
                    float f = mc.player.getYaw();
                    float g = mc.player.getPitch();
                    float speedX = -MathHelper.sin(f * 0.017453292F) * MathHelper.cos(g * 0.017453292F);
                    float speedY = -MathHelper.sin(g * 0.017453292F);
                    float speedZ = MathHelper.cos(f * 0.017453292F) * MathHelper.cos(g * 0.017453292F);
                    float plannedSpeed = MathHelper.sqrt(speedX * speedX + speedY * speedY + speedZ * speedZ);

                    float n = mode.is(Mode.Factor) ? factor.getValue() * 3.0F * ((1.0F + (float) j) / 4.0F) : factor.getValue();

                    speedX *= n / plannedSpeed;
                    speedY *= n / plannedSpeed;
                    speedZ *= n / plannedSpeed;

                    mc.player.addVelocity(speedX, speedY, speedZ);
                    mc.player.useRiptide(20, 8f, mc.player.getActiveItem());

                    if (mc.player.isOnGround())
                        mc.player.move(MovementType.SELF, new Vec3d(0.0, 1.1999999284744263, 0.0));

                    RegistryEntry<SoundEvent> registryEntry = EnchantmentHelper.getEffect(mc.player.getActiveItem(), EnchantmentEffectComponentTypes.TRIDENT_SOUND).orElse(SoundEvents.ITEM_TRIDENT_THROW);
                    mc.world.playSoundFromEntity(null, mc.player, registryEntry.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
        e.cancel();
    }
}
