package thunder.hack.core;

import thunder.hack.cmd.Command;
import net.minecraft.entity.player.PlayerEntity;

import java.io.*;
import java.util.*;

public class FriendManager  {
    public static List<String> friends = new ArrayList<>();

    public boolean isFriend(String name) {
        return this.friends.stream().anyMatch(friend -> friend.equalsIgnoreCase(name));
    }

    public boolean isFriend(PlayerEntity player) {
        return this.isFriend((player.getName().getString()));
    }

    public void removeFriend(String name) {
        friends.remove(name);
    }

    public static void loadFriends(){
        try {
            File file = new File("ThunderHackRecode/misc/friends.txt");

            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    while (reader.ready()) {
                        friends.add(reader.readLine());
                    }

                }
            }
        } catch (Exception ignored) {}
    }

    public static void saveFriends() {
        File file = new File("ThunderHackRecode/misc/friends.txt");
        try {
            file.createNewFile();
        } catch (Exception ignored){

        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String friend : friends) {
                writer.write(friend + "\n");
            }
        } catch (Exception ignored){}
    }

    public List<String> getFriends() {
        return this.friends;
    }

    public void addFriend(String friend) {
        this.friends.add(friend);
        try {
       //     ThunderUtils.saveUserAvatar("https://minotar.net/helm/" + friend + "/100.png", friend);
        } catch (Exception e){
            Command.sendMessage("Не удалось загрузить скин!");
        }
    }

    public void clear() {
        friends.clear();
    }
}

