package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.RedstoneBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.EventEntityRemoved;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static thunder.hack.modules.client.MainSettings.isRu;

public class PistonAura extends Module {

    public PistonAura() {
        super("PistonAura", "Поршни вталкивают-кристал в чела-(Охуенная хуйня)", Category.COMBAT);
    }

    public Setting<Integer> actionInterval = new Setting<>("ActionInterval", 1, 0, 25);
    public Setting<Integer> actionShift = new Setting<>("ActionShift", 3, 1, 4);
    public Setting<patterns> patternsSetting = new Setting<>("Pattern", patterns.All);
    protected Setting<Boolean> supportPlace = (new Setting<>("SupportPlace", false));
    public Setting<Boolean> bypass = new Setting<>("FireBypass", false);
    protected Setting<Boolean> strictDirection = (new Setting<>("StrictDirection", false));
    public Setting<Boolean> trap = new Setting<>("Trap", false);
    public Setting<Float> targetRange = new Setting<>("Target Range", 10.0f, 0.0f, 20.0f);
    public Setting<Float> placeRange = new Setting<>("PlaceRange", 10.0f, 1.0f, 20.0f);
    public Setting<Float> wallRange = new Setting<>("WallRange", 10.0f, 1.0f, 20.0f);
    private final Setting<PlaceUtility.PlaceMode> placeMode = new Setting<>("Place Mode", PlaceUtility.PlaceMode.All);

    public PlayerEntity target;
    private BlockPos targetPos, pistonPos, crystalPos, redStonePos, firePos, pistonHeadPos;
    private boolean builtTrap, isFire;

    private final Timer trapTimer = new Timer();
    private final Timer attackTimer = new Timer();

    private int tickCounter = 0;
    private Runnable postAction = null;
    private Stage stage = Stage.Searching;
    private EndCrystalEntity lastCrystal;
    private Vec3d rotations;


    public void reset() {
        builtTrap = false;
        target = null;
        stage = Stage.Searching;
        trapTimer.reset();
        attackTimer.reset();
        rotations = Vec3d.ZERO;
        pistonPos = null;
        targetPos = null;
        firePos = null;
        pistonHeadPos = null;
        lastCrystal = null;
        tickCounter = 0;
        trapTimer.reset();
        attackTimer.reset();
        postAction = null;
    }

    public enum patterns {
        Small,
        Cross,
        Liner,
        All
    }

    @Override
    public void onEnable() {
        reset();
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (tickCounter < actionInterval.getValue()) tickCounter++;

        if (stage == Stage.Break) {
            if (isFire) {
                reset();
                return;
            }
            breakCrystal();
            return;
        }

        if (tickCounter < actionInterval.getValue()) return;

        handlePistonAura(false);
    }

    @EventHandler
    public void onPostSync(EventPostSync event) {
        if (postAction != null) {
            tickCounter = 0;
            postAction.run();
            postAction = null;
            int extraBlocks = 1;
            while (extraBlocks < actionShift.getValue()) {
                handlePistonAura(true);
                if (postAction != null) {
                    postAction.run();
                    postAction = null;
                } else {
                    return;
                }
                extraBlocks++;
            }
        }
        postAction = null;
    }


    public void handlePistonAura(boolean extra) {
        if(!InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN).found() && (trap.getValue() || supportPlace.getValue())){
            disable(isRu() ? "Нет обсидиана!" : "No obsidian!");
            return;
        }

        if(!InventoryUtility.findBlockInHotBar(Blocks.REDSTONE_BLOCK).found() && !InventoryUtility.findBlockInHotBar(Blocks.REDSTONE_TORCH).found()){
            disable(isRu() ? "Нет редстоуна!" : "No redstone!");
            return;
        }

        if(!InventoryUtility.findItemInHotBar(Items.END_CRYSTAL).found() && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL){
            disable(isRu() ? "Нет кристаллов!" : "No crystals!");
            return;
        }

