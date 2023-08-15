package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class Surround extends Module {
    private final Setting<Integer> actionShift = new Setting<>("PlacePerTick", 4, 1, 15);
    private final Setting<Integer> tickDelay = new Setting<>("Delay", 0, 0, 5);
    private final Setting<PlaceUtility.PlaceMode> placeMode = new Setting<>("Place Mode", PlaceUtility.PlaceMode.All);
    private final Setting<Boolean> crystalBreaker = new Setting<>("Destroy Crystal", false);
    private final Setting<Boolean> strict = new Setting<>("Strict", false);
    private final Setting<Boolean> center = new Setting<>("Center", true);
    private final Setting<Boolean> newBlocks = new Setting<>("1.16 Blocks", true);
    private final Setting<Boolean> allowAnchors = new Setting<>("Allow Anchors", false, (value) -> newBlocks.getValue());
    private final Setting<Parent> autoDisable = new Setting<>("Disable on", new Parent(false, 0));
    private final Setting<Boolean> disableOnYChange = new Setting<>("YChange", false).withParent(autoDisable);
    private final Setting<Boolean> disableOnTP = new Setting<>("TP", true).withParent(autoDisable);
    private final Setting<Boolean> disableWhenDone = new Setting<>("Done", false).withParent(autoDisable);

    private final Setting<Parent> render = new Setting<>("Render", new Parent(false, 0));
    private final Setting<ColorSetting> fillColor = new Setting<>("Fill Color", new ColorSetting(HudEditor.getColor(0))).withParent(render);
    private final Setting<ColorSetting> lineColor = new Setting<>("Line Color", new ColorSetting(HudEditor.getColor(0))).withParent(render);
    private final Setting<Integer> lineWidth = new Setting<>("Line Width", 2, 1, 5).withParent(render);

    public double prevY;
    private int offsetStep = 0;
    private int delayStep = 0;

    public static Timer inactivityTimer = new Timer();
    public static Timer breakTimer = new Timer();

    private final ConcurrentHashMap<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

    public Surround() {
        super("Surround", "окружает тебя обсой", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        inactivityTimer.reset();
        if (fullNullCheck()) {
            disable("NPE protection");
            return;
        }

        prevY = mc.player.getY();
        if (center.getValue()) {
            mc.player.updatePosition(MathHelper.floor(mc.player.getX()) + 0.5, mc.player.getY(), MathHelper.floor(mc.player.getZ()) + 0.5);
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
        }
    }


    public void onRender3D(MatrixStack stack) {
        renderPoses.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 500) {
                renderPoses.remove(pos);
            } else {
                Render3DEngine.drawFilledBox(stack, new Box(pos), Render2DEngine.injectAlpha(fillColor.getValue().getColorObject(), (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)))));
                Render3DEngine.drawBoxOutline(new Box(pos), lineColor.getValue().getColorObject(), lineWidth.getValue());
            }
        });
        handleSurround();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && disableOnTP.getValue())
            disable();
    }

    public void handleSurround() {
        if (fullNullCheck()) {
            disable("NPE protection");
            return;
        }

        if (disableOnYChange.getValue() && mc.player.getY() != prevY) {
            disable();
        }

        if (disableWhenDone.getValue() && inactivityTimer.passedMs(650)) {
            disable();
            return;
        }

        if (delayStep < tickDelay.getValue()) {
            delayStep++;
            return;
        } else {
            delayStep = 0;
        }

        int blocksPlaced = 0;

        List<BlockPos> abp = getNextPos();
        int maxSteps = abp.size();


        while (blocksPlaced < actionShift.getValue()) {
            if (offsetStep >= maxSteps) {
                offsetStep = 0;
                break;
            }

            BlockPos targetPos = abp.get(blocksPlaced);
            int slot = getSlot();

            if (slot == -1) {
                disable(MainSettings.isRu() ? "Нет блоков!" : "No blocks!");
                return;
            }

            if (crystalBreaker.getValue() && breakTimer.passedMs(100))
                for (Entity entity : mc.world.getOtherEntities(null, new Box(targetPos))) {
                    if (entity instanceof EndCrystalEntity) {
                        PlayerInteractEntityC2SPacket attackPacket = PlayerInteractEntityC2SPacket.attack(mc.player, mc.player.isSneaking());
                        AutoCrystal.changeId(attackPacket, entity.getId());
                        mc.player.networkHandler.sendPacket(attackPacket);
                        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                        entity.kill();
                        entity.setRemoved(Entity.RemovalReason.KILLED);
                        entity.onRemoved();
                        PlaceUtility.forcePlace(targetPos, false, Hand.MAIN_HAND, slot, false, placeMode.getValue());
                        breakTimer.reset();
                    }
                }

            if (PlaceUtility.place(targetPos, strict.getValue(), false, Hand.MAIN_HAND, slot, false, placeMode.getValue())) {
                renderPoses.put(targetPos, System.currentTimeMillis());
                PlaceUtility.ghostBlocks.put(targetPos, System.currentTimeMillis());
                blocksPlaced++;
                inactivityTimer.reset();
            }

            offsetStep++;
        }
    }

    private int getSlot() {
        List<Block> canUseBlocks = new ArrayList<>(List.of(Blocks.OBSIDIAN, Blocks.ENDER_CHEST));
        if (newBlocks.getValue()) {
            canUseBlocks.addAll(List.of(Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK));
            if (allowAnchors.getValue()) canUseBlocks.add(Blocks.RESPAWN_ANCHOR);
        }


        int slot = -1;

        final ItemStack mainhandStack = mc.player.getMainHandStack();
        if (mainhandStack != ItemStack.EMPTY && mainhandStack.getItem() instanceof BlockItem) {
            final Block blockFromMainhandItem = ((BlockItem) mainhandStack.getItem()).getBlock();
            if (canUseBlocks.contains(blockFromMainhandItem)) {
                slot = mc.player.getInventory().selectedSlot;
            }
        }

        if (slot == -1) {
            for (int i = 0; i < 9; i++) {
                final ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem) {
                    final Block blockFromItem = ((BlockItem) stack.getItem()).getBlock();
                    if (canUseBlocks.contains(blockFromItem)) {
                        slot = i;
                        break;
                    }
                }
            }
        }
        return slot;
    }

    private List<BlockPos> getNextPos() {
        Direction[] HORIZONTALS = new Direction[]{Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH};
        ArrayList<BlockPos> abp = new ArrayList<>();
        for (BlockPos bp2 : getBlocks(getPlayerPos())) {

            if (!strict.getValue())
                for (Direction enumFacing : HORIZONTALS) {
                    if (PlaceUtility.canPlaceBlock(bp2.offset(enumFacing).down(), false)) {
                        abp.add(bp2.offset(enumFacing).down());
                    }
                }

            for (Direction enumFacing : HORIZONTALS) {
                if (PlaceUtility.canPlaceBlock(bp2.offset(enumFacing), false)) {
                    abp.add(bp2.offset(enumFacing));
                }
            }
        }
        return abp;
    }

    private List<BlockPos> getBlocks(BlockPos center) {
        List<BlockPos> tempPos = new ArrayList<>();
        tempPos.add(center);
        tempPos.add(center.north());
        tempPos.add(center.north().east());
        tempPos.add(center.west());
        tempPos.add(center.west().north());
        tempPos.add(center.south());
        tempPos.add(center.south().west());
        tempPos.add(center.east());
        tempPos.add(center.east().south());

        List<BlockPos> tempPos2 = new ArrayList<>();

        for (BlockPos bp : tempPos) {
            if (!mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(bp)).isEmpty()) {
                tempPos2.add(bp);
            }
        }
        return tempPos2;
    }

    private BlockPos getPlayerPos() {
        return BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - Math.floor(mc.player.getY()) > 0.8 ? Math.floor(mc.player.getY()) + 1.0 : Math.floor(mc.player.getY()), mc.player.getZ());
    }
}
