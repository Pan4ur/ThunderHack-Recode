package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.EventTravel;
import thunder.hack.injection.accesors.ILivingEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;

//https://github.com/InLieuOfLuna/elytra-recast <-- Author of this exploit
public class ElytraRecast extends Module {
    public ElytraRecast() {
        super("ElytraRecast", Category.MOVEMENT);
    }

    public Setting<Exploit> exploit = new Setting<>("Exploit", Exploit.None);
    public Setting<Boolean> changePitch = new Setting<>("ChangePitch", true);
    public Setting<Float> pitchValue = new Setting<>("PitchValue", 55f, -90f, 90f, v -> changePitch.getValue());
    public Setting<Boolean> autoWalk = new Setting<>("AutoWalk", true);
    public Setting<Boolean> autoJump = new Setting<>("AutoJump", true);
    public Setting<Boolean> allowBroken = new Setting<>("AllowBroken", true);

    private float prevClientPitch, prevClientYaw, jitter;

    private enum Exploit {
        None, Strict, Strong
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (changePitch.getValue())
            mc.player.setPitch(pitchValue.getValue());

        switch (exploit.getValue()) {
            case None -> {}
            case Strict -> mc.player.setYaw(mc.player.getYaw() + jitter);
            case Strong -> mc.player.setPitch(pitchValue.getValue() - Math.abs(jitter / 2f));
        }
    }

    @EventHandler
    public void modifyVelocity(EventTravel e) {
        if (changePitch.getValue())
            if (e.isPre()) {
                prevClientPitch = mc.player.getPitch();
                prevClientYaw = mc.player.getYaw();
                mc.player.setPitch(pitchValue.getValue());

                switch (exploit.getValue()) {
                    case None -> {
                    }
                    case Strict -> mc.player.setYaw(mc.player.getYaw() + jitter);
                    case Strong -> mc.player.setPitch(pitchValue.getValue() - Math.abs(jitter / 2f));
                }
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

        jitter = (20 * MathUtility.sin((System.currentTimeMillis() - ThunderHack.initTime) / 50f));

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
