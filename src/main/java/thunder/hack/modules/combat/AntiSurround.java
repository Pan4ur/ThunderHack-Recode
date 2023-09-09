package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.ThunderHack;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.modules.player.SpeedMine;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.world.HoleUtility;

public class AntiSurround extends Module {
    private final Setting<Float> range = new Setting<>("Range", 5f, 1f, 7f);
    private final Setting<Boolean> autoDisable = new Setting<>("Auto Disable", true);
    private final Setting<Boolean> autoSwitch = new Setting<>("Switch", true);
    private final Setting<Boolean> requirePickaxe = new Setting<>("Only Pickaxe", true);
    private final Setting<Boolean> oldVers = new Setting<>("1.12 Mode", false);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);

    private BlockPos blockPos;

    public AntiSurround() {
        super("AntiSurround", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        blockPos = null;
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (fullNullCheck()) return;
        if (autoSwitch.getValue() && !checkPickaxe()) return;

        if (blockPos != null) {
            if (mc.world.getBlockState(blockPos).getBlock().equals(Blocks.AIR)) {
                if (autoDisable.getValue()) {
                    disable(MainSettings.isRu() ? "Сарраунд сломан! Выключаю..." : "Surround has been broken! Turning off...");
                    return;
                }
                blockPos = null;
            }
        }

        BlockPos minePos = null;

        for (Entity player : ThunderHack.combatManager
                .getTargets(range.getValue()).stream()
                .filter(player -> player != mc.player && !ThunderHack.friendManager.isFriend(player))
                .toList()) {
            BlockPos pos = BlockPos.ofFloored(player.getPos());
            if (!checkBlockPos(pos)) continue;

            for (BlockPos pos2 : HoleUtility.getSurroundPoses(pos)) {
                if (!(mc.world.getBlockState(pos2).getBlock() == Blocks.OBSIDIAN)) continue;
                if (mc.world.getBlockState(pos2.add(0, 1, 0)).isAir() && oldVers.getValue()) continue;

                final Vec3d blockVec = new Vec3d(pos2.getX(), pos2.getY(), pos2.getZ());
                final double dist = mc.player.squaredDistanceTo(blockVec);
                if (dist < range.getValue() * range.getValue() && dist >= player.squaredDistanceTo(blockVec)) {
                    minePos = pos2;
                    break;
                }
            }
        }

        if (minePos != null) {
            SearchInvResult pickaxeResult = InventoryUtility.getPickAxe();
            if (autoSwitch.getValue()) {
                pickaxeResult.switchTo(InventoryUtility.SwitchMode.Normal);
            }

            if (rotate.getValue()) {
                float[] rotation = getRotations(minePos);
                mc.player.setYaw(rotation[0]);
                mc.player.setPitch(rotation[1]);
            }

            if (!requirePickaxe.getValue() || mc.player.getMainHandStack().getItem() instanceof PickaxeItem) {
                if (ModuleManager.speedMine.isEnabled() && SpeedMine.progress != 0) {
                    return;
                }
                InteractionUtility.BreakData data = InteractionUtility.getBreakData(minePos, InteractionUtility.Interact.Strict);
                if (data == null) return;

                mc.interactionManager.attackBlock(minePos, data.dir());
                mc.player.swingHand(Hand.MAIN_HAND);
                this.blockPos = minePos;
            }
        }
    }

    public static float @NotNull [] calcAngle(@NotNull Vec3d vec3d) {
        Vec3d vec = new Vec3d(mc.player.getX(), mc.player.getY() + (double) mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
        double d = vec3d.x - vec.x;
        double d3 = vec3d.z - vec.z;
        float f = (float) Math.toDegrees(Math.atan2(d3, d)) - 90.0f;
        float f2 = (float) (-Math.toDegrees(Math.atan2(vec3d.y - vec.y, Math.sqrt(d * d + d3 * d3))));
        float[] fArray = new float[2];

        fArray[0] = mc.player.getYaw() + MathHelper.wrapDegrees(f - mc.player.getYaw());
        fArray[1] = mc.player.getPitch() + MathHelper.wrapDegrees(f2 - mc.player.getPitch());

        return fArray;
    }

    public static float @Nullable [] getRotations(@NotNull BlockPos blockPos) {
        Vec3d vec3d2 = blockPos.toCenterPos();
        InteractionUtility.BreakData data = InteractionUtility.getBreakData(blockPos, InteractionUtility.Interact.Strict);
        if (data == null) return null;

        return calcAngle(vec3d2.add(new Vec3d(data.dir().getUnitVector()).multiply(0.5)));
    }

    public boolean checkPickaxe() {
        Item item = mc.player.getMainHandStack().getItem();

        return item.equals(Items.DIAMOND_PICKAXE) || item.equals(Items.IRON_PICKAXE) ||
                item.equals(Items.GOLDEN_PICKAXE) || item.equals(Items.STONE_PICKAXE) ||
                item.equals(Items.WOODEN_PICKAXE);
    }


    public boolean checkValidBlock(@NotNull Block block) {
        return block.equals(Blocks.OBSIDIAN)
                || block.equals(Blocks.BEDROCK)
                || block.equals(Blocks.CRYING_OBSIDIAN)
                || block.equals(Blocks.NETHERITE_BLOCK)
                || block.equals(Blocks.RESPAWN_ANCHOR);
    }

    public boolean checkBlockPos(@NotNull BlockPos checkPos) {
        if (mc.world == null) return false;

        if (checkValidBlock(mc.world.getBlockState(checkPos.add(0, -1, 0)).getBlock())
                && (mc.world.isAir(checkPos.add(0, 1, 0)) || !oldVers.getValue())) {
            return HoleUtility.isHole(checkPos);
        }

        return false;
    }
}
