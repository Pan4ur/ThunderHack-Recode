package thunder.hack.modules.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.Thunderhack;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;

public class Shaders extends Module {

    public Shaders() {
        super("Shaders", Category.RENDER);
    }

/*
        Thanks to @0x3C50 for Shader rendering example
 */

    public Setting<Mode> mode = new Setting<>("Mode", Mode.Default);

    public enum Mode{
        Default, Smoke, Gradient
    }
    public final Setting<Integer> maxRange = new Setting<>("MaxRange", 64, 16, 256);
    private final Setting<Boolean> players = new Setting<>("Players", true);
    private final Setting<Boolean> friends = new Setting<>("Friends", true);
    private final Setting<Boolean> crystals = new Setting<>("Crystals", true);
    private final Setting<Boolean> creatures = new Setting<>("Creatures", false);
    private final Setting<Boolean> monsters = new Setting<>("Monsters", false);
    private final Setting<Boolean> ambients = new Setting<>("Ambients", false);
    private final Setting<Boolean> others = new Setting<>("Others", false);
    public final Setting<Integer> lineWidth = new Setting<>("LineWidth", 2, 0, 20);
    public final Setting<Integer> quality = new Setting<>("Quality", 10, 5, 30);
    public final Setting<Integer> octaves = new Setting<>("Octaves", 10, 5, 30,v->mode.getValue() == Mode.Smoke);
    public final Setting<Integer> fillAlpha = new Setting<>("FillAlpha", 170, 0, 255,v->mode.getValue() != Mode.Default);
    public final Setting<Boolean> glow = new Setting<>("Glow", true, v->mode.getValue() == Mode.Smoke);

  //  public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x8800FF00),v->mode.getValue() == Mode.Default);

    public final Setting<ColorSetting> outlineColor = new Setting<>("OutlineColor1", new ColorSetting(0x8800FF00),v->mode.getValue() != Mode.Gradient);
    public final Setting<ColorSetting> outlineColor1 = new Setting<>("OutlineColor2", new ColorSetting(0x8800FF00),v->mode.getValue() == Mode.Smoke);
    public final Setting<ColorSetting> outlineColor2 = new Setting<>("OutlineColor3", new ColorSetting(0x8800FF00),v->mode.getValue() == Mode.Smoke);

    public final Setting<ColorSetting> fillColor1 = new Setting<>("FillColor1", new ColorSetting(0x8800FF00),v->mode.getValue() != Mode.Gradient);
    public final Setting<ColorSetting> fillColor2 = new Setting<>("FillColor2", new ColorSetting(0x8800FF00),v->mode.getValue() == Mode.Smoke);
    public final Setting<ColorSetting> fillColor3 = new Setting<>("FillColor3", new ColorSetting(0x8800FF00),v->mode.getValue() == Mode.Smoke);


    public final Setting<Float> factor = new Setting<>("Factor", 2f, 0f, 20f,v->mode.getValue() == Mode.Gradient);
    public final Setting<Float> gradient = new Setting<>("Gradient", 2f, 0f, 20f,v->mode.getValue() == Mode.Gradient);
    public final Setting<Integer> alpha2 = new Setting<>("Alpha2", 170, 0, 255,v->mode.getValue() == Mode.Gradient);


    public boolean shouldRender(Entity entity) {
        if (entity == null) return false;
      //  if(mc.player.squaredDistanceTo(entity.getPos()) > maxRange.getPow2Value()) return false;
        if (entity instanceof PlayerEntity) {
            if (entity == mc.player) return false;
            if (Thunderhack.friendManager.isFriend((PlayerEntity) entity)) {
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
}
