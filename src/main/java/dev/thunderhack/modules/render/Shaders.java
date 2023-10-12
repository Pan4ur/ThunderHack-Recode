package dev.thunderhack.modules.render;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.ColorSetting;
import dev.thunderhack.setting.settings.Parent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.core.ShaderManager;

public class Shaders extends Module {
    public Shaders() {
        super("Shaders", Category.RENDER);
    }

    //Thanks to @0x3C50 for Shader rendering example

    public Setting<ShaderManager.Shader> mode = new Setting<>("Mode", ShaderManager.Shader.Default);
    public Setting<ShaderManager.Shader> handsMode = new Setting<>("HandsMode", ShaderManager.Shader.Default);

    public final Setting<Integer> maxRange = new Setting<>("MaxRange", 64, 16, 256);

    private final Setting<Parent> select = new Setting<>("Select", new Parent(false, 0));
    private final Setting<Boolean> hands = new Setting<>("Hands", true).withParent(select);
    private final Setting<Boolean> players = new Setting<>("Players", true).withParent(select);
    private final Setting<Boolean> friends = new Setting<>("Friends", true).withParent(select);
    private final Setting<Boolean> crystals = new Setting<>("Crystals", true).withParent(select);
    private final Setting<Boolean> creatures = new Setting<>("Creatures", false).withParent(select);
    private final Setting<Boolean> monsters = new Setting<>("Monsters", false).withParent(select);
    private final Setting<Boolean> ambients = new Setting<>("Ambients", false).withParent(select);
    private final Setting<Boolean> others = new Setting<>("Others", false).withParent(select);

    public final Setting<Float> factor = new Setting<>("GradientFactor", 2f, 0f, 20f);
    public final Setting<Float> gradient = new Setting<>("Gradient", 2f, 0f, 20f);
    public final Setting<Integer> alpha2 = new Setting<>("GradientAlpha", 170, 0, 255);
    public final Setting<Integer> lineWidth = new Setting<>("LineWidth", 2, 0, 20);
    public final Setting<Integer> quality = new Setting<>("Quality", 10, 1, 30);
    public final Setting<Integer> octaves = new Setting<>("SmokeOctaves", 10, 5, 30);
    public final Setting<Integer> fillAlpha = new Setting<>("FillAlpha", 170, 0, 255);
    public final Setting<Boolean> glow = new Setting<>("SmokeGlow", true);

    private final Setting<Parent> colors = new Setting<>("Colors", new Parent(false, 0));
    public final Setting<ColorSetting> outlineColor = new Setting<>("Outline", new ColorSetting(0x8800FF00)).withParent(colors);
    public final Setting<ColorSetting> outlineColor1 = new Setting<>("SmokeOutline", new ColorSetting(0x8800FF00)).withParent(colors);
    public final Setting<ColorSetting> outlineColor2 = new Setting<>("SmokeOutline2", new ColorSetting(0x8800FF00)).withParent(colors);
    public final Setting<ColorSetting> fillColor1 = new Setting<>("Fill", new ColorSetting(0x8800FF00)).withParent(colors);
    public final Setting<ColorSetting> fillColor2 = new Setting<>("SmokeFill", new ColorSetting(0x8800FF00)).withParent(colors);
    public final Setting<ColorSetting> fillColor3 = new Setting<>("SmokeFil2", new ColorSetting(0x8800FF00)).withParent(colors);

    public boolean shouldRender(Entity entity) {
        if (entity == null) return false;
        if (mc.player == null) return false;
        if (mc.player.squaredDistanceTo(entity.getPos()) > maxRange.getPow2Value()) return false;
        if (entity instanceof PlayerEntity) {
            if (entity == mc.player) return false;
            if (ThunderHack.friendManager.isFriend((PlayerEntity) entity)) {
                return friends.getValue();
            }
            return players.getValue();
        }

        if (entity instanceof EndCrystalEntity) return crystals.getValue();

        return switch (entity.getType().getSpawnGroup()) {
            case CREATURE, WATER_AMBIENT, WATER_CREATURE -> creatures.getValue();
            case MONSTER -> monsters.getValue();
            case AMBIENT -> ambients.getValue();
            default -> others.getValue();
        };
    }

    public void onRender3D(MatrixStack matrices) {
        if (hands.getValue())
            ThunderHack.shaderManager.renderShader(()-> mc.gameRenderer.renderHand(matrices, mc.gameRenderer.getCamera(), mc.getTickDelta()), handsMode.getValue());
    }

    @Override
    public void onDisable() {
        ThunderHack.shaderManager.reloadShaders();
    }
}
