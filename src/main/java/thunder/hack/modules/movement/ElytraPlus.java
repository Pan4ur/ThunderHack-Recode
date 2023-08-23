package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.EventTravel;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.MovementUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;

import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.START_FALL_FLYING;

public class ElytraPlus extends Module {
    public ElytraPlus() {
        super("Elytra+", Category.MOVEMENT);
    }

    private final Setting<Mode> mode = new Setting("Mode", Mode.FireWork);
    private final Setting<Float> xzSpeed = new Setting<>("XZ Speed", 1.9f, 0.5f, 3f, v -> mode.getValue() == Mode.FireWork || mode.getValue() == Mode.Sunrise);
    private final Setting<Float> ySpeed = new Setting<>("Y Speed", 0.47f, 0f, 2f, v -> mode.getValue() == Mode.FireWork || mode.getValue() == Mode.Sunrise);
    private final Setting<Integer> fireSlot = new Setting<>("Firework Slot", 0, 0, 8, v -> mode.getValue() == Mode.FireWork);
    private final Setting<Float> fireDelay = new Setting<>("Firework Delay", 1.5f, 0f, 1.5f, v -> mode.getValue() == Mode.FireWork);
    private final Setting<Boolean> stayMad = new Setting<>("StayOffTheGround", true, v -> mode.getValue() == Mode.FireWork);
    private final Setting<Boolean> keepFlying = new Setting<>("Keep Flying", false, v -> mode.getValue() == Mode.FireWork);
    private final Setting<Boolean> bowBomb = new Setting<>("BowBomb", false, v -> mode.getValue() == Mode.FireWork || mode.getValue() == Mode.Sunrise);
    public Setting<Bind> bombKey = new Setting<>("BombKey", new Bind(-1, false, false), v -> mode.getValue() == Mode.Sunrise);
    private final Setting<Boolean> instantFly = new Setting<>("InstantFly", true, v -> (mode.getValue() != Mode.FireWork && mode.getValue() != Mode.Sunrise && mode.getValue() != Mode.Pitch40Infinite));
    public Setting<Boolean> cruiseControl = new Setting<>("CruiseControl", false, v -> mode.getValue() == Mode.Boost);
    public Setting<Float> factor = new Setting<>("Factor", 1.5f, 0.1f, 50.0f, v -> (mode.getValue() != Mode.FireWork && mode.getValue() != Mode.Sunrise && mode.getValue() != Mode.Pitch40Infinite));
    public Setting<Float> upFactor = new Setting<>("UpFactor", 1.0f, 0.0f, 10.0f, v -> (mode.getValue() != Mode.FireWork && mode.getValue() != Mode.Sunrise && mode.getValue() != Mode.Pitch40Infinite));
    public Setting<Float> downFactor = new Setting<>("DownFactor", 1.0f, 0.0f, 10.0f, v -> (mode.getValue() != Mode.FireWork && mode.getValue() != Mode.Sunrise && mode.getValue() != Mode.Pitch40Infinite));
    public Setting<Boolean> stopMotion = new Setting<>("StopMotion", true, v -> mode.getValue() == Mode.Boost);
    public Setting<Float> minUpSpeed = new Setting<>("MinUpSpeed", 0.5f, 0.1f, 5.0f, v -> mode.getValue() == Mode.Boost && cruiseControl.getValue());
    public Setting<Boolean> forceHeight = new Setting<>("ForceHeight", false, v -> (mode.getValue() == Mode.Boost && cruiseControl.getValue()));
    private final Setting<Integer> manualHeight = new Setting<>("Height", 121, 1, 256, v -> ((mode.getValue() == Mode.Boost && cruiseControl.getValue())) && forceHeight.getValue());
    public Setting<Float> speed = new Setting<>("Speed", 1.0f, 0.1f, 10.0f, v -> mode.getValue() == Mode.Control);
    private final Setting<Float> sneakDownSpeed = new Setting<>("DownSpeed", 1.0F, 0.1F, 10.0F, v -> mode.getValue() == Mode.Control);
    private final Setting<Boolean> BoostTimer = new Setting<>("Timer", true, v -> mode.getValue() == Mode.Boost);
    public Setting<Boolean> speedLimit = new Setting<>("SpeedLimit", true, v -> (mode.getValue() != Mode.FireWork && mode.getValue() != Mode.Sunrise && mode.getValue() != Mode.Pitch40Infinite));
    public Setting<Float> maxSpeed = new Setting<>("MaxSpeed", 2.5f, 0.1f, 10.0f, v -> speedLimit.getValue() && mode.getValue() != Mode.Pitch40Infinite);
    public Setting<Boolean> noDrag = new Setting<>("NoDrag", false, v -> (mode.getValue() != Mode.FireWork && mode.getValue() != Mode.Sunrise && mode.getValue() != Mode.Pitch40Infinite));
    private final Setting<Float> packetDelay = new Setting<>("Limit", 1F, 0.1F, 5F, v -> mode.getValue() == Mode.Boost);
    private final Setting<Float> staticDelay = new Setting<>("Delay", 5F, 0.1F, 20F, v -> mode.getValue() == Mode.Boost);
    private final Setting<Float> timeout = new Setting<>("Timeout", 0.5F, 0.1F, 1F, v -> mode.getValue() == Mode.Boost);
    private final Setting<Float> infiniteMaxSpeed = new Setting<>("InfiniteMaxSpeed", 150f, 50f, 170f, v -> mode.getValue() == Mode.Pitch40Infinite);
    private final Setting<Float> infiniteMinSpeed = new Setting<>("InfiniteMinSpeed", 25f, 10f, 70f, v -> mode.getValue() == Mode.Pitch40Infinite);
    private final Setting<Integer> infiniteMaxHeight = new Setting<>("InfiniteMaxHeight", 200, 50, 360, v -> mode.getValue() == Mode.Pitch40Infinite);


