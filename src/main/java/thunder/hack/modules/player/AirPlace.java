package thunder.hack.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import thunder.hack.core.PlaceManager;
import thunder.hack.injection.accesors.IMinecraftClient;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class AirPlace extends Module {
    private final Setting<Float> range = new Setting<>("Range", 5f, 0f, 6f);

    private final Setting<Parent> renderGroup = new Setting<>("Render", new Parent(false, 0));
    private final Setting<Boolean> swing = new Setting<>("Swing", true).withParent(renderGroup);
    private final Setting<ColorSetting> fillColor = new Setting<>("Fill Color", new ColorSetting(new Color(100, 50, 255, 50))).withParent(renderGroup);
    private final Setting<ColorSetting> lineColor = new Setting<>("Line Color", new ColorSetting(new Color(100, 50, 255, 150))).withParent(renderGroup);
    private final Setting<Integer> lineWidth = new Setting<>("Line Width", 2, 1, 5).withParent(renderGroup);

    private BlockHitResult hit;

    public AirPlace() {
        super("AirPlace", "Ставишь блоки в воздухе", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        HitResult hitResult = mc.getCameraEntity().raycast(range.getValue(), 0, false);

        if (hitResult instanceof BlockHitResult) hit = (BlockHitResult) hitResult;
        else return;

        if (mc.options.useKey.isPressed()
                && mc.player.getMainHandStack().getItem() instanceof BlockItem) {
            if (mc.player.isSprinting() && !PlaceManager.syncSprinting) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                PlaceManager.syncSprinting = true;
            }

            if (!mc.player.isSneaking() && !PlaceManager.syncSneaking) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                PlaceManager.syncSneaking = true;
            }

            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);

            if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
            else mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        // это чтоб не фастплейсило
        ((IMinecraftClient)mc).setUseCooldown(4);
    }

    public void onRender3D(MatrixStack stack) {
        if (hit == null || !mc.world.getBlockState(hit.getBlockPos()).getBlock().equals(Blocks.AIR)) return;

        Render3DEngine.drawFilledBox(stack, new Box(hit.getBlockPos()), fillColor.getValue().getColorObject());
        Render3DEngine.drawBoxOutline(new Box(hit.getBlockPos()), lineColor.getValue().getColorObject(), lineWidth.getValue());
    }
}
