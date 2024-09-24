package thunder.hack.features.hud.impl;

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
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL40C;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudEditorGui;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.features.modules.combat.AutoAnchor;
import thunder.hack.features.modules.combat.AutoCrystal;
import thunder.hack.features.modules.misc.NameProtect;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;
import thunder.hack.utility.render.animation.EaseOutBack;
import thunder.hack.utility.render.animation.EaseOutCirc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TargetHud extends HudElement {
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
    private final Setting<Boolean> absorp = new Setting<>("Absorption", true);

    private static Identifier custom;

    public EaseOutBack animation = new EaseOutBack();
    public static EaseOutCirc healthAnimation = new EaseOutCirc();
    public static EaseOutCirc headAnimation = new EaseOutCirc();

    private final ArrayList<Particles> particles = new ArrayList<>();

    private boolean sentParticles, direction = false;
    private LivingEntity target;

    float ticks;

    private final Timer timer = new Timer();

    public TargetHud() {
        super("TargetHud", 150, 50);
    }

    @Override
    public void onEnable() {
        try {
            custom = ThunderUtility.getCustomImg("thud");
        } catch (Exception e) {
            sendMessage(".minecraft -> ThunderHackRecode -> misc -> images -> thud.png");
        }
    }

    public String getDurationString(StatusEffectInstance pe) {
        if (pe.isInfinite()) {
            return "*:*";
        } else return pe.getDuration() / 1200 + ":" + (pe.getDuration() % 1200) / 20;
    }

    @Override
    public void onUpdate() {
        animation.update(direction);
        healthAnimation.update();
        headAnimation.update();
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        getTarget();
        if (target == null) return;

        float health = Math.min(target.getMaxHealth(), getHealth());

        context.getMatrices().push();

        if (!HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            if (Mode.is(ModeEn.NurikZapen) && mini.getValue())
                sizeAnimation(context.getMatrices(), getPosX() + 45 + animX.getValue(), getPosY() + 15 + animY.getValue(), animation.getAnimationd());
            else
                sizeAnimation(context.getMatrices(), getPosX() + 75 + animX.getValue(), getPosY() + 25 + animY.getValue(), animation.getAnimationd());
        }

        if (animation.getAnimationd() > 0) {
            float animationFactor = (float) MathUtility.clamp(animation.getAnimationd(), 0, 1f);
            switch (Mode.getValue()) {
                case ThunderHack -> renderThunderHack(context, health, animationFactor);
                case CelkaPasta -> renderCelkaPasta(context, health);
                case NurikZapen -> {
                    if (mini.getValue())
                        renderMiniNurik(context, health, animationFactor);
                    else
                        renderNurik(context, health, animationFactor);

                }
            }
        }
        context.getMatrices().pop();
    }

    private void getTarget() {
        if (AutoCrystal.target != null) {
            target = AutoCrystal.target;
            direction = true;
            if (AutoCrystal.target.isDead()) {
                AutoCrystal.target = null;
                target = null;
            }
        } else if (Aura.target != null) {
            if (Aura.target instanceof LivingEntity) {
                target = (LivingEntity) Aura.target;
                direction = true;
            } else {
                target = null;
                direction = false;
            }
        } else if (AutoAnchor.target != null) {
            target = AutoAnchor.target;
            direction = true;
            if (AutoAnchor.target.isDead()) {
                AutoAnchor.target = null;
                target = null;
            }
        } else if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudEditorGui) {
            target = mc.player;
            direction = true;
        } else {
            direction = false;
            if (animation.getAnimationd() < 0.02)
                target = null;
        }
    }

    private void renderCelkaPasta(DrawContext context, float health) {
        float hurtPercent = (target.hurtTime) / 6f;

        Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX() - 2, getPosY() - 2, 164, 51, 5, color.getValue().getColorObject());
        Render2DEngine.drawRect(context.getMatrices(), getPosX(), getPosY(), 160, 47, new Color(0x66000000, true));

        setBounds(getPosX(), getPosY(), 160, 47);

        Render2DEngine.drawRect(context.getMatrices(), getPosX() + 117, getPosY() + 4, 18, 18, new Color(0x4D000000, true));
        Render2DEngine.drawRect(context.getMatrices(), getPosX() + 137, getPosY() + 4, 18, 18, new Color(0x4D000000, true));
        Render2DEngine.drawRect(context.getMatrices(), getPosX() + 117, getPosY() + 25, 18, 18, new Color(0x4D000000, true));
        Render2DEngine.drawRect(context.getMatrices(), getPosX() + 137, getPosY() + 25, 18, 18, new Color(0x4D000000, true));

        Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX() + 49, getPosY() + 29, 62, 12, 5, color.getValue().getColorObject().brighter().brighter().brighter());
        Render2DEngine.drawRect(context.getMatrices(), getPosX() + 50, getPosY() + 30, 60, 10, new Color(0x9E000000, true));
        Render2DEngine.drawRect(context.getMatrices(), getPosX() + 50, getPosY() + 30, MathUtility.clamp((int) (60 * (health / target.getMaxHealth())), 0, 60), 10, color.getValue().getColorObject().brighter().brighter().brighter());

        if (target instanceof PlayerEntity) {
            RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity) target).getSkinTextures().texture());
        } else {
            RenderSystem.setShaderTexture(0, mc.getEntityRenderDispatcher().getRenderer(target).getTexture(target));
        }

        RenderSystem.setShaderColor(1f, 1f - hurtPercent, 1f - hurtPercent, 1f);
        Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 3.5f + hurtPercent, getPosY() + 3.5f + hurtPercent, 40 - hurtPercent * 2, 40 - hurtPercent * 2, 8, 8, 8, 8, 64, 64);
        Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 3.5f + hurtPercent, getPosY() + 3.5f + hurtPercent, 40 - hurtPercent * 2, 40 - hurtPercent * 2, 40, 8, 8, 8, 64, 64);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        FontRenderers.modules.drawString(context.getMatrices(), ModuleManager.media.isEnabled() ? "Protected " : ModuleManager.nameProtect.isEnabled() && target == mc.player ? NameProtect.getCustomName() : target.getName().getString(), getPosX() + 50, getPosY() + 7, -1);
        FontRenderers.modules.drawCenteredString(context.getMatrices(), hpMode.getValue() == HPmodeEn.HP ? String.valueOf(Math.round(10.0 * getHealth()) / 10.0) : (((Math.round(10.0 * getHealth()) / 10.0) / 20f) * 100 + "%"), getPosX() + 81f, getPosY() + 34f, -1);


        if (target instanceof PlayerEntity pe) {
            celestialArmor(context, pe, getPosX(), getPosY());
            celestialHands(context, pe, getPosX(), getPosY());
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void renderNurik(DrawContext context, float health, float animationFactor) {
        float hurtPercent = (Render2DEngine.interpolateFloat(MathUtility.clamp(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0, 10), target.hurtTime, Render3DEngine.getTickDelta())) / 8f;
        healthAnimation.setValue(health);
        health = (float) healthAnimation.getAnimationD();

        if (animation.getAnimationd() != 1 && !HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            Render2DEngine.drawGradientBlurredShadow1(context.getMatrices(), getPosX() + 4, getPosY() + 4, 131, 40, 14, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
            Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX(), getPosY(), 137, 47.5f, 9);
            Render2DEngine.drawRound(context.getMatrices(), getPosX() + 0.5f, getPosY() + 0.5f, 136f, 46, 9, Render2DEngine.injectAlpha(Color.BLACK, 220));
        } else
            Render2DEngine.drawHudBase2(context.getMatrices(), getPosX(), getPosY(), 137f, 47.5f, 9f, HudEditor.blurStrength.getValue(), HudEditor.blurOpacity.getValue(), animationFactor);

        setBounds(getPosX(), getPosY(), 137, 47.5f);

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
        Render2DEngine.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), animationFactor, animationFactor, animationFactor, animationFactor, getPosX() + 3.5f, getPosY() + 3.5f, getPosX() + 3.5f + 40, getPosY() + 3.5f + 40, 7, 10);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(animationFactor, animationFactor - hurtPercent / 2, animationFactor - hurtPercent / 2, (float) MathUtility.clamp(animation.getAnimationd(), 0, 1f));
        Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 3.5f, getPosY() + 3.5f, 40, 40, 8, 8, 8, 8, 64, 64);
        Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 3.5f, getPosY() + 3.5f, 40, 40, 40, 8, 8, 8, 64, 64);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        context.getMatrices().pop();

        // Баллон
        if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            Render2DEngine.drawRect(context.getMatrices(), getPosX() + 48, getPosY() + 32, 85f, 11f, 4f, (float) (0.15f * animation.getAnimationd()));
            Render2DEngine.drawRect(context.getMatrices(), getPosX() + 48, getPosY() + 32, MathUtility.clamp((85 * (health / target.getMaxHealth())), 8, 85), 11f, 4f, (float) (animation.getAnimationd()));
        } else {
            Render2DEngine.drawGradientRound(context.getMatrices(), getPosX() + 48, getPosY() + 32, 85, 11, 4f, HudEditor.getColor(0).darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker());
            Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(0), HudEditor.getColor(270), getPosX() + 48, getPosY() + 32, (int) MathUtility.clamp((85 * (health / target.getMaxHealth())), 8, 85), 11, 4f);
        }

        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), hpMode.getValue() == HPmodeEn.HP ? String.valueOf(Math.round(10.0 * getHealth()) / 10.0) : (((Math.round(10.0 * getHealth()) / 10.0) / 20f) * 100 + "%"), getPosX() + 92f, getPosY() + 35f,
                Render2DEngine.applyOpacity(Colors.WHITE, animationFactor));
        //

        //Имя
        FontRenderers.sf_bold.drawString(context.getMatrices(), ModuleManager.media.isEnabled() ? "Protected " : ModuleManager.nameProtect.isEnabled() && target == mc.player ? NameProtect.getCustomName() : target.getName().getString(), getPosX() + 48, getPosY() + 7,
                Render2DEngine.applyOpacity(Colors.WHITE, animationFactor));

        if (target instanceof PlayerEntity) {
            RenderSystem.setShaderColor(1f, 1f, 1f, (float) MathUtility.clamp(animation.getAnimationd(), 0, 1f));

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
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    private void renderMiniNurik(DrawContext context, float health, float animationFactor) {
        float hurtPercent = (Render2DEngine.interpolateFloat(MathUtility.clamp(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0, 10), target.hurtTime, Render3DEngine.getTickDelta())) / 8f;
        healthAnimation.setValue(health);
        health = (float) healthAnimation.getAnimationD();

        // Основа
        if (animation.getAnimationd() != 1 && !HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            Render2DEngine.drawGradientBlurredShadow1(context.getMatrices(), getPosX() + 2, getPosY() + 2, 91, 31, 12, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
            Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX(), getPosY(), 95, 35, 7);
            Render2DEngine.drawRound(context.getMatrices(), getPosX() + 0.5f, getPosY() + 0.5f, 94, 34, 7, Render2DEngine.injectAlpha(Color.BLACK, 220));
        } else
            Render2DEngine.drawHudBase2(context.getMatrices(), getPosX(), getPosY(), 95, 35.5f, 8, HudEditor.blurStrength.getValue(), HudEditor.blurOpacity.getValue(), animationFactor);

        setBounds(getPosX(), getPosY(), 95, 35.5f);

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
        Render2DEngine.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), animationFactor, animationFactor, animationFactor, animationFactor, getPosX() + 2.5, getPosY() + 2.5, getPosX() + 2.5 + 30, getPosY() + 2.5 + 30, 5, 10);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(animationFactor, animationFactor - hurtPercent / 2, animationFactor - hurtPercent / 2, animationFactor);
        Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 2.5, getPosY() + 2.5, 30, 30, 8, 8, 8, 8, 64, 64);
        Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 2.5, getPosY() + 2.5, 30, 30, 40, 8, 8, 8, 64, 64);
        RenderSystem.defaultBlendFunc();
        context.getMatrices().pop();

        // Баллон
        if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            Render2DEngine.drawRect(context.getMatrices(), getPosX() + 38, getPosY() + 25, 52f, 7f, 2f, (float) (0.15f * animation.getAnimationd()));
            Render2DEngine.drawRect(context.getMatrices(), getPosX() + 38, getPosY() + 25, MathUtility.clamp((52f * (health / target.getMaxHealth())), 8, 52), 7f, 2f, (float) (animation.getAnimationd()));
        } else {
            Render2DEngine.drawGradientRound(context.getMatrices(), getPosX() + 38, getPosY() + 25, 52, 7, 2f, HudEditor.getColor(0).darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker());
            Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(0), HudEditor.getColor(270), getPosX() + 38, getPosY() + 25, (int) MathUtility.clamp((52 * (health / target.getMaxHealth())), 8, 52), 7, 2f);
        }

        FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), hpMode.getValue() == HPmodeEn.HP ? String.valueOf(Math.round(10.0 * getHealth()) / 10.0) : (((Math.round(10.0 * getHealth()) / 10.0) / 20f) * 100 + "%"), getPosX() + 65, getPosY() + 27f, Render2DEngine.applyOpacity(Colors.WHITE, animationFactor));
        //

        //Имя
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), ModuleManager.media.isEnabled() ? "Protected " : ModuleManager.nameProtect.isEnabled() && target == mc.player ? NameProtect.getCustomName() : target.getName().getString(), getPosX() + 38, getPosY() + 5, Render2DEngine.applyOpacity(Colors.WHITE, animationFactor));

        if (target instanceof PlayerEntity) {
            //Броня
            RenderSystem.setShaderColor(1f, 1f, 1f, (float) MathUtility.clamp(animation.getAnimationd(), 0, 1f));
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
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    private void renderThunderHack(DrawContext context, float health, float animationFactor) {
        float hurtPercent = target.hurtTime / 6f;

        // Основа
        Render2DEngine.drawRound(context.getMatrices(), getPosX(), getPosY(), 70, 50, 6, new Color(0, 0, 0, 139));
        Render2DEngine.drawRound(context.getMatrices(), getPosX() + 50, getPosY(), 100, 50, 6, new Color(0, 0, 0, 255));
        setBounds(getPosX(), getPosY(), 150, 50);
        // Картинка

        imageRender:
        {
            if (!imageMode.is(ImageModeEn.None)) {
                if (imageMode.is(ImageModeEn.Anime)) {
                    RenderSystem.setShaderTexture(0, TextureStorage.thudPic);
                } else {
                    if (custom == null)
                        break imageRender;

                    RenderSystem.setShaderTexture(0, custom);
                }
                context.getMatrices().push();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Render2DEngine.drawRound(context.getMatrices(), getPosX() + 50, getPosY(), 100, 50, 12, new Color(0, 0, 0, 255));
                RenderSystem.disableBlend();
                RenderSystem.setShaderColor(0.3f, 0.3f, 0.3f, 1f);
                Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 50, getPosY(), 95, 50, 0, 0, 100, 50, 100, 50);
                context.getMatrices().pop();
            }
        }

        //Партиклы
        for (final Particles p : particles)
            if (p.opacity > 4)
                p.render2D(context.getMatrices());

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
        headAnimation.setValue(hurtPercent2);

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

        Render2DEngine.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), animationFactor, animationFactor, animationFactor, animationFactor,
                getPosX() + 2.5, getPosY() + 2.5, getPosX() + 2.5 + 45, getPosY() + 2.5 + 45, 5, 10);

        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(animationFactor, animationFactor - hurtPercent / 2, animationFactor - hurtPercent / 2, animationFactor);
        Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 2.5, getPosY() + 2.5, 45, 45, 8, 8, 8, 8, 64, 64);
        Render2DEngine.renderTexture(context.getMatrices(), getPosX() + 2.5, getPosY() + 2.5, 45, 45, 40, 8, 8, 8, 64, 64);
        RenderSystem.defaultBlendFunc();
        context.getMatrices().pop();

        healthAnimation.setValue(health);
        health = (float) healthAnimation.getAnimationD();
        Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX() + 55, getPosY() + 22, 90, 8, blurRadius.getValue(), HudEditor.getColor(0));

        Render2DEngine.drawGradientRound(context.getMatrices(), getPosX() + 55, getPosY() + 35 - 14, 90, 10, 2f, HudEditor.getColor(0).darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker());
        Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(0), HudEditor.getColor(270), getPosX() + 55, getPosY() + 35 - 14, (int) MathUtility.clamp((90 * (health / target.getMaxHealth())), 3, 90), 10, 2f);


        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), hpMode.getValue() == HPmodeEn.HP ? String.valueOf(Math.round(10.0 * getHealth()) / 10.0) : (int) (((Math.round(10.0 * health) / 10.0) / 20f) * 100) + "%", getPosX() + 102, getPosY() + 24f, Render2DEngine.applyOpacity(Colors.WHITE, animationFactor));

        //Имя ебыря
        FontRenderers.sf_bold.drawString(context.getMatrices(), ModuleManager.media.isEnabled() ? "Protected " : ModuleManager.nameProtect.isEnabled() && target == mc.player ? NameProtect.getCustomName() : target.getName().getString(), getPosX() + 55, getPosY() + 5, Render2DEngine.applyOpacity(Colors.WHITE, animationFactor));

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
    }

    private void celestialArmor(DrawContext context, PlayerEntity target, float posX, float posY) {
        for (int i = 0; i < 4; i++)
            if (!target.getInventory().armor.get(3 - i).isEmpty()) {
                context.getMatrices().push();
                context.getMatrices().translate(posX + (i > 1 ? 138 : 118), posY + (i % 2 == 0 ? 5 : 26), 0);
                context.drawItem(target.getInventory().armor.get(3 - i), 0, 0);
                context.drawItemInSlot(mc.textRenderer, target.getInventory().armor.get(3 - i), 0, 0);
                context.getMatrices().pop();
            }
    }

    private void celestialHands(DrawContext context, PlayerEntity target, float posX, float posY) {
        for (int i = 0; i < 2; i++)
            if (!(i == 0 ? target.getMainHandStack() : target.getOffHandStack()).isEmpty()) {
                context.getMatrices().push();
                context.getMatrices().translate(posX + (i == 0 ? 50 : 77), posY + 14, 0);
                context.getMatrices().scale(0.75f, 0.75f, 1f);
                context.drawItem((i == 0 ? target.getMainHandStack() : target.getOffHandStack()), 0, 0);
                context.getMatrices().pop();
                FontRenderers.settings.drawString(context.getMatrices(), "x" + (i == 0 ? target.getMainHandStack() : target.getOffHandStack()).getCount(), posX + (i == 0 ? 50 : 77) + 12, posY + 21, -1);
            }
    }

    private void drawPotionEffect(MatrixStack ms, PlayerEntity entity) {
        StringBuilder finalString = new StringBuilder();
        for (StatusEffectInstance potionEffect : entity.getStatusEffects()) {
            StatusEffect potion = potionEffect.getEffectType().value();
            if ((potion != StatusEffects.REGENERATION.value()) && (potion != StatusEffects.SPEED.value()) && (potion != StatusEffects.STRENGTH.value()) && (potion != StatusEffects.WEAKNESS.value())) {
                continue;
            }
            boolean potRanOut = (double) potionEffect.getDuration() != 0.0;
            if (!entity.hasStatusEffect(potionEffect.getEffectType()) || !potRanOut) continue;
            finalString.append(getPotionName(potion)).append(potionEffect.getAmplifier() < 1 ? "" : potionEffect.getAmplifier() + 1).append(" ").append(getDurationString(potionEffect)).append(" ");
        }
        FontRenderers.settings.drawString(ms, finalString.toString(), getPosX() + 55, getPosY() + 15, new Color(0x8D8D8D).getRGB());
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
            return (absorp.getValue()) ? numValue + target.getAbsorptionAmount() : numValue;
        } else return (absorp.getValue()) ? target.getHealth() + target.getAbsorptionAmount() : target.getHealth();
    }

    public static void sizeAnimation(MatrixStack matrixStack, double width, double height, double animation) {
        matrixStack.translate(width, height, 0);
        matrixStack.scale((float) animation, (float) animation, 1);
        matrixStack.translate(-width, -height, 0);
    }

    public static String getPotionName(StatusEffect p) {
        if (p == StatusEffects.REGENERATION.value()) return "Reg";
        else if (p == StatusEffects.STRENGTH.value()) return "Str";
        else if (p == StatusEffects.SPEED.value()) return "Spd";
        else if (p == StatusEffects.HASTE.value()) return "H";
        else if (p == StatusEffects.WEAKNESS.value()) return "W";
        else if (p == StatusEffects.RESISTANCE.value()) return "Res";
        return "pon";
    }

    public enum HPmodeEn {
        HP, Percentage
    }

    public enum ImageModeEn {
        None, Anime, Custom
    }

    public enum ModeEn {
        ThunderHack, NurikZapen, CelkaPasta
    }
}