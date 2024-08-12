package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.core.manager.player.PlayerManager;
import thunder.hack.events.impl.EventAttackBlock;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IInteractionManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.AutoCrystal;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.Objects;

public final class SpeedMine extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Packet);
    private final Setting<Boolean> avoidNetherrack = new Setting<>("AvoidNetherrack", false);
    private final Setting<StartMode> startMode = new Setting<>("StartMode", StartMode.StartAbort, v -> mode.getValue() == Mode.Packet);
    private final Setting<SwitchMode> switchMode = new Setting<>("SwitchMode", SwitchMode.Alternative, v -> mode.getValue() != Mode.Damage);
    private final Setting<Integer> swapDelay = new Setting<>("SwapDelay", 50, 0, 1000, v -> switchMode.getValue() == SwitchMode.Alternative && mode.getValue() != Mode.Damage);
    private final Setting<Float> factor = new Setting<>("Factor", 1f, 0.5f, 2f, v -> mode.getValue() != Mode.Damage);
    private final Setting<Float> rebreakfactor = new Setting<>("RebreakFactor", 7f, 0.5f, 20f, v -> mode.getValue() == Mode.GrimInstant);
    private final Setting<Float> speed = new Setting<>("Speed", 0.5f, 0f, 1f, v -> mode.getValue() == Mode.Damage);
    public final Setting<Float> range = new Setting<>("Range", 4.2f, 3.0f, 10.0f, v -> mode.getValue() != Mode.Damage);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false, v -> mode.getValue() != Mode.Damage);
    private final Setting<Boolean> placeCrystal = new Setting<>("PlaceCrystal", true, v -> mode.getValue() == Mode.GrimInstant);
    private final Setting<Boolean> resetOnSwitch = new Setting<>("ResetOnSwitch", true, v -> mode.getValue() != Mode.Damage);
    private final Setting<Integer> breakAttempts = new Setting<>("BreakAttempts", 10, 1, 50, v -> mode.getValue() == Mode.Packet);

    private final Setting<SettingGroup> packets = new Setting<>("Packets", new SettingGroup(false, 0), v -> mode.getValue() == Mode.Packet);
    private final Setting<Boolean> stop = new Setting<>("Stop", true, v -> mode.getValue() == Mode.Packet).addToGroup(packets);
    private final Setting<Boolean> abort = new Setting<>("Abort", true, v -> mode.getValue() == Mode.Packet).addToGroup(packets);
    private final Setting<Boolean> start = new Setting<>("Start", true, v -> mode.getValue() == Mode.Packet).addToGroup(packets);
    private final Setting<Boolean> stop2 = new Setting<>("Stop2", true, v -> mode.getValue() == Mode.Packet).addToGroup(packets);

    private final Setting<BooleanSettingGroup> render = new Setting<>("Render", new BooleanSettingGroup(false), v -> mode.getValue() != Mode.Damage);
    private final Setting<Boolean> smooth = new Setting<>("Smooth", true, v -> mode.getValue() != Mode.Damage).addToGroup(render);
    private final Setting<RenderMode> renderMode = new Setting<>("Render Mode", RenderMode.Shrink, v -> mode.getValue() != Mode.Damage).addToGroup(render);
    private final Setting<ColorSetting> startLineColor = new Setting<>("Start Line Color", new ColorSetting(new Color(255, 0, 0, 200)), v -> mode.getValue() != Mode.Damage).addToGroup(render);
    private final Setting<ColorSetting> endLineColor = new Setting<>("End Line Color", new ColorSetting(new Color(47, 255, 0, 200)), v -> mode.getValue() != Mode.Damage).addToGroup(render);
    private final Setting<Integer> lineWidth = new Setting<>("Line Width", 2, 1, 10, v -> mode.getValue() != Mode.Damage).addToGroup(render);
    private final Setting<ColorSetting> startFillColor = new Setting<>("Start Fill Color", new ColorSetting(new Color(255, 0, 0, 120)), v -> mode.getValue() != Mode.Damage).addToGroup(render);
    private final Setting<ColorSetting> endFillColor = new Setting<>("End Fill Color", new ColorSetting(new Color(47, 255, 0, 120)), v -> mode.getValue() != Mode.Damage).addToGroup(render);

    public static BlockPos minePosition;
    private Direction mineFacing;
    private int mineBreaks;
    public static float progress, prevProgress;

    private final Timer attackTimer = new Timer();

    public SpeedMine() {
        super("SpeedMine", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        reset();
    }

    @Override
    public void onEnable() {
        reset();
    }

    @Override
    public void onUpdate() {
        if (mc.player == null
                || mc.world == null
                || mc.interactionManager == null
                || mc.player.getAbilities().creativeMode) return;

        if (mode.getValue() == Mode.Damage) {
            if (((IInteractionManager) mc.interactionManager).getCurBlockDamageMP() < speed.getValue())
                ((IInteractionManager) mc.interactionManager).setCurBlockDamageMP(speed.getValue());

        } else if (mode.getValue() == Mode.Packet) {
            if (minePosition != null) {
                if (mineBreaks >= breakAttempts.getValue() || PlayerUtility.squaredDistanceFromEyes(minePosition.toCenterPos()) > range.getPow2Value()) {
                    reset();
                    return;
                }
                if (progress == 0 && !mc.world.isAir(minePosition) && attackTimer.passedMs(800)) {
                    mc.interactionManager.attackBlock(minePosition, mineFacing);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    attackTimer.reset();
                }
            }

            if (minePosition != null && !mc.world.isAir(minePosition)) {
                int invPickSlot = getTool(minePosition);
                int hotBarPickSlot = InventoryUtility.getPickAxeHotbar().slot();
                int prevSlot = -1;

                if (invPickSlot == -1 && switchMode.getValue() == SwitchMode.Alternative) return;
                if (hotBarPickSlot == -1 && switchMode.getValue() != SwitchMode.Alternative) return;

                if (progress >= 1) {
                    if (placeCrystal.getValue())
                        placeCrystal();

                    if (switchMode.getValue() == SwitchMode.Alternative) {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invPickSlot < 9 ? invPickSlot + 36 : invPickSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                        closeScreen();
                    } else if (switchMode.getValue() == SwitchMode.Normal || switchMode.getValue() == SwitchMode.Silent) {
                        prevSlot = mc.player.getInventory().selectedSlot;
                        InventoryUtility.getPickAxeHotbar().switchTo();
                    }

                    if (stop.getValue())
                        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));
                    if (abort.getValue())
                        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, minePosition, mineFacing));
                    if (start.getValue())
                        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, minePosition, mineFacing));
                    if (stop2.getValue())
                        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));

                    if (switchMode.getValue() == SwitchMode.Alternative) {
                        if (swapDelay.getValue() != 0) {
                            Managers.ASYNC.run(() -> {
                                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invPickSlot < 9 ? invPickSlot + 36 : invPickSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                                closeScreen();
                            }, swapDelay.getValue());
                        } else {
                            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invPickSlot < 9 ? invPickSlot + 36 : invPickSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                            closeScreen();
                        }
                    } else if (switchMode.getValue() == SwitchMode.Silent) {
                        InventoryUtility.switchTo(prevSlot);
                    }

                    mc.interactionManager.breakBlock(minePosition);

                    progress = 0;
                    mineBreaks++;
                }
                prevProgress = progress;
                progress += getBlockStrength(mc.world.getBlockState(minePosition), minePosition);
            } else {
                progress = 0;
                prevProgress = 0;
            }

            if (!mode.getValue().equals(Mode.Damage)) {
                if (rotate.getValue() && progress > 0.95 && minePosition != null && mc.player != null) {
                    float[] angle = PlayerManager.calcAngle(mc.player.getEyePos(), minePosition.toCenterPos());
                    ModuleManager.rotations.fixRotation = angle[0];
                }
            }
        } else if (mode.getValue() == Mode.GrimInstant) {
            if (minePosition != null) {
                if (PlayerUtility.squaredDistanceFromEyes(minePosition.toCenterPos()) > range.getPow2Value()) {
                    reset();
                    return;
                }
            }

            if (minePosition != null) {
                if (mc.world.isAir(minePosition))
                    return;

                int invPickSlot = getTool(minePosition);
                int hotBarPickSlot = InventoryUtility.getPickAxeHotbar().slot();
                int prevSlot = -1;

                if (invPickSlot == -1 && switchMode.getValue() == SwitchMode.Alternative) return;
                if (hotBarPickSlot == -1 && switchMode.getValue() != SwitchMode.Alternative) return;

                if (progress >= 1) {
                    if (placeCrystal.getValue())
                        placeCrystal();

                    if (switchMode.getValue() == SwitchMode.Alternative) {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invPickSlot < 9 ? invPickSlot + 36 : invPickSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                        closeScreen();
                    } else if (switchMode.getValue() == SwitchMode.Normal || switchMode.getValue() == SwitchMode.Silent) {
                        prevSlot = mc.player.getInventory().selectedSlot;
                        InventoryUtility.getPickAxeHotbar().switchTo();
                    }

                    sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));

                    if (switchMode.getValue() == SwitchMode.Alternative) {
                        if (swapDelay.getValue() != 0) {
                            Managers.ASYNC.run(() -> {
                                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invPickSlot < 9 ? invPickSlot + 36 : invPickSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                                closeScreen();
                            }, swapDelay.getValue());
                        } else {
                            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invPickSlot < 9 ? invPickSlot + 36 : invPickSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                            closeScreen();
                        }
                    } else if (switchMode.getValue() == SwitchMode.Silent) {
                        InventoryUtility.switchTo(prevSlot);
                    }

                    progress = 0;
                    mineBreaks++;

                    if (placeCrystal.getValue())
                        placeCrystal();
                }
                prevProgress = progress;
                progress += getBlockStrength(mc.world.getBlockState(minePosition), minePosition) * (mineBreaks >= 1 ? rebreakfactor.getValue() : 1);
            } else {
                progress = 0;
                prevProgress = 0;
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack stack) {

        if (mode.getValue() == Mode.Damage
                || mc.world == null
                || minePosition == null
                || mc.world.isAir(minePosition))
            return;

        switch (renderMode.getValue()) {
            case Shrink -> {
                Box shrunkMineBox = new Box(minePosition.getX(), minePosition.getY(), minePosition.getZ(), minePosition.getX(), minePosition.getY(), minePosition.getZ());
                float noom = (float) MathUtility.clamp(Render2DEngine.interpolate(prevProgress, progress, Render3DEngine.getTickDelta()), 0f, 1f);

                Render3DEngine.FILLED_QUEUE.add(
                        new Render3DEngine.FillAction(
                                shrunkMineBox.shrink(noom, noom, noom).offset(0.5 + noom * 0.5, 0.5 + noom * 0.5, 0.5 + noom * 0.5),
                                getColor(startFillColor.getValue().getColorObject(), endFillColor.getValue().getColorObject(), progress)
                        )
                );

                Render3DEngine.OUTLINE_QUEUE.add(
                        new Render3DEngine.OutlineAction(
                                shrunkMineBox.shrink(noom, noom, noom).offset(0.5 + noom * 0.5, 0.5 + noom * 0.5, 0.5 + noom * 0.5),
                                getColor(startLineColor.getValue().getColorObject(), endLineColor.getValue().getColorObject(), progress),
                                lineWidth.getValue()
                        )
                );
            }
            case Grow -> {
                float noom = (float) MathUtility.clamp(Render2DEngine.interpolate(prevProgress, progress, Render3DEngine.getTickDelta()), 0f, 1f);
                Box shrunkMineBox = new Box(minePosition.getX(), minePosition.getY(), minePosition.getZ(), minePosition.getX() + 1, minePosition.getY() + noom, minePosition.getZ() + 1);

                Render3DEngine.FILLED_QUEUE.add(
                        new Render3DEngine.FillAction(
                                shrunkMineBox,
                                getColor(startFillColor.getValue().getColorObject(), endFillColor.getValue().getColorObject(), progress)
                        )
                );

                Render3DEngine.OUTLINE_QUEUE.add(
                        new Render3DEngine.OutlineAction(
                                shrunkMineBox,
                                getColor(startLineColor.getValue().getColorObject(), endLineColor.getValue().getColorObject(), progress),
                                lineWidth.getValue()
                        )
                );
            }
            case Block -> {
                Box renderBox = new Box(minePosition);

                Render3DEngine.FILLED_QUEUE.add(
                        new Render3DEngine.FillAction(
                                renderBox,
                                getColor(startFillColor.getValue().getColorObject(), endFillColor.getValue().getColorObject(), progress)
                        )
                );
                Render3DEngine.OUTLINE_QUEUE.add(
                        new Render3DEngine.OutlineAction(
                                renderBox,
                                getColor(startLineColor.getValue().getColorObject(), endLineColor.getValue().getColorObject(), progress),
                                lineWidth.getValue()
                        )
                );
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onAttackBlock(@NotNull EventAttackBlock event) {
        if (mc.player != null
                && canBreak(event.getBlockPos())
                && !mc.player.getAbilities().creativeMode
                && (mode.getValue() == Mode.Packet || mode.getValue() == Mode.GrimInstant)
                && !event.getBlockPos().equals(minePosition)) {
            addBlockToMine(event.getBlockPos(), event.getEnumFacing(), true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    @SuppressWarnings("unused")
    private void onSync(EventSync event) {
        if (rotate.getValue() && progress > 0.95 && minePosition != null && mc.player != null) {
            float[] angle = Managers.PLAYER.calcAngle(mc.player.getEyePos(), minePosition.toCenterPos().add(0, -0.25f, 0));

            mc.player.setYaw(angle[0]);
            mc.player.setPitch(angle[1]);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPacketSend(PacketEvent.@NotNull SendPost e) {
        if (e.getPacket() instanceof UpdateSelectedSlotC2SPacket && resetOnSwitch.getValue() && !switchMode.is(SwitchMode.Silent) && !mode.is(Mode.GrimInstant)) {
            addBlockToMine(minePosition, mineFacing, true);
        }
    }

    private void reset() {
        minePosition = null;
        mineFacing = null;
        progress = 0;
        mineBreaks = 0;
        prevProgress = 0;
    }

    private void closeScreen() {
        if (mc.player == null) return;

        sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
    }

    private float getBlockStrength(@NotNull BlockState state, BlockPos position) {
        if (state == Blocks.AIR.getDefaultState()) {
            return 0.02f;
        }

        float hardness = state.getHardness(mc.world, position);

        if (hardness < 0)
            return 0;
        return getDigSpeed(state, position) / hardness / (canBreak(position) ? 30f : 100f);
    }

    private float getDestroySpeed(BlockPos position, BlockState state) {
        float destroySpeed = 1;
        int slot = getTool(position);

        if (mc.player == null)
            return 0;
        if (slot != -1 && mc.player.getInventory().getStack(slot) != null && !mc.player.getInventory().getStack(slot).isEmpty()) {
            destroySpeed *= mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(state);
        }

        return destroySpeed;
    }

    public float getDigSpeed(BlockState state, BlockPos position) {
        if (mc.player == null) return 0;
        float digSpeed = getDestroySpeed(position, state);

        if (digSpeed > 1) {
            int slot = getTool(position);
            if (slot != -1) {
                ItemStack itemstack = mc.player.getInventory().getStack(slot);
                int efficiencyModifier = EnchantmentHelper.getLevel(mc.world.getRegistryManager().get(Enchantments.EFFICIENCY.getRegistryRef()).getEntry(Enchantments.EFFICIENCY).get(), itemstack);
                if (efficiencyModifier > 0 && !itemstack.isEmpty()) {
                    digSpeed += (float) (StrictMath.pow(efficiencyModifier, 2) + 1);
                }
            }
        }

        if (mc.player.hasStatusEffect(StatusEffects.HASTE))
            digSpeed *= 1 + (Objects.requireNonNull(mc.player.getStatusEffect(StatusEffects.HASTE)).getAmplifier() + 1) * 0.2F;


        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE))
            digSpeed *= (float) Math.pow(0.3f, Objects.requireNonNull(mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE)).getAmplifier() + 1);


        if (mc.player.isSubmergedInWater())
            digSpeed *= (float) mc.player.getAttributeInstance(EntityAttributes.PLAYER_SUBMERGED_MINING_SPEED).getValue();

        if (!mc.player.isOnGround()) digSpeed /= 5;

        return digSpeed < 0 ? 0 : digSpeed * factor.getValue();
    }

    private int getTool(final BlockPos pos) {
        int index = -1;
        float currentFastest = 1.f;

        if (mc.world == null
                || mc.player == null
                || mc.world.getBlockState(pos).getBlock() instanceof AirBlock)
            return -1;

        for (int i = 9; i < 45; ++i) {
            final ItemStack stack = mc.player.getInventory().getStack(i >= 36 ? i - 36 : i);

            if (stack != ItemStack.EMPTY) {
                if (!(stack.getMaxDamage() - stack.getDamage() > 10))
                    continue;

                final float digSpeed = EnchantmentHelper.getLevel(mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.EFFICIENCY).get(), stack);
                final float destroySpeed = stack.getMiningSpeedMultiplier(mc.world.getBlockState(pos));

                if (digSpeed + destroySpeed > currentFastest) {
                    currentFastest = digSpeed + destroySpeed;
                    index = i;
                }
            }
        }

        return index >= 36 ? index - 36 : index;
    }

    private boolean canBreak(BlockPos pos) {
        if (mc.world == null)
            return false;

        final BlockState blockState = mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();
        return block.getHardness() != -1;
    }

    private @NotNull Color getColor(@NotNull Color startColor, @NotNull Color endColor, float progress) {
        if (!smooth.getValue())
            return progress >= 0.95 ? endColor : startColor;

        final int rDiff = endColor.getRed() - startColor.getRed();
        final int gDiff = endColor.getGreen() - startColor.getGreen();
        final int bDiff = endColor.getBlue() - startColor.getBlue();
        final int aDiff = endColor.getAlpha() - startColor.getAlpha();

        return new Color(
                fixColorValue(startColor.getRed() + (int) (rDiff * progress)),
                fixColorValue(startColor.getGreen() + (int) (gDiff * progress)),
                fixColorValue(startColor.getBlue() + (int) (bDiff * progress)),
                fixColorValue(startColor.getAlpha() + (int) (aDiff * progress)));
    }

    public void placeCrystal() {
        if (AutoCrystal.target == null)
            return;

        AutoCrystal.PlaceData data = getCevData();

        if (data == null)
            data = getBestData();

        if (data != null) {
            ModuleManager.autoCrystal.placeCrystal(data.bhr(), true, false);
            debug("placing..");
            ModuleManager.autoTrap.pause();
            ModuleManager.breaker.pause();
        }
    }

    public AutoCrystal.PlaceData getCevData() {
        if (mc.world.isAir(minePosition.down())) {
            if (ExplosionUtility.getSelfExplosionDamage(minePosition.toCenterPos().add(0,0.5,0), 0, false) > ModuleManager.autoCrystal.maxSelfDamage.getValue())
                return null;

            return ModuleManager.autoCrystal.getPlaceData(minePosition, null, mc.player.getPos());
        }

        return null;
    }

    public AutoCrystal.PlaceData getBestData() {
        BlockState prevState = mc.world.getBlockState(minePosition);
        mc.world.setBlockState(minePosition, Blocks.AIR.getDefaultState());

        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP || dir == Direction.DOWN) continue;
            if (ExplosionUtility.getSelfExplosionDamage(minePosition.down().offset(dir).toCenterPos().add(0,0.5,0), 0, false) > ModuleManager.autoCrystal.maxSelfDamage.getValue())
                continue;

            AutoCrystal.PlaceData autoMineData = ModuleManager.autoCrystal.getPlaceData(minePosition.down().offset(dir), null, mc.player.getPos());
            if (autoMineData != null) {
                mc.world.setBlockState(minePosition, prevState);
                return autoMineData;
            }
        }


        float selfDmg = ExplosionUtility.getSelfExplosionDamage(minePosition.toCenterPos().add(0,0.5,0), 0, false);
        mc.world.setBlockState(minePosition, prevState);

        AutoCrystal.PlaceData autoMineData = ModuleManager.autoCrystal.getPlaceData(minePosition, null, mc.player.getPos());
        if (selfDmg > ModuleManager.autoCrystal.maxSelfDamage.getValue())
            return null;

        return autoMineData;
    }

    private int fixColorValue(int colorVal) {
        return colorVal > 255 ? 255 : Math.max(colorVal, 0);
    }

    public void addBlockToMine(BlockPos pos, @Nullable Direction facing, boolean allowReMine) {
        if (!allowReMine && (minePosition != null || progress != 0))
            return;
        if (mc.player == null)
            return;

        progress = 0;
        mineBreaks = 0;
        minePosition = pos;
        mineFacing = facing == null ? mc.player.getHorizontalFacing() : facing;

        if (pos != null && mineFacing != null) {
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, mineFacing));
            sendPacket(new PlayerActionC2SPacket(startMode.getValue() == StartMode.StartAbort ? PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK : PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));
        }
    }

    public boolean isBlockDrop(Entity ent) {
        return isOn() && minePosition != null && minePosition.toCenterPos().squaredDistanceTo(ent.getPos()) <= 1f && ent.age < 3;
    }

    public enum Mode {
        Packet,
        GrimInstant,
        Damage
    }

    public enum RenderMode {
        Block,
        Shrink,
        Grow
    }

    public enum SwitchMode {
        Silent,
        Normal,
        Alternative
    }

    public enum StartMode {
        StartAbort,
        StartStop
    }
}
