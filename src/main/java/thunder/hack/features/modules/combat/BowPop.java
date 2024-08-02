package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.*;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import java.util.Random;

public final class BowPop extends Module {
    public Setting<Boolean> rotation = new Setting<>("Rotation", false);
    public Setting<ModeEn> Mode = new Setting<>("Mode", ModeEn.Maximum);
    public Setting<Float> factor = new Setting<>("Factor", 1f, 1f, 20f);
    public Setting<exploitEn> exploit = new Setting<>("Exploit", exploitEn.Strong);
    public Setting<Float> scale = new Setting<>("Scale", 0.01f, 0.01f, 0.4f);
    public Setting<Boolean> minimize = new Setting<>("Minimize", false);
    public Setting<Float> delay = new Setting<>("Delay", 5f, 0f, 10f);
    public final Setting<SettingGroup> selection = new Setting<>("Selection", new SettingGroup(false, 0));
    public final Setting<Boolean> bow = new Setting<>("Bows", true).addToGroup(selection);
    public final Setting<Boolean> pearls = new Setting<>("EPearls", true).addToGroup(selection);
    public final Setting<Boolean> xp = new Setting<>("XP", true).addToGroup(selection);
    public final Setting<Boolean> eggs = new Setting<>("Eggs", true).addToGroup(selection);
    public final Setting<Boolean> potions = new Setting<>("SplashPotions", true).addToGroup(selection);
    public final Setting<Boolean> snowballs = new Setting<>("Snowballs", true).addToGroup(selection);

    public static Timer delayTimer = new Timer();
    private final Random rnd = new Random();

    public BowPop() {
        super("BowPop", Category.COMBAT);
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (fullNullCheck() || !delayTimer.passedMs((long) (delay.getValue() * 1000))) return;
        if (event.getPacket() instanceof PlayerActionC2SPacket && ((PlayerActionC2SPacket) event.getPacket()).getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && (mc.player.getActiveItem().getItem() == Items.BOW && bow.getValue())
                || event.getPacket() instanceof PlayerInteractItemC2SPacket && ((PlayerInteractItemC2SPacket) event.getPacket()).getHand() == Hand.MAIN_HAND && ((mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL && pearls.getValue()) || (mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE && xp.getValue()) || (mc.player.getMainHandStack().getItem() == Items.EGG && eggs.getValue()) || (mc.player.getMainHandStack().getItem() == Items.SPLASH_POTION && potions.getValue()) || (mc.player.getMainHandStack().getItem() == Items.SNOWBALL && snowballs.getValue()))) {

            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));

            double[] strict_direction = new double[]{100f * -Math.sin(Math.toRadians(mc.player.getYaw())), 100f * Math.cos(Math.toRadians(mc.player.getYaw()))};

            if (exploit.getValue() == exploitEn.Fast) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX(), minimize.getValue() ? mc.player.getY() : mc.player.getY() - 1e-10, mc.player.getZ(), true);
                    spoof(mc.player.getX(), mc.player.getY() + 1e-10, mc.player.getZ(), false);
                }
            }
            if (exploit.getValue() == exploitEn.Strong) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX(), mc.player.getY() + 1e-10, mc.player.getZ(), false);
                    spoof(mc.player.getX(), minimize.getValue() ? mc.player.getY() : mc.player.getY() - 1e-10, mc.player.getZ(), true);
                }
            }
            if (exploit.getValue() == exploitEn.Phobos) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX(), mc.player.getY() + 0.00000000000013, mc.player.getZ(), true);
                    spoof(mc.player.getX(), mc.player.getY() + 0.00000000000027, mc.player.getZ(), false);
                }
            }
            if (exploit.getValue() == exploitEn.Strict) {
                for (int i = 0; i < getRuns(); i++) {
                    if (rnd.nextBoolean()) {
                        spoof(mc.player.getX() - strict_direction[0], mc.player.getY(), mc.player.getZ() - strict_direction[1], false);
                    } else {
                        spoof(mc.player.getX() + strict_direction[0], mc.player.getY(), mc.player.getZ() + strict_direction[1], true);
                    }
                }
            }
            if (exploit.getValue() == exploitEn.WB) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX() + getWorldBorderRnd(), mc.player.getY(), mc.player.getZ() + getWorldBorderRnd(), false);
                }
            }
            delayTimer.reset();
        }
    }

    private void spoof(double x, double y, double z, boolean ground) {
        if (rotation.getValue())
            sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, mc.player.getYaw(), mc.player.getPitch(), ground));
        else sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, ground));
    }

    private int getRuns() {
        return switch (Mode.getValue()) {
            case Normal -> (int) Math.floor(factor.getValue());
            case Maximum -> (int) (30f * factor.getValue());
            case Factorised -> 10 + (int) ((factor.getValue() - 1));
        };
    }

    private int getWorldBorderRnd() {
        if (mc.isInSingleplayer()) return 1;

        int n = rnd.nextInt(29000000);
        if (rnd.nextBoolean()) return n;
        return -n;
    }

    private enum exploitEn {
        Strong, Fast, Strict, Phobos, WB
    }

    private enum ModeEn {
        Normal, Maximum, Factorised
    }
}
