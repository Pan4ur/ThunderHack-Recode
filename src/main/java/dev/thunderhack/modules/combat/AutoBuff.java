package dev.thunderhack.modules.combat;

import dev.thunderhack.event.events.EventPostSync;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.Timer;
import dev.thunderhack.utils.player.PlayerUtility;
import meteordevelopment.orbit.EventHandler;
import dev.thunderhack.event.events.EventAfterRotate;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Hand;

public class AutoBuff extends Module {
    private final Setting<Boolean> strenght = new Setting<>("Strenght", true);
    private final Setting<Boolean> speed = new Setting<>("SpeedPot", true);
    private final Setting<Boolean> fire = new Setting<>("FireRes", true);
    private final Setting<Boolean> heal = new Setting<>("Heal", true);
    private final Setting<Boolean> regen = new Setting<>("Regeneration", true);

    public Setting<Integer> health = new Setting<>("Health", 8, 0, 20);
    public Timer timer = new Timer();

    public AutoBuff() {
        super("AutoBuff", Category.COMBAT);
    }

    public static int getPotionSlot(Potions potion) {
        for (int i = 0; i < 9; ++i) {
            if (isStackPotion(mc.player.getInventory().getStack(i), potion)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isPotionOnHotBar(Potions potions) {
        return getPotionSlot(potions) != -1;
    }

    public static boolean isStackPotion(ItemStack stack, Potions potion) {
        if (stack == null)
            return false;

        if (stack.getItem() == Items.SPLASH_POTION) {
            StatusEffect id = null;

            switch (potion) {
                case STRENGTH -> id = StatusEffects.STRENGTH;
                case SPEED -> id = StatusEffects.SPEED;
                case FIRERES -> id = StatusEffects.FIRE_RESISTANCE;
                case HEAL -> id = StatusEffects.INSTANT_HEALTH;
                case REGEN -> id = StatusEffects.REGENERATION;
            }

            for (StatusEffectInstance effect : PotionUtil.getPotion(stack).getEffects()) {
                if (effect.getEffectType() == id) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onPostRotationSet(EventAfterRotate event) {
        if (Aura.target != null && mc.player.getAttackCooldownProgress(1) > 0.5f)
            return;
        if (mc.player.age > 80 && shouldThrow()) mc.player.setPitch(90);
    }

    private boolean shouldThrow() {
        return (!mc.player.hasStatusEffect(StatusEffects.SPEED) && isPotionOnHotBar(Potions.SPEED) && speed.getValue())
                || (!mc.player.hasStatusEffect(StatusEffects.STRENGTH) && isPotionOnHotBar(Potions.STRENGTH) && strenght.getValue())
                || (!mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && isPotionOnHotBar(Potions.FIRERES) && fire.getValue())
                || (mc.player.getHealth() + mc.player.getAbsorptionAmount() < health.getValue() && isPotionOnHotBar(Potions.HEAL) && heal.getValue());
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {
        if (Aura.target != null && mc.player.getAttackCooldownProgress(1) > 0.5f)
            return;
        mc.executeTask(() -> {
            if (mc.player.age > 80 && shouldThrow() && timer.passedMs(1000)) {
                if (!mc.player.hasStatusEffect(StatusEffects.SPEED) && isPotionOnHotBar(Potions.SPEED) && speed.getValue()) {
                    throwPotion(Potions.SPEED);
                }
                if (!mc.player.hasStatusEffect(StatusEffects.STRENGTH) && isPotionOnHotBar(Potions.STRENGTH) && strenght.getValue()) {
                    throwPotion(Potions.STRENGTH);
                }
                if (!mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && isPotionOnHotBar(Potions.FIRERES) && fire.getValue()) {
                    throwPotion(Potions.FIRERES);
                }
                if (mc.player.getHealth() + mc.player.getAbsorptionAmount() < health.getValue() && heal.getValue() && isPotionOnHotBar(Potions.HEAL)) {
                    throwPotion(Potions.HEAL);
                }
                if (!mc.player.hasStatusEffect(StatusEffects.REGENERATION) && isPotionOnHotBar(Potions.REGEN) && regen.getValue()) {
                    throwPotion(Potions.REGEN);
                }
                sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                timer.reset();
            }
        });
    }

    public void throwPotion(Potions potion) {
        int slot = getPotionSlot(potion);
        sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtility.getWorldActionId(mc.world)));
    }

    public enum Potions {
        STRENGTH, SPEED, FIRERES, HEAL, REGEN
    }
}