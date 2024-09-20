package thunder.hack.features.modules.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4d;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.injection.accesors.IEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.misc.FakePlayer;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class LogoutSpots extends Module {
    public LogoutSpots() {
        super("LogoutSpots", Category.RENDER);
    }

    private final Setting<RenderMode> renderMode = new Setting<>("RenderMode", RenderMode.TexturedChams);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x8800FF00));
    private final Setting<Boolean> notifications = new Setting<>("Notifications", true);
    private final Setting<Boolean> ignoreBots = new Setting<>("IgnoreBots", true);

    private final Map<UUID, PlayerEntity> playerCache = Maps.newConcurrentMap();
    private final Map<UUID, PlayerEntity> logoutCache = Maps.newConcurrentMap();

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof PlayerListS2CPacket pac) {
            if (pac.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                for (PlayerListS2CPacket.Entry ple : pac.getPlayerAdditionEntries()) {
                    for (UUID uuid : logoutCache.keySet()) {
                        if (!uuid.equals(ple.profile().getId())) continue;
                        PlayerEntity pl = logoutCache.get(uuid);
                        if (ignoreBots.getValue() && isABot(pl)) continue;
                        if (notifications.getValue())
                            sendMessage(pl.getName().getString() + " logged back at  X: " + (int) pl.getX() + " Y: " + (int) pl.getY() + " Z: " + (int) pl.getZ());
                        logoutCache.remove(uuid);
                    }
                }
            }
            playerCache.clear();
        }

        if (e.getPacket() instanceof PlayerRemoveS2CPacket pac) {
            for (UUID uuid2 : pac.profileIds) {
                for (UUID uuid : playerCache.keySet()) {
                    if (!uuid.equals(uuid2)) continue;
                    final PlayerEntity pl = playerCache.get(uuid);
                    if (ignoreBots.getValue() && isABot(pl)) continue;
                    if (pl != null) {
                        if (notifications.getValue())
                            sendMessage(pl.getName().getString() + " logged out at  X: " + (int) pl.getX() + " Y: " + (int) pl.getY() + " Z: " + (int) pl.getZ());
                        if (!logoutCache.containsKey(uuid))
                            logoutCache.put(uuid, pl);
                    }
                }
            }
            playerCache.clear();
        }
    }

    @Override
    public void onEnable() {
        playerCache.clear();
        logoutCache.clear();
    }

    @Override
    public void onUpdate() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == null || player.equals(mc.player)) continue;
            playerCache.put(player.getGameProfile().getId(), player);
        }
    }

    public void onRender3D(MatrixStack s) {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        if (renderMode.is(RenderMode.Box)) RenderSystem.defaultBlendFunc();
        else RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        for (UUID uuid : logoutCache.keySet()) {
            final PlayerEntity data = logoutCache.get(uuid);
            if (data != null) {
                if (renderMode.is(RenderMode.Box)) {
                    Render3DEngine.drawBoxOutline(data.getBoundingBox(), color.getValue().getColorObject(), 2);
                } else {
                    PlayerEntityModel<PlayerEntity> modelPlayer = new PlayerEntityModel<>(new EntityRendererFactory.Context(
                            mc.getEntityRenderDispatcher(), mc.getItemRenderer(),
                            mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(),
                            mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer).getPart(EntityModelLayers.PLAYER), false);
                    modelPlayer.getHead().scale(new Vector3f(-0.3f, -0.3f, -0.3f));

                    renderEntity(s, data, modelPlayer, ((OtherClientPlayerEntity)data).getSkinTextures().texture(), color.getValue().getAlpha());
                }
            }
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public void onRender2D(DrawContext context) {
        for (UUID uuid : logoutCache.keySet()) {
            final PlayerEntity data = logoutCache.get(uuid);
            if (data != null) {
                Vec3d vector = new Vec3d(data.getX(), data.getY() + 2, data.getZ());
                Vector4d position = null;

                vector = Render3DEngine.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
                if (vector.z > 0 && vector.z < 1) {
                    position = new Vector4d(vector.x, vector.y, vector.z, 0);
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                }

                String string = data.getName().getString() + " " + String.format("%.1f", (data.getHealth() + data.getAbsorptionAmount())) + " X: " + (int) data.getX() + " " + " Z: " + (int) data.getZ();

                if (position != null) {
                    float diff = (float) (position.z - position.x) / 2;
                    float textWidth = (FontRenderers.sf_bold.getStringWidth(string) * 1);
                    float tagX = (float) ((position.x + diff - textWidth / 2) * 1);

                    Render2DEngine.drawRect(context.getMatrices(), tagX - 2, (float) (position.y - 13f), textWidth + 4, 11, new Color(0x99000001, true));
                    FontRenderers.sf_bold.drawString(context.getMatrices(), string, tagX, (float) position.y - 10, -1);
                }
            }
        }
    }

    private void renderEntity(@NotNull MatrixStack matrices, @NotNull LivingEntity entity, @NotNull PlayerEntityModel<PlayerEntity> modelBase, Identifier texture, int alpha) {
        modelBase.leftPants.visible = true;
        modelBase.rightPants.visible = true;
        modelBase.leftSleeve.visible = true;
        modelBase.rightSleeve.visible = true;
        modelBase.jacket.visible = true;
        modelBase.hat.visible = true;

        double x = entity.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = entity.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = entity.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        ((IEntity) entity).setPos(entity.getPos());
        matrices.push();
        matrices.translate((float) x, (float) y, (float) z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtility.rad(180 - entity.bodyYaw)));
        prepareScale(matrices);
        modelBase.animateModel((PlayerEntity) entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), Render3DEngine.getTickDelta());
        float limbSpeed = Math.min(entity.limbAnimator.getSpeed(), 1f);
        modelBase.setAngles((PlayerEntity) entity, entity.limbAnimator.getPos(), limbSpeed, entity.age, entity.headYaw - entity.bodyYaw, entity.getPitch());
        BufferBuilder buffer;
        if (renderMode.is(RenderMode.TexturedChams)) {
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        }
        RenderSystem.setShaderColor(color.getValue().getGlRed(), color.getValue().getGlGreen(), color.getValue().getGlBlue(), alpha / 255f);
        modelBase.render(matrices, buffer, 10, 0);
        Render2DEngine.endBuilding(buffer);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        matrices.pop();
    }

    private static void prepareScale(@NotNull MatrixStack matrixStack) {
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.scale(1.6f, 1.8f, 1.6f);
        matrixStack.translate(0.0F, -1.501F, 0.0F);
    }

    private boolean isABot(PlayerEntity ent) {
        return !ent.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + ent.getName().getString()).getBytes(StandardCharsets.UTF_8))) && ent instanceof OtherClientPlayerEntity
                && (FakePlayer.fakePlayer == null || ent.getId() != FakePlayer.fakePlayer.getId())
                && !ent.getName().getString().contains("-");
    }

    private enum RenderMode {
        Chams, TexturedChams, Box
    }
}