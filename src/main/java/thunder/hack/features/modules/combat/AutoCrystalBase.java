package thunder.hack.features.modules.combat;

import com.google.common.collect.Lists;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.EventTick;
import thunder.hack.gui.notification.Notification;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.world.ExplosionUtility;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.BlockAnimationUtility;

import java.util.ArrayList;
import java.util.List;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class AutoCrystalBase extends Module {
    private final Setting<TargetLogic> targetLogic = new Setting<>("TargetLogic", TargetLogic.Distance);
    private final Setting<Integer> range = new Setting<>("Range", 5, 1, 7);
    private final Setting<Float> minDamageDelta = new Setting<>("MinDamageDelta", 5f, 1f, 20f);
    private final Setting<Integer> placeDelay = new Setting<>("PlaceDelay", 300, 0, 3000);
    private final Setting<Integer> calcDelay = new Setting<>("CalcDelay", 150, 0, 3000);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Strict);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private final Setting<Boolean> notification = new Setting<>("Notification", true);
    private final Setting<Boolean> disableNoObby = new Setting<>("DisableNoObby", false);

    private final Setting<BooleanSettingGroup> render = new Setting<>("Render", new BooleanSettingGroup(true));
    private final Setting<BlockAnimationUtility.BlockRenderMode> renderMode = new Setting<>("RenderMode", BlockAnimationUtility.BlockRenderMode.All).addToGroup(render);
    private final Setting<BlockAnimationUtility.BlockAnimationMode> animationMode = new Setting<>("AnimationMode", BlockAnimationUtility.BlockAnimationMode.Fade).addToGroup(render);
    private final Setting<ColorSetting> renderFillColor = new Setting<>("RenderFillColor", new ColorSetting(HudEditor.getColor(0))).addToGroup(render);
    private final Setting<ColorSetting> renderLineColor = new Setting<>("RenderLineColor", new ColorSetting(HudEditor.getColor(0))).addToGroup(render);
    private final Setting<Integer> renderLineWidth = new Setting<>("RenderLineWidth", 2, 1, 5).addToGroup(render);

    private PlayerEntity target;
    private ObbyData bestData;
    private final Timer placeTimer = new Timer();
    private final Timer calcTimer = new Timer();

    public AutoCrystalBase() {
        super("AutoCrystalBase", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;

        if (!InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN).found() && disableNoObby.getValue()) {
            disable(isRu() ? "Нет обсидиана!" : "No obsidian!");
            return;
        }

        switch (targetLogic.getValue()) {
            case HP -> target = Managers.COMBAT.getTargetByHealth(15);
            case Distance -> target = Managers.COMBAT.getNearestTarget(15);
            case FOV -> target = Managers.COMBAT.getTargetByFOV(15);
        }

        if (target != null && (target.isDead() || target.getHealth() < 0)) {
            target = null;
            return;
        }

        if (calcTimer.every(calcDelay.getValue()))
            Managers.ASYNC.run(() -> calcPosition(range.getValue(), mc.player.getPos()));
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (rotate.getValue() && bestData != null && isWorth()) {
            float[] angle = InteractionUtility.calculateAngle(bestData.bhr().getPos());
            mc.player.setYaw(angle[0]);
            mc.player.setPitch(angle[1]);
        }
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {
        SearchInvResult obbyResult = InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN);
        if (placeTimer.every(placeDelay.getValue()) && bestData != null && obbyResult.found() && isWorth()) {
            InventoryUtility.saveSlot();
            obbyResult.switchTo();
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bestData.bhr());
            sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            if (render.getValue().isEnabled())
                BlockAnimationUtility.renderBlock(bestData.position(),
                        renderLineColor.getValue().getColorObject(),
                        renderLineWidth.getValue(),
                        renderFillColor.getValue().getColorObject(),
                        animationMode.getValue(),
                        renderMode.getValue()
                );

            if (notification.getValue()) {
                String content;
                if (isRu())
                    content = "Ставлю на" + Formatting.GRAY + " X:" + bestData.position().getX() + " Y:" + bestData.position().getY() + " Z:" + bestData.position().getZ() + Formatting.WHITE + " урон возрастет на " + Formatting.RED + MathUtility.round2(bestData.damage - ModuleManager.autoCrystal.renderDamage);
                else
                    content = "Placing obby on" + Formatting.GRAY + " X:" + bestData.position().getX() + " Y:" + bestData.position().getY() + " Z:" + bestData.position().getZ() + Formatting.WHITE + " damage will increase by " + Formatting.RED + MathUtility.round2(bestData.damage - ModuleManager.autoCrystal.renderDamage);
                Managers.NOTIFICATION.publicity("AutoCrystalBase", content, 2, Notification.Type.INFO);
            }

            InventoryUtility.returnSlot();
        }
    }

    public boolean isWorth() {
        return ModuleManager.autoCrystal.isEnabled()
                && bestData != null
                && (ModuleManager.autoCrystal.renderDamage + minDamageDelta.getValue()) < bestData.damage;
    }

    public void calcPosition(float range, Vec3d center) {
        if (mc.player == null || mc.world == null) return;

        if (target == null) {
            bestData = null;
            return;
        }

        List<ObbyData> list = getPossibleBlocks(target, center, range).stream().filter(data -> ModuleManager.autoCrystal.isSafe(data.damage, data.selfDamage, data.overrideDamage)).toList();

        if (list.isEmpty()) {
            bestData = null;
        } else {
            bestData = filterPositions(list);
        }
    }

    private @NotNull List<ObbyData> getPossibleBlocks(PlayerEntity target, Vec3d center, float range) {
        List<ObbyData> blocks = new ArrayList<>();
        BlockPos playerPos = BlockPos.ofFloored(center);
        for (int x = (int) Math.floor(playerPos.getX() - range); x <= Math.ceil(playerPos.getX() + range); x++) {
            for (int y = (int) Math.floor(playerPos.getY() - range); y <= Math.ceil(playerPos.getY() + range); y++) {
                for (int z = (int) Math.floor(playerPos.getZ() - range); z <= Math.ceil(playerPos.getZ() + range); z++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    if (!mc.world.isAir(bp)) continue;
                    BlockHitResult placeResult = InteractionUtility.getPlaceResult(bp, interact.getValue(), false);
                    if (placeResult != null) {
                        AutoCrystal.PlaceData data = getPlaceData(bp, target);
                        if (data != null)
                            blocks.add(new ObbyData(bp, placeResult, data.damage(), data.selfDamage(), data.overrideDamage()));
                    }
                }
            }
        }
        return blocks;
    }

    private ObbyData filterPositions(@NotNull List<ObbyData> clearedList) {
        ObbyData bestData = null;
        float bestVal = 0f;

        for (ObbyData data : clearedList) {
            if ((ModuleManager.autoCrystal.shouldOverrideMinDmg(data.damage) || data.damage > ModuleManager.autoCrystal.minDamage.getValue())) {
                if (bestData != null && Math.abs(bestData.damage - data.damage) < 1f) {
                    if (bestData.selfDamage >= data.selfDamage) {
                        bestData = data;
                        bestVal = data.damage;
                    }
                } else {
                    if (bestVal < data.damage) {
                        bestData = data;
                        bestVal = data.damage;
                    }
                }
            }
        }

        return bestData;
    }

    public @Nullable AutoCrystal.PlaceData getPlaceData(BlockPos bp, PlayerEntity target) {
        if (mc.player == null || mc.world == null) return null;

        if (!mc.world.isAir(bp.up()))
            return null;

        if (ModuleManager.autoCrystal.isPositionBlockedByEntity(bp, true)) return null;

        Vec3d crystalVec = new Vec3d(0.5f + bp.getX(), 1f + bp.getY(), 0.5f + bp.getZ());

        float damage = target == null ? 10f : ExplosionUtility.getDamageOfGhostBlock(crystalVec, target, bp);
        float selfDamage = ExplosionUtility.getDamageOfGhostBlock(crystalVec, mc.player, bp);
        boolean overrideDamage = ModuleManager.autoCrystal.shouldOverrideMaxSelfDmg(damage, selfDamage);

        if (ModuleManager.autoCrystal.protectFriends.getValue()) {
            List<PlayerEntity> players = Lists.newArrayList(mc.world.getPlayers());
            for (PlayerEntity pl : players) {
                if (!Managers.FRIEND.isFriend(pl)) continue;
                float fdamage = ExplosionUtility.getDamageOfGhostBlock(crystalVec, target, bp);
                if (fdamage > selfDamage) {
                    selfDamage = fdamage;
                }
            }
        }

        if (damage < 1.5f) return null;
        if (selfDamage > ModuleManager.autoCrystal.maxSelfDamage.getValue() && !overrideDamage) return null;

        BlockHitResult interactResult = ModuleManager.autoCrystal.getInteractResult(bp, crystalVec);
        if (interactResult == null) return null;

        return new AutoCrystal.PlaceData(interactResult, damage, selfDamage, overrideDamage);
    }

    private record ObbyData(BlockPos position, BlockHitResult bhr, float damage, float selfDamage,
                            boolean overrideDamage) {
    }

    private enum TargetLogic {
        Distance, HP, FOV
    }
}
