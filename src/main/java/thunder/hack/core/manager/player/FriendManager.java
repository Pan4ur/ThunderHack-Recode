package thunder.hack.core.manager.player;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.jetbrains.annotations.NotNull;
import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.core.manager.IManager;
import thunder.hack.core.manager.client.ConfigManager;

import java.io.*;
import java.util.*;

public class FriendManager implements IManager {
    public static List<String> friends = new ArrayList<>();

    public boolean isFriend(String name) {
        return friends.stream().anyMatch(friend -> friend.equalsIgnoreCase(name));
    }

    public boolean isFriend(@NotNull PlayerEntity player) {
        return isFriend(player.getName().getString());
    }

    public void removeFriend(String name) {
        friends.remove(name);
    }

    public void addFriend(String friend) {
        friends.add(friend);
    }

    public List<String> getFriends() {
        return friends;
    }

    public void clear() {
        friends.clear();
    }

    public List<AbstractClientPlayerEntity> getNearFriends() {
        if (mc.world == null) return new ArrayList<>();

        return mc.world.getPlayers().stream()
                .filter(player -> friends.contains(player.getName().getString()))
                .toList();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveFriends() {
        File file = new File(ConfigManager.CONFIG_FOLDER_NAME + "/misc/friends.txt");
        try {
            file.createNewFile();
        } catch (Exception ignored) {
        }

        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String friend : friends)
                writer.write(friend + "\n");
        } catch (Exception ignored) {
        }
    }

    public void loadFriends() {
        try {
            File file = new File(ConfigManager.CONFIG_FOLDER_NAME + "/misc/friends.txt");

            if (file.exists()) {
                try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    while (reader.ready())
                        friends.add(reader.readLine());
                }
            }
        } catch (Exception ignored) {
        }
    }
}