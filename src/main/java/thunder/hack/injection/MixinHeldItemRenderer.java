package thunder.hack.injection;


import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventHeldItemRenderer;
import thunder.hack.modules.render.Animations;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.modules.render.Shaders;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.ShaderManager;

import static thunder.hack.modules.Module.mc;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"), cancellable = true)
    private void onRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        EventHeldItemRenderer event = new EventHeldItemRenderer( hand, item, equipProgress, matrices);
        Thunderhack.EVENT_BUS.post(event);
    }



    @Shadow
    public abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    // empty Filled map





    @Inject(method = "renderFirstPersonItem", at = @At(value = "HEAD"), cancellable = true)
    private void onRenderItemHook(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {

        if(Thunderhack.moduleManager != null && Thunderhack.moduleManager.get(Animations.class).isEnabled() && !(item.isEmpty()) && !(item.getItem() instanceof FilledMapItem)){
            ci.cancel();
            renderFirstPersonItemCustom(player,tickDelta,pitch,hand,swingProgress,item,equipProgress,matrices,vertexConsumers,light);
        }
    }




    private void renderFirstPersonItemCustom(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!player.isUsingSpyglass()) {
            boolean bl = hand == Hand.MAIN_HAND;
            Arm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();
            matrices.push();

                boolean bl2;
                float f;
                float g;
                float h;
                float j;
                if (item.isOf(Items.CROSSBOW)) {
                    bl2 = CrossbowItem.isCharged(item);
                    boolean bl3 = arm == Arm.RIGHT;
                    int i = bl3 ? 1 : -1;
                    if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                        this.applyEquipOffset(matrices, arm, equipProgress);
                        matrices.translate((float)i * -0.4785682F, -0.094387F, 0.05731531F);
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * 65.3F));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * -9.785F));
                        f = (float)item.getMaxUseTime() - ((float)mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                        g = f / (float)CrossbowItem.getPullTime(item);
                        if (g > 1.0F) {
                            g = 1.0F;
                        }

                        if (g > 0.1F) {
                            h = MathHelper.sin((f - 0.1F) * 1.3F);
                            j = g - 0.1F;
                            float k = h * j;
                            matrices.translate(k * 0.0F, k * 0.004F, k * 0.0F);
                        }

                        matrices.translate(g * 0.0F, g * 0.0F, g * 0.04F);
                        matrices.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
                        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)i * 45.0F));
                    } else {
                        f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                        g = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F);
                        h = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
                        matrices.translate((float)i * f, g, h);
                        this.applyEquipOffset(matrices, arm, equipProgress);
                        this.applySwingOffset(matrices, arm, swingProgress);
                        if (bl2 && swingProgress < 0.001F && bl) {
                            matrices.translate((float)i * -0.641864F, 0.0F, 0.0F);
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * 10.0F));
                        }
                    }

                    EventHeldItemRenderer event = new EventHeldItemRenderer(hand, item, equipProgress, matrices);
                    Thunderhack.EVENT_BUS.post(event);
                    this.renderItem(player, item, bl3 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl3, matrices, vertexConsumers, light);
                } else {
                    bl2 = arm == Arm.RIGHT;
                    int l;
                    float m;
                    if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                        l = bl2 ? 1 : -1;
                        switch (item.getUseAction()) {
                            case NONE:
                                this.applyEquipOffset(matrices, arm, equipProgress);
                                break;
                            case EAT:
                            case DRINK:
                                this.applyEatOrDrinkTransformation(matrices, tickDelta, arm, item);
                                this.applyEquipOffset(matrices, arm, equipProgress);
                                break;
                            case BLOCK:
                                this.applyEquipOffset(matrices, arm, equipProgress);
                                break;
                            case BOW:
                                this.applyEquipOffset(matrices, arm, equipProgress);
                                matrices.translate((float)l * -0.2785682F, 0.18344387F, 0.15731531F);
                                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
                                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)l * 35.3F));
                                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)l * -9.785F));
                                m = (float)item.getMaxUseTime() - ((float)mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                                f = m / 20.0F;
                                f = (f * f + f * 2.0F) / 3.0F;
                                if (f > 1.0F) {
                                    f = 1.0F;
                                }

                                if (f > 0.1F) {
                                    g = MathHelper.sin((m - 0.1F) * 1.3F);
                                    h = f - 0.1F;
                                    j = g * h;
                                    matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                                }

                                matrices.translate(f * 0.0F, f * 0.0F, f * 0.04F);
                                matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
                                matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)l * 45.0F));
                                break;
                            case SPEAR:
                                this.applyEquipOffset(matrices, arm, equipProgress);
                                matrices.translate((float)l * -0.5F, 0.7F, 0.1F);
                                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-55.0F));
                                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)l * 35.3F));
                                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)l * -9.785F));
                                m = (float)item.getMaxUseTime() - ((float)mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                                f = m / 10.0F;
                                if (f > 1.0F) {
                                    f = 1.0F;
                                }

                                if (f > 0.1F) {
                                    g = MathHelper.sin((m - 0.1F) * 1.3F);
                                    h = f - 0.1F;
                                    j = g * h;
                                    matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                                }

                                matrices.translate(0.0F, 0.0F, f * 0.2F);
                                matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
                                matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)l * 45.0F));
                                break;
                            case BRUSH:
                                this.applyBrushTransformation(matrices, tickDelta, arm, item, equipProgress);
                        }
                    } else if (player.isUsingRiptide()) {
                        this.applyEquipOffset(matrices, arm, equipProgress);
                        l = bl2 ? 1 : -1;
                        matrices.translate((float)l * -0.4F, 0.8F, 0.3F);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)l * 65.0F));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)l * -85.0F));
                    } else {
                   //     Animations.Mode mode = Animations.mode.getValue();
                       // if (mode == Animations.Mode.IDK) {
                            float n = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                            m = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F);
                            f = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
                            int o = bl2 ? 1 : -1;
                            matrices.translate(0, 0, 0);
                            this.applyEquipOffset(matrices, arm, equipProgress);
                            this.applySwingOffset(matrices, arm, swingProgress);
                       /* } else if (mode == Animations.Mode.Default) {
                            float var4 = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927f);
                            matrices.translate(0, 0, 0);
                            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(var4 * -20.0f));
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(var4 * -75.0f));

                            this.applyEquipOffset(matrices, arm, equipProgress);
                            this.applySwingOffset(matrices, arm, swingProgress);
                        }

                        */
                        /*
                        else if (mode == Animations.Mode.Swipe) {
                            transformFirstPersonItem(equipProgress / 3.0f, swingprogress);
                            translate();
                            float var3 = MathHelper.sin(swingprogress * swingprogress * 3.1415927f);
                            float var4 = MathHelper.sin(MathHelper.sqrt(swingprogress) * 3.1415927f);
                            GlStateManager.rotate(var3 * -20.0f, 0.0f, 1.0f, 0.0f);
                            GlStateManager.rotate(var4 * -20.0f, 0.0f, 0.0f, 2.0f);
                            GlStateManager.rotate(var4 * -75.0f, 1.0f, 0.0f, 0.0f);
                        } else if (mode == Animations.Mode.Rich) {
                            transformSideFirstPerson2(enumhandside, p_187457_7_);
                            translate4();
                            float var3 = MathHelper.sin(swingprogress * swingprogress * (float) Math.PI);
                            float var4 = MathHelper.sin(MathHelper.sqrt(swingprogress) * (float) Math.PI);
                            GlStateManager.rotate(var4 * -20.0f, 0.0f, 0.0f, 2.0f);
                            GlStateManager.rotate(var4 * -75.0f, 1.0f, 0.0f, 0.0f);
                        } else if (mode == Animations.Mode.New) {
                            transformSideFirstPerson2(enumhandside, p_187457_7_);
                            translate3();
                            float var3 = MathHelper.sin(swingprogress * swingprogress * 3.1415927f);
                            float var4 = MathHelper.sin(MathHelper.sqrt(swingprogress) * 3.1415927f);
                            GlStateManager.rotate(var4 * -70, var4 * 40, 0.0f, 0);
                            GlStateManager.rotate(40, -30, 0.0f, 0);
                        } else if (mode == Animations.Mode.Oblique) {
                            transformSideFirstPerson2(enumhandside, p_187457_7_);
                            float var4 = MathHelper.sin(MathHelper.sqrt(swingprogress) * 3.1415927f);
                            GlStateManager.rotate(var4 * -70, var4 * 70, 0.0f, var4 * -90);
                        } else if (mode == Animations.Mode.Glide) {
                            transformFirstPersonItem(equipProgress / 2, 0);
                            translate();
                        } else if (mode == Animations.Mode.Fap) {
                            transformSideFirstPerson2(enumhandside, p_187457_7_);
                            GlStateManager.translate(0.96f, -0.02f, -0.71999997f);
                            GlStateManager.rotate(45.0f, 0.0f, 1.0f, 0.0f);
                            float var3 = MathHelper.sin(0.0f);
                            float var4 = MathHelper.sin(MathHelper.sqrt(0.0f) * 3.1415927f);
                            GlStateManager.rotate(var3 * -20.0f, 0.0f, 1.0f, 0.0f);
                            GlStateManager.rotate(var4 * -20.0f, 0.0f, 0.0f, 1.0f);
                            GlStateManager.rotate(var4 * -80.0f, 1.0f, 0.0f, 0.0f);
                            GlStateManager.translate(-0.5f, 0.2f, 0.0f);
                            GlStateManager.rotate(30.0f, 0.0f, 1.0f, 0.0f);
                            GlStateManager.rotate(-80.0f, 1.0f, 0.0f, 0.0f);
                            GlStateManager.rotate(60.0f, 0.0f, 1.0f, 0.0f);
                            int alpha = (int) Math.min(255L, (System.currentTimeMillis() % 255L > 127L ? Math.abs(Math.abs(System.currentTimeMillis()) % 255L - 255L) : System.currentTimeMillis() % 255L) * 2L);
                            float f5 = (double) f1 > 0.5 ? 1.0f - f1 : f1;
                            GlStateManager.translate(0.3f, -0.0f, 0.4f);
                            GlStateManager.rotate(0.0f, 0.0f, 0.0f, 1.0f);
                            GlStateManager.translate(0.0f, 0.5f, 0.0f);
                            GlStateManager.rotate(90.0f, 1.0f, 0.0f, -1.0f);
                            GlStateManager.translate(0.6f, 0.5f, 0.0f);
                            GlStateManager.rotate(-90.0f, 1.0f, 0.0f, -1.0f);
                            GlStateManager.rotate(-10.0f, 1.0f, 0.0f, -1.0f);
                            GlStateManager.rotate((-f5) * 10.0f, 10.0f, 10.0f, -9.0f);
                            GlStateManager.rotate(10.0f, -1.0f, 0.0f, 0.0f);


                            GlStateManager.translate(0.0, 0.0, -0.5);
                            GlStateManager.rotate(Thunderhack.moduleManager.getModuleByClass(Animations.class).abobka228 ? (float) (-alpha) / Thunderhack.moduleManager.getModuleByClass(Animations.class).fapSmooth.getValue() : 1.0f, 1.0f, -0.0f, 1.0f);
                            GlStateManager.translate(0.0, 0.0, 0.5);
                        }

                         */
                    }


                    EventHeldItemRenderer event = new EventHeldItemRenderer(hand, item, equipProgress, matrices);
                    Thunderhack.EVENT_BUS.post(event);
                    this.renderItem(player, item, bl2 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl2, matrices, vertexConsumers, light);
                }
            matrices.pop();
        }
    }

    private void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate((float)i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    private void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        float f = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * (45.0F + f * -20.0F)));
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * g * -20.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * -45.0F));
    }

    private void applyEatOrDrinkTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack) {
        float f = (float)mc.player.getItemUseTimeLeft() - tickDelta + 1.0F;
        float g = f / (float)stack.getMaxUseTime();
        float h;
        if (g < 0.8F) {
            h = MathHelper.abs(MathHelper.cos(f / 4.0F * 3.1415927F) * 0.005F);
            matrices.translate(0.0F, h, 0.0F);
        }

        h = 1.0F - (float)Math.pow((double)g, 27.0);
        int i = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate(h * 0.6F * (float)i, h * -0.5F, h * 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * h * 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * 10.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * h * 30.0F));
    }

    private void applyBrushTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, float equipProgress) {
        this.applyEquipOffset(matrices, arm, equipProgress);
        float f = (float)mc.player.getItemUseTimeLeft() - tickDelta + 1.0F;
        float g = 1.0F - f / (float)stack.getMaxUseTime();
        float m = -15.0F + 75.0F * MathHelper.cos(g * 45.0F * 3.1415927F);
        if (arm != Arm.RIGHT) {
            matrices.translate(0.1, 0.83, 0.35);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
            matrices.translate(-0.3, 0.22, 0.35);
        } else {
            matrices.translate(-0.25, 0.22, 0.35);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
        }

    }
}