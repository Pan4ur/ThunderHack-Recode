package thunder.hack.modules.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtil;
import thunder.hack.utility.ThunderUtils;
import thunder.hack.utility.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.StringUtils;
import thunder.hack.utility.player.PlayerUtil;

import java.util.ArrayList;
import java.util.Objects;

public class FGHelper extends Module {
    public FGHelper() {
        super("FGHelper", Category.MISC);
    }


    private Setting<Mode> mode = new Setting("Server", Mode.Survival);
    public Setting<Boolean> photomath = new Setting<>("PhotoMath", false, v-> mode.getValue() == Mode.Survival);
    public Setting<Boolean> spam = new Setting<>("Spam", false,v-> photomath.getValue() && mode.getValue() == Mode.Survival);
    public Setting<Boolean> cappuccino = new Setting<>("Cappuccino", true, v-> mode.getValue() == Mode.Survival);
    public Setting<Integer> triggerhealth = new Setting<>("TriggerHealth", 10, 1, 36, v-> mode.getValue() == Mode.Survival && cappuccino.getValue());
    public Setting<Boolean> americano = new Setting<>("Americano", true, v-> mode.getValue() == Mode.Survival);
    public Setting<Boolean> powder = new Setting<>("Powder", true, v-> mode.getValue() == Mode.Survival);
    public Setting<Boolean> antiTpHere = new Setting<>("AntiTpHere", false, v-> mode.getValue() == Mode.Survival);
    public Setting<Boolean> clanInvite = new Setting<>("ClanInvite", false, v-> mode.getValue() == Mode.Survival);
    public Setting<Integer> clanInviteDelay = new Setting<>("InviteDelay", 10, 1, 30, v-> mode.getValue() == Mode.Survival && clanInvite.getValue());
    public Setting<Boolean> jboost = new Setting<>("JumpBoostRemove", true,v-> powder.getValue() && mode.getValue() == Mode.Survival);
    public final Setting<Boolean> fixAll = new Setting<>("/fix all", true, v-> mode.getValue() == Mode.Grief);
    public final Setting<Boolean> feed = new Setting<>("/feed", true, v-> mode.getValue() == Mode.Grief);
    public final Setting<Boolean> near = new Setting<>("/near", true, v-> mode.getValue() == Mode.Grief);
    public final Setting<Boolean> airDropWay = new Setting<>("AirDropWay", true, v-> mode.getValue() == Mode.Grief);
    public final Setting<Boolean> farmilka = new Setting<>("Farmilka", true, v-> mode.getValue() == Mode.Grief);

    public Timer timer = new Timer();
    private Timer pvpTimer = new Timer();
    private Timer inviteTimer = new Timer();


