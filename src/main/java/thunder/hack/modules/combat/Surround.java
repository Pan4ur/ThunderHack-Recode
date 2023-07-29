package thunder.hack.modules.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.*;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.Timer;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.util.concurrent.ConcurrentHashMap;


public class Surround extends Module {

    public Surround() {super("Surround", "окружает тебя обсой", Category.COMBAT);}

    private static final Setting<Integer> actionShift = new Setting<>("PlacePerTick", 4, 1, 8);
    private static final Setting<Integer> tickDelay = new Setting<>("Delay", 0, 0, 5);
    private final Setting<Boolean> crystalBreaker = new Setting<>("Destroy Crystal", false);
    private final Setting<Boolean> strict = new Setting<>("Strict", false);
    private final Setting<Boolean> center = new Setting<>("Center", true);
    private final Setting<Boolean> render = new Setting<>("Render", true);
    private static final Setting<Parent> autoDisable = new Setting<>("Disable on", new Parent(false,0));
    public static final Setting<Boolean> disableOnYChange = new Setting<>("YChange", false).withParent(autoDisable);
    public static final Setting<Boolean> disableOnTP = new Setting<>("TP", true).withParent(autoDisable);
    public static final Setting<Boolean> disableWhenDone = new Setting<>("Done", false).withParent(autoDisable);

    private static final Vec3d[] STRICT = {
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1),
            new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1)
    };

    private static final Vec3d[] NORMAL = {
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1),
            new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1),
            new Vec3d(1, -1, 0),
            new Vec3d(0, -1, 1),
            new Vec3d(-1, -1, 0),
            new Vec3d(0, -1, -1),
            new Vec3d(0, -1, 0)
    };

    private int offsetStep = 0;
    private int delayStep = 0;

    public static Timer inactivityTimer = new Timer();

    private ConcurrentHashMap<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        inactivityTimer.reset();
        prevY = mc.player.getY();
        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }
        if (center.getValue()) {
            mc.player.updatePosition( MathHelper.floor(mc.player.getX()) + 0.5, mc.player.getY(), MathHelper.floor(mc.player.getZ()) + 0.5);
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
        }
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if(render.getValue())
            renderPoses.forEach((pos, time) -> {
                if (System.currentTimeMillis() - time > 500) {
                    renderPoses.remove(pos);
                } else {
                    Render3DEngine.drawFilledBox(event.getMatrixStack(),new Box(pos), Render2DEngine.injectAlpha(HudEditor.getColor(0), (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)))));
                    Render3DEngine.drawBoxOutline(new Box(pos), HudEditor.getColor(0), 2);
                }
            });
        handleSurround();
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && disableOnTP.getValue()) toggle();
    }

    public double prevY;

    public void handleSurround() {
        if (fullNullCheck()) {
            toggle();
            return;
        }

        if (disableOnYChange.getValue() && mc.player.getY() != prevY) {
            toggle();
        }

        if (disableWhenDone.getValue() && inactivityTimer.passedMs(650)) {
            toggle();
            return;
        }

        if (delayStep < tickDelay.getValue()) {
            delayStep++;
            return;
        } else {
            delayStep = 0;
        }

        Vec3d[] offsetPattern;
        int maxSteps;
        if (strict.getValue()) {
            offsetPattern = STRICT;
            maxSteps = STRICT.length;
        } else {
            offsetPattern = NORMAL;
            maxSteps = NORMAL.length;
        }

        int blocksPlaced = 0;

        while (blocksPlaced < actionShift.getValue()) {
            if (offsetStep >= maxSteps) {
                offsetStep = 0;
                break;
            }

            BlockPos offsetPos = BlockPos.ofFloored(offsetPattern[offsetStep]);
            BlockPos targetPos = BlockPos.ofFloored(mc.player.getPos()).add(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ());

            int slot = getSlot();

            if (slot == -1) {
                toggle();
                if(MainSettings.language.getValue() == MainSettings.Language.RU){
                    Command.sendMessage("[Surround] Нет блоков!");
                } else {
                    Command.sendMessage("[Surround] No blocks!");
                }
                return;
            }
            if(crystalBreaker.getValue())
                for (Entity entity : mc.world.getOtherEntities(null, new Box(targetPos))) {
                    if (entity instanceof EndCrystalEntity) {

                        PlayerInteractEntityC2SPacket attackPacket = PlayerInteractEntityC2SPacket.attack(mc.player, ((mc.player)).isSneaking());
                        AutoCrystal.changeId(attackPacket,entity.getId());
                        mc.player.networkHandler.sendPacket(attackPacket);
                        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    }
                }

            if (PlaceUtility.place(targetPos, strict.getValue(), false,Hand.MAIN_HAND, slot,true) != null) {
                renderPoses.put(targetPos, System.currentTimeMillis());
                blocksPlaced++;
                inactivityTimer.reset();
            }

            offsetStep++;
        }
    }

    private int getSlot() {
        int slot = -1;

        final ItemStack mainhandStack = mc.player.getMainHandStack();
        if (mainhandStack != ItemStack.EMPTY && mainhandStack.getItem() instanceof BlockItem) {
            final Block blockFromMainhandItem = ((BlockItem) mainhandStack.getItem()).getBlock();
            if (blockFromMainhandItem == Blocks.OBSIDIAN || blockFromMainhandItem == Blocks.ENDER_CHEST) {
                slot = mc.player.getInventory().selectedSlot;
            }
        }

        if (slot == -1) {
            for (int i = 0; i < 9; i++) {
                final ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem) {
                    final Block blockFromItem = ((BlockItem) stack.getItem()).getBlock();
                    if (blockFromItem == Blocks.OBSIDIAN || blockFromItem == Blocks.ENDER_CHEST) {
                        slot = i;
                        break;
                    }
                }
            }
        }
        return slot;
    }
}
