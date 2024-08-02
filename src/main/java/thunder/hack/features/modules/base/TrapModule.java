package thunder.hack.features.modules.base;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.world.HoleUtility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class TrapModule extends PlaceModule {
    protected final Setting<PlaceTiming> placeTiming = new Setting<>("Place Timing", PlaceTiming.Default);
    protected final Setting<Integer> blocksPerTick = new Setting<>("Block/Tick", 8, 1, 12, v -> placeTiming.getValue() == PlaceTiming.Default);
    protected final Setting<Integer> placeDelay = new Setting<>("Delay/Place", 3, 0, 10);
    protected final Setting<TrapMode> trapMode = new Setting<>("Trap Mode", TrapMode.Full);

    private int delay;
    protected PlayerEntity target;
    private final ArrayList<BlockPos> sequentialBlocks = new ArrayList<>();

    public TrapModule(@NotNull String name, @NotNull Category category) {
        super(name, category);
    }

    protected abstract boolean needNewTarget();

    protected abstract @Nullable PlayerEntity getTarget();

    @Override
    public void onDisable() {
        target = null;
        delay = 0;
        sequentialBlocks.clear();
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onSync(EventSync event) {
        if (needNewTarget()) {
            target = getTarget();
            return;
        }

        if (placeTiming.getValue() == PlaceTiming.Vanilla && !rotate.is(InteractionUtility.Rotate.None)) {
            BlockPos targetBlock = getBlockToPlace();
            if (targetBlock != null && mc.player != null) {
                BlockHitResult result = InteractionUtility.getPlaceResult(targetBlock, interact.getValue(), false);
                if (result != null) {
                    float[] angle = InteractionUtility.calculateAngle(result.getPos());
                    mc.player.setYaw(angle[0]);
                    mc.player.setPitch(angle[1]);
                }
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPostSync(EventPostSync event) {
        if (delay > 0) {
            delay--;
            return;
        }

        InteractionUtility.Rotate rotateMod = placeTiming.is(PlaceTiming.Vanilla) && !rotate.is(InteractionUtility.Rotate.None) ? InteractionUtility.Rotate.None : rotate.getValue();

        if (placeTiming.getValue() == PlaceTiming.Default) {
            int placed = 0;
            while (placed < blocksPerTick.getValue()) {
                BlockPos targetBlock = getBlockToPlace();
                if (targetBlock == null)
                    break;
                if (placeBlock(targetBlock, rotateMod)) {
                    placed++;
                    delay = placeDelay.getValue();
                    inactivityTimer.reset();
                } else
                    break;
            }
        } else if (placeTiming.getValue() == PlaceTiming.Vanilla) {
            BlockPos targetBlock = getBlockToPlace();

            if (targetBlock != null) {
                if (placeBlock(targetBlock, rotateMod)) {
                    sequentialBlocks.add(targetBlock);
                    delay = placeDelay.getValue();
                    inactivityTimer.reset();
                }
            }
        }
    }

    protected @Nullable BlockPos getBlockToPlace() {
        if (target == null || mc.player == null) return null;
        return getBlocks(target).stream()
                .filter(pos -> pos.getSquaredDistance(mc.player.getPos()) < range.getPow2Value())
                .filter(pos -> InteractionUtility.canPlaceBlock(pos, interact.getValue(), true))
                .max(Comparator.comparing(pos -> mc.player.squaredDistanceTo(pos.toCenterPos())))
                .orElse(null);
    }

    protected List<BlockPos> getBlocks(@NotNull PlayerEntity player) {
        final Vec3d playerPos = player.getPos();
        final List<BlockPos> offsets = new ArrayList<>();
        final List<BlockPos> holePoses = HoleUtility.getHolePoses(playerPos);
        final List<BlockPos> surroundPoses = HoleUtility.getSurroundPoses(playerPos);

        if (mc.player == null || mc.world == null) return offsets;

        switch (trapMode.getValue()) {
            case Full -> {
                offsets.addAll(holePoses.stream()
                        .map(BlockPos::down)
                        .toList());

                if (interact.getValue() != InteractionUtility.Interact.AirPlace)
                    offsets.addAll(addHelpOffsets(surroundPoses));

                offsets.addAll(surroundPoses);
                offsets.addAll(surroundPoses.stream()
                        .map(BlockPos::up)
                        .toList());

                if (interact.getValue() != InteractionUtility.Interact.AirPlace) {
                    surroundPoses.stream()
                            .map(pos -> pos.up(2))
                            .filter(pos -> pos.getSquaredDistance(mc.player.getPos()) < range.getPow2Value())
                            .max(Comparator.comparing(pos -> mc.player.squaredDistanceTo(pos.toCenterPos())))
                            .ifPresent(pos -> {
                                offsets.add(pos);
                                offsets.add(pos.down());
                            });
                }

                offsets.addAll(holePoses.stream()
                        .map(pos -> pos.up(2))
                        .toList());
            }
            case Legs -> {
                offsets.addAll(holePoses.stream()
                        .map(BlockPos::down)
                        .toList());
                if (interact.getValue() != InteractionUtility.Interact.AirPlace) {
                    surroundPoses.stream()
                            .filter(pos -> pos.getSquaredDistance(mc.player.getPos()) < range.getPow2Value())
                            .max(Comparator.comparing(pos -> player.squaredDistanceTo(pos.toCenterPos())))
                            .ifPresent(pos -> {
                                offsets.add(pos);
                                offsets.add(pos.up());
                                offsets.add(pos.up(2));
                            });
                    offsets.addAll(addHelpOffsets(surroundPoses));
                }
                offsets.addAll(surroundPoses);
                offsets.addAll(holePoses.stream()
                        .map(pos -> pos.up(2))
                        .toList());
            }
            case Head -> {
                offsets.addAll(holePoses.stream()
                        .map(BlockPos::down)
                        .toList());
                if (interact.getValue() != InteractionUtility.Interact.AirPlace) {
                    surroundPoses.stream()
                            .map(BlockPos::down)
                            .filter(pos -> pos.getSquaredDistance(mc.player.getPos()) < range.getPow2Value())
                            .max(Comparator.comparing(pos -> player.squaredDistanceTo(pos.toCenterPos())))
                            .ifPresent(pos -> {
                                offsets.add(pos);
                                offsets.add(pos.up());
                                offsets.add(pos.up(3));
                            });
                }
                offsets.addAll(surroundPoses.stream()
                        .map(BlockPos::up)
                        .toList());
                offsets.addAll(holePoses.stream()
                        .map(pos -> pos.up(2))
                        .toList());
            }
        }

        return offsets;
    }

    private @NotNull List<BlockPos> addHelpOffsets(@NotNull List<BlockPos> surroundPoses) {
        final List<BlockPos> helpOffsets = new ArrayList<>();

        if (mc.world == null || mc.player == null)
            return helpOffsets;

        surroundPoses.stream()
                .map(BlockPos::down)
                .filter(pos -> pos.getSquaredDistance(mc.player.getPos()) < range.getPow2Value())
                .filter(pos -> {
                    for (Direction dir : Direction.values()) {
                        if (!mc.world.getBlockState(pos.add(dir.getVector().up())).isReplaceable()) {
                            return false;
                        }
                    }
                    return true;
                })
                .forEach(helpOffsets::add);

        return helpOffsets;
    }

    protected enum TrapMode {
        Full,
        Legs,
        Head
    }

    protected enum PlaceTiming {
        Default,
        Vanilla
    }
}