        if(!(InventoryUtility.findItemInHotBar(Items.PISTON).found() || InventoryUtility.findItemInHotBar(Items.STICKY_PISTON).found())){
            disable(isRu() ? "Нет поршней!" : "No pistons!");
            return;
        }

        switch (stage) {
            case Searching -> {
                findPos();
                stage = Stage.Trap;
            }
            case Trap -> {
                if (!trap.getValue()) {
                    stage = Stage.Piston;
                    return;
                }
                if (mc.world.getBlockState(targetPos.add(0, 2, 0)).getBlock() == Blocks.OBSIDIAN || pistonPos.getY() >= targetPos.add(0, 2, 0).getY()) {
                    stage = Stage.Piston;
                    return;
                }

                if (!builtTrap) {
                    final BlockPos offset = new BlockPos(crystalPos.getX() - targetPos.getX(), 0, crystalPos.getZ() - targetPos.getZ());
                    final BlockPos trapBase = targetPos.add(offset.getX() * -1, 0, offset.getZ() * -1);

                    List<BlockPos> trapPos = new ArrayList<>();
                    trapPos.add(targetPos.add(0, 2, 0));
                    trapPos.add(trapBase.add(0, 2, 0));
                    trapPos.add(trapBase.add(0, 1, 0));

                    for (BlockPos bp : trapPos) {
                        if (PlaceUtility.place(bp, true, strictDirection.getValue(), Hand.MAIN_HAND, InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN).slot(), false, placeMode.getValue())) {
                            if (bp == targetPos.add(0, 2, 0)) {
                                builtTrap = true;
                                stage = Stage.Piston;
                            }
                            break;
                        }
                    }
                }
            }
            case Piston -> {
                if (pistonPos == null) {
                    stage = Stage.Searching;
                    return;
                }

                if (pistonHeadPos == null) {
                    stage = Stage.Searching;
                    return;
                }

                if (mc.world.getBlockState(pistonPos).getBlock() instanceof PistonBlock) {
                    stage = isFire ? Stage.Fire : Stage.Crystal;
                }

                if (mc.world.getBlockState(pistonPos.down()).isReplaceable() && supportPlace.getValue()) {
                    PlaceUtility.forcePlace(pistonPos.down(), strictDirection.getValue(), Hand.MAIN_HAND, InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN).slot(), false);
                    return;
                }

                final float[] angle = PlaceUtility.calcAngle(pistonPos, strictDirection.getValue(), true);
                if (angle == null) return;
                if (extra) {
                    Thunderhack.placeManager.rotate(angle[0], angle[1]);
                } else {
                    mc.player.setYaw(angle[0]);
                    mc.player.setPitch(angle[1]);
                }


                postAction = () -> {
                    int piston_slot;
                    if (!InventoryUtility.findBlockInHotBar(Blocks.PISTON).found()) {
                        if (!InventoryUtility.findBlockInHotBar(Blocks.STICKY_PISTON).found()) {
                            disable(isRu() ? "Нет поршней!" : "No pistons!");
                            return;
                        } else {
                            piston_slot = InventoryUtility.findBlockInHotBar(Blocks.STICKY_PISTON).slot();
                        }
                    } else {
                        piston_slot = InventoryUtility.findBlockInHotBar(Blocks.PISTON).slot();
                    }


                    final float angle2 = PlaceUtility.calculateAngle(pistonHeadPos.toCenterPos(), pistonPos.toCenterPos())[0];

                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(angle2, 0, mc.player.isOnGround()));
                    float prevYaw = mc.player.getYaw();
                    mc.player.setYaw(angle2);
                    mc.player.prevYaw = angle2;
                    ((IClientPlayerEntity) mc.player).setLastYaw(angle2);
                    int prevSlot = mc.player.getInventory().selectedSlot;
                    PlaceUtility.forcePlace(pistonPos, strictDirection.getValue(), Hand.MAIN_HAND, piston_slot, false);
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
                    mc.player.getInventory().selectedSlot = prevSlot;
                    mc.player.setYaw(prevYaw);

