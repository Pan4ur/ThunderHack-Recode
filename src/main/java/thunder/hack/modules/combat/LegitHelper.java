package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.AsyncManager;
import thunder.hack.events.impl.EventEntitySpawn;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IMinecraftClient;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.setting.impl.BooleanParent;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;

import static thunder.hack.modules.combat.Criticals.getEntity;
import static thunder.hack.modules.combat.Criticals.getInteractType;

public class LegitHelper extends Module {

    public LegitHelper() {
        super("LegitHelper", Category.COMBAT);
    }


    private final Setting<BooleanParent> anchors = new Setting<>("Anchors", new BooleanParent(true));
    private final Setting<Integer> anchorDelay = new Setting<>("AnchorDelay", 50, 5, 250).withParent(anchors);
    private final Setting<Bind> anchorBind = new Setting<>("AnchorBind", new Bind(GLFW.GLFW_KEY_Y, false, false)).withParent(anchors);

    private final Setting<BooleanParent> crystals = new Setting<>("Crystals", new BooleanParent(true));
    private final Setting<Integer> crystalDelay = new Setting<>("CrystalDelay", 50, 5, 250).withParent(crystals);
    private final Setting<Bind> crystalBind = new Setting<>("CrystalBind", new Bind(GLFW.GLFW_KEY_U, false, false)).withParent(crystals);
    private final Setting<Boolean> changePitch = new Setting<>("ChangePitch", false).withParent(crystals);
    private final Setting<Boolean> crystalOptimizer = new Setting<>("CrystalOptimizer", false).withParent(crystals);
    private final Setting<Boolean> switchBack = new Setting<>("SwitchBack", false).withParent(crystals);

    private final Setting<BooleanParent> shieldBreaker = new Setting<>("ShieldBreaker", new BooleanParent(false));
    private final Setting<Integer> breakerDelay = new Setting<>("BreakerDelay", 50, 5, 250).withParent(shieldBreaker);
    private final Setting<Boolean> swapBack = new Setting<>("SwapBack", true).withParent(shieldBreaker);


    private Timer timer = new Timer();
    private Vec3d lastCrystalVec = Vec3d.ZERO;

    @Override
    public void onUpdate() {
        if (isKeyPressed(anchorBind) && timer.every(anchorDelay.getValue() * 5L + 100)) {
            int glowSlot = InventoryUtility.getGlowStone().slot();
            int anchorSlot = InventoryUtility.getAnchor().slot();
            if (glowSlot == -1 || anchorSlot == -1) return;

            int prevSlot = mc.player.getInventory().selectedSlot;

            ThunderHack.asyncManager.run(() -> {
                mc.player.getInventory().selectedSlot = anchorSlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(anchorSlot));
                AsyncManager.sleep(anchorDelay.getValue());
                ((IMinecraftClient) mc).idoItemUse();
                AsyncManager.sleep(anchorDelay.getValue());
                mc.player.getInventory().selectedSlot = glowSlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(glowSlot));
                AsyncManager.sleep(anchorDelay.getValue());
                ((IMinecraftClient) mc).idoItemUse();
                AsyncManager.sleep(anchorDelay.getValue());
                mc.player.getInventory().selectedSlot = prevSlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
                AsyncManager.sleep(anchorDelay.getValue());
                ((IMinecraftClient) mc).idoItemUse();
            });

            return;
        }

        boolean crystalAtCrosshair = mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() instanceof EndCrystalEntity;
        boolean obbyAtCrosshair = mc.crosshairTarget instanceof BlockHitResult bhr && mc.world.getBlockState(bhr.getBlockPos()).getBlock() == Blocks.OBSIDIAN;

        if (isKeyPressed(crystalBind) && timer.every(crystalDelay.getValue() * (crystalAtCrosshair ? 1L : obbyAtCrosshair ? 2L : 4L))) {
            int crystalSlot = InventoryUtility.findItemInHotBar(Items.END_CRYSTAL).slot();
            int obbySlot = InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN).slot();
            if (obbySlot == -1 || crystalSlot == -1 || crystalSlot >= 9 || obbySlot >= 9) return;

            if (crystalAtCrosshair) {
                mc.interactionManager.attackEntity(mc.player, ((EntityHitResult) mc.crosshairTarget).getEntity());
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }

            int prevSlot = mc.player.getInventory().selectedSlot;

            ThunderHack.asyncManager.run(() -> {
                if (!obbyAtCrosshair) {
                    mc.player.getInventory().selectedSlot = obbySlot;
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(obbySlot));
                    AsyncManager.sleep(crystalDelay.getValue());
                    ((IMinecraftClient) mc).idoItemUse();
                    AsyncManager.sleep(crystalDelay.getValue());
                }
                mc.player.getInventory().selectedSlot = crystalSlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(crystalSlot));
                AsyncManager.sleep(crystalDelay.getValue());
                ((IMinecraftClient) mc).idoItemUse();
                lastCrystalVec = mc.crosshairTarget.getPos();
                if (switchBack.getValue()) {
                    AsyncManager.sleep(crystalDelay.getValue());
                    mc.player.getInventory().selectedSlot = prevSlot;
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
                }
            });
        }


        if (shieldBreaker.getValue().isEnabled()
                && mc.crosshairTarget instanceof EntityHitResult ehr
                && ehr.getEntity() instanceof PlayerEntity pl
                && !ThunderHack.friendManager.isFriend(pl)
                && (pl.getOffHandStack().getItem() == Items.SHIELD || pl.getMainHandStack().getItem() == Items.SHIELD)
                && pl.getActiveItem().getItem() == Items.SHIELD && timer.every(500)) {

            int axeSlot = InventoryUtility.getAxeHotBar().slot();
            if (axeSlot == -1)
                return;

            int prevSlot = mc.player.getInventory().selectedSlot;
            ThunderHack.asyncManager.run(() -> {
                AsyncManager.sleep(breakerDelay.getValue());
                mc.player.getInventory().selectedSlot = axeSlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(axeSlot));
                AsyncManager.sleep(breakerDelay.getValue());
                if (mc.crosshairTarget instanceof EntityHitResult ehr2)
                    mc.interactionManager.attackEntity(mc.player, ehr2.getEntity());
                mc.player.swingHand(Hand.MAIN_HAND);

                if (swapBack.getValue()) {
                    AsyncManager.sleep(breakerDelay.getValue());
                    mc.player.getInventory().selectedSlot = prevSlot;
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
                }
            });
        }
    }


    @EventHandler
    public void onEntitySpawn(EventEntitySpawn e) {
        if (e.getEntity() instanceof EndCrystalEntity cr && e.getEntity().squaredDistanceTo(lastCrystalVec) < 4f) {
            lastCrystalVec = Vec3d.ZERO;
            if (changePitch.getValue()) {
                float pitch = InteractionUtility.calculateAngle(cr.getPos().add(0, 0.15, 0))[1];
                double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
                mc.player.setPitch((float) (pitch - (pitch - mc.player.getPitch()) % gcdFix));
            }
            mc.interactionManager.attackEntity(mc.player, e.getEntity());
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.@NotNull Send event) {
        if (crystalOptimizer.getValue() && event.getPacket() instanceof PlayerInteractEntityC2SPacket
                && getInteractType(event.getPacket()) == Criticals.InteractType.ATTACK && getEntity(event.getPacket()) instanceof EndCrystalEntity c) {
            c.kill();
            c.setRemoved(Entity.RemovalReason.KILLED);
            c.onRemoved();
        }
    }
}
