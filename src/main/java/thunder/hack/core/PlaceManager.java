package thunder.hack.core;

import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.utility.player.Action;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import java.util.LinkedList;

import static thunder.hack.modules.Module.mc;

public class PlaceManager {

    private final static LinkedList<Action> actions = new LinkedList<>();

    private static float[] trailingRotation = null;
    public static Runnable trailingBreakAction = null;
    public static Runnable trailingPlaceAction = null;
    
    
    public static boolean syncSprinting;
    public static boolean syncSneaking;


    public PlaceManager() {
        Thunderhack.EVENT_BUS.register(this);
    }

    public static void setTrailingRotation(float[] rotation) {
        trailingRotation = rotation;
    }

    public static boolean add(Action action) {
        actions.add(action);
        return true;
    }

    @Subscribe
    public void onSync(EventSync event) {
        handleActions(event);
    }

    private void handleActions(EventSync event) {
        int startSlot = mc.player.getInventory().selectedSlot;

        boolean ranAction = false;

        while (!actions.isEmpty()) {
            Action action = actions.pop();
            if (ranAction && action.isOptional()) continue;

            if (action.isRotate()) {
                event.cancel();
                rotate(action.getYaw(), action.getPitch());
            }

            Runnable runnable = action.getAction();
            if (runnable != null) runnable.run();
            ranAction = true;
        }

        if (trailingRotation != null) {
            event.cancel();
            rotate(trailingRotation[0], trailingRotation[1]);
            trailingRotation = null;
        }

        if (trailingBreakAction != null) {
            trailingBreakAction.run();
            trailingBreakAction = null;
        }

        if (trailingPlaceAction != null) {
            trailingPlaceAction.run();
            trailingPlaceAction = null;
        }

        if (mc.player.isSprinting() && syncSprinting) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }

        if (!mc.player.isSneaking() && syncSneaking) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }

        if (mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot).getItem() != Items.END_CRYSTAL && mc.player.getInventory().selectedSlot != startSlot) {
             mc.player.getInventory().selectedSlot = startSlot;
             mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(startSlot));
        }

        syncSneaking = false;
        syncSprinting = false;
    }

    public static boolean isRotating = false;

    public void rotate(float yaw, float pitch) {
        float prevYaw = mc.player.getYaw();
        float prevPitch = mc.player.getPitch();
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
        isRotating = true;
        ((IClientPlayerEntity) mc.player).iSendMovementPackets();
        isRotating = false;
        mc.player.setYaw(prevYaw);
        mc.player.setPitch(prevPitch);
    }
}
