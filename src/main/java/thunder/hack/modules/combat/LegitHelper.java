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
import thunder.hack.core.impl.AsyncManager;
import thunder.hack.events.impl.EventEntitySpawn;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IMinecraftClient;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;

import static thunder.hack.modules.combat.Criticals.getEntity;
import static thunder.hack.modules.combat.Criticals.getInteractType;

public class LegitHelper extends Module {

    public LegitHelper() {
        super("LegitHelper", Category.COMBAT);
    }

    private final Setting<Integer> swapDelay = new Setting<>("SwapDelay", 50, 10, 250);
    private final Setting<Bind> anchorBind = new Setting<>("AnchorBind", new Bind(GLFW.GLFW_KEY_Y, false, false));
    private final Setting<Bind> crystalBind = new Setting<>("CrystalBind", new Bind(GLFW.GLFW_KEY_U, false, false));
    private final Setting<Boolean> changePitch = new Setting<>("ChangePitch", false);
    private final Setting<Boolean> crystalOptimizer = new Setting<>("CrystalOptimizer", false);
    private final Setting<Boolean> shieldBreaker = new Setting<>("ShieldBreaker", false);

    private Timer timer = new Timer();
    private Vec3d lastCrystalVec = Vec3d.ZERO;

    @Override
    public void onUpdate() {
        if (isKeyPressed(anchorBind) && timer.every(swapDelay.getValue() * 5L + 100)) {
            int glowSlot = InventoryUtility.getGlowStone().slot();
            int anchorSlot = InventoryUtility.getAnchor().slot();
            if (glowSlot == -1 || anchorSlot == -1) return;
            new AnchorThread(glowSlot, mc.player.getInventory().selectedSlot, anchorSlot, swapDelay.getValue()).start();
            return;
        }

        boolean crystalAtCrosshair = mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() instanceof EndCrystalEntity;
        boolean obbyAtCrosshair = mc.crosshairTarget instanceof BlockHitResult bhr && mc.world.getBlockState(bhr.getBlockPos()).getBlock() == Blocks.OBSIDIAN;

        if (isKeyPressed(crystalBind) && timer.every(swapDelay.getValue() * (crystalAtCrosshair ? 1L : obbyAtCrosshair ? 2L : 4L))) {
            int crystalSlot = InventoryUtility.findItemInHotBar(Items.END_CRYSTAL).slot();
            int obbySlot = InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN).slot();
            if (obbySlot == -1 || crystalSlot == -1 || crystalSlot >= 9 || obbySlot >= 9) return;

            if (crystalAtCrosshair) {
                mc.interactionManager.attackEntity(mc.player, ((EntityHitResult) mc.crosshairTarget).getEntity());
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
            new CrystalThread(crystalSlot, obbySlot, mc.player.getInventory().selectedSlot, swapDelay.getValue(), obbyAtCrosshair).start();
        }

        if (shieldBreaker.getValue()
                && mc.crosshairTarget instanceof EntityHitResult ehr
                && ehr.getEntity() instanceof PlayerEntity pl
                && (pl.getOffHandStack().getItem() == Items.SHIELD || pl.getMainHandStack().getItem() == Items.SHIELD)
                && pl.getActiveItem().getItem() == Items.SHIELD && timer.every(500)) {

            int axeSlot = InventoryUtility.getAxeHotBar().slot();
            new ShieldBreaker(axeSlot, mc.player.getInventory().selectedSlot, swapDelay.getValue()).start();
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

    public class CrystalThread extends Thread {
        int crystalSlot, obbySlot, originalSlot, delay;
        boolean onBlock;

        public CrystalThread(int crystalSlot, int obbySlot, int originalSlot, int delay, boolean onBlock) {
            this.crystalSlot = crystalSlot;
            this.obbySlot = obbySlot;
            this.originalSlot = originalSlot;
            this.delay = delay;
            this.onBlock = onBlock;
        }

        @Override
        public void run() {
            if (!onBlock) {
                mc.player.getInventory().selectedSlot = obbySlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(obbySlot));
                AsyncManager.sleep(delay);
                ((IMinecraftClient) mc).idoItemUse();
                AsyncManager.sleep(delay);
            }
            mc.player.getInventory().selectedSlot = crystalSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(crystalSlot));
            AsyncManager.sleep(delay);
            ((IMinecraftClient) mc).idoItemUse();
            lastCrystalVec = mc.crosshairTarget.getPos();
            AsyncManager.sleep(delay);
            mc.player.getInventory().selectedSlot = originalSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
            super.run();
        }
    }

    public class AnchorThread extends Thread {
        int anchorSlot, glowSlot, originalSlot, delay;

        public AnchorThread(int glowSlot, int originalSlot, int anchorSlot, int delay) {
            this.glowSlot = glowSlot;
            this.originalSlot = originalSlot;
            this.delay = delay;
            this.anchorSlot = anchorSlot;
        }

        @Override
        public void run() {
            mc.player.getInventory().selectedSlot = anchorSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(anchorSlot));
            AsyncManager.sleep(delay);
            ((IMinecraftClient) mc).idoItemUse();
            AsyncManager.sleep(delay);
            mc.player.getInventory().selectedSlot = glowSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(glowSlot));
            AsyncManager.sleep(delay);
            ((IMinecraftClient) mc).idoItemUse();
            AsyncManager.sleep(delay);
            mc.player.getInventory().selectedSlot = originalSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
            AsyncManager.sleep(delay);
            ((IMinecraftClient) mc).idoItemUse();
            super.run();
        }
    }

    public class ShieldBreaker extends Thread {
        int axeslot, originalSlot, delay;

        public ShieldBreaker(int axeslot, int originalSlot, int delay) {
            this.axeslot = axeslot;
            this.originalSlot = originalSlot;
            this.delay = delay;
        }

        @Override
        public void run() {
            AsyncManager.sleep(delay);
            mc.player.getInventory().selectedSlot = axeslot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(axeslot));
            AsyncManager.sleep(delay);
            if (mc.crosshairTarget instanceof EntityHitResult ehr)
                mc.interactionManager.attackEntity(mc.player, ehr.getEntity());
            mc.player.swingHand(Hand.MAIN_HAND);
            AsyncManager.sleep(delay);
            mc.player.getInventory().selectedSlot = originalSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
            super.run();
        }
    }
}