    private boolean hasElytra = false;
    private boolean infiniteFlag = false;
    private double height;
    private final thunder.hack.utility.Timer instantFlyTimer = new thunder.hack.utility.Timer();
    private final thunder.hack.utility.Timer staticTimer = new thunder.hack.utility.Timer();
    private final thunder.hack.utility.Timer strictTimer = new thunder.hack.utility.Timer();
    private boolean hasTouchedGround = false;
    private float prevClientPitch;
    private float infinitePitch;

    public enum Mode {FireWork, Sunrise, Boost, Control, Pitch40Infinite}

    private int lastItem = -1;
    private float acceleration;
    private boolean TakeOff, start;

    private int getElytra() {
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.getItem() == Items.ELYTRA && s.getDamage() < 430) {
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }

    private int getFireworks() {
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.getItem() == Items.FIREWORK_ROCKET) {
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }

    @Override
    public void onEnable() {
        if (mc.player.getY() < infiniteMaxHeight.getValue() && mode.getValue() == Mode.Pitch40Infinite) {
            disable(MainSettings.isRu() ? "Поднимись выше " + Formatting.AQUA + infiniteMaxHeight.getValue() + Formatting.GRAY + " высоты!" : "Rise above " + Formatting.AQUA + infiniteMaxHeight.getValue() + Formatting.GRAY + " high!");
        }

        infiniteFlag = false;
        acceleration = 0;
        if (mode.getValue() == Mode.FireWork) {
            start = true;
            if (mc.player.getInventory().getStack(38).getItem() == Items.ELYTRA)
                return;
            int elytra = getElytra();
            if (elytra != -1) {
                lastItem = elytra;
                mc.interactionManager.clickSlot(0, elytra, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, 6, 0, SlotActionType.PICKUP, mc.player);
                if (!mc.player.playerScreenHandler.getCursorStack().isEmpty())
                    mc.interactionManager.clickSlot(0, elytra, 0, SlotActionType.PICKUP, mc.player);
            }
        }
        if (mc.player != null) {
            height = mc.player.getY();
            if (!mc.player.isCreative()) mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
        }
        hasElytra = false;
    }

