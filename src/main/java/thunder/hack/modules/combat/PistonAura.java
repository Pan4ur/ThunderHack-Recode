package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtil;
import thunder.hack.utility.player.InventoryUtil;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.player.PlayerUtil;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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

    public PlayerEntity target;
    public BlockPos pistonPos;
    public BlockPos crystalPos;
    public BlockPos redStonePos;
    public boolean builtTrap;
    public boolean isFire;
    public Timer trapTimer = new Timer();
    public Timer attackTimer = new Timer();

    private int tickCounter = 0;
    private Runnable postAction = null;

    public Stage stage = Stage.Searching;
    BlockPos targetPos;
    BlockPos firePos;
    BlockPos pistonHeadPos;

    public Vec3d rotations;

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

        if(pistonHeadPos != null){
            final float[] angle = PlaceUtility.calculateAngle(pistonHeadPos.toCenterPos(),pistonPos.toCenterPos());
            mc.player.setYaw(angle[0]);
            mc.player.setPitch(angle[1]);
        }

        if(stage == Stage.Break) {
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
            int extraBlocks = 0;
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
        if (fullNullCheck()) return;
        //  globalCheck();
        switch (stage) {
            case Searching -> {
                findPos(false);
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

                    if (InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN) == -1) {
                        disable();
                        return;
                    }

                    List<BlockPos> trapPos = new ArrayList<>();
                    trapPos.add(targetPos.add(0, 2, 0));
                    trapPos.add(trapBase.add(0, 2, 0));
                    trapPos.add(trapBase.add(0, 1, 0));

                    for(BlockPos bp : trapPos){
                        if (PlaceUtility.place( bp,true, strictDirection.getValue(),Hand.MAIN_HAND,InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN),false)) {
                            if(bp == targetPos.add(0,2,0)){
                                builtTrap = true;
                                stage = Stage.Piston;
                            }
                            break;
                        }
                    }
                }
            }
            case Piston -> {
                BlockPos support = pistonPos.down();
                if (mc.world.getBlockState(support).isReplaceable() && supportPlace.getValue()) {
                    PlaceUtility.forcePlace(support, strictDirection.getValue(),Hand.MAIN_HAND,InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN), false);
                    return;
                }

                final float[] angle = PlaceUtility.calcAngle(pistonPos,strictDirection.getValue(),true);

                if (extra) {
                    Thunderhack.placeManager.rotate(angle[0], angle[1]);
                } else {
                    mc.player.setYaw(angle[0]);
                    mc.player.setPitch(angle[1]);
                }

                postAction = () -> {
                    int piston_slot = -1;
                    if (InventoryUtil.findHotbarBlock(Blocks.PISTON) == -1) {
                        if (InventoryUtil.findHotbarBlock(Blocks.STICKY_PISTON) == -1) {
                            disable();
                            return;
                        } else {
                            piston_slot = InventoryUtil.findHotbarBlock(Blocks.STICKY_PISTON);
                        }
                    } else {
                        piston_slot = InventoryUtil.findHotbarBlock(Blocks.PISTON);
                    }
                    PlaceUtility.forcePlace(pistonPos, false, Hand.MAIN_HAND, piston_slot, false);
                    stage = isFire ? Stage.Fire : Stage.Crystal;
                };
            }
            case Fire -> {
                BlockPos support = firePos.down();
                if (mc.world.getBlockState(support).isReplaceable() && supportPlace.getValue()) {
                    PlaceUtility.forcePlace(support, strictDirection.getValue(),Hand.MAIN_HAND,InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN), false);
                    return;
                }

                float[] angle = PlaceUtility.calcAngle(firePos,strictDirection.getValue(),true);
                if(angle == null) return;
                if (extra) {
                    Thunderhack.placeManager.rotate(angle[0], angle[1]);
                } else {
                    mc.player.setYaw(angle[0]);
                    mc.player.setPitch(angle[1]);
                }
                postAction = () -> {
                    PlaceUtility.forcePlace(firePos, strictDirection.getValue(),Hand.MAIN_HAND,InventoryUtil.getItemSlotHotbar(Items.FLINT_AND_STEEL), false);
                    stage = Stage.Crystal;
                };
            }
            case Crystal -> {
                if (mc.world.getBlockState(crystalPos).isReplaceable() && supportPlace.getValue()) {
                    PlaceUtility.forcePlace(crystalPos,  strictDirection.getValue(), Hand.MAIN_HAND,InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN), false);
                    return;
                }

                BlockHitResult result = handlePlaceRotation(crystalPos);
                if (result == null) return;
                float[] angle = PlaceUtility.calculateAngle(rotations);
                if (extra) {
                    Thunderhack.placeManager.rotate(angle[0] + MathUtil.random(-0.2f, 0.2f), angle[1]);
                } else {
                    mc.player.setYaw(angle[0] + MathUtil.random(-0.2f, 0.2f));
                    mc.player.setPitch(angle[1]);
                }

                postAction = () -> {
                    int crystal_slot = InventoryUtil.getItemSlotHotbar(Items.END_CRYSTAL);
                    int prev_slot = mc.player.getInventory().selectedSlot;
                    if (crystal_slot != -1) {
                        mc.player.getInventory().selectedSlot = crystal_slot;
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(crystal_slot));
                    }
                    mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL ? Hand.OFF_HAND : Hand.MAIN_HAND, result, PlayerUtil.getWorldActionId(mc.world)));
                    mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL ? Hand.OFF_HAND : Hand.MAIN_HAND));
                    mc.player.getInventory().selectedSlot = prev_slot;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prev_slot));
                    stage = Stage.RedStone;
                };
            }
            case RedStone -> {
                BlockPos support = redStonePos.down();
                if (mc.world.getBlockState(support).isReplaceable() && supportPlace.getValue()) {
                    PlaceUtility.forcePlace(support,  strictDirection.getValue(),Hand.MAIN_HAND, InventoryUtil.getItemSlotHotbar(Items.OBSIDIAN), false);
                }
                float[] angle = PlaceUtility.calcAngle(redStonePos,strictDirection.getValue(),true);
                if(angle == null) return;
                if (extra) {
                    Thunderhack.placeManager.rotate(angle[0], angle[1]);
                } else {
                    mc.player.setYaw(angle[0]);
                    mc.player.setPitch(angle[1]);
                }

                postAction = () -> {
                    int redstone_slot = -1;
                    if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) == -1) {
                        if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_TORCH) == -1) {
                            disable();
                        } else {
                            redstone_slot = InventoryUtil.findHotbarBlock(Blocks.REDSTONE_TORCH);
                        }
                    } else {
                        redstone_slot = InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK);
                    }
                    PlaceUtility.forcePlace(redStonePos, strictDirection.getValue(), Hand.MAIN_HAND, redstone_slot, false);
                    stage = Stage.Break;
                };
            }
            case Break -> {
                if(isFire) stage = Stage.Searching;
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
            if (!(ent instanceof EndCrystalEntity) || target.squaredDistanceTo(ent.getPos()) > 4 || ent.age < 2)
                continue;
            float[] angle = PlaceUtility.calculateAngle(ent.getPos());
            mc.player.setYaw(angle[0] + MathUtil.random(-3f,3f));
            mc.player.setPitch(angle[1]);
            if(attackTimer.passedMs(100)) {
                mc.interactionManager.attackEntity(mc.player, ent);
                mc.player.swingHand(Hand.MAIN_HAND);
                attackTimer.reset();
            }
            lastCrystal = (EndCrystalEntity) ent;
        }
    }
    private EndCrystalEntity lastCrystal;

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event){
        if (event.getPacket() instanceof PlaySoundS2CPacket && ((PlaySoundS2CPacket) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS) && ((PlaySoundS2CPacket) event.getPacket()).getSound().value().equals(SoundEvents.ENTITY_GENERIC_EXPLODE)) {
            if (lastCrystal == null || !lastCrystal.isAlive()) return;
            double soundRange = lastCrystal.squaredDistanceTo(((PlaySoundS2CPacket) event.getPacket()).getX() + 0.5, ((PlaySoundS2CPacket) event.getPacket()).getY() + 0.5, ((PlaySoundS2CPacket) event.getPacket()).getZ() + 0.5);
            if (soundRange > 121) return;
            reset();
        }

        if (event.getPacket() instanceof PlaySoundFromEntityS2CPacket && ((PlaySoundFromEntityS2CPacket) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS) && ((PlaySoundFromEntityS2CPacket) event.getPacket()).getSound().value().equals(SoundEvents.ENTITY_GENERIC_EXPLODE)) {
            if (lastCrystal == null || !lastCrystal.isAlive()) return;
            if (((PlaySoundFromEntityS2CPacket)event.getPacket()).getEntityId() != lastCrystal.getId()) return;
            reset();
        }
    }

    void globalCheck() {
        if (!PlaceUtility.canPlaceBlock(crystalPos, false) || !PlaceUtility.canPlaceBlock(pistonPos, false) || !PlaceUtility.canPlaceBlock(redStonePos, false) || !PlaceUtility.canPlaceBlock(firePos, false)) {
            findPos(false);
        }
    }

    @Override
    public String getDisplayInfo() {
        return stage.toString();
    }


    public void onRender3D(MatrixStack stack) {
        if (pistonPos == null || crystalPos == null || redStonePos == null) {
            return;
        }
        Render3DEngine.drawFilledBox(stack,new Box(pistonHeadPos.down()), Render2DEngine.injectAlpha(Color.CYAN,100));
        Render3DEngine.drawFilledBox(stack,new Box(crystalPos), Render2DEngine.injectAlpha(Color.PINK,100));
        Render3DEngine.drawFilledBox(stack,new Box(pistonPos.down()), Render2DEngine.injectAlpha(Color.GREEN,100));
        Render3DEngine.drawFilledBox(stack,new Box(redStonePos.down()), Render2DEngine.injectAlpha(Color.RED,100));
        if (firePos != null)
            Render3DEngine.drawFilledBox(stack,new Box(firePos.down()), Render2DEngine.injectAlpha(Color.yellow,100));
    }

    private void findPos(boolean disable) {
        ArrayList<Structure> list = new ArrayList<>();
        for (PlayerEntity target : Objects.requireNonNull(getPlayersSorted(targetRange.getValue()))) {
            for (int i = 0; i <= 3; i++) {
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


        if (bypass.getValue()) {
            List<Structure> structure0 = list.stream().filter(Structure::isFirePa).sorted(Comparator.comparingDouble(Structure::getMaxRange)).collect(Collectors.toList());
            if (structure0.size() == 0) {
                isFire = false;
                structure0 = list.stream().filter(Structure::isNormalPa).sorted(Comparator.comparingDouble(Structure::getMaxRange)).collect(Collectors.toList());
                if (!structure0.isEmpty()) {
                    Structure structure = structure0.get(0);
                    pistonPos = structure.getPistonPos();
                    crystalPos = structure.getCrystalPos();
                    redStonePos = structure.getRedstonePos();
                    pistonHeadPos = structure.getPistonHeadPos();
                    targetPos = structure.targetPos;
                    target = structure.getTarget();
                } else {
                    if (disable) disable();
                }
            } else {
                isFire = true;
                Structure structure = structure0.get(0);
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
            List<Structure> structure0 = list.stream().filter(Structure::isNormalPa).sorted(Comparator.comparingDouble(Structure::getMaxRange)).toList();
            if (!structure0.isEmpty()) {
                Structure structure = structure0.get(0);
                pistonPos = structure.getPistonPos();
                crystalPos = structure.getCrystalPos();
                redStonePos = structure.getRedstonePos();
                pistonHeadPos = structure.getPistonHeadPos();
                targetPos = structure.targetPos;
                target = structure.getTarget();
            } else {
                if (disable) disable();
            }
        }
    }

    public static List<PlayerEntity> getPlayersSorted(float range) {
        if (!fullNullCheck()) {
            synchronized (mc.world.getPlayers()) {
                List<PlayerEntity> playerList = new ArrayList();
                for (PlayerEntity player : mc.world.getPlayers()) {
                    if (mc.player != player && mc.player.squaredDistanceTo(player) <= range * range) {
                        playerList.add(player);
                    }
                }
                playerList.sort(Comparator.comparing((eP) -> mc.player.squaredDistanceTo(eP)));
                return playerList;
            }
        } else {
            return null;
        }
    }

    public void reset() {
        builtTrap = false;
        target = null;
        stage = Stage.Searching;
        trapTimer.reset();
        attackTimer.reset();
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

    public class Structure {
        private BlockPos pistonPos;
        private BlockPos crystalPos;
        private BlockPos targetPos;
        private BlockPos redstonePos;
        private BlockPos firePos;
        private PlayerEntity target;

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
            return PlaceUtility.canPlaceBlock(pos,strictDirection.getValue(),false);
        }

        public boolean canPlaceCrystal(BlockPos blockPos) {
            if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) return false;

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

            for(Entity ent : Thunderhack.asyncManager.getAsyncEntities()){
                if(ent.getBoundingBox().intersects(new Box(blockPos).stretch(0,2, 0)) && (!(ent instanceof EndCrystalEntity) || ent.age > 20)){
                    final_result = false;
                    break;
                }
            }
            return final_result;
        }

        public double getMaxRange() {
            final double piston = mc.player.squaredDistanceTo(this.pistonPos.toCenterPos());
            final double crystal = mc.player.squaredDistanceTo(this.crystalPos.toCenterPos());
            final double redstone = mc.player.squaredDistanceTo(this.redstonePos.toCenterPos());
            final double fire = mc.player.squaredDistanceTo(this.firePos.toCenterPos());
            return Math.max(Math.max(fire, crystal), Math.max(redstone, piston));
        }
    }
}
