package thunder.hack.utility.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockAnimationUtility {
    private static final Map<BlockRenderData, Long> blocks = new ConcurrentHashMap<>();

    public static void onRender(MatrixStack matrixStack) {
        blocks.forEach((animation, time) -> {
            if (System.currentTimeMillis() - time > 300f) {
                blocks.remove(animation);
            } else {
                animation.renderWithTime(System.currentTimeMillis() - time, matrixStack);
            }
        });
    }

    public static void renderBlock(BlockPos pos, Color lineColor, int lineWidth, Color fillColor, BlockAnimationMode animationMode, BlockRenderMode renderMode) {
        if (renderMode == BlockRenderMode.None) return;

        blocks.put(new BlockRenderData(pos, lineColor, lineWidth, fillColor, animationMode, renderMode), System.currentTimeMillis());
    }

    public static boolean isRendering(BlockPos pos) {
        return blocks.keySet().stream().anyMatch(blockRenderData -> blockRenderData.pos().equals(pos));
    }

    private record BlockRenderData(BlockPos pos, Color lineColor, int lineWidth, Color fillColor,
                                   BlockAnimationMode animationMode, BlockRenderMode renderMode) {
        void renderWithTime(Long time, MatrixStack stack) {
            switch (animationMode) {
                case Static -> {
                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Line) {
                        Render3DEngine.drawBoxOutline(new Box(pos), lineColor, lineWidth);
                    }
                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Fill) {
                        Render3DEngine.drawFilledBox(stack, new Box(pos), fillColor);
                    }
                }
                case Decrease -> {
                    float scale = 1 - (float) time / 300f;
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Line)
                        Render3DEngine.drawBoxOutline(box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5, 0.5 + scale * 0.5, 0.5 + scale * 0.5), lineColor, lineWidth);

                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Fill)
                        Render3DEngine.drawFilledBox(stack, box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5, 0.5 + scale * 0.5, 0.5 + scale * 0.5), Render2DEngine.injectAlpha(fillColor, (int) (fillColor.getAlpha() * (1f - (time / 300f)))));
                }
                case Fade -> {
                    Box box = new Box(pos);
                    renderBox(time, stack, box, renderMode, lineColor, lineWidth, fillColor);
                }
                case Fill -> {
                    float scale = (float) time / 300f;
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Line)
                        Render3DEngine.drawBoxOutline(box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5, 0.5 + scale * 0.5, 0.5 + scale * 0.5), lineColor, lineWidth);

                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Fill)
                        Render3DEngine.drawFilledBox(stack, box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5, 0.5 + scale * 0.5, 0.5 + scale * 0.5), Render2DEngine.injectAlpha(fillColor, (int) (fillColor.getAlpha() * (time / 300f))));
                }
                case Flash -> {
                    float scale;
                    if (time > 100) {
                        scale = 1 - (float) (time - 100) / 400;
                    } else {
                        scale = (float) time / 100;
                    }


                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());

                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Line)
                        Render3DEngine.drawBoxOutline(box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5, 0.5 + scale * 0.5, 0.5 + scale * 0.5), lineColor, lineWidth);

                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Fill)
                        Render3DEngine.drawFilledBox(stack, box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5, 0.5 + scale * 0.5, 0.5 + scale * 0.5), Render2DEngine.injectAlpha(fillColor, (int) (fillColor.getAlpha() * scale)));
                }
                case Grow -> {
                    float scale = (float) time / 300f;
                    Box box = new Box(pos.getX(), pos.getY() + scale, pos.getZ(), pos.getX() + 1, pos.getY(), pos.getZ() + 1);

                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Line)
                        Render3DEngine.drawBoxOutline(box, lineColor, lineWidth);

                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Fill)
                        Render3DEngine.drawFilledBox(stack, box, Render2DEngine.injectAlpha(fillColor, (int) (fillColor.getAlpha() * (time / 300f))));
                }
                case TNT -> {
                    float scale;

                    if (time < 200) {
                        scale = 1f;
                    } else {
                        scale = 1 + (time - 200f) / 400f;
                    }


                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());

                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Line)
                        Render3DEngine.drawBoxOutline(box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5f, 0.5 + scale * 0.5, 0.5 + scale * 0.5), lineColor, lineWidth);

                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Fill)
                        Render3DEngine.drawFilledBox(stack, box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5, 0.5 + scale * 0.5, 0.5 + scale * 0.5), Render2DEngine.injectAlpha(fillColor, (int) (fillColor.getAlpha() * scale)));
                }
                case Pull -> {
                    float scale;

                    if (time < 200) {
                        scale = 1.5f - (time / 200f) * 0.5f;
                    } else {
                        scale = 1f;
                    }

                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());

                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Line)
                        Render3DEngine.drawBoxOutline(box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5f, 0.5 + scale * 0.5, 0.5 + scale * 0.5), lineColor, lineWidth);

                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Fill)
                        Render3DEngine.drawFilledBox(stack, box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5, 0.5 + scale * 0.5, 0.5 + scale * 0.5), Render2DEngine.injectAlpha(fillColor, (int) (fillColor.getAlpha() * scale)));
                }
                case Hover -> {
                    float scale;

                    scale = 1f + (time) / 1500f;

                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());

                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Line)
                        Render3DEngine.drawBoxOutline(box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5f, 0.5f + scale * 0.5, 0.5 + scale * 0.5), Render2DEngine.injectAlpha(lineColor, (int) (lineColor.getAlpha() * (1f - (time / 300f)))), lineWidth);

                    if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Fill)
                        Render3DEngine.drawFilledBox(stack, box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5, 0.5 + scale * 0.5, 0.5 + scale * 0.5), Render2DEngine.injectAlpha(fillColor, (int) (fillColor.getAlpha() * (1f - (time / 300f)))));
                }
            }
        }

        private static void renderBox(Long time, MatrixStack stack, Box box, BlockRenderMode renderMode, Color lineColor, int lineWidth, Color fillColor) {
            if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Line)
                Render3DEngine.drawBoxOutline(box, Render2DEngine.injectAlpha(lineColor, (int) (fillColor.getAlpha() * (1f - (time / 300f)))), lineWidth);

            if (renderMode == BlockRenderMode.All || renderMode == BlockRenderMode.Fill)
                Render3DEngine.drawFilledBox(stack, box, Render2DEngine.injectAlpha(fillColor, (int) (fillColor.getAlpha() * (1f - (time / 300f)))));
        }
    }

    public enum BlockRenderMode {
        Fill, Line, All, None
    }

    public enum BlockAnimationMode {
        Fade, Hover, Decrease, Static, Flash, Grow, Fill, TNT, Pull
    }
}
