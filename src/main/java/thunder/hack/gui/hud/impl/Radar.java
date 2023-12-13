package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

import java.util.concurrent.CopyOnWriteArrayList;

public class Radar extends HudElement {
    public Radar() {
        super("Radar", 100, 100);
    }

    public Setting<Mode> mode = new Setting<>("Mode", Mode.Rect);
    public Setting<ColorMode> colorMode = new Setting<>("ColorMode", ColorMode.Sync);
    private final Setting<Integer> size = new Setting<>("Size", 80, 20, 300);
    public final Setting<ColorSetting> color2 = new Setting<>("Color", new ColorSetting(0xFF101010));
    public final Setting<ColorSetting> color3 = new Setting<>("PlayerColor", new ColorSetting(0xC59B9B9B));

    private CopyOnWriteArrayList<PlayerEntity> players = new CopyOnWriteArrayList<>();

    @Override
    public void onUpdate() {
        players.clear();
        players.addAll(mc.world.getPlayers());
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        if (mode.getValue() == Mode.Text) {
            float offset_y = 0;
            for (PlayerEntity entityPlayer : players) {
                if (entityPlayer == mc.player)
                    continue;

                String str = entityPlayer.getName().getString() + " " + String.format("%.1f", (entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount())) + " " + String.format("%.1f", mc.player.distanceTo(entityPlayer)) + " m";
                if (colorMode.getValue() == ColorMode.Sync) {
                    FontRenderers.sf_bold.drawString(context.getMatrices(), str, getPosX(), getPosY() + offset_y, HudEditor.getColor((int) (offset_y * 2f)).getRGB());
                } else {
                    FontRenderers.sf_bold.drawString(context.getMatrices(), str, getPosX(), getPosY() + offset_y, color2.getValue().getColor());
                }
                offset_y += FontRenderers.sf_bold.getFontHeight(str);
            }
        }
    }

    public void onRenderShaders(DrawContext context) {
        if (mode.getValue() == Mode.Rect) {
            Render2DEngine.drawHudBase(context.getMatrices(), getPosX(), getPosY(), size.getValue(), size.getValue(), HudEditor.hudRound.getValue());


            Render2DEngine.drawRectDumbWay(context.getMatrices(),
                    (float) (getPosX() + (size.getValue() / 2F - 0.5)),
                    (float) (getPosY() + 3.5),
                    (float) (getPosX() + (size.getValue() / 2F + 0.2)),
                    (float) ((getPosY() + size.getValue()) - 3.5),
                    color2.getValue().getColorObject(), color2.getValue().getColorObject(), color2.getValue().getColorObject(), color2.getValue().getColorObject()
            );

            Render2DEngine.drawRectDumbWay(
                    context.getMatrices(),
                    getPosX() + 3.5f,
                    getPosY() + (size.getValue() / 2F - 0.2f),
                    (getPosX() + size.getValue()) - 3.5f,
                    getPosY() + (size.getValue() / 2F + 0.5f),
                    color2.getValue().getColorObject(), color2.getValue().getColorObject(), color2.getValue().getColorObject(), color2.getValue().getColorObject()
            );

            setBounds(size.getValue(), size.getValue());

            for (PlayerEntity entityPlayer : players) {
                if (entityPlayer == mc.player)
                    continue;

                float posX = (float) (entityPlayer.prevX + (entityPlayer.prevX - entityPlayer.getX()) * mc.getTickDelta() - mc.player.getX()) * 2;
                float posZ = (float) (entityPlayer.prevZ + (entityPlayer.prevZ - entityPlayer.getZ()) * mc.getTickDelta() - mc.player.getZ()) * 2;
                float cos = (float) Math.cos(mc.player.getYaw(mc.getTickDelta()) * 0.017453292);
                float sin = (float) Math.sin(mc.player.getYaw(mc.getTickDelta()) * 0.017453292);
                float rotY = -(posZ * cos - posX * sin);
                float rotX = -(posX * cos + posZ * sin);
                if (rotY > size.getValue() / 2F - 6) {
                    rotY = size.getValue() / 2F - 6;
                } else if (rotY < -(size.getValue() / 2F - 8)) {
                    rotY = -(size.getValue() / 2F - 8);
                }
                if (rotX > size.getValue() / 2F - 5) {
                    rotX = size.getValue() / 2F - 5;
                } else if (rotX < -(size.getValue() / 2F - 5)) {
                    rotX = -(size.getValue() / 2F - 5);
                }

                Render2DEngine.drawRound(context.getMatrices(), (getPosX() + size.getValue() / 2F + rotX) - 2, (getPosY() + size.getValue() / 2F + rotY) - 2, 4, 4, 2f, color3.getValue().getColorObject());
            }
        }
    }


    private enum Mode {
        Rect, Text
    }

    public enum ColorMode {
        Sync, Custom
    }
}