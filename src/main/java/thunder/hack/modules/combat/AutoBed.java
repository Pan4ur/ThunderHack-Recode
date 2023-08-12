package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
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
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.render.StorageEsp;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.ExplosionUtil;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AutoBed extends Module {
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<Boolean> airPlace = new Setting<>("AirPlace", false);
    private final Setting<Boolean> strictDirection = new Setting<>("StrictDirection", false);
    private final Setting<Float> breakRange = new Setting<>("BreakRange", 6f, 2f, 6f);
    private final Setting<Float> placeRange = new Setting<>("PlaceRange", 5f, 2f, 6f);
    private final Setting<Integer> breakDelay = new Setting<>("BreakDelay", 0, 0, 1000);
    private final Setting<Integer> placeDelay = new Setting<>("PlaceDelay", 0, 0, 1000);
    private final Setting<Boolean> autoSwitch = new Setting<>("Swap", true);
    private final Setting<Boolean> autoMove = new Setting<>("AutoMove", true);
    private final Setting<Boolean> render = new Setting<>("Render", true);

    private final Timer hitTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private final Timer angleInactivityTimer = new Timer();
    private static boolean isSpoofingAngles;
    private static float rotationYaw, rotationPitch;
    private BlockPos breakPos = null;
    private BlockPos finalPos = null;
    private Direction finalFacing = null;
    private int priorSlot = -1;
    private final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();

    public AutoBed() {
        super("AutoBed", Category.COMBAT);
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (fullNullCheck()) return;

        breakPos = null;
        finalPos = null;

        if (!Objects.equals(mc.world.getRegistryKey().getValue().getPath(), "the_nether")) return;

        if(hitTimer.passedMs(breakDelay.getValue())) breakPos = findBedTarget();

        if (breakPos == null && placeTimer.passedMs(placeDelay.getValue())) {
            if (mc.player.getMainHandStack().getItem() instanceof BedItem || isOffhand()) {
                findPlaceTarget();
            } else if (!( Thunderhack.combatManager.getTargets(placeRange.getValue() + 2).isEmpty())) {
                if (autoSwitch.getValue() && !isOffhand()) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack itemStack = mc.player.getInventory().main.get(i);
                        if (itemStack.getItem() instanceof BedItem) {
                            priorSlot = mc.player.getInventory().selectedSlot;
                            mc.player.getInventory().selectedSlot = i;
                            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
                            findPlaceTarget();
                            break;
                        }
                    }
                    if (autoMove.getValue() && mc.player.getMainHandStack().getItem() instanceof BedItem ) {
                        for (int i = 9; i <= 35; i++)
                            if (mc.player.getInventory().getStack(i).getItem() instanceof BedItem) {
                                mc.interactionManager.clickSlot(0, i, mc.player.getInventory().selectedSlot < 9 ? mc.player.getInventory().selectedSlot + 36 : mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                            }
                    }
                }
            }
        } else if (breakPos != null) {
            // TODO search best point
            float[] angle = calcBedBreakAngle(breakPos);
            Command.sendMessage(angle[0] + " " + angle[1]);

            rotationYaw = angle[0];
            rotationPitch = angle[1];
            isSpoofingAngles = true;
            angleInactivityTimer.reset();
        }

        if (isSpoofingAngles) {
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
        }

        if (angleInactivityTimer.passedMs(450)) isSpoofingAngles = false;
    }

    @EventHandler
    public void onPostSync(EventPostSync event) {
        if (breakPos != null) breakBed(breakPos);
        else if (finalPos != null) placeBed();

        if (priorSlot != -1 && !isOffhand()) {
            mc.player.getInventory().selectedSlot = priorSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(priorSlot));
            priorSlot = -1;
        }
    }

    public boolean isOffhand() {
        return mc.player.getOffHandStack().getItem() instanceof BedItem;
    }

    private void breakBed(BlockPos bed) {
        if (bed == null) return;
        BlockHitResult result1 = mc.world.raycast(new RaycastContext(PlaceUtility.getEyesPos(((mc.player))), new Vec3d(bed.getX() + 0.5, bed.getY(), bed.getZ() + 0.5), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ((mc.player))));
        BlockHitResult result = strictDirection.getValue() ? result1 : null;
        Direction facing = result == null || result.getSide() == null ? Direction.UP : result.getSide();

        if(mc.player.isSneaking()) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        if(result == null || result.getSide() == null)
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(bed.getX(), bed.getY(), bed.getZ()), facing, bed, false));
        else
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);

        hitTimer.reset();
    }

    public void onRender3D(MatrixStack stack) {
        if (render.getValue()) {
            renderBlocks.forEach((pos, time) -> {
                if (System.currentTimeMillis() - time > 500) {
                    renderBlocks.remove(pos);
                } else {
                    Render3DEngine.drawFilledBox(stack,new Box(pos), Render2DEngine.injectAlpha(HudEditor.getColor(0),100));
                    Render3DEngine.drawBoxOutline(new Box(pos), HudEditor.getColor(0), 2);
                }
            });
        }
    }

    private void placeBed() {
        if(PlaceUtility.forcePlace(finalPos, strictDirection.getValue(), isOffhand() ? Hand.OFF_HAND : Hand.MAIN_HAND, -1, true)) {
            renderBlocks.put(finalPos, System.currentTimeMillis());
            placeTimer.reset();
            finalPos = null;
        }
    }

    private BlockPos findBedTarget() {
        BedBlockEntity bed = (BedBlockEntity) StorageEsp.getBlockEntities().stream()
                .filter(e -> e instanceof BedBlockEntity)
                .filter(e -> mc.player.squaredDistanceTo(e.getPos().getX(), e.getPos().getY(), e.getPos().getZ()) <= breakRange.getPow2Value())
                .filter(e -> suicideCheck(e.getPos()))
                .min(Comparator.comparing(e -> mc.player.squaredDistanceTo(e.getPos().getX(), e.getPos().getY(), e.getPos().getZ()))).orElse(null);
        if (bed != null) return bed.getPos();
        return null;
    }

    private void findPlaceTarget() {
        List<PlayerEntity> targets = Thunderhack.combatManager.getTargets(placeRange.getValue() + 2);
        if (targets.isEmpty()) return;
        checkTarget(BlockPos.ofFloored(targets.get(0).getPos()), true);
    }

    private void checkTarget(BlockPos pos, boolean firstCheck) {
        if (mc.world.getBlockState(pos).getBlock() instanceof BedBlock) return;
        if (ExplosionUtil.getSelfExplosionDamage(new Vec3d(pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D)) > mc.player.getHealth() + mc.player.getAbsorptionAmount() + 0.5) {
            if (firstCheck && airPlace.getValue()) {
                checkTarget(pos.up(), false);
            }
            return;
        }
        if (!mc.world.getBlockState(pos).isReplaceable()) {
            if (firstCheck && airPlace.getValue()) {
                checkTarget(pos.up(), false);
            }
            return;
        }

        ArrayList<BlockPos> positions = new ArrayList<>();
        HashMap<BlockPos, Direction> facings = new HashMap<>();

        for (Direction facing : Direction.values()) {
            BlockPos position;
            if (facing == Direction.DOWN || facing == Direction.UP || !(mc.player.squaredDistanceTo((position = pos.offset(facing)).toCenterPos()) <= Math.pow(placeRange.getValue(), 2)) || (!mc.world.getBlockState(position).isReplaceable()) || mc.world.getBlockState(position.down()).isReplaceable()) continue;
           // if (rotate.getValue() && Direction.fromRotation(calculateLookAt(position, Direction.UP)[0]).getOpposite() != facing) continue;
            positions.add(position);
            facings.put(position, facing.getOpposite());
        }

        if (positions.isEmpty()) {
            if (firstCheck && airPlace.getValue()) checkTarget(pos.up(), false);
            return;
        }

        positions.sort(Comparator.comparingDouble(p -> mc.player.squaredDistanceTo(p.toCenterPos())));
        finalPos = positions.get(0);
        finalFacing = facings.get(finalPos);

        float[] rotation;

        if (rotate.getValue()) rotation = PlaceUtility.calcAngle(finalPos, strictDirection.getValue(), true);
        else rotation = PlaceUtility.rotationToDirection(finalFacing);

        if(rotation == null) return;

        rotationYaw = rotation[0];
        rotationPitch = rotation[1];

        isSpoofingAngles = true;
        angleInactivityTimer.reset();
    }

    public static float[] calculateLookAt(BlockPos pos, Direction facing) {
        return PlaceUtility.calculateAngle(new Vec3d(pos.getX() + 0.5 + facing.getVector().getX() * 0.5, pos.getY() + 0.5 + facing.getVector().getY() * 0.5, pos.getZ() + 0.5 + facing.getVector().getZ() * 0.5));
    }


    private boolean suicideCheck(BlockPos pos) {
        return (mc.player.getHealth() + mc.player.getAbsorptionAmount() - ExplosionUtil.getSelfExplosionDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5)) > 0.5);
    }

    public float[] calcBedBreakAngle(BlockPos bp){
        float[] first = calcBedPieceBreakAngle(bp);
        if(first != null) return first;

        ArrayList<BlockPos> offsets = new ArrayList<>();
        offsets.add(bp.south());
        offsets.add(bp.east());
        offsets.add(bp.north());
        offsets.add(bp.west());

        BlockPos offset2 = null;

        for(BlockPos offset : offsets){
            if(mc.world.getBlockState(offset).getBlock() instanceof BedBlock){
                offset2 = offset;
                break;
            }
        }
        return calcBedPieceBreakAngle(offset2);
    }

    public float[] calcBedPieceBreakAngle(BlockPos bp){
        Vec3d playerEyes = PlaceUtility.getEyesPos(mc.player);

        for (Vec3d point : PlaceUtility.halfMultiPoint) {
            Vec3d p = new Vec3d(bp.getX() + point.getX(), bp.getY() + point.getY(), bp.getZ() + point.getZ());
            double distanceTo = playerEyes.distanceTo(p);
            if (distanceTo > placeRange.getValue()) continue;

            BlockHitResult result = mc.world.raycast(new RaycastContext(playerEyes, p, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ((mc.player))));
            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(bp)) {
                break;
            }
           // Command.sendMessage(point.getBind());
            return PlaceUtility.calculateAngle(p);
        }
        return null;
    }

}
