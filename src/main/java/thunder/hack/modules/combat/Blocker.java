package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.world.HoleUtility;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Blocker extends Module {
    private final Setting<Integer> actionShift = new Setting<>("Place Per Tick", 1, 1, 5);
    private final Setting<Integer> actionInterval = new Setting<>("Delay", 0, 0, 5);
    private final Setting<Boolean> crystalBreaker = new Setting<>("Destroy Crystal", false);

    private final Setting<Boolean> newBlocks = new Setting<>("1.16 Blocks", true);
    private final Setting<Boolean> allowAnchors = new Setting<>("Allow Anchors", false, (value) -> newBlocks.getValue());

    private final Setting<Parent> logic = new Setting<>("Logic", new Parent(false, 0));
    private final Setting<Boolean> antiCev = new Setting<>("Anti Cev", true).withParent(logic);
    private final Setting<Boolean> antiCiv = new Setting<>("Anti Civ", true).withParent(logic);
    private final Setting<Boolean> expand = new Setting<>("Expand", true).withParent(logic);

    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<InteractionUtility.Interact> interactMode = new Setting<>("Interact Mode", InteractionUtility.Interact.Vanilla);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.All);
    private final Setting<Boolean> swing = new Setting<>("Swing", true);

    private final Setting<Parent> render = new Setting<>("Render", new Parent(false, 0));
    private final Setting<ColorSetting> fillColor = new Setting<>("Fill Color", new ColorSetting(HudEditor.getColor(0))).withParent(render);
    private final Setting<ColorSetting> lineColor = new Setting<>("Line Color", new ColorSetting(HudEditor.getColor(0))).withParent(render);
    private final Setting<Integer> lineWidth = new Setting<>("Line Width", 2, 1, 5).withParent(render);

    private final List<BlockPos> placePositions = new CopyOnWriteArrayList<>();
    private final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();
    public static final Timer inactivityTimer = new Timer();
    private int tickCounter = 0;

    public Blocker() {
        super("Blocker", Category.COMBAT);
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        renderBlocks.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 500) {
                renderBlocks.remove(pos);
            } else {
                Render3DEngine.drawFilledBox(stack, new Box(pos), Render2DEngine.injectAlpha(fillColor.getValue().getColorObject(), 100));
                Render3DEngine.drawBoxOutline(new Box(pos), lineColor.getValue().getColorObject(), lineWidth.getValue());
            }
        });
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (tickCounter < actionInterval.getValue()) {
            tickCounter++;
            return;
        }
        if (tickCounter >= actionInterval.getValue()) {
            tickCounter = 0;
        }

        SearchInvResult searchResult = InventoryUtility.findInInventory(stack -> {
            Item item = stack.getItem();
            return item == Items.OBSIDIAN || item == Items.ENDER_CHEST
                    || (newBlocks.getValue() && (item == Items.CRYING_OBSIDIAN || item == Items.NETHERITE_BLOCK || (allowAnchors.getValue() && item == Items.RESPAWN_ANCHOR)));
        });

        if (!searchResult.found()) return;

        int blocksPlaced = 0;

        if (placePositions.isEmpty()) return;

        while (blocksPlaced < actionShift.getValue()) {
            BlockPos pos = placePositions.stream()
                    .filter(p -> {
                        InteractionUtility.checkEntities = true;
                        boolean canPlace = InteractionUtility.canPlaceBlock(p, interactMode.getValue());
                        InteractionUtility.checkEntities = false;

                        return canPlace;
                    })
                    .min(Comparator.comparing(p -> mc.player.getPos().distanceTo(new Vec3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5))))
                    .orElse(null);

            if (pos != null) {
                if (crystalBreaker.getValue())
                    for (Entity entity : mc.world.getOtherEntities(null, new Box(pos))) {
                        if (entity instanceof EndCrystalEntity) {
                            mc.interactionManager.attackEntity(mc.player, entity);
                            if (swing.getValue())
                                mc.player.swingHand(Hand.MAIN_HAND);
                        }
                    }

                if (InteractionUtility.placeBlock(pos, rotate.getValue(), interactMode.getValue(), placeMode.getValue(), searchResult, true)) {
                    if (swing.getValue())
                        mc.player.swingHand(Hand.MAIN_HAND);

                    blocksPlaced++;
                    renderBlocks.put(pos, System.currentTimeMillis());
                    // InteractionUtility.ghostBlocks.put(pos, System.currentTimeMillis());
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
    public void onPacketReceive(PacketEvent.@NotNull Receive e) {
        BlockPos playerPos = mc.player.getBlockPos();
        if (e.getPacket() instanceof BlockBreakingProgressS2CPacket) {
            BlockBreakingProgressS2CPacket packet = e.getPacket();
            BlockPos pos = packet.getPos();

            if (mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos).getBlock() == Blocks.AIR)
                return;

            if (antiCev.getValue() && pos.equals(playerPos.up().up())) {
                placePositions.add(playerPos.up().up().up());
            }

            if (HoleUtility.getSurroundPoses(playerPos).contains(pos)) {
                placePositions.add(pos.up());

                if (expand.getValue()) {
                    for (Vec3i vec : HoleUtility.VECTOR_PATTERN) {
                        InteractionUtility.checkEntities = true;
                        if (InteractionUtility.canPlaceBlock(pos.add(vec), interactMode.getValue())) {
                            placePositions.add(pos.add(vec));
                        }
                        InteractionUtility.checkEntities = false;
                    }
                }
            }

            if (antiCiv.getValue()) {
                for (BlockPos checkPos : HoleUtility.getSurroundPoses(playerPos)) {
                    if (checkPos.up().equals(pos))
                        placePositions.add(playerPos.add(checkPos.add(0, 2, 0)));
                }
            }
        }
    }
}