    @Subscribe
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof GameMessageS2CPacket && photomath.getValue()) {
            final GameMessageS2CPacket packet = event.getPacket();
            if (packet.content().getString().contains("Решите: ") && Objects.equals(ThunderUtils.solvename(packet.content().getString()), "FATAL ERROR")) {
                int solve = Integer.parseInt(StringUtils.substringBetween(packet.content().getString(), "Решите: ", " + ")) + Integer.parseInt(StringUtils.substringBetween(packet.content().getString(), " + ", " кто первый"));
                for (int i = 0; i < (spam.getValue() ? 9 : 1); i++) mc.player.networkHandler.sendChatMessage(String.valueOf(solve));
            }
        }
        if (event.getPacket() instanceof GameMessageS2CPacket && airDropWay.getValue()) {
            final GameMessageS2CPacket packet = event.getPacket();

            if (packet.content().getString().contains("Аирдроп")){
                int xCord = Integer.parseInt(StringUtils.substringBetween(packet.content().getString(), "координаты X: ", " Y:"));
                int yCord = Integer.parseInt(StringUtils.substringBetween(packet.content().getString(), "Y: ", " Z:"));
                int zCord = Integer.parseInt(StringUtils.substringBetween(packet.content().getString() + "nigga", "Z: ", "nigga"));
                Thunderhack.gps_position = new BlockPos(xCord, yCord, zCord);
                Thunderhack.notificationManager.publicity("FGHelper","Поставлена метка на аирдроп! X: " + xCord + " Y: " + yCord + " Z: " + zCord, 5, Notification.Type.SUCCESS);
            }
        }
        if(event.getPacket() instanceof GameMessageS2CPacket && antiTpHere.getValue()){
            final GameMessageS2CPacket packet = event.getPacket();
            if (packet.content().getString().contains("Телепортирование...") && check(packet.content().getString())) {
                flag = true;
                atphtimer.reset();
            }
        }
    }



    @Override
    public void onUpdate() {
        if(mode.getValue() == Mode.Survival) {
            if (mc.player.getHealth() < triggerhealth.getValue() && timer.passedMs(200) && InventoryUtil.getCappuchinoAtHotbar() != -1 && cappuccino.getValue()) {
                int hotbarslot = mc.player.getInventory().selectedSlot;
                mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.AMBIENT, 150.0f, 1.0F);
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(InventoryUtil.getCappuchinoAtHotbar()));
                mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtil.getWorldActionId(mc.world)));
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(hotbarslot));
                timer.reset();
            }
            if (timer.passedMs(200) && InventoryUtil.getAmericanoAtHotbar() != -1 && !mc.player.hasStatusEffect(StatusEffects.HASTE) && americano.getValue()) {
                int hotbarslot = mc.player.getInventory().selectedSlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(InventoryUtil.getAmericanoAtHotbar()));
                mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtil.getWorldActionId(mc.world)));
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(hotbarslot));
                timer.reset();
            }
            if (timer.passedMs(500) && InventoryUtil.getPowderAtHotbar() != -1 && !(mc.player.hasStatusEffect(StatusEffects.STRENGTH)) && mc.crosshairTarget != null && mc.crosshairTarget.getType() != HitResult.Type.BLOCK && powder.getValue()) {
                int hotbarslot = mc.player.getInventory().selectedSlot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(InventoryUtil.getPowderAtHotbar()));
                mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtil.getWorldActionId(mc.world)));
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(hotbarslot));
                timer.reset();
            }
            if (jboost.getValue() && powder.getValue()) {
                if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                    mc.player.removeStatusEffect(StatusEffects.JUMP_BOOST);
                }
            }

            if (flag && atphtimer.passedMs(100) && antiTpHere.getValue()) {
                StringBuilder log = new StringBuilder("Тебя телепортировали в X: " + (int) mc.player.getX() + " Z: " + (int) mc.player.getZ() + ". Ближайшие игроки : ");

                for (PlayerEntity entity : mc.world.getPlayers()) {
                    if (entity == mc.player) continue;
                    log.append(entity.getName().getString()).append(" ");
                }
                Command.sendMessage(String.valueOf(log));

                mc.player.networkHandler.sendCommand("back");
                flag = false;
            }
            if (inviteTimer.passedS(clanInviteDelay.getValue()) && clanInvite.getValue()) {
                ArrayList<String> playersNames = new ArrayList<>();
                for (PlayerListEntry player : mc.player.networkHandler.getPlayerList()) {
                    playersNames.add(player.getProfile().getName());
                }
                if (playersNames.size() > 1) {
                    int randomName = (int) Math.floor(Math.random() * playersNames.size());
                    mc.player.networkHandler.sendCommand("c invite " + playersNames.get(randomName));
                    playersNames.clear();
                    inviteTimer.reset();
                }
            }

        } else {
            if(feed.getValue() && mc.player.getHungerManager().getFoodLevel() < 8){
                if(canSendCommand()) {
                    mc.player.networkHandler.sendChatCommand("feed");
                }
            }
            if(fixAll.getValue()){
                if(canSendCommand()){
                    mc.player.networkHandler.sendChatCommand("fix all");
                }
            }
            if(mc.player.hurtTime > 0){
                pvpTimer.reset();
            }
            if(near.getValue()) {
                if (mc.player.age % 30 == 0) mc.player.networkHandler.sendChatCommand("near");
            }
            if(farmilka.getValue()){
                for(Entity ent : mc.world.getEntities()){
                    if(ent instanceof PlayerEntity) continue;
                    if(ent instanceof LivingEntity) {
                        if (((LivingEntity) ent).isDead()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
                    }
                }
            }
        }
    }

    private boolean canSendCommand(){
        if(pvpTimer.passedMs(30000)){
            pvpTimer.reset();
            return true;
        }
        return false;
    }

    public enum Mode {
        Survival, Grief
    }


    Timer atphtimer = new Timer();
    Timer checktimer = new Timer();
    private boolean flag = false;

    public boolean check(String checkstring) {
        return checktimer.passedMs(3000) && (Objects.equals(ThunderUtils.solvename(checkstring), "FATAL ERROR"));
    }

    @Subscribe
    public void onPacketSend(PacketEvent.Send e) {
        if (e.getPacket() instanceof CommandExecutionC2SPacket) {
            checktimer.reset();
        }
    }
}