                    stage = isFire ? Stage.Fire : Stage.Crystal;
                };
            }
            case Fire -> {
                if (firePos == null) {
                    stage = Stage.Searching;
                    return;
                }

                if (mc.world.getBlockState(firePos).getBlock() instanceof FireBlock) {
                    stage = Stage.Crystal;
                }

                BlockPos support = firePos.down();
                if (mc.world.getBlockState(support).isReplaceable() && supportPlace.getValue()) {
                    PlaceUtility.forcePlace(support, strictDirection.getValue(), Hand.MAIN_HAND, InventoryUtility.findHotbarBlock(Blocks.OBSIDIAN), false);
                    return;
                }

                float[] angle = PlaceUtility.calcAngle(firePos, strictDirection.getValue(), true);
                if (angle == null) return;
                if (extra) {
                    Thunderhack.placeManager.rotate(angle[0], angle[1]);
                } else {
                    mc.player.setYaw(angle[0]);
                    mc.player.setPitch(angle[1]);
                }
                postAction = () -> {
                    PlaceUtility.forcePlace(firePos, strictDirection.getValue(), Hand.MAIN_HAND, InventoryUtility.getItemSlotHotbar(Items.FLINT_AND_STEEL), false);
                    stage = Stage.Crystal;
                };
            }
            case Crystal -> {
                if (crystalPos == null) {
                    stage = Stage.Searching;
                    return;
                }

                if (mc.world.getBlockState(crystalPos).isReplaceable() && supportPlace.getValue()) {
                    PlaceUtility.forcePlace(crystalPos, strictDirection.getValue(), Hand.MAIN_HAND, InventoryUtility.findHotbarBlock(Blocks.OBSIDIAN), false);
                    return;
                }

                BlockHitResult result = handlePlaceRotation(crystalPos);
                if (result == null) return;
                float[] angle = PlaceUtility.calculateAngle(rotations);
                if (extra) {
                    Thunderhack.placeManager.rotate(angle[0] + MathUtility.random(-0.2f, 0.2f), angle[1]);
                } else {
                    mc.player.setYaw(angle[0] + MathUtility.random(-0.2f, 0.2f));
                    mc.player.setPitch(angle[1]);
                }

                postAction = () -> {
                    boolean offHand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
                    int prev_slot = -1;
                    if(!offHand) {
                        int crystal_slot = InventoryUtility.findItemInHotBar(Items.END_CRYSTAL).slot();
                        prev_slot = mc.player.getInventory().selectedSlot;
                        if (crystal_slot != -1) {
                            mc.player.getInventory().selectedSlot = crystal_slot;
                            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(crystal_slot));
                        }
                    }
                    mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(offHand ? Hand.OFF_HAND : Hand.MAIN_HAND, result, PlayerUtility.getWorldActionId(mc.world)));
                    mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(offHand ? Hand.OFF_HAND : Hand.MAIN_HAND));

                    if(!offHand) {
                        mc.player.getInventory().selectedSlot = prev_slot;
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prev_slot));
                    }

                    stage = Stage.RedStone;
                };
            }
            case RedStone -> {
                if (redStonePos == null) {
                    stage = Stage.Searching;
                    return;
                }

                if (mc.world.getBlockState(redStonePos).getBlock() instanceof RedstoneBlock) {
                    stage = Stage.Break;
                }

                BlockPos support = redStonePos.down();
                if (mc.world.getBlockState(support).isReplaceable() && supportPlace.getValue()) {
                    PlaceUtility.forcePlace(support, strictDirection.getValue(), Hand.MAIN_HAND, InventoryUtility.getItemSlotHotbar(Items.OBSIDIAN), false);
                }
                float[] angle = PlaceUtility.calcAngle(redStonePos, strictDirection.getValue(), true);
                if (angle == null) return;
                if (extra) {
                    Thunderhack.placeManager.rotate(angle[0], angle[1]);
                } else {
                    mc.player.setYaw(angle[0]);
                    mc.player.setPitch(angle[1]);
                }

                postAction = () -> {
                    int redstone_slot = -1;
                    if (InventoryUtility.findHotbarBlock(Blocks.REDSTONE_BLOCK) == -1) {
                        if (InventoryUtility.findHotbarBlock(Blocks.REDSTONE_TORCH) == -1) {
                            disable(isRu() ? "Нет редстоуна!" : "No redstone!");
                        } else {
                            redstone_slot = InventoryUtility.findHotbarBlock(Blocks.REDSTONE_TORCH);
                        }
                    } else {
                        redstone_slot = InventoryUtility.findHotbarBlock(Blocks.REDSTONE_BLOCK);
                    }
                    PlaceUtility.forcePlace(redStonePos, strictDirection.getValue(), Hand.MAIN_HAND, redstone_slot, false);
                    stage = Stage.Break;
                };
            }
            case Break -> {
                if (isFire)
                    stage = Stage.Searching;
            }
        }
    }

    private BlockHitResult handlePlaceRotation(BlockPos pos) {
        Vec3d eyesPos = PlaceUtility.getEyesPos(((mc.player)));

        if (strictDirection.getValue()) {
            Vec3d closestPoint = null;
            Direction closestDirection = null;
            double closestDistance = 999D;

            for (Vec3d point : PlaceUtility.multiPoint) {
                Vec3d p = new Vec3d(pos.getX() + point.getX(), pos.getY() + point.getY(), pos.getZ() + point.getZ());
                double dist = p.distanceTo(eyesPos);
                if ((dist < closestDistance && closestDirection == null)) {
                    closestPoint = p;
                    closestDistance = dist;
                }

                BlockHitResult result = mc.world.raycast(new RaycastContext(eyesPos, p, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ((mc.player))));

                if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
                    double visDist = result.getPos().distanceTo(eyesPos);
                    if (closestDirection == null || visDist < closestDistance) {
                        closestDirection = result.getSide();
                        closestDistance = visDist;
                        closestPoint = result.getPos();
                    }
                }
            }

            if (closestPoint != null) {
                rotations = closestPoint;
                return new BlockHitResult(closestPoint, closestDirection == null ? Direction.getFacing(eyesPos.x - closestPoint.x, eyesPos.y - closestPoint.y, eyesPos.z - closestPoint.z) : closestDirection, pos, false);
            }

            return null;
        }
        rotations = new Vec3d(pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D);
        return new BlockHitResult(new Vec3d(pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D), Direction.UP, pos, false);
    }


    public void breakCrystal() {
        for (Entity ent : mc.world.getEntities()) {
            if (!(ent instanceof EndCrystalEntity) || target.squaredDistanceTo(ent.getPos()) > 16 || ent.age < 2)
                continue;
            float[] angle = PlaceUtility.calculateAngle(ent.getPos());
            mc.player.setYaw(angle[0] + MathUtility.random(-3f, 3f));
            mc.player.setPitch(angle[1]);
            if (attackTimer.passedMs(200)) {
                mc.interactionManager.attackEntity(mc.player, ent);
                mc.player.swingHand(Hand.MAIN_HAND);
                attackTimer.reset();
            }
            lastCrystal = (EndCrystalEntity) ent;
        }
    }


    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlaySoundS2CPacket && ((PlaySoundS2CPacket) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS) && ((PlaySoundS2CPacket) event.getPacket()).getSound().value().equals(SoundEvents.ENTITY_GENERIC_EXPLODE)) {
            if (lastCrystal == null || !lastCrystal.isAlive())
                return;
            double soundRange = lastCrystal.squaredDistanceTo(((PlaySoundS2CPacket) event.getPacket()).getX() + 0.5, ((PlaySoundS2CPacket) event.getPacket()).getY() + 0.5, ((PlaySoundS2CPacket) event.getPacket()).getZ() + 0.5);
            if (soundRange > 121)
                return;
            reset();
        }

        if (event.getPacket() instanceof PlaySoundFromEntityS2CPacket && ((PlaySoundFromEntityS2CPacket) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS) && ((PlaySoundFromEntityS2CPacket) event.getPacket()).getSound().value().equals(SoundEvents.ENTITY_GENERIC_EXPLODE)) {
            if (lastCrystal == null || !lastCrystal.isAlive())
                return;
            if (((PlaySoundFromEntityS2CPacket) event.getPacket()).getEntityId() != lastCrystal.getId())
                return;
            reset();
        }
    }


    @EventHandler
    public void onEntityRemove(EventEntityRemoved e) {
        if (lastCrystal != null && lastCrystal == e.entity)
            reset();
    }


    @Override
    public String getDisplayInfo() {
        return stage.toString();
    }


    public void onRender3D(MatrixStack stack) {
        if (pistonPos == null || crystalPos == null || redStonePos == null) {
            return;
        }
        Render3DEngine.drawFilledBox(stack, new Box(pistonHeadPos.down()), Render2DEngine.injectAlpha(Color.CYAN, 100));
        Render3DEngine.drawFilledBox(stack, new Box(crystalPos), Render2DEngine.injectAlpha(Color.PINK, 100));
        Render3DEngine.drawFilledBox(stack, new Box(pistonPos.down()), Render2DEngine.injectAlpha(Color.GREEN, 100));
        Render3DEngine.drawFilledBox(stack, new Box(redStonePos.down()), Render2DEngine.injectAlpha(Color.RED, 100));
        if (firePos != null)
            Render3DEngine.drawFilledBox(stack, new Box(firePos.down()), Render2DEngine.injectAlpha(Color.yellow, 100));
    }


    private void findPos() {
        ArrayList<Structure> list = new ArrayList<>();
        for (PlayerEntity target : Objects.requireNonNull(getPlayersSorted(targetRange.getValue()))) {
            for (int i = 0; i <= 2; i++) {
                if (patternsSetting.getValue() == patterns.Small || patternsSetting.getValue() == patterns.All) {
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(1, i, -1),//piston
                            new BlockPos(0, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(1, 0, -2)},//redstone
                            new BlockPos[]{new BlockPos(0, 0, 1), new BlockPos(1, i, 1), new BlockPos(0, 1 + i, 1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(1, i, 1),//piston
                            new BlockPos(0, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(1, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, -1), new BlockPos(1, i, -1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(1, i, 1),//piston
                            new BlockPos(1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(2, i, 1)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 1), new BlockPos(-1, i, 0), new BlockPos(-1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(-1, i, 1),//piston
                            new BlockPos(-1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(-2, i, 1)},//redstone
                            new BlockPos[]{new BlockPos(1, i, 1), new BlockPos(1, i, 0), new BlockPos(1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-1, i, 1),//piston
                            new BlockPos(0, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(-1, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, -1), new BlockPos(0, i, -1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-1, i, -1),//piston
                            new BlockPos(0, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(-1, i, -2)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 1), new BlockPos(0, i, 1), new BlockPos(0, 1 + i, 1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, -1),//crystal
                            new BlockPos(-1, i, -1),//piston
                            new BlockPos(-1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(-2, i, -1)},//redstone
                            new BlockPos[]{new BlockPos(1, i, 0), new BlockPos(1, i, -1), new BlockPos(1, i + 1, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, -1),//crystal
                            new BlockPos(1, i, -1),//piston
                            new BlockPos(1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(2, i, -1)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 0), new BlockPos(-1, i, -1), new BlockPos(-1, 1 + i, 0)})//fire
                    );
                }
                if (patternsSetting.getValue() == patterns.Cross || patternsSetting.getValue() == patterns.All) {
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(2, i, -1),//piston
                            new BlockPos(1, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(3, i, -1), new BlockPos(2, i, -2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, -1), new BlockPos(1, i, 1), new BlockPos(0, i, 1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(2, i, 1),//piston
                            new BlockPos(1, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(3, i, 1), new BlockPos(2, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, 1), new BlockPos(1, i, -1), new BlockPos(0, i, -1), new BlockPos(0, 1 + i, -1), new BlockPos(0, 1 + i, 1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(1, i, 2),//piston
                            new BlockPos(1, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(1, i, 3), new BlockPos(2, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(1, i, 0), new BlockPos(-1, i, 0), new BlockPos(-1, i, 1), new BlockPos(-1, 1 + i, 0), new BlockPos(1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(-1, i, 2),//piston
                            new BlockPos(-1, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(-1, i, 3), new BlockPos(-2, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 0), new BlockPos(1, i, 1), new BlockPos(1, i, 0), new BlockPos(-1, 1 + i, 0), new BlockPos(1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-2, i, 1),//piston
                            new BlockPos(-1, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(-3, i, 1), new BlockPos(-2, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, 1), new BlockPos(-1, i, -1), new BlockPos(0, i, -1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-2, i, -1),//piston
                            new BlockPos(-1, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(-3, i, -1), new BlockPos(-2, i, -2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, -1), new BlockPos(0, i, -1), new BlockPos(-1, i, 1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, -1),//crystal
                            new BlockPos(-1, i, -2),//piston
                            new BlockPos(-1, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(-1, i, -3), new BlockPos(-2, i, -2)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 0), new BlockPos(1, i, 0), new BlockPos(1, i, -1), new BlockPos(-1, 1 + i, 0), new BlockPos(1, 1 + i, 0)})//fire
                    );
                }
                if (patternsSetting.getValue() == patterns.Liner || patternsSetting.getValue() == patterns.All) {
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(2, i, 0),//piston
                            new BlockPos(1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(3, i, 0)},//redstone
                            new BlockPos[]{new BlockPos(1, i, 1), new BlockPos(1, i, -1), new BlockPos(0, i, 1),
                                    new BlockPos(0, i, -1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-2, i, 0),//piston
                            new BlockPos(-1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(-3, i, 0)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, -1), new BlockPos(-1, i, 1), new BlockPos(0, i, 1),
                                    new BlockPos(0, i, -1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(0, i, 2),//piston
                            new BlockPos(0, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(0, i, 3)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 1), new BlockPos(1, i, 1), new BlockPos(-1, i, 0),
                                    new BlockPos(1, i, 0), new BlockPos(1, 1 + i, 0), new BlockPos(-1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, -1),//crystal
                            new BlockPos(0, i, -2),//piston
                            new BlockPos(0, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(0, i, -3)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, -1), new BlockPos(1, i, -1), new BlockPos(-1, i, 0),
                                    new BlockPos(1, i, 0), new BlockPos(1, 1 + i, 0), new BlockPos(-1, 1 + i, 0)})//fire
                    );
                }
            }
            this.target = target;
        }


        if (bypass.getValue() && InventoryUtility.findItemInHotBar(Items.FLINT_AND_STEEL).found()) {
            List<Structure> bestStructure = list.stream().filter(Structure::isFirePa).sorted(Comparator.comparingDouble(Structure::getMaxRange)).collect(Collectors.toList());
            if (bestStructure.size() == 0) {
                isFire = false;
                bestStructure = list.stream().filter(Structure::isNormalPa).sorted(Comparator.comparingDouble(Structure::getMaxRange)).collect(Collectors.toList());
                if (!bestStructure.isEmpty()) {
                    Structure structure = bestStructure.get(0);
                    pistonPos = structure.getPistonPos();
                    crystalPos = structure.getCrystalPos();
                    redStonePos = structure.getRedstonePos();
                    pistonHeadPos = structure.getPistonHeadPos();
                    targetPos = structure.targetPos;
                    target = structure.getTarget();
                } else {
                    disable(isRu() ? "Нет цели или цель не в холке!" : "No target or target is not in hole!");
                }
            } else {
                isFire = true;
                Structure structure = bestStructure.get(0);
                pistonPos = structure.getPistonPos();
                crystalPos = structure.getCrystalPos();
                redStonePos = structure.getRedstonePos();
                firePos = structure.getFirePos();
                pistonHeadPos = structure.getPistonHeadPos();
                targetPos = structure.targetPos;
                target = structure.getTarget();
            }
        } else {
            isFire = false;
            List<Structure> bestStructure = list.stream().filter(Structure::isNormalPa).sorted(Comparator.comparingDouble(Structure::getMaxRange)).toList();
            if (!bestStructure.isEmpty()) {
                Structure structure = bestStructure.get(0);
                pistonPos = structure.getPistonPos();
                crystalPos = structure.getCrystalPos();
                redStonePos = structure.getRedstonePos();
                pistonHeadPos = structure.getPistonHeadPos();
                targetPos = structure.targetPos;
                target = structure.getTarget();
            } else {
                disable(isRu() ? "Нет цели или цель не в холке!" : "No target or target is not in hole!");
            }
        }
    }


    public static List<PlayerEntity> getPlayersSorted(float range) {
        synchronized (mc.world.getPlayers()) {
            List<PlayerEntity> playerList = new ArrayList<>();
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (mc.player != player && mc.player.squaredDistanceTo(player) <= range * range) {
                    playerList.add(player);
                }
            }
            playerList.sort(Comparator.comparing(player -> mc.player.squaredDistanceTo(player)));
            return playerList;
        }
    }


    public class Structure {
        private final BlockPos pistonPos;
        private BlockPos crystalPos;
        private final BlockPos targetPos;
        private BlockPos redstonePos;
        private BlockPos firePos;
        private final PlayerEntity target;

        private BlockPos pistonHeadPos;

        public BlockPos getPistonHeadPos() {
            return pistonHeadPos;
        }

        public BlockPos getPistonPos() {
            return pistonPos;
        }

        public BlockPos getCrystalPos() {
            return crystalPos;
        }

        public BlockPos getRedstonePos() {
            return redstonePos;
        }

        public BlockPos getFirePos() {
            return firePos;
        }

        public PlayerEntity getTarget() {
            return target;
        }

        public Structure(PlayerEntity target, BlockPos crystalPos, BlockPos pistonPos, BlockPos pistonHeadPos, BlockPos[] redstonePos, BlockPos[] firePos) {
            this.target = target;
            this.targetPos = BlockPos.ofFloored(target.getPos());
            this.pistonPos = canPlace(targetPos.add(pistonPos.getX(), pistonPos.getY() + 1, pistonPos.getZ())) ? targetPos.add(pistonPos.getX(), pistonPos.getY() + 1, pistonPos.getZ()) : null;
            this.crystalPos = canPlaceCrystal(targetPos.add(crystalPos.getX(), crystalPos.getY(), crystalPos.getZ())) ? targetPos.add(crystalPos.getX(), crystalPos.getY(), crystalPos.getZ()) : null;
            this.pistonHeadPos = mc.world.isAir(targetPos.add(pistonHeadPos.getX(), pistonHeadPos.getY() + 1, pistonHeadPos.getZ())) ? targetPos.add(pistonHeadPos.getX(), pistonHeadPos.getY() + 1, pistonHeadPos.getZ()) : null;

            if (this.pistonHeadPos != null && !mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(this.pistonHeadPos)).isEmpty()) {
                this.pistonHeadPos = null;
            }

            if (this.crystalPos != null && !mc.world.getNonSpectatingEntities(Entity.class, new Box(this.crystalPos)).isEmpty()) {
                this.crystalPos = null;
            }

            this.redstonePos = null;
            List<BlockPos> tempRed = Arrays.stream(redstonePos).map(blockPos -> targetPos.add(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ())).toList();
            for (BlockPos pos : tempRed) {
                if (canPlace(pos)) {
                    this.redstonePos = pos;
                    break;
                }
            }
            this.firePos = null;
            List<BlockPos> tempFire = Arrays.stream(firePos).map(blockPos -> targetPos.add(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ())).toList();
            for (BlockPos pos : tempFire) {
                if (canPlace(pos)) {
                    this.firePos = pos;
                    break;
                }
            }
        }

        public boolean isNormalPa() {
            return pistonPos != null && crystalPos != null && targetPos != null && redstonePos != null && pistonHeadPos != null;
        }

        public boolean isFirePa() {
            return pistonPos != null && crystalPos != null && targetPos != null && redstonePos != null && pistonHeadPos != null && firePos != null;
        }

        private boolean canPlace(BlockPos pos) {
            if (pos == null) return false;
            return PlaceUtility.canPlaceBlock(pos, strictDirection.getValue(), true);
        }

        public boolean canPlaceCrystal(BlockPos blockPos) {
            if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN)
                return false;

            if (!(mc.world.getBlockState(blockPos.up()).getBlock() == Blocks.AIR)) return false;

            if (!(mc.world.getBlockState(blockPos.up().up()).getBlock() == Blocks.AIR)) return false;

            if (!PlaceUtility.canSee(new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.7, blockPos.getZ() + 0.5), new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5))) {
                if (PlaceUtility.getEyesPos(((mc.player))).distanceTo(new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5)) > wallRange.getValue()) {
                    return false;
                }
            }

            Vec3d playerEyes = PlaceUtility.getEyesPos(((mc.player)));
            boolean canPlace = false;

            if (strictDirection.getValue()) {
                for (Vec3d point : PlaceUtility.fastMultiPoint) {
                    Vec3d p = new Vec3d(blockPos.getX() + point.getX(), blockPos.getY() + point.getY(), blockPos.getZ() + point.getZ());
                    double distanceTo = playerEyes.distanceTo(p);
                    if (distanceTo > placeRange.getValue()) {
                        continue;
                    }
                    if (distanceTo > wallRange.getValue()) {
                        BlockHitResult result = mc.world.raycast(new RaycastContext(playerEyes, p, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ((mc.player))));
                        if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(blockPos)) {
                            canPlace = true;
                            break;
                        }
                    } else {
                        canPlace = true;
                        break;
                    }
                }
            } else {
                for (Direction dir : Direction.values()) {
                    Vec3d p = new Vec3d(blockPos.getX() + 0.5 + dir.getOffsetX() * 0.5, blockPos.getY() + 0.5 + dir.getOffsetY() * 0.5, blockPos.getZ() + 0.5 + dir.getOffsetZ() * 0.5);
                    double distanceTo = playerEyes.distanceTo(p);
                    if (distanceTo > placeRange.getValue()) {
                        continue;
                    }
                    if (distanceTo < wallRange.getValue()) {
                        canPlace = true;
                        break;
                    }
                }
            }
            if (!canPlace) return false;
            boolean final_result = true;

            for (Entity ent : Thunderhack.asyncManager.getAsyncEntities()) {
                if (ent.getBoundingBox().intersects(new Box(blockPos).stretch(0, 2, 0)) && (!(ent instanceof EndCrystalEntity) || ent.age > 20)) {
                    final_result = false;
                    break;
                }
            }
            return final_result;
        }

        public double getMaxRange() {
            if (this.pistonPos == null || this.crystalPos == null || this.redstonePos == null) return 999;
            final double piston = mc.player.squaredDistanceTo(this.pistonPos.toCenterPos());
            final double crystal = mc.player.squaredDistanceTo(this.crystalPos.toCenterPos());
            final double redstone = mc.player.squaredDistanceTo(this.redstonePos.toCenterPos());
            BlockPos firePos = this.firePos != null ? this.firePos : this.pistonPos;
            final double fire = mc.player.squaredDistanceTo(firePos.toCenterPos());
            return Math.max(Math.max(fire, crystal), Math.max(redstone, piston));
        }
    }

    public enum Stage {
        Searching,
        Trap,
        Piston,
        Fire,
        Crystal,
        RedStone,
        Break
    }
}
