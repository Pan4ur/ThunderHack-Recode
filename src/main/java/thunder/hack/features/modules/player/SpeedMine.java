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
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.core.manager.player.PlayerManager;
import thunder.hack.events.impl.EventAttackBlock;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.AutoCrystal;
import thunder.hack.injection.accesors.IInteractionManager;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.world.ExplosionUtility;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

public final class SpeedMine extends Module {
    public SpeedMine() {
        super("SpeedMine", Category.PLAYER);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Packet);
    public final Setting<Boolean> doubleMine = new Setting<>("DoubleMine", false);
    private final Setting<StartMode> startMode = new Setting<>("StartMode", StartMode.StartAbort, v -> mode.is(Mode.Packet) && !doubleMine.getValue());
    private final Setting<SwitchMode> switchMode = new Setting<>("SwitchMode", SwitchMode.Alternative, v -> mode.not(Mode.Damage));
    private final Setting<Integer> swapDelay = new Setting<>("SwapDelay", 50, 0, 1000, v -> mode.getValue() != Mode.Damage);
    private final Setting<Float> factor = new Setting<>("Factor", 1f, 0.5f, 2f, v -> mode.getValue() != Mode.Damage);
    private final Setting<Float> speed = new Setting<>("Speed", 0.5f, 0f, 1f, v -> mode.getValue() == Mode.Damage);
    public final Setting<Float> range = new Setting<>("Range", 4.2f, 3.0f, 10.0f, v -> mode.getValue() != Mode.Damage);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false, v -> mode.getValue() != Mode.Damage);
    private final Setting<Boolean> placeCrystal = new Setting<>("PlaceCrystal", true);
    private final Setting<Boolean> resetOnSwitch = new Setting<>("ResetOnSwitch", true, v -> mode.getValue() != Mode.Damage);
    private final Setting<Integer> breakAttempts = new Setting<>("BreakAttempts", 10, 1, 50, v -> mode.getValue() == Mode.Packet);
    private final Setting<Boolean> pauseEat = new Setting<>("Pause On Eat", false);
    private final Setting<Boolean> clientRemove = new Setting<>("ClientRemove", true);

    private final Setting<SettingGroup> packets = new Setting<>("Packets", new SettingGroup(false, 0), v -> mode.is(Mode.Packet) && !doubleMine.getValue());
    private final Setting<Boolean> stop = new Setting<>("Stop", true, v -> mode.is(Mode.Packet) && !doubleMine.getValue()).addToGroup(packets);
    private final Setting<Boolean> abort = new Setting<>("Abort", true, v -> mode.is(Mode.Packet) && !doubleMine.getValue()).addToGroup(packets);
    private final Setting<Boolean> start = new Setting<>("Start", true, v -> mode.is(Mode.Packet) && !doubleMine.getValue()).addToGroup(packets);
    private final Setting<Boolean> stop2 = new Setting<>("Stop2", true, v -> mode.is(Mode.Packet) && !doubleMine.getValue()).addToGroup(packets);

    private final Setting<BooleanSettingGroup> render = new Setting<>("Render", new BooleanSettingGroup(false), v -> mode.getValue() != Mode.Damage);
    private final Setting<Boolean> smooth = new Setting<>("Smooth", true, v -> mode.getValue() != Mode.Damage).addToGroup(render);
    private final Setting<RenderMode> renderMode = new Setting<>("Render Mode", RenderMode.Shrink, v -> mode.getValue() != Mode.Damage).addToGroup(render);
    private final Setting<ColorSetting> startLineColor = new Setting<>("Start Line Color", new ColorSetting(new Color(255, 0, 0, 200)), v -> mode.getValue() != Mode.Damage).addToGroup(render);
    private final Setting<ColorSetting> endLineColor = new Setting<>("End Line Color", new ColorSetting(new Color(47, 255, 0, 200)), v -> mode.getValue() != Mode.Damage).addToGroup(render);
    private final Setting<Integer> lineWidth = new Setting<>("Line Width", 2, 1, 10, v -> mode.getValue() != Mode.Damage).addToGroup(render);
    private final Setting<ColorSetting> startFillColor = new Setting<>("Start Fill Color", new ColorSetting(new Color(255, 0, 0, 120)), v -> mode.getValue() != Mode.Damage).addToGroup(render);
    private final Setting<ColorSetting> endFillColor = new Setting<>("End Fill Color", new ColorSetting(new Color(47, 255, 0, 120)), v -> mode.getValue() != Mode.Damage).addToGroup(render);


    public ArrayList<MineAction> actions = new ArrayList<>();

    @Override
    public void onDisable() {
        actions.forEach(MineAction::reset);
        actions.clear();
    }

    @Override
    public void onEnable() {
        actions.forEach(MineAction::reset);
        actions.clear();
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck() || mc.player.getAbilities().creativeMode)
            return;

        if (PlayerUtility.isEating() && pauseEat.getValue()) return;

        if (mode.getValue() == Mode.Damage)
            if (((IInteractionManager) mc.interactionManager).getCurBlockDamageMP() < speed.getValue())
                ((IInteractionManager) mc.interactionManager).setCurBlockDamageMP(speed.getValue());

        actions.removeIf(MineAction::update);
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (mode.is(Mode.Damage) || mc.world == null)
            return;

        actions.forEach(a -> {
            if (!mc.world.isAir(a.getPos())) {
                float noom = (float) MathUtility.clamp(Render2DEngine.interpolate(a.getPrevProgress(), a.getProgress(), Render3DEngine.getTickDelta()), 0f, 1f);
                Box renderBox =

                        switch (renderMode.getValue()) {
                            case Block -> new Box(a.getPos());
                            case Grow ->
                                    new Box(a.getPos().getX(), a.getPos().getY(), a.getPos().getZ(), a.getPos().getX() + 1, a.getPos().getY() + noom, a.getPos().getZ() + 1);
                            case Shrink ->
                                    new Box(a.getPos().getX(), a.getPos().getY(), a.getPos().getZ(), a.getPos().getX(), a.getPos().getY(), a.getPos().getZ())
                                            .shrink(noom, noom, noom)
                                            .offset(0.5 + noom * 0.5, 0.5 + noom * 0.5, 0.5 + noom * 0.5);
                        };

                Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(
                        renderBox,
                        Render2DEngine.getColor(startFillColor.getValue().getColorObject(), endFillColor.getValue().getColorObject(), a.getProgress(), smooth.getValue())
                ));

                Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(
                        renderBox,
                        Render2DEngine.getColor(startLineColor.getValue().getColorObject(), endLineColor.getValue().getColorObject(), a.getProgress(), smooth.getValue()),
                        lineWidth.getValue()
                ));
            }
        });
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onAttackBlock(@NotNull EventAttackBlock event) {
        if (fullNullCheck() || !canBreak(event.getBlockPos()) || mc.player.getAbilities().creativeMode || mode.is(Mode.Damage))
            return;

        if (!alreadyActing(event.getBlockPos())) {
            if (!doubleMine.getValue() || actions.size() >= 2) {
                if (!actions.isEmpty())
                    actions.removeFirst().cancel();
            }

            actions.add(new MineAction(event.getBlockPos(), event.getEnumFacing()));
        }

        event.cancel();
    }

    public boolean alreadyActing(BlockPos blockPos) {
        return actions.stream().anyMatch(a -> a.pos.equals(blockPos));
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOW)
    private void onSync(EventSync event) {
        actions.forEach(MineAction::onSync);
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPacketSend(PacketEvent.@NotNull SendPost e) {
        if (e.getPacket() instanceof UpdateSelectedSlotC2SPacket && resetOnSwitch.getValue() && !switchMode.is(SwitchMode.Silent) && !mode.is(Mode.GrimInstant))
            actions.forEach(MineAction::reset);
    }

    private void closeScreen() {
        if (mc.player == null) return;

        sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
    }

    public float getBlockStrength(@NotNull BlockState state, BlockPos position) {
        if (state == Blocks.AIR.getDefaultState())
            return 0.02f;

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

        if (!mc.player.isOnGround() && ModuleManager.freeCam.isDisabled())
            digSpeed /= 5;

        return digSpeed < 0 ? 0 : digSpeed * factor.getValue();
    }

    public int getTool(final BlockPos pos) {
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
        if (mc.world == null || PlayerUtility.squaredDistanceFromEyes(pos.toCenterPos()) > range.getPow2Value())
            return false;

        final BlockState blockState = mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();
        return block.getHardness() != -1;
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

    public AutoCrystal.@Nullable PlaceData getCevData() {

        for (MineAction action : actions) {
            if (mc.world.isAir(action.getPos().down())) {
                if (ExplosionUtility.getSelfExplosionDamage(action.getPos().toCenterPos().add(0, 0.5, 0), 0, false) > ModuleManager.autoCrystal.maxSelfDamage.getValue())
                    return null;

                return ModuleManager.autoCrystal.getPlaceData(action.getPos(), null, mc.player.getPos());
            }
        }
        return null;
    }

    public AutoCrystal.@Nullable PlaceData getBestData() {
        for (MineAction action : actions) {
            BlockState prevState = mc.world.getBlockState(action.getPos());
            mc.world.setBlockState(action.getPos(), Blocks.AIR.getDefaultState());

            for (Direction dir : Direction.values()) {
                if (dir == Direction.UP || dir == Direction.DOWN) continue;
                if (ExplosionUtility.getSelfExplosionDamage(action.getPos().down().offset(dir).toCenterPos().add(0, 0.5, 0), 0, false) > ModuleManager.autoCrystal.maxSelfDamage.getValue())
                    continue;

                AutoCrystal.PlaceData autoMineData = ModuleManager.autoCrystal.getPlaceData(action.getPos().down().offset(dir), null, mc.player.getPos());
                if (autoMineData != null) {
                    mc.world.setBlockState(action.getPos(), prevState);
                    return autoMineData;
                }
            }

            float selfDmg = ExplosionUtility.getSelfExplosionDamage(action.getPos().toCenterPos().add(0, 0.5, 0), 0, false);
            mc.world.setBlockState(action.getPos(), prevState);

            AutoCrystal.PlaceData autoMineData = ModuleManager.autoCrystal.getPlaceData(action.getPos(), null, mc.player.getPos());
            if (selfDmg > ModuleManager.autoCrystal.maxSelfDamage.getValue())
                continue;

            return autoMineData;
        }

        return null;
    }

    public boolean isBlockDrop(Entity ent) {
        if (ent instanceof ItemEntity && isOn() && ent.age < 3)
            for (MineAction a : actions)
                if (a.getPos().toCenterPos().squaredDistanceTo(ent.getPos()) <= 1f)
                    return true;

        return false;
    }

    public class MineAction {
        @NotNull
        private final BlockPos pos;
        private float progress, prevProgress;

        private int mineBreaks;

        private final Timer attackTimer = new Timer();

        public MineAction(@NotNull BlockPos pos, Direction direction) {
            this.pos = pos;
            progress = 0;
            mineBreaks = 0;
            start(direction);
        }

        public void start(Direction direction) {
            Direction startDirection = direction == null ? mc.player.getHorizontalFacing() : direction;

            if (startDirection != null)
                if (doubleMine.getValue()) {
                    sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, startDirection));
                    sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, startDirection));
                    sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, startDirection));
                } else {
                    sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, startDirection));
                    sendPacket(new PlayerActionC2SPacket(startMode.getValue() == StartMode.StartAbort ? PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK : PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, startDirection));
                }
        }

        public boolean update() {
            Direction dir = InteractionUtility.getStrictDirections(pos).stream().findFirst().orElse(mc.player.getHorizontalFacing());

            if (mineBreaks >= breakAttempts.getValue() && mode.not(Mode.GrimInstant))
                return true;

            if (PlayerUtility.squaredDistanceFromEyes(pos.toCenterPos()) > range.getPow2Value()) {
                cancel();
                return true;
            }

            if (mc.world.isAir(pos)) {
                progress = 0;
                prevProgress = -1;
                return false;
            }

            if (progress == 0 && prevProgress == -1 && mode.is(Mode.Packet) && attackTimer.every(800)) {
                start(dir);
                mc.player.swingHand(Hand.MAIN_HAND);
            }

            int pickSlot = getTool(pos);
            int prevSlot = mc.player.getInventory().selectedSlot;

            if (pickSlot == -1)
                return false;

            boolean instant = mineBreaks > 0 && mode.is(Mode.GrimInstant);

            if (progress >= 1 || instant) {
                if (placeCrystal.getValue())
                    placeCrystal();

                switchTo(pickSlot, -1);

                if (mode.getValue() == Mode.GrimInstant || doubleMine.getValue()) {
                    sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, dir));
                } else {
                    if (stop.getValue())
                        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, dir));
                    if (abort.getValue())
                        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, dir));
                    if (start.getValue())
                        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, dir));
                    if (stop2.getValue())
                        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, dir));
                }

                if (clientRemove.getValue())
                    mc.interactionManager.breakBlock(pos);

                int delay = doubleMine.getValue() ? 100 : swapDelay.getValue();

                if (delay != 0)
                    Managers.ASYNC.run(() -> switchTo(prevSlot, pickSlot), delay);
                else
                    switchTo(prevSlot, pickSlot);

                mineBreaks++;

                progress = prevProgress = 0;

                if (doubleMine.getValue() && mode.is(Mode.GrimInstant) && actions.size() >= 2)
                    return true;
            } else {
                prevProgress = progress;
                progress += getBlockStrength(mc.world.getBlockState(pos), pos);
            }

            fixMovement();

            return false;
        }

        private void switchTo(int slot, int from) {
            if (switchMode.getValue() == SwitchMode.Alternative || slot >= 9) {
                if (from == -1)
                    clickSlot(slot < 9 ? slot + 36 : slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP);
                else
                    clickSlot(from < 9 ? from + 36 : from, mc.player.getInventory().selectedSlot, SlotActionType.SWAP);
                closeScreen();
            } else if (switchMode.is(SwitchMode.Silent)) InventoryUtility.switchToSilent(slot);
            else InventoryUtility.switchTo(slot);
        }

        public void fixMovement() {
            if (rotate.getValue() && progress > 0.95)
                ModuleManager.rotations.fixRotation = PlayerManager.calcAngle(mc.player.getEyePos(), pos.toCenterPos())[0];
        }

        public BlockPos getPos() {
            return pos;
        }

        public float getPrevProgress() {
            return prevProgress;
        }

        public float getProgress() {
            return progress;
        }

        public void onSync() {
            if (rotate.getValue() && progress > 0.95) {
                float[] angle = PlayerManager.calcAngle(mc.player.getEyePos(), pos.toCenterPos().add(0, -0.25f, 0));
                mc.player.setYaw(angle[0]);
                mc.player.setPitch(angle[1]);
            }
        }

        public void reset() {
            if (progress == 0)
                return;

            prevProgress = progress = 0;
            Direction dir = InteractionUtility.getStrictDirections(pos).stream().findFirst().orElse(mc.player.getHorizontalFacing());
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, dir));
            start(dir);
        }

        public void cancel() {
            if (progress != 0)
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.DOWN));
        }

        public boolean instantBreaking() {
            return mineBreaks > 0 && mode.is(Mode.GrimInstant);
        }
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
