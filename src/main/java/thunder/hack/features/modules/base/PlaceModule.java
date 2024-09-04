package thunder.hack.features.modules.base;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.BlockAnimationUtility;

import java.util.ArrayList;
import java.util.List;

public abstract class PlaceModule extends Module {
    protected final Setting<Float> range = new Setting<>("Range", 5f, 0f, 7f);
    protected final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Strict);
    protected final Setting<InteractMode> placeMode = new Setting<>("Place Mode", InteractMode.Normal);
    protected final Setting<InteractionUtility.Rotate> rotate = new Setting<>("Rotate", InteractionUtility.Rotate.None);
    protected final Setting<Boolean> swing = new Setting<>("Swing", false);

    protected final Setting<BooleanSettingGroup> crystalBreaker = new Setting<>("Crystal Breaker", new BooleanSettingGroup(false));
    protected final Setting<Integer> crystalAge = new Setting<>("CrystalAge", 0, 0, 20).addToGroup(crystalBreaker);
    protected final Setting<Integer> breakDelay = new Setting<>("Break Delay", 100, 1, 1000).addToGroup(crystalBreaker);
    protected final Setting<Boolean> remove = new Setting<>("Remove", false).addToGroup(crystalBreaker);
    protected final Setting<InteractMode> breakCrystalMode = new Setting<>("Break Mode", InteractMode.Normal).addToGroup(crystalBreaker);
    protected final Setting<Boolean> antiWeakness = new Setting<>("Anti Weakness", false).addToGroup(crystalBreaker);

    protected final Setting<SettingGroup> blocks = new Setting<>("Blocks", new SettingGroup(false, 0));
    protected final Setting<Boolean> obsidian = new Setting<>("Obsidian", true).addToGroup(blocks);
    protected final Setting<Boolean> anchor = new Setting<>("Anchor", false).addToGroup(blocks);
    protected final Setting<Boolean> enderChest = new Setting<>("EnderChest", true).addToGroup(blocks);
    protected final Setting<Boolean> netherite = new Setting<>("Netherite", false).addToGroup(blocks);
    protected final Setting<Boolean> cryingObsidian = new Setting<>("Crying Obsidian", true).addToGroup(blocks);
    protected final Setting<Boolean> dirt = new Setting<>("Dirt", false).addToGroup(blocks);
    protected final Setting<Boolean> oakPlanks = new Setting<>("OakPlanks", false).addToGroup(blocks);

    protected final Setting<SettingGroup> pause = new Setting<>("Pause", new SettingGroup(false, 0));
    protected final Setting<Boolean> eatPause = new Setting<>("On Eat", false).addToGroup(pause);
    protected final Setting<Boolean> breakPause = new Setting<>("On Break", false).addToGroup(pause);

    protected final Setting<BooleanSettingGroup> render = new Setting<>("Render", new BooleanSettingGroup(true));
    protected final Setting<BlockAnimationUtility.BlockRenderMode> renderMode = new Setting<>("Render Mode", BlockAnimationUtility.BlockRenderMode.All).addToGroup(render);
    protected final Setting<BlockAnimationUtility.BlockAnimationMode> animationMode = new Setting<>("Animation Mode", BlockAnimationUtility.BlockAnimationMode.Fade).addToGroup(render);
    protected final Setting<ColorSetting> renderFillColor = new Setting<>("Fill Color", new ColorSetting(HudEditor.getColor(0))).addToGroup(render);
    protected final Setting<ColorSetting> renderLineColor = new Setting<>("Line Color", new ColorSetting(HudEditor.getColor(0))).addToGroup(render);
    protected final Setting<Integer> renderLineWidth = new Setting<>("Line Width", 2, 1, 5).addToGroup(render);

    public final Timer inactivityTimer = new Timer();
    public final Timer pauseTimer = new Timer();
    protected final Timer attackTimer = new Timer();

    public PlaceModule(@NotNull String name, @NotNull Category category) {
        super(name, category);
    }

    protected boolean shouldPause() {
        return (eatPause.getValue() && PlayerUtility.isEating())
                || (breakPause.getValue() && PlayerUtility.isMining()) || !pauseTimer.passedMs(350) ;
    }

    protected boolean placeBlock(BlockPos pos) {
        return placeBlock(pos, crystalBreaker.getValue().isEnabled(), placeMode.getValue(), rotate.getValue());
    }

    protected boolean placeBlock(BlockPos pos, boolean ignoreEntities) {
        return placeBlock(pos, ignoreEntities, placeMode.getValue(), rotate.getValue());
    }

    protected boolean placeBlock(BlockPos pos, InteractionUtility.Rotate rotate) {
        return placeBlock(pos, crystalBreaker.getValue().isEnabled(), placeMode.getValue(), rotate);
    }

    protected boolean placeBlock(BlockPos pos, InteractMode mode) {
        return placeBlock(pos, crystalBreaker.getValue().isEnabled(), mode, rotate.getValue());
    }

    protected boolean placeBlock(BlockPos pos, boolean ignoreEntities, InteractMode mode, InteractionUtility.Rotate rotate) {
        if (shouldPause()) return false;
        boolean validInteraction = false;
        SearchInvResult result = getBlockResult();

        if (!result.found())
            return false;

        if (crystalBreaker.getValue().isEnabled()
                && mc.world != null
                && attackTimer.passedMs(breakDelay.getValue())) {
            mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos))
                    .stream()
                    .findFirst()
                    .ifPresent(this::breakCrystal);
        }

        if (mode == InteractMode.Packet) {
            validInteraction = InteractionUtility.placeBlock(pos, rotate, interact.getValue(), InteractionUtility.PlaceMode.Packet, result.slot(), true, ignoreEntities);
        }
        if (mode == InteractMode.Normal) {
            validInteraction = InteractionUtility.placeBlock(pos, rotate, interact.getValue(), InteractionUtility.PlaceMode.Normal, result.slot(), true, ignoreEntities);
        }

        if (validInteraction && mc.player != null) {
            if (render.getValue().isEnabled())
                renderBlock(pos);
            if (swing.getValue())
                mc.player.swingHand(Hand.MAIN_HAND);
        }

        return validInteraction;
    }

    protected void breakCrystal(EndCrystalEntity entity) {
        if (mc.player == null || mc.world == null
                || mc.interactionManager == null
                || shouldPause()
                || !attackTimer.passedMs(breakDelay.getValue())
                || mc.player.squaredDistanceTo(entity) > range.getPow2Value()
                || entity.age < crystalAge.getValue())
            return;

        int preSlot = mc.player.getInventory().selectedSlot;
        if (antiWeakness.getValue() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
            final SearchInvResult result = InventoryUtility.getAntiWeaknessItem();
            if (!result.found())
                return;

            result.switchTo();
        }

        if (breakCrystalMode.getValue() == InteractMode.Packet)
            sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));

        if (breakCrystalMode.getValue() == InteractMode.Normal)
            mc.interactionManager.attackEntity(mc.player, entity);

        sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        attackTimer.reset();

        if (remove.getValue()) {
            entity.kill();
            entity.setRemoved(Entity.RemovalReason.KILLED);
            entity.onRemoved();
        }

        if (antiWeakness.getValue() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS))
            InventoryUtility.switchTo(preSlot);
    }

    protected boolean canPlaceBlock(BlockPos pos, boolean ignoreEntities) {
        return InteractionUtility.canPlaceBlock(pos, interact.getValue(), ignoreEntities);
    }

    protected void renderBlock(BlockPos pos) {
        BlockAnimationUtility.renderBlock(pos,
                renderLineColor.getValue().getColorObject(),
                renderLineWidth.getValue(),
                renderFillColor.getValue().getColorObject(),
                animationMode.getValue(),
                renderMode.getValue()
        );
    }

    protected SearchInvResult getBlockResult() {
        final List<Block> canUseBlocks = new ArrayList<>();
        if (mc.player == null) return SearchInvResult.notFound();
        if (obsidian.getValue()) canUseBlocks.add(Blocks.OBSIDIAN);
        if (enderChest.getValue()) canUseBlocks.add(Blocks.ENDER_CHEST);
        if (cryingObsidian.getValue()) canUseBlocks.add(Blocks.CRYING_OBSIDIAN);
        if (netherite.getValue()) canUseBlocks.add(Blocks.NETHERITE_BLOCK);
        if (anchor.getValue()) canUseBlocks.add(Blocks.RESPAWN_ANCHOR);
        if (dirt.getValue())
            canUseBlocks.addAll(List.of(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.PODZOL));
        if (oakPlanks.getValue())
            canUseBlocks.addAll(List.of(Blocks.OAK_PLANKS, Blocks.BIRCH_PLANKS, Blocks.DARK_OAK_PLANKS));
        final ItemStack mainHandStack = mc.player.getMainHandStack();
        if (mainHandStack != ItemStack.EMPTY && mainHandStack.getItem() instanceof BlockItem) {
            final Block blockFromMainHandItem = ((BlockItem) mainHandStack.getItem()).getBlock();
            if (canUseBlocks.contains(blockFromMainHandItem))
                return new SearchInvResult(mc.player.getInventory().selectedSlot, true, mainHandStack);
        }

        return InventoryUtility.findBlockInHotBar(canUseBlocks);
    }

    public void pause() {
        pauseTimer.reset();
    }

    protected enum InteractMode {
        Packet,
        Normal
    }
}
