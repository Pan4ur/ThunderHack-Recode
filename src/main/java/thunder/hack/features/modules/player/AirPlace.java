package thunder.hack.features.modules.player;

import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class AirPlace extends Module {
    public AirPlace() {
        super("AirPlace", Category.PLAYER);
    }

    private final Setting<Float> range = new Setting<>("Range", 5f, 0f, 6f);

    private final Setting<SettingGroup> renderGroup = new Setting<>("Render", new SettingGroup(false, 0));
    private final Setting<Boolean> swing = new Setting<>("Swing", true).addToGroup(renderGroup);
    private final Setting<ColorSetting> fillColor = new Setting<>("Fill Color", new ColorSetting(new Color(100, 50, 255, 50))).addToGroup(renderGroup);
    private final Setting<ColorSetting> lineColor = new Setting<>("Line Color", new ColorSetting(new Color(100, 50, 255, 150))).addToGroup(renderGroup);
    private final Setting<Integer> lineWidth = new Setting<>("Line Width", 2, 1, 5).addToGroup(renderGroup);

    private BlockHitResult hit;
    private int cooldown;

    @Override
    public void onUpdate() {
        if (cooldown > 0)
            cooldown--;

        HitResult hitResult = mc.getCameraEntity().raycast(range.getValue(), 0, false);

        if (hitResult instanceof BlockHitResult bhr) hit = bhr;
        else return;

        boolean main = mc.player.getMainHandStack().getItem() instanceof BlockItem;
        boolean off = mc.player.getOffHandStack().getItem() instanceof BlockItem;
        if (mc.options.useKey.isPressed() && (main || off) && cooldown <= 0) {
            mc.interactionManager.interactBlock(mc.player, main ? Hand.MAIN_HAND : Hand.OFF_HAND, hit);
            if (swing.getValue()) mc.player.swingHand(main ? Hand.MAIN_HAND : Hand.OFF_HAND);
            else sendPacket(new HandSwingC2SPacket(main ? Hand.MAIN_HAND : Hand.OFF_HAND));
            cooldown = ModuleManager.fastUse.isEnabled() && (ModuleManager.fastUse.blocks.getValue() || ModuleManager.fastUse.all.getValue()) ? 0 : 4;
        }
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (hit == null || !mc.world.getBlockState(hit.getBlockPos()).getBlock().equals(Blocks.AIR) || (!(mc.player.getMainHandStack().getItem() instanceof BlockItem) && !(mc.player.getOffHandStack().getItem() instanceof BlockItem)))
            return;

        Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(
                new Box(hit.getBlockPos()),
                fillColor.getValue().getColorObject()
        ));
        Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(
                new Box(hit.getBlockPos()),
                lineColor.getValue().getColorObject(),
                lineWidth.getValue()
        ));
    }
}
