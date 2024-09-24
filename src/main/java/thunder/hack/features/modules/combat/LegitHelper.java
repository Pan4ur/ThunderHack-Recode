package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.AsyncManager;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventEntitySpawn;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IMinecraftClient;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import static thunder.hack.features.modules.combat.Criticals.getEntity;
import static thunder.hack.features.modules.combat.Criticals.getInteractType;

public class LegitHelper extends Module {
    public LegitHelper() {
        super("LegitHelper", Category.COMBAT);
    }

    private final Setting<BooleanSettingGroup> minecarts = new Setting<>("Minecarts", new BooleanSettingGroup(true));
    private final Setting<Float> maxDistance = new Setting<>("MaxDistance", 4f, 2f, 6f).addToGroup(minecarts);
    private final Setting<Boolean> refill = new Setting<>("Refill", true).addToGroup(minecarts);
    private final Setting<Integer> refillSlot = new Setting<>("RefillSlot", 9, 1, 9, v -> refill.getValue()).addToGroup(minecarts);

    private final Setting<BooleanSettingGroup> anchors = new Setting<>("Anchors", new BooleanSettingGroup(true));
    private final Setting<Integer> anchorDelay = new Setting<>("AnchorDelay", 50, 5, 250).addToGroup(anchors);
    private final Setting<Bind> anchorBind = new Setting<>("AnchorBind", new Bind(GLFW.GLFW_KEY_Y, false, false)).addToGroup(anchors);

    private final Setting<BooleanSettingGroup> crystals = new Setting<>("Crystals", new BooleanSettingGroup(true));
    private final Setting<Integer> crystalDelay = new Setting<>("CrystalDelay", 50, 5, 250).addToGroup(crystals);
    private final Setting<Bind> crystalBind = new Setting<>("CrystalBind", new Bind(GLFW.GLFW_KEY_U, false, false)).addToGroup(crystals);
    private final Setting<Boolean> changePitch = new Setting<>("ChangePitch", false).addToGroup(crystals);
    private final Setting<Boolean> crystalOptimizer = new Setting<>("CrystalOptimizer", false).addToGroup(crystals);
    private final Setting<Boolean> switchBack = new Setting<>("SwitchBack", false).addToGroup(crystals);

    private final Setting<BooleanSettingGroup> shieldBreaker = new Setting<>("ShieldBreaker", new BooleanSettingGroup(false));
    private final Setting<Integer> breakerDelay = new Setting<>("BreakerDelay", 50, 5, 250).addToGroup(shieldBreaker);
    private final Setting<Boolean> swapBack = new Setting<>("SwapBack", true).addToGroup(shieldBreaker);

    private final Setting<BooleanSettingGroup> windBoostJump = new Setting<>("WindBoostJump", new BooleanSettingGroup(true));
    private final Setting<Bind> windBoostBind = new Setting<>("WindBoostBind", new Bind(GLFW.GLFW_KEY_I, false, false)).addToGroup(windBoostJump);

    private final Setting<BooleanSettingGroup> crossBow = new Setting<>("CrossBow", new BooleanSettingGroup(true));
    private final Setting<Bind> crossBowBind = new Setting<>("CrossBowBind", new Bind(GLFW.GLFW_KEY_O, false, false)).addToGroup(crossBow);
    private final Setting<Boolean> cbswapBack = new Setting<>("CBSwapBack", true).addToGroup(crossBow);

    private Timer timer = new Timer();
    private Timer cbtimer = new Timer();

    private Vec3d lastCrystalVec = Vec3d.ZERO;
    private Vec3d rotationVec = Vec3d.ZERO;

    @Override
    public void onUpdate() {
        if (anchors.getValue().isEnabled() && isKeyPressed(anchorBind) && timer.every(anchorDelay.getValue() * 5L + 100)) {
            int glowSlot = InventoryUtility.findItemInHotBar(Items.GLOWSTONE).slot();
            int anchorSlot = InventoryUtility.findItemInHotBar(Items.RESPAWN_ANCHOR).slot();
            if (glowSlot == -1 || anchorSlot == -1) return;

            int prevSlot = mc.player.getInventory().selectedSlot;

            Managers.ASYNC.run(() -> {
                mc.player.getInventory().selectedSlot = anchorSlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(anchorSlot));
            });

            Managers.ASYNC.run(() -> mc.executeSync(() -> ((IMinecraftClient) mc).idoItemUse()), anchorDelay.getValue());

            Managers.ASYNC.run(() -> {
                mc.player.getInventory().selectedSlot = glowSlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(glowSlot));
            }, anchorDelay.getValue() * 2);

