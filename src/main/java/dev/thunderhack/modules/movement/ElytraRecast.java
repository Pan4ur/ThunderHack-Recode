package dev.thunderhack.modules.movement;

import dev.thunderhack.event.events.EventSync;
import dev.thunderhack.mixins.accesors.ILivingEntity;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.math.MathUtility;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.event.events.EventTravel;

public class ElytraRecast extends Module {
    public ElytraRecast() {
        super("ElytraRecast", Category.MOVEMENT);
    }

    /*
    https://github.com/InLieuOfLuna/elytra-recast
                        ^ Author of this exploit
     */


    public Setting<Exploit> exploit = new Setting<>("Exploit", Exploit.None);
    public Setting<Boolean> changePitch = new Setting<>("ChangePitch", true);
    public Setting<Float> pitchValue = new Setting<>("PitchValue", 55f, -90f, 90f, v -> changePitch.getValue());
    public Setting<Boolean> autoWalk = new Setting<>("AutoWalk", true);
    public Setting<Boolean> autoJump = new Setting<>("AutoJump", true);
    public Setting<Boolean> allowBroken = new Setting<>("AllowBroken", true);

    private float prevClientPitch, prevClientYaw;


    private enum Exploit {
        None, Strict
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (changePitch.getValue())
            mc.player.setPitch(pitchValue.getValue());
        if (exploit.getValue() == Exploit.Strict)
            mc.player.setYaw(mc.player.getYaw() + (20 * MathUtility.sin((System.currentTimeMillis() - ThunderHack.initTime) / 50f)));
    }

    @EventHandler
    public void modifyVelocity(EventTravel e) {
        if (e.isPre()) {
            prevClientPitch = mc.player.getPitch();
            prevClientYaw = mc.player.getYaw();
            mc.player.setPitch(pitchValue.getValue());
            if (exploit.getValue() == Exploit.Strict)
                mc.player.setYaw(mc.player.getYaw() + (20 * MathUtility.sin((System.currentTimeMillis() - ThunderHack.initTime) / 50f)));
        } else {
            mc.player.setPitch(prevClientPitch);
            if (exploit.getValue() == Exploit.Strict)
                mc.player.setYaw(prevClientYaw);
        }
    }

    @Override
    public void onDisable() {
        if (!InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode()))
            mc.options.forwardKey.setPressed(false);
        if (!InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode()))
            mc.options.jumpKey.setPressed(false);
    }

    @Override
    public void onUpdate() {
        if (autoJump.getValue()) mc.options.jumpKey.setPressed(true);
        if (autoWalk.getValue()) mc.options.forwardKey.setPressed(true);

        if (!mc.player.isFallFlying() && mc.player.fallDistance > 0 && checkElytra() && !mc.player.isFallFlying())
            castElytra();

        ((ILivingEntity) mc.player).setLastJumpCooldown(0);
    }

    public boolean castElytra() {
        if (checkElytra() && check()) {
            sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            return true;
        }
        return false;
    }

    private boolean checkElytra() {
        if (mc.player.input.jumping && !mc.player.getAbilities().flying && !mc.player.hasVehicle() && !mc.player.isClimbing()) {
            ItemStack is = mc.player.getEquippedStack(EquipmentSlot.CHEST);
            return is.isOf(Items.ELYTRA) && (ElytraItem.isUsable(is) || allowBroken.getValue());
        }
        return false;
    }

    private boolean check() {
        if (!mc.player.isTouchingWater() && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)) {
            ItemStack is = mc.player.getEquippedStack(EquipmentSlot.CHEST);
            if (is.isOf(Items.ELYTRA) && (ElytraItem.isUsable(is) || allowBroken.getValue())) {
                mc.player.startFallFlying();
                return true;
            }
        }
        return false;
    }
}
