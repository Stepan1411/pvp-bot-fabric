package org.stepan1411.pvp_bot.stats;

import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotSettings;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Отправляет анонимную статистику на сервер
 * https://stepan1411.github.io/pvpbot-stats/
 */
public class StatsReporter {
    
    private static final String STATS_ENDPOINT = "https://pvpbot-stats--stepanksv141114.replit.app/api/stats";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static String serverId = null;
    private static boolean enabled = true;
    private static net.minecraft.server.MinecraftServer currentServer = null;
    
    /**
     * Запускает периодическую отправку статистики
     */
    public static void start(net.minecraft.server.MinecraftServer server) {
        currentServer = server;
        // Проверяем настройки
        BotSettings settings = BotSettings.get();
        if (!settings.isSendStats()) {
            System.out.println("[PVP_BOT] Statistics reporting disabled in settings");
            enabled = false;
            return;
        }
        
        // Загружаем или создаём ID сервера
        serverId = loadOrCreateServerId();
        
        // Отправляем статистику сразу при старте
        sendStats();
        
        // Отправляем каждые 5 секунд (чтобы бэкенд знал что сервер онлайн)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                sendStats();
            } catch (Exception e) {
                // Тихо игнорируем ошибки чтобы не спамить логи
            }
        }, 5, 5, TimeUnit.SECONDS);
        
        System.out.println("[PVP_BOT] Statistics reporter started (Server ID: " + serverId.substring(0, 8) + "...)");
    }
    
    /**
     * Останавливает отправку статистики
     */
    public static void stop() {
        scheduler.shutdown();
        // Отправляем финальную статистику с bots_count = 0
        if (enabled) {
            sendStats();
        }
    }
    
    /**
     * Отправляет статистику на сервер
     */
    public static void sendStats() {
        if (!enabled || serverId == null) {
            return;
        }
        
        try {
            JsonObject stats = new JsonObject();
            stats.addProperty("server_id", serverId);
            stats.addProperty("bots_count", BotManager.getAllBots().size());
            stats.addProperty("bots_spawned_total", BotManager.getBotsSpawnedTotal());
            stats.addProperty("bots_killed_total", BotManager.getBotsKilledTotal());
            stats.addProperty("mod_version", getModVersion());
            stats.addProperty("minecraft_version", "1.21.11");
            stats.addProperty("timestamp", System.currentTimeMillis());
            if (currentServer != null) {
                var playerManager = getPlayerManager();
                if (playerManager != null) {
                    int totalPlayers = playerManager.getPlayerList().size();
                    int realPlayers = totalPlayers - BotManager.getAllBots().size();
                    stats.addProperty("real_players_count", Math.max(0, realPlayers));
                    stats.addProperty("total_players_count", totalPlayers);
                    com.google.gson.JsonArray botsArray = new com.google.gson.JsonArray();
                    for (String botName : BotManager.getAllBots()) {
                        botsArray.add(botName);
                    }
                    stats.add("bots_list", botsArray);
                    
                    com.google.gson.JsonArray playersArray = new com.google.gson.JsonArray();
                    for (var player : playerManager.getPlayerList()) {
                        if (!BotManager.getAllBots().contains(player.getName().getString())) {
                            com.google.gson.JsonObject playerObj = new com.google.gson.JsonObject();
                            playerObj.addProperty("name", player.getName().getString());
                            // TODO: Добавить проверку OP статуса
                            playerObj.addProperty("is_op", false);
                            playersArray.add(playerObj);
                        }
                    }
                    stats.add("players_list", playersArray);
                    stats.addProperty("server_core", getServerCore());
                }
            }
            
            // Отправляем POST запрос
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(STATS_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "PVPBOT-Stats/1.0")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(stats.toString()))
                    .build();
            
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            // Успешно отправлено
                        }
                    })
                    .exceptionally(e -> {
                        // Тихо игнорируем ошибки
                        return null;
                    });
            
        } catch (Exception e) {
            // Тихо игнорируем ошибки
        }
    }
    
    /**
     * Получает PlayerManager из сервера
     */
    private static net.minecraft.server.PlayerManager getPlayerManager() {
        try {
            if (currentServer != null) {
                return currentServer.getPlayerManager();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    /**
     * Загружает или создаёт уникальный ID сервера
     */
    private static String loadOrCreateServerId() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
            Path serverIdFile = configDir.resolve("server_id.txt");
            
            if (Files.exists(serverIdFile)) {
                return Files.readString(serverIdFile).trim();
            } else {
                String newId = UUID.randomUUID().toString();
                Files.createDirectories(configDir);
                Files.writeString(serverIdFile, newId);
                return newId;
            }
        } catch (IOException e) {
            // Если не можем сохранить - генерируем временный ID
            return UUID.randomUUID().toString();
        }
    }
    
    /**
     * Получает версию мода
     */
    private static String getModVersion() {
        return FabricLoader.getInstance()
                .getModContainer("pvp_bot")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }
    
    /**
     * Определяет ядро сервера
     */
    private static String getServerCore() {
        try {
            // Проверяем наличие классов разных ядер
            if (classExists("org.spongepowered.api.Sponge")) {
                return "Sponge";
            } else if (classExists("org.bukkit.Bukkit")) {
                return "Bukkit/Spigot/Paper";
            } else if (classExists("net.minecraftforge.common.MinecraftForge")) {
                return "Forge";
            } else if (classExists("net.fabricmc.loader.api.FabricLoader")) {
                return "Fabric";
            }
            return "Vanilla";
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * Проверяет существование класса
     */
    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
