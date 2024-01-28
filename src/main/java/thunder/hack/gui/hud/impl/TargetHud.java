package thunder.hack.gui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL40C;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudEditorGui;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.combat.Aura;
import thunder.hack.modules.combat.AutoCrystal;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.BetterAnimation;
import thunder.hack.utility.render.animation.BetterDynamicAnimation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TargetHud extends HudElement {
    private static final Identifier thudPic = new Identifier("textures/thud.png");

    public static BetterDynamicAnimation healthanimation = new BetterDynamicAnimation();
    public static BetterDynamicAnimation ebaloAnimation = new BetterDynamicAnimation();
    private final ArrayList<Particles> particles = new ArrayList<>();
    private final Timer timer = new Timer();
    public BetterAnimation animation = new BetterAnimation();
    float ticks;

    private final Setting<Integer> blurRadius = new Setting<>("BallonBlur", 10, 1, 10);
    private final Setting<Integer> animX = new Setting<>("AnimationX", 0, -2000, 2000);
    private final Setting<Integer> animY = new Setting<>("AnimationY", 0, -2000, 2000);
    private final Setting<HPmodeEn> hpMode = new Setting<>("HP Mode", HPmodeEn.HP);
    private final Setting<ImageModeEn> imageMode = new Setting<>("Image", ImageModeEn.Anime);
    private final Setting<ModeEn> Mode = new Setting<>("Mode", ModeEn.ThunderHack);
    private final Setting<ColorSetting> color = new Setting<>("Color1", new ColorSetting(-16492289), v -> Mode.getValue() == ModeEn.CelkaPasta);
    private final Setting<ColorSetting> color2 = new Setting<>("Color2", new ColorSetting(-16492289), v -> Mode.getValue() == ModeEn.CelkaPasta);
    private final Setting<Boolean> funTimeHP = new Setting<>("FunTimeHP", false);
    private final Setting<Boolean> mini = new Setting<>("Mini", false, v -> Mode.getValue() == ModeEn.NurikZapen);

    private boolean sentParticles;
    private boolean direction = false;
    private LivingEntity target;

    public TargetHud() {
        super("TargetHud", 150, 50);
    }

    public static void sizeAnimation(MatrixStack matrixStack, double width, double height, double animation) {
        matrixStack.translate(width, height, 0);
        matrixStack.scale((float) animation, (float) animation, 1);
        matrixStack.translate(-width, -height, 0);
    }

    public static String getPotionName(StatusEffect potion) {
        if (potion == StatusEffects.REGENERATION) {
            return "Reg";
        } else if (potion == StatusEffects.STRENGTH) {
            return "Str";
        } else if (potion == StatusEffects.SPEED) {
            return "Spd";
        } else if (potion == StatusEffects.HASTE) {
            return "H";
        } else if (potion == StatusEffects.WEAKNESS) {
            return "W";
        } else if (potion == StatusEffects.RESISTANCE) {
            return "Res";
        }
        return "pon";
    }

    public static String getDurationString(StatusEffectInstance pe) {
        if (pe.isInfinite()) {
            return "*:*";
        } else {
            int var1 = pe.getDuration();
            int mins = var1 / 1200;
            int sec = (var1 % 1200) / 20;

            return mins + ":" + sec;
        }
    }

    @Override
    public void onUpdate() {
        animation.update(direction);
        healthanimation.update();
        ebaloAnimation.update();
    }

    //  сила скорка спешка слабость реген сопротивление
    //  Str 1:23 Spd2 1:23 H3 1:23 Reg4 1:23 Res5 1:23

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        //таргеты

        if (AutoCrystal.target != null) {
            target = AutoCrystal.target;
            direction = true;
            if (AutoCrystal.target.isDead()) {
                AutoCrystal.target = null;
                return;
            }
        } else if (Aura.target != null) {
            if (Aura.target instanceof LivingEntity) {
                target = (LivingEntity) Aura.target;
                direction = true;
            } else {
                target = null;
                direction = false;
            }
        } else if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudEditorGui) {
            target = mc.player;
            direction = true;
        } else {
            direction = false;
            if (animation.getAnimationd() < 0.02)
                target = null;
        }
        if (target == null) return;

        context.getMatrices().push();
        sizeAnimation(context.getMatrices(), getPosX() + 75 + animX.getValue(), getPosY() + 25 + animY.getValue(), animation.getAnimationd());

        if (animation.getAnimationd() > 0) {

            if (Mode.getValue() == ModeEn.ThunderHack) {
                float hurtPercent = target.hurtTime / 6f;

                // Основа
                Render2DEngine.drawRound(context.getMatrices(), getPosX(), getPosY(), 70, 50, 6, new Color(0, 0, 0, 139));
                Render2DEngine.drawRound(context.getMatrices(), getPosX() + 50, getPosY(), 100, 50, 6, new Color(0, 0, 0, 255));

                // Картинка
                if (imageMode.getValue() == ImageModeEn.Anime) {
                    context.getMatrices().push();

                    RenderSystem.setShaderTexture(0, thudPic);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    Render2DEngine.drawRound(context.getMatrices(), getPosX() + 50, getPosY(), 100, 50, 12, new Color(0, 0, 0, 255));
                    RenderSystem.disableBlend();
                    RenderSystem.setShaderColor(0.3f, 0.3f, 0.3f, 1f);
                    Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 50, getPosY(), 95, 50, 0, 0, 100, 50, 100, 50);
                    context.getMatrices().pop();
                }

                //Партиклы
                for (final Particles p : particles) {
                    if (p.opacity > 4) p.render2D(context.getMatrices());
                }

                if (timer.passedMs(1000 / 60)) {
                    ticks += 0.1f;
                    for (final Particles p : particles) {
                        p.updatePosition();

                        if (p.opacity < 1) particles.remove(p);
                    }
                    timer.reset();
                }

                final java.util.ArrayList<Particles> removeList = new java.util.ArrayList<>();
                for (final Particles p : particles) {
                    if (p.opacity <= 1) {
                        removeList.add(p);
                    }
                }

                for (final Particles p : removeList) {
                    particles.remove(p);
                }

                if ((target.hurtTime == 9 && !sentParticles)) {
                    for (int i = 0; i <= 6; i++) {
                        final Particles p = new Particles();
                        final Color c = Particles.mixColors(color.getValue().getColorObject(), color2.getValue().getColorObject(), (Math.sin(ticks + getPosX() * 0.4f + i) + 1) * 0.5f);
                        p.init(getPosX(), getPosY(), MathUtility.random(-3f, 3f), MathUtility.random(-3f, 3f), 20, c);
                        particles.add(p);
                    }
                    sentParticles = true;
                }

                if (target.hurtTime == 8) sentParticles = false;

                // Бошка
                float hurtPercent2 = hurtPercent;
                ebaloAnimation.setValue(hurtPercent2);
                hurtPercent2 = (float) ebaloAnimation.getAnimationD();
                if (hurtPercent2 < 0 && hurtPercent2 > -0.17) {
                    hurtPercent2 = 0;
                }

                if (target instanceof PlayerEntity) {
                    RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity) target).getSkinTextures().texture());
                } else {
                    RenderSystem.setShaderTexture(0, mc.getEntityRenderDispatcher().getRenderer(target).getTexture(target));
                }
                RenderSystem.enableBlend();
                RenderSystem.colorMask(false, false, false, true);
                RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
                RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
                RenderSystem.colorMask(true, true, true, true);

                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                Render2DEngine.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), 1f, 1f - hurtPercent, 1f - hurtPercent, 1f, getPosX() + 2.5f + hurtPercent2, getPosY() + 2.5f + hurtPercent2, getPosX() + 2.5f - hurtPercent + 44, getPosY() + 2.5f - hurtPercent + 44, 5, 10);

                RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
                RenderSystem.setShaderColor(1f, 1f - hurtPercent, 1f - hurtPercent, 1f);
                Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 2.5f + hurtPercent2, getPosY() + 2.5f + hurtPercent2, 44 - hurtPercent2 * 2, 44 - hurtPercent2 * 2, 8, 8, 8, 8, 64, 64);
                Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 2.5f + hurtPercent2, getPosY() + 2.5f + hurtPercent2, 44 - hurtPercent2 * 2, 44 - hurtPercent2 * 2, 40, 8, 8, 8, 64, 64);
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();

                // Баллон
                float health = Math.min(20, getHealth());
                healthanimation.setValue(health);
                health = (float) healthanimation.getAnimationD();

                Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX() + 55, getPosY() + 22, 90, 8, blurRadius.getValue(), HudEditor.getColor(0));

                Render2DEngine.drawGradientRound(context.getMatrices(), getPosX() + 55, getPosY() + 35 - 14, 90, 10, 2f, HudEditor.getColor(0).darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker());
                Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(0), HudEditor.getColor(270), getPosX() + 55, getPosY() + 35 - 14, (int) MathUtility.clamp((90 * (health / 20)), 3, 90), 10, 2f);


                FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), hpMode.getValue() == HPmodeEn.HP ? String.valueOf(Math.round(10.0 * getHealth()) / 10.0) : ((Math.round(10.0 * health) / 10.0) / 20f) * 100 + "%", getPosX() + 102, getPosY() + 20.5f, -1);

                //Имя ебыря
                FontRenderers.sf_bold.drawString(context.getMatrices(), ModuleManager.media.isEnabled() ? "Protected " : target.getName().getString(), getPosX() + 55, getPosY() + 5, -1, false);

                if (target instanceof PlayerEntity) {
                    //Броня
                    List<ItemStack> armor = ((PlayerEntity) target).getInventory().armor;
                    ItemStack[] items = new ItemStack[]{target.getMainHandStack(), armor.get(3), armor.get(2), armor.get(1), armor.get(0), target.getOffHandStack()};

                    float xItemOffset = getPosX() + 60;
                    for (ItemStack itemStack : items) {
                        if (itemStack.isEmpty()) continue;
                        context.getMatrices().push();
                        context.getMatrices().translate(xItemOffset, getPosY() + 35, 0);
                        context.getMatrices().scale(0.75f, 0.75f, 0.75f);
                        context.drawItem(itemStack, 0, 0);
                        context.drawItemInSlot(mc.textRenderer, itemStack, 0, 0);

                        context.getMatrices().pop();
                        xItemOffset += 14;
                    }

                    //Поушены
                    drawPotionEffect(context.getMatrices(), ((PlayerEntity) target));
                }
            } else if (Mode.getValue() == ModeEn.NurikZapen) {
                float hurtPercent = (Render2DEngine.interpolateFloat(MathUtility.clamp(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0, 10), target.hurtTime, mc.getTickDelta())) / 8f;
                float health = Math.min(20, getHealth());
                healthanimation.setValue(health);
                health = (float) healthanimation.getAnimationD();

                if (mini.getValue()) {
                    // Основа
                    Render2DEngine.drawGradientBlurredShadow(context.getMatrices(), getPosX() + 2, getPosY() + 2, 91, 31, 12, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
                    Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX(), getPosY(), 95, 35, 7);
                    Render2DEngine.drawRound(context.getMatrices(), getPosX() + 0.5f, getPosY() + 0.5f, 94, 34, 7, Render2DEngine.injectAlpha(Color.BLACK, 220));

                    // Бошка
                    if (target instanceof PlayerEntity) {
                        RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity) target).getSkinTextures().texture());
                    } else {
                        RenderSystem.setShaderTexture(0, mc.getEntityRenderDispatcher().getRenderer(target).getTexture(target));
                    }

                    context.getMatrices().push();
                    context.getMatrices().translate(getPosX() + 2.5 + 15, getPosY() + 2.5 + 15, 0);
                    context.getMatrices().scale(1 - hurtPercent / 20f, 1 - hurtPercent / 20f, 1f);
                    context.getMatrices().translate(-(getPosX() + 2.5 + 15), -(getPosY() + 2.5 + 15), 0);
                    RenderSystem.enableBlend();
                    RenderSystem.colorMask(false, false, false, true);
                    RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
                    RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
                    RenderSystem.colorMask(true, true, true, true);
                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    Render2DEngine.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), 1f, 1f, 1f, 1f, getPosX() + 2.5, getPosY() + 2.5, getPosX() + 2.5 + 30, getPosY() + 2.5 + 30, 5, 10);
                    RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
                    RenderSystem.setShaderColor(1f, 1f - hurtPercent / 2, 1f - hurtPercent / 2, 1f);
                    Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 2.5, getPosY() + 2.5, 30, 30, 8, 8, 8, 8, 64, 64);
                    Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 2.5, getPosY() + 2.5, 30, 30, 40, 8, 8, 8, 64, 64);
                    RenderSystem.defaultBlendFunc();
                    context.getMatrices().pop();

                    // Баллон
                    Render2DEngine.drawGradientRound(context.getMatrices(), getPosX() + 38, getPosY() + 25, 52, 7, 2f, HudEditor.getColor(0).darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker());
                    Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(0), HudEditor.getColor(270), getPosX() + 38, getPosY() + 25, (int) MathUtility.clamp((52 * (health / 20)), 8, 52), 7, 2f);

                    FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), hpMode.getValue() == HPmodeEn.HP ? String.valueOf(Math.round(10.0 * getHealth()) / 10.0) : (((Math.round(10.0 * getHealth()) / 10.0) / 20f) * 100 + "%"), getPosX() + 65, getPosY() + 24f, -1);
                    //

                    //Имя
                    FontRenderers.sf_bold_mini.drawString(context.getMatrices(), ModuleManager.media.isEnabled() ? "Protected " : target.getName().getString(), getPosX() + 38, getPosY() + 5, -1, false);

                    if (target instanceof PlayerEntity) {
                        //Броня
                        List<ItemStack> armor = ((PlayerEntity) target).getInventory().armor;
                        ItemStack[] items = new ItemStack[]{target.getMainHandStack(), armor.get(3), armor.get(2), armor.get(1), armor.get(0), target.getOffHandStack()};

                        float xItemOffset = getPosX() + 38;
                        for (ItemStack itemStack : items) {
                            context.getMatrices().push();
                            context.getMatrices().translate(xItemOffset, getPosY() + 13, 0);
                            context.getMatrices().scale(0.5f, 0.5f, 0.5f);
                            context.drawItem(itemStack, 0, 0);
                            context.drawItemInSlot(mc.textRenderer, itemStack, 0, 0);
                            context.getMatrices().pop();
                            xItemOffset += 9;
                        }
                    }
                } else {
                    // Основа
                    Render2DEngine.drawGradientBlurredShadow(context.getMatrices(), getPosX() + 2, getPosY() + 2, 133, 44, 14, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
                    Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX(), getPosY(), 137, 47.5f, 9);
                    Render2DEngine.drawRound(context.getMatrices(), getPosX() + 0.5f, getPosY() + 0.5f, 136f, 46, 9, Render2DEngine.injectAlpha(Color.BLACK, 220));

                    // Бошка
                    if (target instanceof PlayerEntity) {
                        RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity) target).getSkinTextures().texture());
                    } else {
                        RenderSystem.setShaderTexture(0, mc.getEntityRenderDispatcher().getRenderer(target).getTexture(target));
                    }

                    context.getMatrices().push();
                    context.getMatrices().translate(getPosX() + 3.5f + 20, getPosY() + 3.5f + 20, 0);
                    context.getMatrices().scale(1 - hurtPercent / 15f, 1 - hurtPercent / 15f, 1f);
                    context.getMatrices().translate(-(getPosX() + 3.5f + 20), -(getPosY() + 3.5f + 20), 0);
                    RenderSystem.enableBlend();
                    RenderSystem.colorMask(false, false, false, true);
                    RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
                    RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
                    RenderSystem.colorMask(true, true, true, true);
                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    Render2DEngine.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), 1f, 1f, 1f, 1f, getPosX() + 3.5f, getPosY() + 3.5f, getPosX() + 3.5f + 40, getPosY() + 3.5f + 40, 7, 10);
                    RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
                    RenderSystem.setShaderColor(1f, 1f - hurtPercent / 2, 1f - hurtPercent / 2, 1f);
                    Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 3.5f, getPosY() + 3.5f, 40, 40, 8, 8, 8, 8, 64, 64);
                    Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 3.5f, getPosY() + 3.5f, 40, 40, 40, 8, 8, 8, 64, 64);
                    RenderSystem.defaultBlendFunc();
                    context.getMatrices().pop();

                    // Баллон
                    Render2DEngine.drawGradientRound(context.getMatrices(), getPosX() + 48, getPosY() + 32, 85, 11, 4f, HudEditor.getColor(0).darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker());
                    Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(0), HudEditor.getColor(270), getPosX() + 48, getPosY() + 32, (int) MathUtility.clamp((85 * (health / 20)), 8, 85), 11, 4f);

                    FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), hpMode.getValue() == HPmodeEn.HP ? String.valueOf(Math.round(10.0 * getHealth()) / 10.0) : (((Math.round(10.0 * getHealth()) / 10.0) / 20f) * 100 + "%"), getPosX() + 92f, getPosY() + 32.5f, -1);
                    //

                    //Имя
                    FontRenderers.sf_bold.drawString(context.getMatrices(), ModuleManager.media.isEnabled() ? "Protected " : target.getName().getString(), getPosX() + 48, getPosY() + 7, -1, false);

                    if (target instanceof PlayerEntity) {
                        //Броня
                        List<ItemStack> armor = ((PlayerEntity) target).getInventory().armor;
                        ItemStack[] items = new ItemStack[]{target.getMainHandStack(), armor.get(3), armor.get(2), armor.get(1), armor.get(0), target.getOffHandStack()};

                        float xItemOffset = getPosX() + 48;
                        for (ItemStack itemStack : items) {
                            context.getMatrices().push();
                            context.getMatrices().translate(xItemOffset, getPosY() + 15, 0);
                            context.getMatrices().scale(0.75f, 0.75f, 0.75f);
                            context.drawItem(itemStack, 0, 0);
                            context.drawItemInSlot(mc.textRenderer, itemStack, 0, 0);
                            context.getMatrices().pop();
                            xItemOffset += 12;
                        }
                    }
                }
            } else {
                float hurtPercent = (target.hurtTime) / 6f;
                float health = Math.min(20, getHealth());

                Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX() - 2, getPosY() - 2, 164, 51, 5, color.getValue().getColorObject());
                Render2DEngine.drawRect(context.getMatrices(), getPosX(), getPosY(), 160, 47, new Color(0x66000000, true));

                Render2DEngine.drawRect(context.getMatrices(), getPosX() + 117, getPosY() + 4, 18, 18, new Color(0x4D000000, true));
                Render2DEngine.drawRect(context.getMatrices(), getPosX() + 137, getPosY() + 4, 18, 18, new Color(0x4D000000, true));
                Render2DEngine.drawRect(context.getMatrices(), getPosX() + 117, getPosY() + 25, 18, 18, new Color(0x4D000000, true));
                Render2DEngine.drawRect(context.getMatrices(), getPosX() + 137, getPosY() + 25, 18, 18, new Color(0x4D000000, true));

                Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX() + 49, getPosY() + 29, 62, 12, 5, color.getValue().getColorObject().brighter().brighter().brighter());
                Render2DEngine.drawRect(context.getMatrices(), getPosX() + 50, getPosY() + 30, 60, 10, new Color(0x9E000000, true));
                Render2DEngine.drawRect(context.getMatrices(), getPosX() + 50, getPosY() + 30, (int) (60 * (health / 20)), 10, color.getValue().getColorObject().brighter().brighter().brighter());

                if (target instanceof PlayerEntity) {
                    RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity) target).getSkinTextures().texture());
                } else {
                    RenderSystem.setShaderTexture(0, mc.getEntityRenderDispatcher().getRenderer(target).getTexture(target));
                }

                RenderSystem.setShaderColor(1f, 1f - hurtPercent, 1f - hurtPercent, 1f);
                Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 3.5f + hurtPercent, getPosY() + 3.5f + hurtPercent, 40 - hurtPercent * 2, 40 - hurtPercent * 2, 8, 8, 8, 8, 64, 64);
                Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 3.5f + hurtPercent, getPosY() + 3.5f + hurtPercent, 40 - hurtPercent * 2, 40 - hurtPercent * 2, 40, 8, 8, 8, 64, 64);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

                FontRenderers.modules.drawString(context.getMatrices(), ModuleManager.media.isEnabled() ? "Protected " : target.getName().getString(), getPosX() + 50, getPosY() + 7, -1, false);
                FontRenderers.modules.drawCenteredString(context.getMatrices(), hpMode.getValue() == HPmodeEn.HP ? String.valueOf(Math.round(10.0 * getHealth()) / 10.0) : (((Math.round(10.0 * getHealth()) / 10.0) / 20f) * 100 + "%"), getPosX() + 81f, getPosY() + 31f, -1);


                if (target instanceof PlayerEntity) {
                    if (!((PlayerEntity) target).getInventory().armor.get(3).isEmpty()) {
                        context.getMatrices().push();
                        context.getMatrices().translate(getPosX() + 118, getPosY() + 5, 0);
                        context.drawItem(((PlayerEntity) target).getInventory().armor.get(3), 0, 0);
                        context.drawItemInSlot(mc.textRenderer, ((PlayerEntity) target).getInventory().armor.get(3), 0, 0);
                        context.getMatrices().pop();
                    }

                    if (!((PlayerEntity) target).getInventory().armor.get(2).isEmpty()) {
                        context.getMatrices().push();
                        context.getMatrices().translate(getPosX() + 118, getPosY() + 26, 0);
                        context.drawItem(((PlayerEntity) target).getInventory().armor.get(2), 0, 0);
                        context.drawItemInSlot(mc.textRenderer, ((PlayerEntity) target).getInventory().armor.get(2), 0, 0);
                        context.getMatrices().pop();
                    }

                    if (!((PlayerEntity) target).getInventory().armor.get(1).isEmpty()) {
                        context.getMatrices().push();
                        context.getMatrices().translate(getPosX() + 138, getPosY() + 5, 0);
                        context.drawItem(((PlayerEntity) target).getInventory().armor.get(1), 0, 0);
                        context.drawItemInSlot(mc.textRenderer, ((PlayerEntity) target).getInventory().armor.get(1), 0, 0);
                        context.getMatrices().pop();
                    }

                    if (!((PlayerEntity) target).getInventory().armor.get(0).isEmpty()) {
                        context.getMatrices().push();
                        context.getMatrices().translate(getPosX() + 138, getPosY() + 26, 0);
                        context.drawItem(((PlayerEntity) target).getInventory().armor.get(0), 0, 0);
                        context.drawItemInSlot(mc.textRenderer, ((PlayerEntity) target).getInventory().armor.get(0), 0, 0);
                        context.getMatrices().pop();
                    }

                    if (!target.getMainHandStack().isEmpty()) {
                        context.getMatrices().push();
                        context.getMatrices().translate(getPosX() + 50, getPosY() + 14, 0);
                        context.getMatrices().scale(0.75f, 0.75f, 1f);
                        context.drawItem(target.getMainHandStack(), 0, 0);
                        context.getMatrices().pop();
                        FontRenderers.settings.drawString(context.getMatrices(), "x" + target.getMainHandStack().getCount(), getPosX() + 62, getPosY() + 21, -1);
                    }

                    if (!target.getOffHandStack().isEmpty()) {
                        context.getMatrices().push();
                        context.getMatrices().translate(getPosX() + 77, getPosY() + 14, 0);
                        context.getMatrices().scale(0.75f, 0.75f, 1f);
                        context.drawItem(target.getOffHandStack(), 0, 0);
                        context.getMatrices().pop();
                        FontRenderers.settings.drawString(context.getMatrices(), "x" + target.getOffHandStack().getCount(), getPosX() + 90, getPosY() + 21, -1);
                    }
                }
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }
        }
        context.getMatrices().pop();
    }

    private void drawPotionEffect(MatrixStack ms, PlayerEntity entity) {
        StringBuilder finalString = new StringBuilder();
        for (StatusEffectInstance potionEffect : entity.getStatusEffects()) {
            StatusEffect potion = potionEffect.getEffectType();
            if ((potion != StatusEffects.REGENERATION) && (potion != StatusEffects.SPEED) && (potion != StatusEffects.STRENGTH) && (potion != StatusEffects.WEAKNESS)) {
                continue;
            }
            boolean potRanOut = (double) potionEffect.getDuration() != 0.0;
            if (!entity.hasStatusEffect(potion) || !potRanOut) continue;
            finalString.append(getPotionName(potion)).append(potionEffect.getAmplifier() < 1 ? "" : potionEffect.getAmplifier() + 1).append(" ").append(getDurationString(potionEffect)).append(" ");
        }
        FontRenderers.settings.drawString(ms, finalString.toString(), getPosX() + 55, getPosY() + 15, new Color(0x8D8D8D).getRGB(), false);
    }

    public float getHealth() {
        // Первый в комьюнити хп резольвер. Правда, еж?
        if (target instanceof PlayerEntity ent && (mc.getNetworkHandler() != null && mc.getNetworkHandler().getServerInfo() != null && mc.getNetworkHandler().getServerInfo().address.contains("funtime") || funTimeHP.getValue())) {
            ScoreboardObjective scoreBoard = null;
            String resolvedHp = "";
            if ((ent.getScoreboard()).getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                scoreBoard = (ent.getScoreboard()).getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
                if (scoreBoard != null) {
                    ReadableScoreboardScore readableScoreboardScore = ent.getScoreboard().getScore(ent, scoreBoard);
                    MutableText text2 = ReadableScoreboardScore.getFormattedScore(readableScoreboardScore, scoreBoard.getNumberFormatOr(StyledNumberFormat.EMPTY));
                    resolvedHp = text2.getString();
                }
            }
            float numValue = 0;
            try {
                numValue = Float.parseFloat(resolvedHp);
            } catch (NumberFormatException ignored) {
            }
            return numValue;
        } else return target.getHealth();
    }

    public enum HPmodeEn {
        HP,
        Percentage
    }

    public enum ImageModeEn {
        None,
        Anime,
        Custom
    }

    public enum ModeEn {
        ThunderHack,
        NurikZapen,
        CelkaPasta
    }
}