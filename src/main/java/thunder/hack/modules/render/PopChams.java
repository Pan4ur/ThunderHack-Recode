package thunder.hack.modules.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.Render3DEvent;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.combat.AimBot;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtil;
import thunder.hack.utility.render.Render2DEngine;

import java.util.concurrent.CopyOnWriteArrayList;

public class PopChams extends Module {
    public PopChams() {
        super("PopChams", Category.RENDER);
    }

    public final CopyOnWriteArrayList<Person> popList = new CopyOnWriteArrayList<>();
    public static Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x8800FF00));
    public Setting<Integer> aSpeed = new Setting("AlphaSpeed", 5, 1, 100);


    public static void renderEntity(MatrixStack matrices, LivingEntity entity, BipedEntityModel<PlayerEntity> modelBase, int alpha) {
        double x = entity.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = entity.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = entity.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        matrices.push();
        matrices.translate((float) x, (float) y, (float) z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtil.rad(180 - entity.bodyYaw)));
        prepareScale(matrices);

        modelBase.animateModel((PlayerEntity) entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), mc.getTickDelta());
        modelBase.setAngles((PlayerEntity) entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), entity.age, entity.headYaw - entity.bodyYaw, entity.getPitch());

        RenderSystem.enableBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        modelBase.render(matrices, buffer, 10, 0, color.getValue().getRed() / 255f, color.getValue().getGreen() / 255f, color.getValue().getBlue() / 255f, alpha / 255f);
        tessellator.draw();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private static void prepareScale(MatrixStack matrixStack) {
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.scale(1.6f, 1.8f, 1.6f);
        matrixStack.translate(0.0F, -1.501F, 0.0F);
    }

    @Subscribe
    public void onTotemPop(TotemPopEvent e) {
        if (e.getEntity().equals(mc.player)) return;
        PlayerEntity entity = new PlayerEntity(mc.world, BlockPos.ORIGIN, e.getEntity().bodyYaw, new GameProfile(e.getEntity().getUuid(), e.getEntity().getName().getString())) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
        entity.copyPositionAndRotation(e.getEntity());
        entity.bodyYaw = e.getEntity().bodyYaw;
        entity.headYaw = e.getEntity().headYaw;
        entity.handSwingProgress = e.getEntity().handSwingProgress;
        entity.handSwingTicks = e.getEntity().handSwingTicks;
        entity.setSneaking(e.getEntity().isSneaking());
        entity.limbAnimator.setSpeed(e.getEntity().limbAnimator.getSpeed());
        entity.limbAnimator.pos = e.getEntity().limbAnimator.getPos();
        popList.add(new Person(entity));
    }

    @Subscribe
    public void onRender3D(Render3DEvent e) {
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 0, 1);
        popList.forEach(person -> {
            person.modelPlayer.leftPants.visible = false;
            person.modelPlayer.rightPants.visible = false;
            person.modelPlayer.leftSleeve.visible = false;
            person.modelPlayer.rightSleeve.visible = false;
            person.modelPlayer.jacket.visible = false;
            person.modelPlayer.hat.visible = false;
            renderEntity(e.getMatrixStack(), person.player, person.modelPlayer, person.getAlpha());
        });
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }


    @Override
    public void onUpdate() {
        popList.forEach(person -> person.update(popList));
    }


    public class Person {
        private final PlayerEntity player;
        private final PlayerEntityModel modelPlayer;
        private int alpha;

        public int getAlpha() {
            return MathUtil.clamp(alpha, 0, 255);
        }

        public Person(PlayerEntity player) {
            this.player = player;
            modelPlayer = new PlayerEntityModel(new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer).getPart(EntityModelLayers.PLAYER), false);
            modelPlayer.getHead().scale(new Vector3f(-0.3f, -0.3f, -0.3f));
            alpha = color.getValue().getAlpha();
        }

        public void update(CopyOnWriteArrayList<Person> arrayList) {
            if (alpha <= 0) {
                arrayList.remove(this);
                player.kill();
                player.remove(Entity.RemovalReason.KILLED);
                player.onRemoved();
                return;
            }
            alpha -= aSpeed.getValue();
        }
    }
}
