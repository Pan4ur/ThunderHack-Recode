package thunder.hack.features.modules.misc;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.chunk.WorldChunk;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ConfigManager;
import thunder.hack.gui.notification.Notification;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.render.StorageEsp;
import thunder.hack.setting.Setting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class StashLogger extends Module {
    private final Setting<Boolean> sound = new Setting<>("Sound", true);
    private final Setting<Boolean> saveToFile = new Setting<>("SaveToFile", true);
    private final Setting<Integer> minChests = new Setting<>("MinChests", 5, 0, 100);
    private final Setting<Integer> minShulkers = new Setting<>("MinShulkers", 0, 0, 100);

    private List<WorldChunk> savedChunks = new ArrayList<>();

    public StashLogger() {
        super("StashLogger", Category.MISC);
    }

    @Override
    public void onEnable() {
        savedChunks.clear();
    }

    @Override
    public void onUpdate() {
        for (WorldChunk chunk : StorageEsp.getLoadedChunks()) {
            if (savedChunks.contains(chunk))
                continue;

            List<BlockEntity> storages = chunk.getBlockEntities().values().stream().toList();

            int chests = 0;
            int shulkers = 0;

            for (BlockEntity storage : storages) {
                if (storage instanceof ChestBlockEntity)
                    chests++;
                if (storage instanceof ShulkerBoxBlockEntity)
                    shulkers++;
            }

            if (chests >= minChests.getValue() && shulkers >= minShulkers.getValue()) {
                savedChunks.add(chunk);

                String str = "Stash pos: X:" + chunk.getPos().getCenterX() + " Z:" + chunk.getPos().getCenterZ() + " Chests: " + chests + " Shulkers: " + shulkers;
                Managers.NOTIFICATION.publicity("StashLogger", str, 5, Notification.Type.SUCCESS);
                sendMessage(str);

                if (sound.getValue())
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 1f, 1f);

                String serverIP = "unknown_server";
                if (mc.getNetworkHandler().getServerInfo() != null && mc.getNetworkHandler().getServerInfo().address != null)
                    serverIP = mc.getNetworkHandler().getServerInfo().address.replace(':', '_');

                if (saveToFile.getValue())
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(ConfigManager.STASHLOGGER_FOLDER, serverIP + ".txt"), true));
                        writer.append("\nStash pos: X:" + chunk.getPos().getCenterX() + " Z:" + chunk.getPos().getCenterZ() + " Chests: " + chests + " Shulkers: " + shulkers);
                        writer.close();
                    } catch (Exception e) {
                    }
            }
        }
    }
}
