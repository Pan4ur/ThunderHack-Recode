package thunder.hack.core.impl;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import thunder.hack.core.IManager;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TelemetryManager implements IManager {
    public String SERVER = "https://thunderhack-site.vercel.app";
    private String playerName;

    public void onLoad() {
        LogUtils.getLogger().info("Loading TelemetryManager...");
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) {
            playerName = mc.getSession().getUsername();
            LogUtils.getLogger().info("Player name set to: " + playerName);
        } else {
            playerName = "Unknown";
            LogUtils.getLogger().warn("Player name could not be set. Defaulting to 'Unknown'.");
        }
    }

    public void telemetryLogin() {
        int responseCode = -1;
        HttpURLConnection connection = null;
        try {
            LogUtils.getLogger().info("Starting telemetry login process.");

            URL url = new URL(SERVER + "/api/play");
            LogUtils.getLogger().info("Connecting to URL: " + url.toString());

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            LogUtils.getLogger().info("HTTP connection configured. Method: POST");

            String jsonPayload = "{\"username\": \"" + playerName + "\"}";
            LogUtils.getLogger().info("Payload prepared: " + jsonPayload);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
                LogUtils.getLogger().info("Payload sent to the server.");
            }

            responseCode = connection.getResponseCode();
            LogUtils.getLogger().info("Received response code: " + responseCode);
        } catch (Exception e) {
            LogUtils.getLogger().error("An error occurred during telemetry login: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
                LogUtils.getLogger().info("HTTP connection disconnected.");
            }
        }
    }
}