    @EventHandler
    public void modifyVelocity(EventTravel e) {
        if (mode.getValue() == Mode.Pitch40Infinite) {
            if (e.isPre()) {
                prevClientPitch = mc.player.getPitch();
                mc.player.setPitch(getInfinitePitch());
            } else {
                mc.player.setPitch(prevClientPitch);
            }
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (mode.getValue() == Mode.Pitch40Infinite)
            mc.player.setPitch(getInfinitePitch());
    }

    private float getInfinitePitch() {
        if (mc.player.getY() < infiniteMaxHeight.getValue()) {
            if (Thunderhack.playerManager.currentPlayerSpeed * 72f < infiniteMinSpeed.getValue() && !infiniteFlag)
                infiniteFlag = true;
            if (Thunderhack.playerManager.currentPlayerSpeed * 72f > infiniteMaxSpeed.getValue() && infiniteFlag)
                infiniteFlag = false;
        } else {
            infiniteFlag = true;
        }

        if (infiniteFlag) {
            infinitePitch++;
        } else {
            infinitePitch--;
        }

        infinitePitch = MathUtility.clamp(infinitePitch, -40, 40);
        return infinitePitch;
    }


    @Override
    public void onDisable() {
        Thunderhack.TICK_TIMER = 1.0f;
        hasElytra = false;
        if (mc.player != null) {
            if (!mc.player.isCreative()) mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
        }
        if (mode.getValue() == Mode.FireWork) {
            acceleration = 0f;
            if (keepFlying.getValue())
                return;
            if (lastItem == -1)
                return;
            mc.interactionManager.clickSlot(0, 6, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, this.lastItem, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.playerScreenHandler.getCursorStack().isEmpty())
                mc.interactionManager.clickSlot(0, 6, 0, SlotActionType.PICKUP, mc.player);
            lastItem = -1;
        }
    }

    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.FireWork) {

            int fireworkSlot = getFireworks();
            if (fireworkSlot != -1) {
                mc.interactionManager.clickSlot(0, fireworkSlot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, fireSlot.getValue() + 36, 0, SlotActionType.PICKUP, mc.player);
                if (!mc.player.playerScreenHandler.getCursorStack().isEmpty())
                    mc.interactionManager.clickSlot(0, fireworkSlot, 0, SlotActionType.PICKUP, mc.player);
                return;
            }
            Command.sendMessage("Нет фейерверков!");
            if (!keepFlying.getValue()) {
                disable();
                return;
            }

            if (getElytra() == -1 && mc.player.getInventory().getStack(38).getItem() != Items.ELYTRA) {
                disable(MainSettings.isRu() ? "Нет элитр!" : "No Elytra!");
                return;
            }

            if (mc.player.isOnGround()) {
                mc.player.jump();
                TakeOff = true;
                start = true;
            } else if (TakeOff && mc.player.fallDistance > 0.05) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, START_FALL_FLYING));
                useFireWork();
                TakeOff = false;
            }
        } else if (mode.getValue() == Mode.Sunrise) {
            if (mc.player.horizontalCollision) acceleration = 0;

            if (mc.player.verticalCollision) {
                acceleration = 0;
                mc.player.setVelocity(mc.player.getVelocity().getX(), 0.41999998688697815, mc.player.getVelocity().getZ());
            }

            int elytra = getSlotIDFromItem(Items.ELYTRA);

            if (elytra == -1) {
                // Command.sendMessage("Нету элитр в инвентаре!");
                return;
            }
            if (mc.player.isOnGround()) {
                mc.player.jump();
            }
            if (System.currentTimeMillis() - lastStartFalling > 80L) {
                disabler(elytra);
            }

            if (mc.player.fallDistance > 0.25f) {

                setSpeed(Math.min((acceleration = (acceleration + 11.0F / xzSpeed.getValue())) / 100.0F, xzSpeed.getValue()));

                if (!MovementUtility.isMoving()) acceleration = 0;

                if (!bowBomb.getValue() && !mc.player.isOnGround()) {
                    mc.player.setVelocity(mc.player.getVelocity().getX(), -0.01F - (mc.player.age % 2 == 0 ? 1.0E-4F : 0.006F), mc.player.getVelocity().getZ());

                }
                if (bowBomb.getValue()) {
                    if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), bombKey.getValue().getKey())) {
                        setSpeed(0.8f);
                        mc.player.setVelocity(mc.player.getVelocity().getX(), mc.player.age % 2 == 0 ? 0.41999998688697815 : -0.41999998688697815, mc.player.getVelocity().getZ());
                        acceleration = 70;
                    } else {
                        mc.player.setVelocity(mc.player.getVelocity().getX(), -0.01F - (mc.player.age % 2 == 0 ? 1.0E-4F : 0.006F), mc.player.getVelocity().getZ());
                    }
                }

                if (!mc.player.isSneaking() && mc.options.jumpKey.isPressed()) {
                    mc.player.setVelocity(mc.player.getVelocity().getX(), ySpeed.getValue(), mc.player.getVelocity().getZ());
                }
                if (mc.options.sneakKey.isPressed()) {
                    mc.player.setVelocity(mc.player.getVelocity().getX(), -ySpeed.getValue(), mc.player.getVelocity().getZ());
                }
            }
        } else if (mode.getValue() != Mode.Pitch40Infinite) {
            if (fullNullCheck()) return;

            if (mc.player.isOnGround()) {
                hasTouchedGround = true;
            }

            if (!cruiseControl.getValue()) {
                height = mc.player.getY();
            }

            for (ItemStack is : mc.player.getArmorItems()) {
                if (is.getItem() instanceof ElytraItem) {
                    hasElytra = true;
                    break;
                } else {
                    hasElytra = false;
                }
            }

            if (strictTimer.passedMs(1500) && !strictTimer.passedMs(2000)) {
                Thunderhack.TICK_TIMER = 1.0f;
            }

            if (!mc.player.isFallFlying()) {
                if (hasTouchedGround && BoostTimer.getValue() && !mc.player.isOnGround()) {
                    Thunderhack.TICK_TIMER = 0.3f;
                }
                if (!mc.player.isOnGround() && instantFly.getValue() && mc.player.getVelocity().getY() < 0D) {
                    if (!instantFlyTimer.passedMs((long) (1000 * timeout.getValue())))
                        return;
                    instantFlyTimer.reset();
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    hasTouchedGround = false;
                    strictTimer.reset();
                }
            }
        }
    }


    @EventHandler
    public void onMove(EventMove e) {
        if (mode.getValue() == Mode.FireWork) {

            int fireworkSlot = getFireworks();
            if (fireworkSlot == -1) {
                return;
            }
            e.cancel(); // отменяем, для изменения значений
            double motionY = 0; // вводим переменную дельты моушена по Y

            if (mc.player.isFallFlying()) { // если мы летим на элитре
                if (start) { // если стоит флаг старта
                    start = false; // убираем флаг старта
                    useFireWork(); // юзаем фейерверк
                }

                if (mc.player.age % (int) (fireDelay.getValue() * 20) == 0) { // каждые fireDelay * 20 тиков (в целестиале "Задержка фейерверка") ..
                    useFireWork(); // юзаем фейерверк
                }

                if (!MovementUtility.isMoving()) {
                    e.set_x(0);
                    e.set_z(0);
                    acceleration = 0f; // сбрасываем множитель ускорения
                } else { //
                    double[] moveDirection = MovementUtility.forward(lerp(0f, xzSpeed.getValue(), Math.min(acceleration, 1f))); // расчитываем моушены исходя из ускорения и угла поворота камеры
                    e.set_x(moveDirection[0]); // выставляем моушен X
                    e.set_z(moveDirection[1]); // выставляем моушен Z
                    acceleration += 0.1f; // увеличивам множитель ускорения
                }

                if (mc.player.input.jumping) {   // если нажата кнопка прыжка (mc.gameSettings.keyBindJump.isKeyDown() не робит, хз почему)..
                    motionY = ySpeed.getValue(); // дельта будет равна ySpeed (в целестиале "Скорость по Y")
                } else if (mc.options.sneakKey.isPressed()) { // иначе если нажат шифт
                    if (mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, -1.5, 0.0)).iterator().hasNext() && stayMad.getValue()) // если мы касаемся земли и включен чек "Stay Off The Ground" (в целке "Не приземляться")..
                        motionY = ySpeed.getValue(); // обратно набираем высоту
                    else  // иначе
                        motionY = -ySpeed.getValue(); // опускаемся вниз со скоростью ySpeed (в целестиале "Скорость по Y")
                } else {
                    if (bowBomb.getValue())
                        motionY += mc.player.age % 2 == 0 ? -0.42f : 0.42f;
                    else
                        motionY += mc.player.age % 2 == 0 ? -0.08f : 0.08f;
                }
                e.set_y(motionY);
            }
        } else {
            if (fullNullCheck() || !hasElytra || !mc.player.isFallFlying()) return;

            if (!mc.player.isTouchingWater() || mc.player != null && mc.player.getAbilities().flying && !mc.player.isInLava() || mc.player.getAbilities().flying && mc.player.isFallFlying()) {

                e.cancel();

                if (mode.getValue() != Mode.Boost) {
                    Vec3d lookVec = mc.player.getRotationVec(mc.getTickDelta());

                    float pitch = mc.player.getPitch() * 0.017453292F;

                    double lookDist = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
                    double motionDist = Math.sqrt(e.get_x() * e.get_x() + e.get_z() * e.get_z());
                    double lookVecDist = lookVec.length();

                    float cosPitch = MathHelper.cos(pitch);
                    cosPitch = (float) ((double) cosPitch * (double) cosPitch * Math.min(1.0D, lookVecDist / 0.4D));

                    if (mode.getValue() != Mode.Control) {
                        e.set_y(e.get_y() + (-0.08D + (double) cosPitch * (0.06D / downFactor.getValue())));
                    }

                    if (mode.getValue() == Mode.Control) {
                        if (mc.options.sneakKey.isPressed()) {
                            e.set_y(-sneakDownSpeed.getValue());
                        } else if (!mc.player.input.jumping) {
                            e.set_y(-0.00000000000003D * downFactor.getValue());
                        }
                    } else if (mode.getValue() != Mode.Control && e.get_y() < 0.0D && lookDist > 0.0D) {
                        double downSpeed = e.get_y() * -0.1D * (double) cosPitch;
                        e.set_y(e.get_y() + downSpeed);
                        e.set_x(e.get_x() + (lookVec.x * downSpeed / lookDist) * factor.getValue());
                        e.set_z(e.get_z() + (lookVec.z * downSpeed / lookDist) * factor.getValue());
                        mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());

                    }

                    if (pitch < 0.0F && mode.getValue() != Mode.Control) {
                        double rawUpSpeed = motionDist * (double) (-MathHelper.sin(pitch)) * 0.04D;
                        e.set_y(e.get_y() + rawUpSpeed * 3.2D * upFactor.getValue());
                        e.set_x(e.get_x() - lookVec.x * rawUpSpeed / lookDist);
                        e.set_z(e.get_z() - lookVec.z * rawUpSpeed / lookDist);
                        mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());
                    } else if (mode.getValue() == Mode.Control && mc.player.input.jumping) {
                        if (motionDist > upFactor.getValue() / upFactor.getMax()) {
                            double rawUpSpeed = motionDist * 0.01325D;
                            e.set_y(e.get_y() + rawUpSpeed * 3.2D);
                            e.set_x(e.get_x() - lookVec.x * rawUpSpeed / lookDist);
                            e.set_z(e.get_z() - lookVec.z * rawUpSpeed / lookDist);
                            mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());
                        } else {
                            double[] dir = MovementUtility.forward(speed.getValue());
                            e.set_x(dir[0]);
                            e.set_z(dir[1]);
                            mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());
                        }
                    }

                    if (lookDist > 0.0D) {
                        e.set_x(e.get_x() + (lookVec.x / lookDist * motionDist - e.get_x()) * 0.1D);
                        e.set_z(e.get_z() + (lookVec.z / lookDist * motionDist - e.get_z()) * 0.1D);
                        mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());
                    }

                    if (mode.getValue() == Mode.Control && !mc.player.input.jumping) {
                        double[] dir = MovementUtility.forward(speed.getValue());
                        e.set_x(dir[0]);
                        e.set_z(dir[1]);
                        mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());
                    }

                    if (!noDrag.getValue()) {
                        e.set_y(e.get_y() * 0.9900000095367432D);
                        e.set_x(e.get_x() * 0.9800000190734863D);
                        e.set_z(e.get_z() * 0.9900000095367432D);
                        mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());
                    }

                    double finalDist = Math.sqrt(e.get_x() * e.get_x() + e.get_z() * e.get_z());

                    if (speedLimit.getValue() && finalDist > maxSpeed.getValue()) {
                        e.set_x(e.get_x() * maxSpeed.getValue() / finalDist);
                        e.set_z(e.get_z() * maxSpeed.getValue() / finalDist);
                        mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());
                    }

                } else {
                    float moveForward = mc.player.input.movementForward;

                    if (cruiseControl.getValue()) {
                        if (mc.player.input.jumping) {
                            height += upFactor.getValue() * 0.5;
                        } else if (mc.player.input.sneaking) {
                            height -= downFactor.getValue() * 0.5;
                        }

                        if (forceHeight.getValue()) {
                            height = manualHeight.getValue();
                        }

                        double horizSpeed = Thunderhack.playerManager.currentPlayerSpeed;
                        double horizPct = MathHelper.clamp(horizSpeed / 1.7, 0.0, 1.0);
                        double heightPct = 1 - Math.sqrt(horizPct);
                        double minAngle = 0.6;

                        if (horizSpeed >= minUpSpeed.getValue() && instantFlyTimer.passedMs((long) (2000 * packetDelay.getValue()))) {
                            double pitch = -((45 - minAngle) * heightPct + minAngle);

                            double diff = (height + 1 - mc.player.getY()) * 2;
                            double heightDiffPct = MathHelper.clamp(Math.abs(diff), 0.0, 1.0);
                            double pDist = -Math.toDegrees(Math.atan2(Math.abs(diff), horizSpeed * 30.0)) * Math.signum(diff);

                            double adjustment = (pDist - pitch) * heightDiffPct;

                            mc.player.setPitch((float) pitch);
                            mc.player.setPitch(mc.player.getPitch() + (float) adjustment);
                            mc.player.prevPitch = mc.player.getPitch();
                        } else {
                            mc.player.setPitch(0.25F);
                            mc.player.prevPitch = 0.25F;
                            moveForward = 1F;
                        }
                    }

                    Vec3d vec3d = mc.player.getRotationVec(mc.getTickDelta());

                    float f = mc.player.getPitch() * 0.017453292F;

                    double d6 = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
                    double d8 = Math.sqrt(e.get_x() * e.get_x() + e.get_z() * e.get_z());
                    double d1 = vec3d.length();
                    float f4 = MathHelper.cos(f);
                    f4 = (float) ((double) f4 * (double) f4 * Math.min(1.0D, d1 / 0.4D));

                    e.set_y(e.get_y() + (-0.08D + (double) f4 * 0.06D));

                    if (e.get_y() < 0.0D && d6 > 0.0D) {
                        double d2 = e.get_y() * -0.1D * (double) f4;
                        e.set_y(e.get_y() + d2);
                        e.set_x(e.get_x() + vec3d.x * d2 / d6);
                        e.set_z(e.get_z() + vec3d.z * d2 / d6);
                        mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());
                    }

                    if (f < 0.0F) {
                        double d10 = d8 * (double) (-MathHelper.sin(f)) * 0.04D;
                        e.set_y(e.get_y() + d10 * 3.2D);
                        e.set_x(e.get_x() - vec3d.x * d10 / d6);
                        e.set_z(e.get_z() - vec3d.z * d10 / d6);
                        mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());
                    }

                    if (d6 > 0.0D) {
                        e.set_x(e.get_x() + (vec3d.x / d6 * d8 - e.get_x()) * 0.1D);
                        e.set_z(e.get_z() + (vec3d.z / d6 * d8 - e.get_z()) * 0.1D);
                        mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());
                    }

                    if (!noDrag.getValue()) {
                        e.set_y(e.get_y() * 0.9900000095367432D);
                        e.set_x(e.get_x() * 0.9800000190734863D);
                        e.set_z(e.get_z() * 0.9900000095367432D);
                        mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());
                    }

                    float yaw = mc.player.getYaw() * 0.017453292F;

                    if (f > 0F && e.get_y() < 0D) {
                        if (moveForward != 0F && instantFlyTimer.passedMs((long) (2000 * packetDelay.getValue())) && staticTimer.passedMs((long) (1000 * staticDelay.getValue()))) {
                            if (stopMotion.getValue()) {
                                e.set_x(0);
                                e.set_z(0);
                            }
                            instantFlyTimer.reset();
                            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                        } else if (!instantFlyTimer.passedMs((long) (2000 * packetDelay.getValue()))) {
                            e.set_x(e.get_x() - moveForward * Math.sin(yaw) * factor.getValue() / 20F);
                            e.set_z(e.get_z() + moveForward * Math.cos(yaw) * factor.getValue() / 20F);
                            mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());
                            staticTimer.reset();
                        }
                    }

                    double finalDist = Math.sqrt(e.get_x() * e.get_x() + e.get_z() * e.get_z());

                    if (speedLimit.getValue() && finalDist > maxSpeed.getValue()) {
                        e.set_x(e.get_x() * maxSpeed.getValue() / finalDist);
                        e.set_z(e.get_z() * maxSpeed.getValue() / finalDist);
                        mc.player.setVelocity(e.get_x(), e.get_y(), e.get_z());
                    }
                }
            }
        }
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }


    public void useFireWork() {
        SearchInvResult fireworkResult = InventoryUtility.findInHotBar(stack -> stack.getItem() instanceof FireworkRocketItem);

        if (mc.player.getOffHandStack().getItem() == Items.FIREWORK_ROCKET) {
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, PlayerUtility.getWorldActionId(mc.world)));
        } else if (fireworkResult.found()) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(fireworkResult.slot()));
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtility.getWorldActionId(mc.world)));
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        }
    }

    public static void disabler(int elytra) {
        elytra = elytra >= 0 && elytra < 9 ? elytra + 36 : elytra;
        if (elytra != -2) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, elytra, 1, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 6, 1, SlotActionType.PICKUP, mc.player);
        }
        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        if (elytra != -2) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 6, 1, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, elytra, 1, SlotActionType.PICKUP, mc.player);
        }
        lastStartFalling = System.currentTimeMillis();
    }

    public static long lastStartFalling;

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) acceleration = 0;
    }

    public static void setSpeed(float speed) {
        float yaw = mc.player.getYaw();
        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;
        if (forward != 0.0F) {
            if (strafe > 0.0F) {
                yaw += (float) (forward > 0.0F ? -45 : 45);
            } else if (strafe < 0.0F) {
                yaw += (float) (forward > 0.0F ? 45 : -45);
            }

            strafe = 0.0F;
            if (forward > 0.0F) {
                forward = 1.0F;
            } else if (forward < 0.0F) {
                forward = -1.0F;
            }
        }

        double cos = Math.cos(Math.toRadians(yaw + 90.0F));
        double sin = Math.sin(Math.toRadians(yaw + 90.0F));
        mc.player.setVelocity((double) (forward * speed) * cos + (double) (strafe * speed) * sin, mc.player.getVelocity().y, (double) (forward * speed) * sin - (double) (strafe * speed) * cos);
    }

    public int getSlotIDFromItem(Item item) {
        for (ItemStack stack : mc.player.getArmorItems()) {
            if (stack.getItem() == item) {
                return -2;
            }
        }
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.getItem() == item) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }
}
