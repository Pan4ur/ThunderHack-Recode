package thunder.hack.features.modules.render;

import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class Trajectories extends Module {
    public Trajectories() {
        super("Trajectories", Category.RENDER);
    }

    private final Setting<Mode> mode = new Setting<>("ColorMode", Mode.Sync);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x2250b4b4), v -> mode.getValue() == Mode.Custom);
    private final Setting<Mode> lmode = new Setting<>("LandedColorMode", Mode.Sync);
    private final Setting<ColorSetting> lcolor = new Setting<>("LandedColor", new ColorSetting(0x2250b4b4), v -> lmode.getValue() == Mode.Custom);

    private boolean isThrowable(Item item) {
        return item instanceof EnderPearlItem || item instanceof TridentItem || item instanceof ExperienceBottleItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem;
    }

    private float getDistance(Item item) {
        return item instanceof BowItem ? 1.0f : 0.4f;
    }

    private float getThrowVelocity(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) return 0.5f;
        if (item instanceof ExperienceBottleItem) return 0.59f;
        if (item instanceof TridentItem) return 2f;
        return 1.5f;
    }

    private int getThrowPitch(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof ExperienceBottleItem)
            return 20;
        return 0;
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (mc.options.hudHidden) return;
        if (mc.player == null || mc.world == null || !mc.options.getPerspective().isFirstPerson())
            return;
        Hand hand;

        ItemStack mainHand = mc.player.getMainHandStack();
        ItemStack offHand = mc.player.getOffHandStack();

        if (mainHand.getItem() instanceof BowItem || mainHand.getItem() instanceof CrossbowItem || isThrowable(mainHand.getItem())) {
            hand = Hand.MAIN_HAND;
        } else if (offHand.getItem() instanceof BowItem || offHand.getItem() instanceof CrossbowItem || isThrowable(offHand.getItem())) {
            hand = Hand.OFF_HAND;
        } else return;

        boolean prev_bob = mc.options.getBobView().getValue();
        mc.options.getBobView().setValue(false);

        if ((offHand.getItem() instanceof CrossbowItem && EnchantmentHelper.getLevel(mc.world.getRegistryManager().get(Enchantments.MULTISHOT.getRegistryRef()).getEntry(Enchantments.MULTISHOT).get(), offHand) != 0) ||
                (mainHand.getItem() instanceof CrossbowItem && EnchantmentHelper.getLevel(mc.world.getRegistryManager().get(Enchantments.MULTISHOT.getRegistryRef()).getEntry(Enchantments.MULTISHOT).get(), mainHand) != 0)) {

            calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), mc.player.getYaw() - 10);
            calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), mc.player.getYaw());
            calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), mc.player.getYaw() + 10);

        } else calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), mc.player.getYaw());
        mc.options.getBobView().setValue(prev_bob);
    }

    private void calcTrajectory(Item item, float yaw) {
        double x = Render2DEngine.interpolate(mc.player.prevX, mc.player.getX(), Render3DEngine.getTickDelta());
        double y = Render2DEngine.interpolate(mc.player.prevY, mc.player.getY(), Render3DEngine.getTickDelta());
        double z = Render2DEngine.interpolate(mc.player.prevZ, mc.player.getZ(), Render3DEngine.getTickDelta());

        y = y + mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161;

        if (item == mc.player.getMainHandStack().getItem()) {
            x = x - MathHelper.cos(yaw / 180.0f * 3.1415927f) * 0.16f;
            z = z - MathHelper.sin(yaw / 180.0f * 3.1415927f) * 0.16f;
        } else {
            x = x + MathHelper.cos(yaw / 180.0f * 3.1415927f) * 0.16f;
            z = z + MathHelper.sin(yaw / 180.0f * 3.1415927f) * 0.16f;
        }

        final float maxDist = getDistance(item);
        double motionX = -MathHelper.sin(yaw / 180.0f * 3.1415927f) * MathHelper.cos(mc.player.getPitch() / 180.0f * 3.1415927f) * maxDist;
        double motionY = -MathHelper.sin((mc.player.getPitch() - getThrowPitch(item)) / 180.0f * 3.141593f) * maxDist;
        double motionZ = MathHelper.cos(yaw / 180.0f * 3.1415927f) * MathHelper.cos(mc.player.getPitch() / 180.0f * 3.1415927f) * maxDist;

        float power = mc.player.getItemUseTime() / 20.0f;
        power = (power * power + power * 2.0f) / 3.0f;

        if (power > 1.0f || power == 0) {
            power = 1.0f;
        }

        final float distance = MathHelper.sqrt((float) (motionX * motionX + motionY * motionY + motionZ * motionZ));
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;

        final float pow = (item instanceof BowItem ? (power * 2.0f) : item instanceof CrossbowItem ? (2.2f) : 1.0f) * getThrowVelocity(item);

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
            if (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() == Blocks.WATER) {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }

            if (item instanceof BowItem) motionY -= 0.05000000074505806;
            else if (mc.player.getMainHandStack().getItem() instanceof CrossbowItem) motionY -= 0.05000000074505806;
            else motionY -= 0.03f;


            Vec3d pos = new Vec3d(x, y, z);

            for (Entity ent : mc.world.getEntities()) {
                if (ent instanceof ArrowEntity || ent.equals(mc.player)) continue;
                if (ent.getBoundingBox().intersects(new Box(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.3))) {
                    Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(
                            ent.getBoundingBox(),
                            lmode.getValue() == Mode.Sync ? HudEditor.getColor(i * 10) : lcolor.getValue().getColorObject(),
                            2f));
                    Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(
                            ent.getBoundingBox(), lmode.getValue() == Mode.Sync ? Render2DEngine.injectAlpha(HudEditor.getColor(i * 10), 100) : lcolor.getValue().getColorObject()
                    ));
                    break;
                }
            }

            BlockHitResult bhr = mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            if (bhr != null && bhr.getType() == HitResult.Type.BLOCK) {
                Render3DEngine.OUTLINE_SIDE_QUEUE.add(new Render3DEngine.OutlineSideAction(
                        new Box(bhr.getBlockPos()), lmode.getValue() == Mode.Sync ? HudEditor.getColor(i * 10) : lcolor.getValue().getColorObject(), 2f, bhr.getSide()
                ));
                Render3DEngine.FILLED_SIDE_QUEUE.add(new Render3DEngine.FillSideAction(
                        new Box(bhr.getBlockPos()), lmode.getValue() == Mode.Sync ? Render2DEngine.injectAlpha(HudEditor.getColor(i * 10), 100) : lcolor.getValue().getColorObject(), bhr.getSide()
                ));


                break;
            }

            if (y <= -65) break;
            if (motionX == 0 && motionY == 0 && motionZ == 0) continue;

            Render3DEngine.drawLine(lastPos, pos, mode.getValue() == Mode.Sync ? HudEditor.getColor(i) : color.getValue().getColorObject());
        }
    }

    private enum Mode {
        Custom,
        Sync
    }
}
