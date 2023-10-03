package thunder.hack.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
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
import thunder.hack.ThunderHack;
import thunder.hack.core.PlayerManager;
import thunder.hack.events.impl.EventAttackBlock;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IInteractionManager;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class SpeedMine extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Packet);
    public final Setting<SwitchMode> switchMode = new Setting<>("SwitchMode", SwitchMode.Alternative, v -> mode.getValue() == Mode.Packet);
    private final Setting<Float> startDmg = new Setting<>("StartDmg", 0f, 0f, 1f, v -> mode.getValue() == Mode.Damage);
    private final Setting<Float> finishDmg = new Setting<>("FinishDmg", 1f, 0f, 1f, v -> mode.getValue() == Mode.Damage);
    private final Setting<Float> range = new Setting<>("Range", 4.2f, 3.0f, 10.0f, v -> mode.getValue() == Mode.Packet);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false, v -> mode.getValue() == Mode.Packet);
    private final Setting<Boolean> resetOnSwitch = new Setting<>("ResetOnSwitch", true, v -> mode.getValue() == Mode.Packet);
    private final Setting<Integer> breakAttempts = new Setting<>("BreakAttempts", 10, 1, 50, v -> mode.getValue() == Mode.Packet);
    private final Setting<Parent> packets = new Setting<>("Packets", new Parent(false, 0), v -> mode.getValue() == Mode.Packet);
    private final Setting<Boolean> stop = new Setting<>("Stop", true, v -> mode.getValue() == Mode.Packet).withParent(packets);
    private final Setting<Boolean> abort = new Setting<>("Abort", true, v -> mode.getValue() == Mode.Packet).withParent(packets);
    private final Setting<Boolean> start = new Setting<>("Start", true, v -> mode.getValue() == Mode.Packet).withParent(packets);
    private final Setting<Boolean> stop2 = new Setting<>("Stop2", true, v -> mode.getValue() == Mode.Packet).withParent(packets);
    private final Setting<Parent> render = new Setting<>("Render", new Parent(false, 0), v -> mode.getValue() == Mode.Packet);
    private final Setting<RenderMode> renderMode = new Setting<>("Render Mode", RenderMode.Shrink, v -> mode.getValue() == Mode.Packet).withParent(render);
    private final Setting<ColorSetting> startLineColor = new Setting<>("Start Line Color", new ColorSetting(new Color(255, 0, 0, 200)), v -> mode.getValue() == Mode.Packet).withParent(render);
    private final Setting<ColorSetting> endLineColor = new Setting<>("End Line Color", new ColorSetting(new Color(47, 255, 0, 200)), v -> mode.getValue() == Mode.Packet).withParent(render);
    private final Setting<Integer> lineWidth = new Setting<>("Line Width", 2, 1, 10, v -> mode.getValue() == Mode.Packet).withParent(render);
    private final Setting<ColorSetting> startFillColor = new Setting<>("Start Fill Color", new ColorSetting(new Color(255, 0, 0, 120)), v -> mode.getValue() == Mode.Packet).withParent(render);
    private final Setting<ColorSetting> endFillColor = new Setting<>("End Fill Color", new ColorSetting(new Color(47, 255, 0, 120)), v -> mode.getValue() == Mode.Packet).withParent(render);

    public static BlockPos minePosition;
    private Direction mineFacing;
    private int mineBreaks;
    public static float progress, prevProgress;
    public boolean worth = false;

    private Timer attackTimer = new Timer();

    public SpeedMine() {
        super("SpeedMine", "SpeedMine", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (!mc.player.getAbilities().creativeMode) {
            if (mode.getValue() == Mode.Damage) {
                if (((IInteractionManager) mc.interactionManager).getCurBlockDamageMP() < startDmg.getValue())
                    ((IInteractionManager) mc.interactionManager).setCurBlockDamageMP(startDmg.getValue());
                if (((IInteractionManager) mc.interactionManager).getCurBlockDamageMP() >= finishDmg.getValue())
                    ((IInteractionManager) mc.interactionManager).setCurBlockDamageMP(1f);

            } else if (mode.getValue() == Mode.Packet) {
                if (minePosition != null) {
                    if (mineBreaks >= breakAttempts.getValue() || mc.player.squaredDistanceTo(minePosition.toCenterPos()) > range.getPow2Value()) {
                        minePosition = null;
                        mineFacing = null;
                        progress = 0;
                        mineBreaks = 0;
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
                    int hotbarPickSlot = InventoryUtility.getPickAxeHotbar().slot();
                    int prevSlot = -1;

                    if (invPickSlot == -1 && switchMode.getValue() == SwitchMode.Alternative) return;
                    if (hotbarPickSlot == -1 && switchMode.getValue() != SwitchMode.Alternative) return;

                    if (progress >= 1) {
                        if (switchMode.getValue() == SwitchMode.Alternative) {
                            if (invPickSlot < 9) {
                                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 30, invPickSlot, SlotActionType.SWAP, mc.player);
                                closeScreen();
                            }
                            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 30, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                            closeScreen();
                        } else if (switchMode.getValue() == SwitchMode.Normal || switchMode.getValue() == SwitchMode.Silent) {
                            prevSlot = mc.player.getInventory().selectedSlot;
                            InventoryUtility.getPickAxeHotbar().switchTo();
                        }

                        if(stop.getValue())
                            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));
                        if(abort.getValue())
                            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, minePosition, mineFacing));
                        if(start.getValue())
                            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, minePosition, mineFacing));
                        if(stop2.getValue())
                            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, minePosition, mineFacing));

                        if (switchMode.getValue() == SwitchMode.Alternative) {
                            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 30, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                            closeScreen();
                            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 30, invPickSlot, SlotActionType.SWAP, mc.player);
                            closeScreen();
                        } else if (switchMode.getValue() == SwitchMode.Silent) {
                            InventoryUtility.switchTo(prevSlot);
                        }

                        progress = 0;
                        mineBreaks++;
                    }
                    prevProgress = progress;
                    progress += getBlockStrength(mc.world.getBlockState(minePosition), minePosition);
                } else {
                    progress = 0;
                    prevProgress = 0;
                }
            }
        }
    }

    public void closeScreen() {
        sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
    }

    public boolean checkWorth() {
        if (isDisabled()
                || mode.getValue() != Mode.Packet
                || minePosition == null
                || progress < 0.95
                || mc.world.getBlockState(minePosition).getBlock() != Blocks.OBSIDIAN
        ) return false;

        for (PlayerEntity pl : ThunderHack.asyncManager.getAsyncPlayers()) {
            if (pl == null) continue;
            if (pl == mc.player) continue;
            mc.world.removeBlock(minePosition, false);
            float dmg = ExplosionUtility.getExplosionDamage1(minePosition.toCenterPos(), pl);
            mc.world.setBlockState(minePosition, Blocks.OBSIDIAN.getDefaultState());
            if (ThunderHack.friendManager.isFriend(pl.getEntityName())) continue;
            if (dmg > 7.5f) return true;
        }

        return false;
    }

    public boolean isWorth() {
        return worth;
    }

    public float getBlockStrength(BlockState state, BlockPos position) {
        float hardness = state.getHardness(mc.world, position);
        if (hardness < 0) return 0;
        return getDigSpeed(state, position) / hardness / (canBreak(position) ? 30f : 100f);
    }

    public float getDestroySpeed(BlockPos position, BlockState state) {
        float destroySpeed = 1;
        int slot = getTool(position);
        if (slot != -1 && mc.player.getInventory().getStack(slot) != null && !mc.player.getInventory().getStack(slot).isEmpty()) {
            destroySpeed *= mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(state);
        }
        return destroySpeed;
    }

    public float getDigSpeed(BlockState state, BlockPos position) {
        float digSpeed = getDestroySpeed(position, state);
        if (digSpeed > 1) {
            ItemStack itemstack = mc.player.getInventory().getStack(getTool(position));
            int efficiencyModifier = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemstack);
            if (efficiencyModifier > 0 && !itemstack.isEmpty()) {
                digSpeed += (float) (StrictMath.pow(efficiencyModifier, 2) + 1);
            }
        }
        if (mc.player.hasStatusEffect(StatusEffects.HASTE)) {
            digSpeed *= 1 + (mc.player.getStatusEffect(StatusEffects.HASTE).getAmplifier() + 1) * 0.2F;
        }
        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float fatigueScale;
            switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> fatigueScale = 0.3F;
                case 1 -> fatigueScale = 0.09F;
                case 2 -> fatigueScale = 0.0027F;
                default -> fatigueScale = 8.1E-4F;
            }

            digSpeed *= fatigueScale;
        }
        if (mc.player.isSubmergedInWater() && !EnchantmentHelper.hasAquaAffinity(mc.player)) {
            digSpeed /= 5;
        }
        if (!mc.player.isOnGround()) {
            digSpeed /= 5;
        }
        return (digSpeed < 0 ? 0 : digSpeed);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.SendPost e) {
        if (e.getPacket() instanceof UpdateSelectedSlotC2SPacket && resetOnSwitch.getValue()) {
            progress = 0;
            prevProgress = 0;
        }
    }

    @Override
    public void onDisable() {
        minePosition = null;
        mineFacing = null;
        progress = 0;
        mineBreaks = 0;
        prevProgress = 0;
    }

    @Override
    public void onEnable() {
        minePosition = null;
        mineFacing = null;
        progress = 0;
        mineBreaks = 0;
        prevProgress = 0;
    }

    public void onRender3D(MatrixStack stack) {
        worth = checkWorth();

        if (mode.getValue() == Mode.Packet) {
            if (minePosition != null && !mc.world.isAir(minePosition)) {
                switch (renderMode.getValue()) {
                    case Shrink -> {
                        Box shrunkMineBox = new Box(minePosition.getX(), minePosition.getY(), minePosition.getZ(), minePosition.getX(), minePosition.getY(), minePosition.getZ());
                        float noom = (float) MathUtility.clamp(Render2DEngine.interpolate(prevProgress, progress, mc.getTickDelta()), 0f, 1f);

                        Render3DEngine.drawFilledBox(
                                stack,
                                shrunkMineBox.shrink(noom, noom, noom).offset(0.5 + noom * 0.5, 0.5 + noom * 0.5, 0.5 + noom * 0.5),
                                progress >= 0.95 ? endFillColor.getValue().getColorObject() : startFillColor.getValue().getColorObject()
                        );

                        Render3DEngine.drawBoxOutline(
                                shrunkMineBox.shrink(noom, noom, noom).offset(0.5 + noom * 0.5, 0.5 + noom * 0.5, 0.5 + noom * 0.5),
                                progress >= 0.95 ? endLineColor.getValue().getColorObject() : startLineColor.getValue().getColorObject(),
                                lineWidth.getValue()
                        );
                    }
                    case Block -> {
                        Box renderBox = new Box(minePosition);

                        Render3DEngine.drawFilledBox(
                                stack,
                                renderBox,
                                progress >= 0.95 ? endFillColor.getValue().getColorObject() : startFillColor.getValue().getColorObject()
                        );

                        Render3DEngine.drawBoxOutline(
                                renderBox,
                                progress >= 0.95 ? endLineColor.getValue().getColorObject() : startLineColor.getValue().getColorObject(),
                                lineWidth.getValue()
                        );
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAttackBlock(EventAttackBlock event) {
        if (canBreak(event.getBlockPos()) && !mc.player.getAbilities().creativeMode) {
            if (mode.getValue() == Mode.Packet) {
                if (!event.getBlockPos().equals(minePosition)) {
                    minePosition = event.getBlockPos();
                    mineFacing = event.getEnumFacing();
                    progress = 0;
                    mineBreaks = 0;
                    if (minePosition != null && mineFacing != null) {
                        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, minePosition, mineFacing));
                        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, minePosition, mineFacing));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntitySync(EventSync event) {
        if (rotate.getValue()) {
            if (progress > 0.95) {
                if (minePosition != null) {
                    float[] angle = PlayerManager.calcAngle(mc.player.getEyePos(), minePosition.toCenterPos());
                    mc.player.setYaw(angle[0]);
                    mc.player.setPitch(angle[1]);
                }
            }
        }
    }

    private int getTool(final BlockPos pos) {
        int index = -1;
        float CurrentFastest = 1.0f;
        if (mc.world.getBlockState(pos).getBlock() instanceof AirBlock) return -1;

        for (int i = 9; i < 45; ++i) {
            final ItemStack stack = mc.player.getInventory().getStack(i >= 36 ? i - 36 : i);
            if (stack != ItemStack.EMPTY) {
                if (!(mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getMaxDamage() - mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getDamage() > 10))
                    continue;
                final float digSpeed = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
                final float destroySpeed = stack.getMiningSpeedMultiplier(mc.world.getBlockState(pos));
                if (digSpeed + destroySpeed > CurrentFastest) {
                    CurrentFastest = digSpeed + destroySpeed;
                    index = i;
                }
            }
        }
        return index >= 36 ? index - 36 : index;
    }

    private boolean canBreak(BlockPos pos) {
        final BlockState blockState = mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();
        return block.getHardness() != -1;
    }

    public enum Mode {
        Packet,
        Damage
    }

    public enum RenderMode {
        Block,
        Shrink
    }

    public enum SwitchMode {
        Silent,
        Normal,
        None,
        Alternative
    }
}
