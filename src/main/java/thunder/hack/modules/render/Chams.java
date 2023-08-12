package thunder.hack.modules.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventHeldItemRenderer;
import thunder.hack.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.player.PlaceUtility;

import java.awt.*;

public class Chams extends Module {
    public Chams() {
        super("Chams", Category.RENDER);
    }

    private final Setting<Parent> selection = new Setting<>("Selection", new Parent(false,0));
    private final Setting<Boolean> players = new Setting<>("Players", true).withParent(selection);
    private final Setting<Boolean> friends = new Setting<>("Friends", true).withParent(selection);
    private final Setting<Boolean> crystals = new Setting<>("Crystals", true).withParent(selection);
    private final Setting<Boolean> creatures = new Setting<>("Creatures", false).withParent(selection);
    private final Setting<Boolean> monsters = new Setting<>("Monsters", false).withParent(selection);
    private final Setting<Boolean> ambients = new Setting<>("Ambients", false).withParent(selection);
    private final Setting<Boolean> handItems = new Setting<>("HandItems", false).withParent(selection);

    private static final Setting<Parent> colors = new Setting<>("Colors", new Parent(false,0));
    private static final Setting<ColorSetting> player = new Setting<>("Player", new ColorSetting(0xFFFFFFFF)).withParent(colors);
    private static final Setting<ColorSetting> friend = new Setting<>("Friend", new ColorSetting(0xFFFFFFFF)).withParent(colors);
    private static final Setting<ColorSetting> crystal = new Setting<>("Crystal", new ColorSetting(0xFFFFFFFF)).withParent(colors);
    private static final Setting<ColorSetting> creature = new Setting<>("Creature", new ColorSetting(0xFFFFFFFF)).withParent(colors);
    private static final Setting<ColorSetting> monster = new Setting<>("Monster", new ColorSetting(0xFFFFFFFF)).withParent(colors);
    private static final Setting<ColorSetting> ambient = new Setting<>("Ambient", new ColorSetting(0xFFFFFFFF)).withParent(colors);
    private final Setting<ColorSetting> handItemsColor = new Setting<>("HandItemsColor", new ColorSetting(new Color(0x9317DE5D, true))).withParent(colors);

    private static final Setting<Parent> scales = new Setting<>("Scales", new Parent(false,0));
    private static final Setting<Float> playerScale = new Setting<>("PlayerScale", 1F, 0f, 5F).withParent(scales);
    private static final Setting<Float> friendScale = new Setting<>("FriendScale", 1F, 0f, 5F).withParent(scales);
    private static final Setting<Float> crystalScale = new Setting<>("CrystalScale", 2F, 0f, 5F).withParent(scales);
    private static final Setting<Float> creatureScale = new Setting<>("CreatureScale", 1F, 0f, 5F).withParent(scales);
    private static final Setting<Float> monsterScale = new Setting<>("MonsterScale", 1F, 0f, 5F).withParent(scales);
    private static final Setting<Float> ambientScale = new Setting<>("AmbientScale", 1F, 0f, 5F).withParent(scales);


    @EventHandler
    public void onRenderHands(EventHeldItemRenderer e){
        if(handItems.getValue()){
            RenderSystem.setShaderColor(handItemsColor.getValue().getRed() / 255f,handItemsColor.getValue().getGreen() / 255f,handItemsColor.getValue().getBlue() / 255f,handItemsColor.getValue().getAlpha() / 255f);
        }
    }


    public static ColorSetting getEntityColor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            if (Thunderhack.friendManager.isFriend((PlayerEntity) entity)) {
                return friend.getValue();
            }

            return player.getValue();
        }

        if (entity instanceof EndCrystalEntity) {
            return crystal.getValue();
        }

        switch (entity.getType().getSpawnGroup()) {
            case CREATURE:
            case WATER_AMBIENT:
            case WATER_CREATURE: return creature.getValue();
            case MONSTER: return monster.getValue();
            case AMBIENT: return ambient.getValue();
            default: return new ColorSetting(0xFFFFFFFF);
        }
    }

    public static float getEntityScale(Entity entity) {
        if (entity instanceof PlayerEntity) {
            if (Thunderhack.friendManager.isFriend((PlayerEntity) entity)) {
                return friendScale.getValue();
            }

            return playerScale.getValue();
        }

        if (entity instanceof EndCrystalEntity) {
            return crystalScale.getValue();
        }

        switch (entity.getType().getSpawnGroup()) {
            case CREATURE:
            case WATER_AMBIENT:
            case WATER_CREATURE: return creatureScale.getValue();
            case MONSTER: return monsterScale.getValue();
            case AMBIENT: return ambientScale.getValue();
            default: return 1F;
        }
    }

    public boolean shouldRender(Entity entity) {
        if (entity == null) {
            return false;
        }

        if (!PlaceUtility.canSee(entity)) {
            return false;
        }

        if (entity instanceof PlayerEntity) {
            if (entity == mc.player) return false;
            if (Thunderhack.friendManager.isFriend((PlayerEntity) entity)) {
                return friends.getValue();
            }
            return players.getValue();
        }

        if (entity instanceof EndCrystalEntity) {
            return crystals.getValue();
        }

        switch (entity.getType().getSpawnGroup()) {
            case CREATURE:
            case WATER_AMBIENT:
            case WATER_CREATURE: return creatures.getValue();
            case MONSTER: return monsters.getValue();
            case AMBIENT: return ambients.getValue();
            default: return false;
        }
    }
}
