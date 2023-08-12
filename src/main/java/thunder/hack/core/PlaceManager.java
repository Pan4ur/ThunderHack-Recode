package thunder.hack.core;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import thunder.hack.utility.math.Placement;

import java.util.LinkedList;

import static thunder.hack.modules.Module.mc;

public class PlaceManager {

    private final static LinkedList<Placement> placements = new LinkedList<>();

    private static float[] trailingRotation = null;

    public static Runnable trailingBreakAction = null;
    public static Runnable trailingPlaceAction = null;
    public static Runnable trailingChargeAction = null;


    public static boolean syncSprinting;
    public static boolean syncSneaking;

    public static void setTrailingRotation(float[] rotation) {
        trailingRotation = rotation;
    }

    public static void add(Placement action) {
        placements.add(action);
    }

    @EventHandler
    public void onSync(EventSync event) {
        handleActions(event);
    }

    private void handleActions(EventSync event) {
        int startSlot = mc.player.getInventory().selectedSlot;

        while (!placements.isEmpty()) {
            Placement action = placements.pop();
            if (action.isRotate()) {
                event.cancel();
                rotate(action.getYaw(), action.getPitch());
            }
            action.getAction().run();
        }

        if (trailingRotation != null) {
            event.cancel();
            rotate(trailingRotation[0], trailingRotation[1]);
            trailingRotation = null;
        }

        if (trailingPlaceAction != null) {
            trailingPlaceAction.run();
            trailingPlaceAction = null;
        }

        if (trailingBreakAction != null) {
            trailingBreakAction.run();
            trailingBreakAction = null;
        }

        if (trailingChargeAction != null) {
            trailingChargeAction.run();
            trailingChargeAction = null;
        }

        if (mc.player.isSprinting() && syncSprinting) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }

        if (!mc.player.isSneaking() && syncSneaking) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }

        if (mc.player.getInventory().selectedSlot != startSlot) {
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
