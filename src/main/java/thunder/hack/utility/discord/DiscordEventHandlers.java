package thunder.hack.utility.discord;

import java.util.Arrays;
import java.util.List;
import thunder.hack.utility.discord.callbacks.JoinGameCallback;
import thunder.hack.utility.discord.callbacks.ErroredCallback;
import thunder.hack.utility.discord.callbacks.ReadyCallback;
import thunder.hack.utility.discord.callbacks.SpectateGameCallback;
import thunder.hack.utility.discord.callbacks.JoinRequestCallback;
import thunder.hack.utility.discord.callbacks.DisconnectedCallback;
import com.sun.jna.Structure;

public class DiscordEventHandlers extends Structure {
    public DisconnectedCallback disconnected;
    public JoinRequestCallback joinRequest;
    public SpectateGameCallback spectateGame;
    public ReadyCallback ready;
    public ErroredCallback errored;
    public JoinGameCallback joinGame;
    
    protected List<String> getFieldOrder() {
        return Arrays.asList("ready", "disconnected", "errored", "joinGame", "spectateGame", "joinRequest");
    }
}