package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.modules.render.HoleESP;
import thunder.hack.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.StreamSupport;

public class Blocker extends Module {
    public Blocker() {
        super("Blocker", Category.COMBAT);
    }

    private final Setting<Integer> actionShift = new Setting<>("PlacePerTick", 1, 1, 5);
    private final Setting<Integer> actionInterval = new Setting<>("Delay", 0, 0, 5);
    private final Setting<Boolean> crystalBreaker = new Setting<>("Destroy Crystal", false);

    public final Setting<Parent> logic = new Setting<>("Logic", new Parent(false,0));
    private final Setting<Boolean> antiCev = new Setting<>("Anti Cev", true).withParent(logic);
    private final Setting<Boolean> antiCiv = new Setting<>("Anti Civ", true).withParent(logic);
    private final Setting<Boolean> diagonal = new Setting<>("Diagonal", true).withParent(logic);
    private final Setting<Boolean> expand = new Setting<>("Expand", true).withParent(logic);


    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<Boolean> render = new Setting<>("Render", true);
    private final Setting<Boolean> strictDirection = new Setting<>("Strict Direction", false);
    private final Setting<PlaceUtility.PlaceMode> placeMode = new Setting<>("Place Mode", PlaceUtility.PlaceMode.All);


    private final List<BlockPos> placePositions = new CopyOnWriteArrayList<>();
    private final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();
    private int tickCounter = 0;
    public static Timer inactivityTimer = new Timer();

    public void onRender3D(MatrixStack stack) {
        if (render.getValue()) {
            renderBlocks.forEach((pos, time) -> {
                if (System.currentTimeMillis() - time > 500) {
                    renderBlocks.remove(pos);
                } else {
                    Render3DEngine.drawFilledBox(stack, new Box(pos), Render2DEngine.injectAlpha(HudEditor.getColor(0), 100));
                    Render3DEngine.drawBoxOutline(new Box(pos), HudEditor.getColor(0), 2);
                }
            });
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (tickCounter < actionInterval.getValue()) {
            tickCounter++;
        }

        if (tickCounter < actionInterval.getValue()) {
            return;
        }

        int obbySlot = InventoryUtility.findHotbarBlock(Blocks.OBSIDIAN);
        int eChestSlot = InventoryUtility.findHotbarBlock(Blocks.ENDER_CHEST);

        if (obbySlot == -1 && eChestSlot == 1) return;

        int blocksPlaced = 0;

        if (placePositions.isEmpty()) return;

        while (blocksPlaced < actionShift.getValue()) {
            BlockPos pos = StreamSupport.stream(placePositions.spliterator(), false)
                    .filter(p -> PlaceUtility.canPlaceBlock(p, strictDirection.getValue(), true))
                    .min(Comparator.comparing(p -> mc.player.getPos().distanceTo(new Vec3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5))))
                    .orElse(null);

            if (pos != null) {

                if (crystalBreaker.getValue())
                    for (Entity entity : mc.world.getOtherEntities(null, new Box(pos))) {
                        if (entity instanceof EndCrystalEntity) {
                            mc.interactionManager.attackEntity(mc.player, entity);
                            mc.player.swingHand(Hand.MAIN_HAND);
                        }
                    }

                if (PlaceUtility.place(pos, rotate.getValue(), strictDirection.getValue(), Hand.MAIN_HAND, obbySlot == -1 ? eChestSlot : obbySlot, false, placeMode.getValue())) {
                    blocksPlaced++;
                    renderBlocks.put(pos, System.currentTimeMillis());
                    PlaceUtility.ghostBlocks.put(pos, System.currentTimeMillis());
                    tickCounter = 0;
                    placePositions.remove(pos);
                    inactivityTimer.reset();
                    if (!mc.player.isOnGround()) return;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof BlockBreakingProgressS2CPacket && HoleESP.validIndestructible(BlockPos.ofFloored(mc.player.getPos()))) {
            BlockBreakingProgressS2CPacket packet = e.getPacket();
            BlockPos pos = packet.getPos();

            if (mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos).getBlock() == Blocks.AIR)
                return;

            BlockPos playerPos = BlockPos.ofFloored(mc.player.getPos());

            if (antiCev.getValue() && pos.equals(playerPos.up().up())) {
                placePositions.add(playerPos.up().up().up());
            }

            if (pos.equals(playerPos.north())) {
                placePositions.add(playerPos.north().add(0, 1, 0));

                if(expand.getValue())
                    placePositions.add(playerPos.north().north());

                if(diagonal.getValue()) {
                    placePositions.add(playerPos.north().east());
                    placePositions.add(playerPos.north().west());
                }
            }
            if (pos.equals(playerPos.east())) {
                placePositions.add(playerPos.east().add(0, 1, 0));

                if(expand.getValue())
                    placePositions.add(playerPos.east().east());

                if(diagonal.getValue()) {
                    placePositions.add(playerPos.east().north());
                    placePositions.add(playerPos.east().south());
                }
            }
            if (pos.equals(playerPos.west())) {
                placePositions.add(playerPos.west().add(0, 1, 0));

                if(expand.getValue())
                    placePositions.add(playerPos.west().west());

                if(diagonal.getValue()) {
                    placePositions.add(playerPos.west().north());
                    placePositions.add(playerPos.west().south());
                }
            }
            if (pos.equals(playerPos.south())) {
                placePositions.add(playerPos.south().add(0, 1, 0));

                if(expand.getValue())
                    placePositions.add(playerPos.south().south());

                if(diagonal.getValue()) {
                    placePositions.add(playerPos.south().west());
                    placePositions.add(playerPos.south().east());
                }
            }

            if(antiCiv.getValue()) {
                if (pos.equals(playerPos.north().up())) {
                    placePositions.add(playerPos.north().add(0, 2, 0));
                }
                if (pos.equals(playerPos.east().up())) {
                    placePositions.add(playerPos.east().add(0, 2, 0));
                }
                if (pos.equals(playerPos.west().up())) {
                    placePositions.add(playerPos.west().add(0, 2, 0));
                }
                if (pos.equals(playerPos.south().up())) {
                    placePositions.add(playerPos.south().add(0, 2, 0));
                }
            }
        }
    }
}