            Managers.ASYNC.run(() -> mc.executeSync(() -> ((IMinecraftClient) mc).idoItemUse()), anchorDelay.getValue() * 3L);

            Managers.ASYNC.run(() -> {
                mc.player.getInventory().selectedSlot = prevSlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            }, anchorDelay.getValue() * 4);

            Managers.ASYNC.run(() -> mc.executeSync(() -> ((IMinecraftClient) mc).idoItemUse()), anchorDelay.getValue() * 5L);

            return;
        }

        boolean crystalAtCrosshair = mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() instanceof EndCrystalEntity;
        boolean obbyAtCrosshair = mc.crosshairTarget instanceof BlockHitResult bhr && mc.world.getBlockState(bhr.getBlockPos()).getBlock() == Blocks.OBSIDIAN;

        if (crystals.getValue().isEnabled() && isKeyPressed(crystalBind) && timer.every(crystalDelay.getValue() * (crystalAtCrosshair ? 1L : obbyAtCrosshair ? 2L : 4L))) {
            int crystalSlot = InventoryUtility.findItemInHotBar(Items.END_CRYSTAL).slot();
            int obbySlot = InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN).slot();
            if (obbySlot == -1 || crystalSlot == -1 || crystalSlot >= 9 || obbySlot >= 9) return;

            if (crystalAtCrosshair) {
                mc.interactionManager.attackEntity(mc.player, ((EntityHitResult) mc.crosshairTarget).getEntity());
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }

            int prevSlot = mc.player.getInventory().selectedSlot;

            if (!obbyAtCrosshair) {
                mc.player.getInventory().selectedSlot = obbySlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(obbySlot));
                ((IMinecraftClient) mc).idoItemUse();
            }

            Managers.ASYNC.run(() -> {
                if (!obbyAtCrosshair)
                    AsyncManager.sleep(crystalDelay.getValue());

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
                && !Managers.FRIEND.isFriend(pl)
                && (pl.getOffHandStack().getItem() == Items.SHIELD || pl.getMainHandStack().getItem() == Items.SHIELD)
                && pl.getActiveItem().getItem() == Items.SHIELD && timer.every(500)) {

            int axeSlot = InventoryUtility.getAxeHotBar().slot();
            if (axeSlot == -1)
                return;

            int prevSlot = mc.player.getInventory().selectedSlot;

            Managers.ASYNC.run(() -> {
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

        if (minecarts.getValue().isEnabled() && refill.getValue())
            if (mc.player.getInventory().getStack(refillSlot.getValue() - 1).getItem() != Items.TNT_MINECART) {
                SearchInvResult result = InventoryUtility.findItemInInventory(Items.TNT_MINECART);
                if (result.found())
                    clickSlot(result.slot(), refillSlot.getValue() - 1, SlotActionType.SWAP);
            }

        if (crossBow.getValue().isEnabled() && isKeyPressed(crossBowBind) && cbtimer.every(300)) {
            SearchInvResult result = InventoryUtility.findInHotBar(i -> i.getItem() == Items.CROSSBOW && i.get(DataComponentTypes.CHARGED_PROJECTILES) != null && !i.get(DataComponentTypes.CHARGED_PROJECTILES).isEmpty());
            if (result.found()) {
                InventoryUtility.saveAndSwitchTo(result.slot());
                InteractionUtility.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
                if (cbswapBack.getValue())
                    InventoryUtility.returnSlot();
            }
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
                && getInteractType(event.getPacket()) == Criticals.InteractType.ATTACK && getEntity(event.getPacket()) instanceof EndCrystalEntity c
                && !ModuleManager.autoCrystal.isEnabled()) {
            c.kill();
            c.setRemoved(Entity.RemovalReason.KILLED);
            c.onRemoved();
        }
    }

    @EventHandler
    public void onPacketSendPost(PacketEvent.@NotNull SendPost event) {
        if (event.getPacket() instanceof PlayerActionC2SPacket action && action.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM) {
            if (minecarts.getValue().isEnabled() && mc.player.getMainHandStack().getItem() == Items.BOW) {
                BlockPos bp = calcTrajectory(mc.player.getYaw());
                if (bp != null && PlayerUtility.squaredDistanceFromEyes(bp.toCenterPos()) <= maxDistance.getPow2Value() && PlayerUtility.squaredDistanceFromEyes(bp.toCenterPos()) > 3) {

                    SearchInvResult baseResult = InventoryUtility.findItemInHotBar(Items.RAIL, Items.ACTIVATOR_RAIL, Items.DETECTOR_RAIL, Items.POWERED_RAIL);
                    SearchInvResult cartResult = InventoryUtility.findItemInHotBar(Items.TNT_MINECART);

                    if (baseResult.found() && cartResult.found()) {
                        InventoryUtility.saveSlot();
                        baseResult.switchTo();
                        sendSequencedPacket(s -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                                new BlockHitResult(new Vec3d(bp.getX() + 0.5, bp.up().getY(), bp.getZ() + 0.)
                                        , Direction.UP,
                                        bp,
                                        false), s));

                        rotationVec = bp.up().toCenterPos();
                        cartResult.switchTo();
                        sendSequencedPacket(s -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                                new BlockHitResult(new Vec3d(bp.getX() + 0.5, bp.up().getY() + .125, bp.getZ() + 0.5)
                                        , Direction.UP,
                                        bp.up(),
                                        false), s));
                        InventoryUtility.returnSlot();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (rotationVec != null) {
            float[] angle = InteractionUtility.calculateAngle(rotationVec);
            mc.player.setYaw(angle[0]);
            mc.player.setPitch(angle[1]);
            rotationVec = null;
        }

        if (isKeyPressed(windBoostBind) && mc.player.isOnGround()) {
            SearchInvResult result = InventoryUtility.findItemInHotBar(Items.WIND_CHARGE);
            if (result.found()) {
                mc.player.setPitch(90);
                mc.player.jump();
                InventoryUtility.saveAndSwitchTo(result.slot());
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                InventoryUtility.returnSlot();
            }
        }
    }

    private BlockPos calcTrajectory(float yaw) {
        double x = Render2DEngine.interpolate(mc.player.prevX, mc.player.getX(), Render3DEngine.getTickDelta());
        double y = Render2DEngine.interpolate(mc.player.prevY, mc.player.getY(), Render3DEngine.getTickDelta());
        double z = Render2DEngine.interpolate(mc.player.prevZ, mc.player.getZ(), Render3DEngine.getTickDelta());

        y = y + mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161;

        double motionX = -MathHelper.sin(yaw / 180.0f * 3.1415927f) * MathHelper.cos(mc.player.getPitch() / 180.0f * 3.1415927f);
        double motionY = -MathHelper.sin((mc.player.getPitch()) / 180.0f * 3.141593f);
        double motionZ = MathHelper.cos(yaw / 180.0f * 3.1415927f) * MathHelper.cos(mc.player.getPitch() / 180.0f * 3.1415927f);
        float power = mc.player.getItemUseTime() / 20.0f;

        power = (power * power + power * 2.0f) / 3.0f;
        if (power > 1.0f) {
            power = 1.0f;
        }

        final float distance = MathHelper.sqrt((float) (motionX * motionX + motionY * motionY + motionZ * motionZ));
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;

        final float pow = power * 3;
        motionX *= pow;
        motionY *= pow;
        motionZ *= pow;

        if (!mc.player.isOnGround())
            motionY += mc.player.getVelocity().getY();


        Vec3d lastPos;
        for (int i = 0; i < 300; i++) {
            lastPos = new Vec3d(x, y, z);
            x += motionX;
            y += motionY;
            z += motionZ;

            motionX *= 0.99;
            motionY *= 0.99;
            motionZ *= 0.99;


            motionY -= 0.05000000074505806;
            Vec3d pos = new Vec3d(x, y, z);

            for (Entity ent : mc.world.getEntities()) {
                if (ent instanceof ArrowEntity || ent.equals(mc.player)) continue;
                if (ent.getBoundingBox().intersects(new Box(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.3)))
                    return null;
            }

            BlockHitResult bhr = mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            if (bhr != null && bhr.getType() == HitResult.Type.BLOCK) {
                return bhr.getBlockPos();
            }

            if (y <= -65) break;
        }
        return null;
    }
}
