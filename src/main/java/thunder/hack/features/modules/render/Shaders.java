package thunder.hack.features.modules.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ShaderManager;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IGameRenderer;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.render.Render3DEngine;

//Thanks to @0x3C50 for Shader rendering example
public class Shaders extends Module {
    public Shaders() {
        super("Shaders", Category.RENDER);
    }

    private final Setting<SettingGroup> select = new Setting<>("Select", new SettingGroup(false, 0));
    private final Setting<Boolean> hands = new Setting<>("Hands", true).addToGroup(select);
    private final Setting<Boolean> players = new Setting<>("Players", true).addToGroup(select);
    private final Setting<Boolean> self = new Setting<>("Self", true, v -> players.getValue()).addToGroup(select);
    private final Setting<Boolean> friends = new Setting<>("Friends", true).addToGroup(select);
    private final Setting<Boolean> crystals = new Setting<>("Crystals", true).addToGroup(select);
    private final Setting<Boolean> creatures = new Setting<>("Creatures", false).addToGroup(select);
    private final Setting<Boolean> monsters = new Setting<>("Monsters", false).addToGroup(select);
    private final Setting<Boolean> ambients = new Setting<>("Ambients", false).addToGroup(select);
    private final Setting<Boolean> others = new Setting<>("Others", false).addToGroup(select);

    public Setting<ShaderManager.Shader> mode = new Setting<>("Mode", ShaderManager.Shader.Default);
    public Setting<ShaderManager.Shader> handsMode = new Setting<>("HandsMode", ShaderManager.Shader.Default);

    public final Setting<Integer> maxRange = new Setting<>("MaxRange", 64, 16, 256, v -> players.getValue() || crystals.getValue() || friends.getValue() || creatures.getValue() || monsters.getValue() || ambients.getValue() || others.getValue());
    public final Setting<Float> factor = new Setting<>("GradientFactor", 2f, 0f, 20f, v -> mode.is(ShaderManager.Shader.Gradient) || handsMode.is(ShaderManager.Shader.Gradient));
    public final Setting<Float> gradient = new Setting<>("Gradient", 2f, 0f, 20f, v -> mode.is(ShaderManager.Shader.Gradient) || handsMode.is(ShaderManager.Shader.Gradient));
    public final Setting<Integer> alpha2 = new Setting<>("GradientAlpha", 170, 0, 255, v -> mode.is(ShaderManager.Shader.Gradient) || handsMode.is(ShaderManager.Shader.Gradient));
    public final Setting<Integer> lineWidth = new Setting<>("LineWidth", 2, 0, 500);
    public final Setting<Integer> quality = new Setting<>("Quality", 3, 0, 6);
    public final Setting<Integer> octaves = new Setting<>("SmokeOctaves", 10, 5, 30);
    public final Setting<Integer> fillAlpha = new Setting<>("FillAlpha", 170, 0, 255);
    public final Setting<Boolean> glow = new Setting<>("SmokeGlow", true);

    private final Setting<SettingGroup> colors = new Setting<>("Colors", new SettingGroup(false, 0));
    public final Setting<ColorSetting> outlineColor = new Setting<>("Outline", new ColorSetting(0x8800FF00)).addToGroup(colors);
    public final Setting<ColorSetting> outlineColor1 = new Setting<>("SmokeOutline", new ColorSetting(0x8800FF00), v -> mode.is(ShaderManager.Shader.Smoke) || handsMode.is(ShaderManager.Shader.Smoke)).addToGroup(colors);
    public final Setting<ColorSetting> outlineColor2 = new Setting<>("SmokeOutline2", new ColorSetting(0x8800FF00), v -> mode.is(ShaderManager.Shader.Smoke) || handsMode.is(ShaderManager.Shader.Smoke)).addToGroup(colors);
    public final Setting<ColorSetting> fillColor1 = new Setting<>("Fill", new ColorSetting(0x8800FF00)).addToGroup(colors);
    public final Setting<ColorSetting> fillColor2 = new Setting<>("SmokeFill", new ColorSetting(0x8800FF00)).addToGroup(colors);
    public final Setting<ColorSetting> fillColor3 = new Setting<>("SmokeFil2", new ColorSetting(0x8800FF00)).addToGroup(colors);

    public boolean shouldRender(Entity entity) {
        if (entity == null)
            return false;

        if (mc.player == null)
            return false;

        if (mc.player.squaredDistanceTo(entity.getPos()) > maxRange.getPow2Value())
            return false;

        if (entity instanceof PlayerEntity) {
            if (entity == mc.player && !self.getValue())
                return false;
            if (Managers.FRIEND.isFriend((PlayerEntity) entity))
                return friends.getValue();
            return players.getValue();
        }

        if (entity instanceof EndCrystalEntity)
            return crystals.getValue();

        return switch (entity.getType().getSpawnGroup()) {
            case CREATURE, WATER_CREATURE -> creatures.getValue();
            case MONSTER -> monsters.getValue();
            case AMBIENT, WATER_AMBIENT -> ambients.getValue();
            default -> others.getValue();
        };
    }

    public void onRender3D(MatrixStack matrices) {
        if (hands.getValue())
            Managers.SHADER.renderShader(() -> ((IGameRenderer) mc.gameRenderer).irenderHand(mc.gameRenderer.getCamera(), Render3DEngine.getTickDelta(), matrices.peek().getPositionMatrix()), handsMode.getValue());
    }

    @Override
    public void onDisable() {
        Managers.SHADER.reloadShaders();
    }
}
