package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import thunder.hack.ThunderHack;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.modules.render.FreeCam;
import thunder.hack.setting.Setting;

public class Step extends Module {

    private final thunder.hack.utility.Timer stepTimer = new thunder.hack.utility.Timer();
    public Setting<Boolean> strict = new Setting<>("Strict", false);
    public Setting<Float> height = new Setting("Height", 2.0F, 1F, 2.5F,v-> !strict.getValue());
    public Setting<Boolean> entityStep = new Setting<>("EntityStep", false);
    public Setting<Boolean> useTimer = new Setting<>("Timer", true);
    public Setting<Boolean> pauseIfShift = new Setting<>("PauseIfShift", false);
    public Setting<Integer> stepDelay = new Setting("StepDelay", 200, 0, 1000);
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.NCP);
    private boolean timer;
    private Entity entityRiding;

    public Step() {
        super("Step", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        ThunderHack.TICK_TIMER = 1f;
        mc.player.setStepHeight(0.6F);
        if (entityRiding != null) {
            if (entityRiding instanceof HorseEntity
                    || entityRiding instanceof LlamaEntity
                    || entityRiding instanceof MuleEntity
                    || entityRiding instanceof PigEntity && mc.player.getControllingVehicle() == entityRiding && ((PigEntity) entityRiding).canBeSaddled()) {
                entityRiding.setStepHeight(1);
            } else {
                entityRiding.setStepHeight(0.5F);
            }
        }
    }

    @Override
    public void onUpdate() {
        if(pauseIfShift.getValue() && mc.options.sneakKey.isPressed()){
            mc.player.setStepHeight(0.6F);
            return;
        }

        if (mc.player.getAbilities().flying || ModuleManager.freeCam.isOn()) {
            mc.player.setStepHeight(0.6F);
            return;
        }
        if (mc.player.isTouchingWater()) {
            mc.player.setStepHeight(0.6F);
            return;
        }
        if (timer && mc.player.isOnGround()) {
            ThunderHack.TICK_TIMER = 1f;
            timer = false;
        }

        if (mc.player.isOnGround() && stepTimer.passedMs(stepDelay.getValue())) {
            if (mc.player.isRiding() && mc.player.getControllingVehicle() != null) {
                entityRiding = mc.player.getControllingVehicle();
                if (entityStep.getValue()) {
                    mc.player.getControllingVehicle().setStepHeight(height.getValue().floatValue());
                }
            } else {
                mc.player.setStepHeight(height.getValue().floatValue());
            }
        } else {
            if (mc.player.isRiding() && mc.player.getControllingVehicle() != null) {
                entityRiding = mc.player.getControllingVehicle();
                if (entityRiding != null) {
                    if (entityRiding instanceof HorseEntity || entityRiding instanceof LlamaEntity || entityRiding instanceof MuleEntity || entityRiding instanceof PigEntity && mc.player.getControllingVehicle() == entityRiding && ((PigEntity) entityRiding).canBeSaddled()) {
                        entityRiding.setStepHeight(1);
                    } else {
                        entityRiding.setStepHeight(0.5F);
                    }
                }
            } else mc.player.setStepHeight(0.6F);
        }
    }

    @EventHandler
    public void onStep(EventSync event) {
        if (mode.getValue().equals(Mode.NCP)) {
            double stepHeight = mc.player.getY() - mc.player.prevY;

            if (stepHeight <= 0.75 || stepHeight > height.getValue() || (strict.getValue() && stepHeight > 1)) {
                return;
            }

            double[] offsets = getOffset(stepHeight);
            if (offsets != null && offsets.length > 1) {
                if (useTimer.getValue()) {
                    ThunderHack.TICK_TIMER = 1F / offsets.length;
                    timer = true;
                }
                for (double offset : offsets)
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.prevX, mc.player.prevY + offset, mc.player.prevZ, false));

            }
            stepTimer.reset();
        }
    }

    public double[] getOffset(double height) {
        if (height == 0.75) {
            if (strict.getValue()) {
                return new double[]{0.42, 0.753, 0.75};
            } else {
                return new double[]{0.42, 0.753};
            }
        } else if (height == 0.8125) {
            if (strict.getValue()) {
                return new double[]{0.39, 0.7, 0.8125};
            } else {
                return new double[]{0.39, 0.7};
            }
        } else if (height == 0.875) {
            if (strict.getValue()) {
                return new double[]{0.39, 0.7, 0.875};
            } else {
                return new double[]{0.39, 0.7};
            }
        } else if (height == 1) {
            if (strict.getValue()) {
                return new double[]{0.42, 0.753, 1};
            } else {
                return new double[]{0.42, 0.753};
            }
        } else if (height == 1.5) {
            return new double[]{0.42, 0.75, 1.0, 1.16, 1.23, 1.2};
        } else if (height == 2) {
            return new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
        } else if (height == 2.5) {
            return new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};
        }

        return null;
    }

    public enum Mode {
        NCP,
        VANILLA
    }
}