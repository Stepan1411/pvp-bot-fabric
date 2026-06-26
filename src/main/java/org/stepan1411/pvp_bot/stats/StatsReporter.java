package org.stepan1411.pvp_bot.stats;

import com.google.gson.Gson;
import net.minecraft.server.MinecraftServer;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class StatsReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger("pvp_bot_stats");
    private static final Gson GSON = new Gson();
    private static final String DEFAULT_BACKEND_URL = "http://pvpbotstatsapi.survivalworld.win:32220";
    private static HttpClient client;
    private static String backendUrl;
    private static String serverName;
    private static long damageDealt;
    private static long damageReceived;

    public static void init() {
        StatsConfig.load();
        if (!StatsConfig.isEnabled()) {
            LOGGER.info("Anonymous statistics disabled via stats_config.json");
            return;
        }
        client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        backendUrl = System.getProperty("pvpbot.stats.url", DEFAULT_BACKEND_URL);
        serverName = System.getProperty("pvpbot.stats.server", "default");
        LOGGER.info("StatsReporter initialized, backend: {}, server: {}", backendUrl, serverName);
        LOGGER.info("TIP: Monitor your bot statistics at http://pvpbotstats.survivalworld.win/");
    }

    public static void onServerStarted(MinecraftServer server) {
        if (client == null) return;
        if (!server.isDedicated()) {
            serverName = System.getProperty("pvpbot.stats.server", "default") + "_sp";
            LOGGER.info("Singleplayer detected — using server name '{}' to avoid conflicting with dedicated server", serverName);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            sendStop();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }));
    }

    public static void addDamageDealt(float amount) {
        damageDealt += Math.round(amount);
    }

    public static void addDamageReceived(float amount) {
        damageReceived += Math.round(amount);
    }

    public static void sendSpawn(String botName) {
        if (client == null || !StatsConfig.isEnabled()) return;
        sendAsync("spawn", payload -> {
            payload.put("botName", botName);
        });
    }

    public static void sendKill() {
        if (client == null || !StatsConfig.isEnabled()) return;
        sendAsync("kill", payload -> {});
    }

    public static void sendHeartbeat() {
        if (client == null || !StatsConfig.isEnabled()) return;
        long dd = damageDealt;
        long dr = damageReceived;
        damageDealt = 0;
        damageReceived = 0;
        sendAsync("heartbeat", payload -> {
            payload.put("botsActive", BotManager.getBotCount());
            if (dd > 0) payload.put("damageDealt", dd);
            if (dr > 0) payload.put("damageReceived", dr);
        });
    }

    public static void sendStop() {
        if (client == null || !StatsConfig.isEnabled()) return;
        sendSync("stop", payload -> {});
    }

    private static void sendAsync(String event, java.util.function.Consumer<Map<String, Object>> enrich) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("server", serverName);
            payload.put("event", event);
            enrich.accept(payload);
            String json = GSON.toJson(payload);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + "/api/ingest"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(3))
                .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .exceptionally(ex -> {
                    LOGGER.warn("Failed to send event '{}': {}", event, ex.getMessage());
                    return null;
                });
        } catch (Exception e) {
            LOGGER.warn("Failed to prepare event '{}': {}", event, e.getMessage());
        }
    }

    private static void sendSync(String event, java.util.function.Consumer<Map<String, Object>> enrich) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("server", serverName);
            payload.put("event", event);
            enrich.accept(payload);
            String json = GSON.toJson(payload);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + "/api/ingest"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(3))
                .build();
            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            LOGGER.warn("Failed to send event '{}': {}", event, e.getMessage());
        }
    }
}
