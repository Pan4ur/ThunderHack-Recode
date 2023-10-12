package dev.thunderhack.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.thunderhack.event.events.EventPostSync;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.ColorSetting;
import dev.thunderhack.utils.render.Render2DEngine;
import dev.thunderhack.utils.render.Render3DEngine;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import dev.thunderhack.modules.client.HudEditor;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BreadCrumbs extends Module {
    public BreadCrumbs() {
        super("BreadCrumbs", Category.RENDER);
    }

    private final Setting<Integer> limit = new Setting<>("ListLimit", 1000, 10, 99999);
    private final List<Vec3d> positions = new CopyOnWriteArrayList<>();
    private final Setting<Mode> lmode = new Setting<>("ColorMode", Mode.Sync);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(3649978), v -> lmode.getValue() == Mode.Custom);

    private enum Mode {
        Custom, Sync
    }

    public void onRender3D(MatrixStack stack) {
        drawLine(2f, false);
        drawLine(5, false);
        drawLine(10, false);
        drawLine(1, true);
    }

    public void drawLine(float width, boolean white) {
        Render3DEngine.setup();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(width);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        for (int i = 0; i < positions.size(); i++) {
            Vec3d vec1 = null;
            try {
                vec1 = positions.get(i - 1);
            } catch (Exception ignored) {
            }
            Vec3d vec2 = positions.get(i);
            if (vec1 != null && vec2 != null) {
                Color c = lmode.getValue() == Mode.Sync ? HudEditor.getColor(i) : color.getValue().getColorObject();
                if (white) c = Color.WHITE;

                if (i < 10) c = Render2DEngine.injectAlpha(c, (int) (c.getAlpha() * (i / 10f)));
                MatrixStack matrices = Render3DEngine.matrixFrom(vec1.getX(), vec1.getY(), vec1.getZ());
                Render3DEngine.vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (vec2.getX() - vec1.getX()), (float) (vec2.getY() - vec1.getY()), (float) (vec2.getZ() - vec1.getZ()), c);
            }
        }

        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        Render3DEngine.cleanup();
    }

    @EventHandler
    public void postSync(EventPostSync e) {
        if (positions.size() > limit.getValue()) positions.remove(0);
        positions.add(new Vec3d(mc.player.getX(), mc.player.getBoundingBox().minY, mc.player.getZ()));
    }

    @Override
    public void onDisable() {
        positions.clear();
    }
}
