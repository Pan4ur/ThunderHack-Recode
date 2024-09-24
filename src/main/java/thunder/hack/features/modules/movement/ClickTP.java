package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.impl.EventSync;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render3DEngine;

public class ClickTP extends Module {
    public ClickTP() {
        super("ClickTP", Category.MOVEMENT);
    }

    private final Setting<Float> blockOffset = new Setting<>("BlockOffset", 1.0f, -1f, 1f);
    private final Setting<Integer> spoofs = new Setting<>("Spoofs", 0, 0, 40);
    private final Setting<Boolean> ground = new Setting<>("Ground", false);

    private int delay;

    @EventHandler
    public void onSync(EventSync e) {
        if (delay >= 0)
            delay--;

        if (mc.options.pickItemKey.isPressed() && delay < 0) {
            HitResult ray = mc.player.raycast(256, Render3DEngine.getTickDelta(), false);
            if (ray instanceof BlockHitResult bhr && !mc.world.isAir(bhr.getBlockPos())) {
                Vec3d pos = bhr.getBlockPos().toCenterPos();
                for (int i = 0; i < spoofs.getValue(); ++i)
                    sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.getX(), pos.getY() + blockOffset.getValue(), pos.getZ(), ground.getValue()));
                mc.player.setPosition(pos.getX(), pos.getY() + blockOffset.getValue(), pos.getZ());
                delay = 5;
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        HitResult ray = mc.player.raycast(256, Render3DEngine.getTickDelta(), false);
        if (ray instanceof BlockHitResult bhr && !mc.world.isAir(bhr.getBlockPos())) {
            BlockPos pos = bhr.getBlockPos();
            Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(new Box(pos), HudEditor.getColor(1), 1));
        }
    }
}
