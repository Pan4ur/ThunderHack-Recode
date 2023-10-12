package dev.thunderhack.utils.player;

import dev.thunderhack.modules.Module;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import dev.thunderhack.event.events.EventMove;

public final class MovementUtility {

    public static boolean isMoving() {
        return Module.mc.player.input.movementForward != 0.0 || Module.mc.player.input.movementSideways != 0.0;
    }

    public static double getSpeed() {
        return Math.hypot(Module.mc.player.getVelocity().x, Module.mc.player.getVelocity().z);
    }

    public static double[] forward(final double d) {
        float f = Module.mc.player.input.movementForward;
        float f2 = Module.mc.player.input.movementSideways;
        float f3 =  Module.mc.player.getYaw();
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += ((f > 0.0f) ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += ((f > 0.0f) ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        final double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        final double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        final double d4 = f * d * d3 + f2 * d * d2;
        final double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }

    public static void setMotion(double speed) {
        double forward = Module.mc.player.input.movementForward;
        double strafe = Module.mc.player.input.movementSideways;
        float yaw = Module.mc.player.getYaw();
        if (forward == 0 && strafe == 0) {
            Module.mc.player.setVelocity(0, Module.mc.player.getVelocity().y,0);
        } else {
            if (forward != 0) {
                if (strafe > 0) {
                    yaw += (float) (forward > 0 ? -45 : 45);
                } else if (strafe < 0) {
                    yaw += (float) (forward > 0 ? 45 : -45);
                }
                strafe = 0;
                if (forward > 0) {
                    forward = 1;
                } else if (forward < 0) {
                    forward = -1;
                }
            }
            double sin = MathHelper.sin((float) Math.toRadians(yaw + 90));
            double cos = MathHelper.cos((float) Math.toRadians(yaw + 90));
            Module.mc.player.setVelocity(forward * speed * cos + strafe * speed * sin, Module.mc.player.getVelocity().y,forward * speed * sin - strafe * speed * cos);
        }
    }

    public static double getJumpSpeed() {
        double jumpSpeed = 0.3999999463558197;
        if (Module.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            double amplifier = Module.mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier();
            jumpSpeed += (amplifier + 1) * 0.1;
        }
        return jumpSpeed;
    }

    public static void modifyEventSpeed(EventMove event, double d) {
        double d2 = Module.mc.player.input.movementForward;
        double d3 = Module.mc.player.input.movementSideways;
        float f = Module.mc.player.getYaw();
        if (d2 == 0.0 && d3 == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else {
            if (d2 != 0.0) {
                if (d3 > 0.0) {
                    f += (float) (d2 > 0.0 ? -45 : 45);
                } else if (d3 < 0.0) {
                    f += (float) (d2 > 0.0 ? 45 : -45);
                }

                d3 = 0.0;
                if (d2 > 0.0) {
                    d2 = 1.0;
                } else if (d2 < 0.0) {
                    d2 = -1.0;
                }
            }
            double sin = Math.sin(Math.toRadians(f + 90.0F));
            double cos = Math.cos(Math.toRadians(f + 90.0F));

            event.setX(d2 * d * cos + d3 * d * sin);
            event.setZ(d2 * d * sin - d3 * d * cos);
        }
    }

    public static double getBaseMoveSpeed() {
        int n;
        double d = 0.2873;

        if (Module.fullNullCheck()) return d;

        if (Module.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            n = Module.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            d *= 1.0 + 0.2 * (n + 1);
        }
        if (Module.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            n = Module.mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier();
            d /= 1.0 + 0.2 * (n + 1);
        }
        if (Module.mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            n = Module.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
            d /= 1.0 + (0.2 * (n + 1));
        }
        return d;
    }
}
