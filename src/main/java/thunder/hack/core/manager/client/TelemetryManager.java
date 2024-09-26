package thunder.hack.core.manager.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.commons.compress.utils.Lists;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.utility.Timer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class TelemetryManager implements IManager {
    private final Timer pingTimer = new Timer();
    private List<String> onlinePlayers = new ArrayList<>();
    private List<String> allPlayers = new ArrayList<>();

    public void onUpdate() {
        if (pingTimer.every(90000))
            fetchData();
    }

    public void fetchData() {
        if (ClientSettings.telemetry.getValue())
            pingServer(mc.getSession().getUsername());
        onlinePlayers = getPlayers(true);
        allPlayers = getPlayers(false);
    }

    public void pingServer(String name) {
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.thunderhack.net/v1/users/online?name=" + name))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            client.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Throwable ignored) {
        }
    }

    public static List<String> getPlayers(boolean online) {
        final HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.thunderhack.net/v1/users" + (online ? "/online" : "")))
                .GET()
                .build();
        final List<String> names = new ArrayList<>();

        try (final HttpClient client = HttpClient.newHttpClient()) {
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonArray array = JsonParser.parseString(response.body()).getAsJsonArray();
            array.forEach(e -> names.add(e.getAsJsonObject().get("name").getAsString()));
        } catch (Throwable ignored) {
        }
        return names;
    }

    public List<String> getOnlinePlayers() {
        return Lists.newArrayList(onlinePlayers.iterator());
    }

    public List<String> getAllPlayers() { // Method 'getAllPlayers()' is never used
        return Lists.newArrayList(allPlayers.iterator());
    }
}
