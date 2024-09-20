package thunder.hack.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class TotemAnimation extends Module {
    public TotemAnimation() {
        super("TotemAnimation", Category.RENDER);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.FadeOut);
    private final Setting<Integer> speed = new Setting<>("Speed", 40, 1, 100);

    private ItemStack floatingItem = null;
    private int floatingItemTimeLeft;

    public void showFloatingItem(ItemStack floatingItem) {
        this.floatingItem = floatingItem;
        floatingItemTimeLeft = getTime();
    }

    @Override
    public void onUpdate() {
        if (floatingItemTimeLeft > 0) {
            --floatingItemTimeLeft;
            if (floatingItemTimeLeft == 0) {
                floatingItem = null;
            }
        }
    }

    public void renderFloatingItem(float tickDelta) {
        if (floatingItem != null && floatingItemTimeLeft > 0 && !mode.is(Mode.Off)) {
            int scaledWidth = mc.getWindow().getScaledWidth();
            int scaledHeight = mc.getWindow().getScaledHeight();

            int elapsedTime = getTime() - floatingItemTimeLeft;
            float animationProgress = ((float) elapsedTime + tickDelta) / (float) getTime();
            float progressSquared = animationProgress * animationProgress;
            float progressCubed = animationProgress * progressSquared;
            float oscillationFactor = 10.25F * progressCubed * progressSquared - 24.95F * progressSquared * progressSquared + 25.5F * progressCubed - 13.8F * progressSquared + 4.0F * animationProgress;
            float oscillationRadians = oscillationFactor * 3.1415927F;
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.push();
            float adjustedProgress = ((float) elapsedTime + tickDelta);
            float scale = 50.0F + 175.0F * MathHelper.sin(oscillationRadians);

            switch (mode.getValue()) {
                case FadeOut -> {
                    final float x2 = (float) (Math.sin(((adjustedProgress * 112) / 180f)) * 100);
                    final float y2 = (float) (Math.cos(((adjustedProgress * 112) / 180f)) * 50);
                    matrixStack.translate((float) (scaledWidth / 2) + x2, (float) (scaledHeight / 2) + y2, -50.0F);
                    matrixStack.scale(scale, -scale, scale);
                }

                case Size -> {
                    matrixStack.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), -50.0F);
                    matrixStack.scale(scale, -scale, scale);
                }

                case Otkisuli -> {
                    matrixStack.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), -50.0F);
                    matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(adjustedProgress * 2));
                    matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(adjustedProgress * 2));
                    matrixStack.scale(200 - adjustedProgress * 1.5f, -200 + adjustedProgress * 1.5f, 200 - adjustedProgress * 1.5f);
                }

                case Insert -> {
                    matrixStack.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), -50.0F);
                    matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(adjustedProgress * 3));
                    matrixStack.scale(200 - adjustedProgress * 1.5f, -200 + adjustedProgress * 1.5f, 200 - adjustedProgress * 1.5f);
                }

                case Fall -> {
                    float downFactor = (float) (Math.pow(adjustedProgress, 3) * 0.2f);
                    matrixStack.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2) + downFactor, -50.0F);
                    matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(adjustedProgress * 5));
                    matrixStack.scale(200 - adjustedProgress * 1.5f, -200 + adjustedProgress * 1.5f, 200 - adjustedProgress * 1.5f);
                }

                case Rocket -> {
                    float downFactor = (float) (Math.pow(adjustedProgress, 3) * 0.2f) - 20;
                    matrixStack.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2) - downFactor, -50.0F);
                    matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(adjustedProgress * floatingItemTimeLeft * 2));
                    matrixStack.scale(200 - adjustedProgress * 1.5f, -200 + adjustedProgress * 1.5f, 200 - adjustedProgress * 1.5f);
                }

                case Roll -> {
                    float rightFactor = (float) (Math.pow(adjustedProgress, 2) * 4.5f);
                    matrixStack.translate((float) (scaledWidth / 2) + rightFactor, (float) (scaledHeight / 2), -50.0F);
                    matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(adjustedProgress * 40));
                    matrixStack.scale(200 - adjustedProgress * 1.5f, -200 + adjustedProgress * 1.5f, 200 - adjustedProgress * 1.5f);
                }
            }

            VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f - animationProgress);
            mc.getItemRenderer().renderItem(floatingItem, ModelTransformationMode.FIXED, 15728880, OverlayTexture.DEFAULT_UV, matrixStack, immediate, mc.world, 0);
            matrixStack.pop();
            immediate.draw();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();
        }
    }

    private int getTime() {
        int invertedSpeed = 101 - speed.getValue();

        if (mode.is(Mode.FadeOut))
            return invertedSpeed / 4;

        if (mode.is(Mode.Insert))
            return invertedSpeed / 2;

        return invertedSpeed;
    }

    private enum Mode {
        FadeOut, Size, Otkisuli, Insert, Fall, Rocket, Roll, Off
    }
}
