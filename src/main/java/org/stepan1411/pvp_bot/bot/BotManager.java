package org.stepan1411.pvp_bot.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BotManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> bots = new HashSet<>();
    private static final Map<String, BotData> botDataMap = new HashMap<>();
    private static Path savePath;
    private static boolean initialized = false;
    
    // Статистика
    private static int botsSpawnedTotal = 0;
    private static int botsKilledTotal = 0;
    
    /**
     * Данные бота для сохранения
     */
    public static class BotData {
        public String name;
        public double x, y, z;
        public float yaw, pitch;
        public String dimension; // minecraft:overworld, minecraft:the_nether, minecraft:the_end
        public String gamemode; // survival, creative, adventure, spectator
        
        public BotData() {}
        
        public BotData(ServerPlayerEntity bot) {
            this.name = bot.getName().getString();
            this.x = bot.getX();
            this.y = bot.getY();
            this.z = bot.getZ();
            this.yaw = bot.getYaw();
            this.pitch = bot.getPitch();
            this.dimension = bot.getEntityWorld().getRegistryKey().getValue().toString();
            this.gamemode = bot.interactionManager.getGameMode().asString();
        }
    }
    
    /**
     * Класс для сохранения статистики
     */
    public static class StatsData {
        public int botsSpawnedTotal = 0;
        public int botsKilledTotal = 0;
    }

    /**
     * Инициализация - загрузка сохранённых ботов
     */
    public static void init(MinecraftServer server) {
        if (initialized) return;
        
        // Создаём папку config/pvpbot если не существует
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
        try {
            Files.createDirectories(configDir);
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to create config directory: " + e.getMessage());
        }
        
        savePath = configDir.resolve("bots.json");
        loadBots();
        loadStats();
        
        // Респавним сохранённых ботов только если включена настройка botsRelogs
        BotSettings settings = BotSettings.get();
        if (settings.isBotsRelogs() && !botDataMap.isEmpty()) {
            System.out.println("[PVP_BOT] Restoring " + botDataMap.size() + " bots...");
            Map<String, BotData> botsToRestore = new HashMap<>(botDataMap);
            bots.clear();
            botDataMap.clear();
            
            // Запускаем респавн с задержкой
            server.execute(() -> restoreBotsDelayed(server, botsToRestore, 0));
        } else if (!settings.isBotsRelogs()) {
            // Если релоги выключены - очищаем список
            bots.clear();
            botDataMap.clear();
            saveBots();
        }
        
        initialized = true;
    }
    
    private static void restoreBotsDelayed(MinecraftServer server, Map<String, BotData> botsToRestore, int index) {
        if (index >= botsToRestore.size()) {
            System.out.println("[PVP_BOT] Restored " + bots.size() + " bots");
            return;
        }
        
        String[] names = botsToRestore.keySet().toArray(new String[0]);
        if (index < names.length) {
            String name = names[index];
            BotData data = botsToRestore.get(name);
            
            // Спавним бота с позицией и измерением
            var dispatcher = server.getCommandManager().getDispatcher();
            try {
                // Формат: player NAME spawn at X Y Z facing YAW PITCH in DIMENSION in GAMEMODE
                String command = String.format(
                    "player %s spawn at %.2f %.2f %.2f facing %.2f %.2f in %s in %s",
                    name, data.x, data.y, data.z, data.yaw, data.pitch, data.dimension, data.gamemode
                );
                dispatcher.execute(command, server.getCommandSource());
                bots.add(name);
                botDataMap.put(name, data);
            } catch (Exception e) {
                // Пробуем упрощённую команду
                try {
                    String simpleCommand = String.format(
                        "player %s spawn at %.2f %.2f %.2f",
                        name, data.x, data.y, data.z
                    );
                    dispatcher.execute(simpleCommand, server.getCommandSource());
                    bots.add(name);
                    botDataMap.put(name, data);
                } catch (Exception e2) {
                    System.out.println("[PVP_BOT] Failed to restore bot: " + name);
                }
            }
            
            // Следующий бот через 10 тиков
            final int nextIndex = index + 1;
            server.execute(() -> {
                final int[] delay = {0};
                server.execute(new Runnable() {
                    @Override
                    public void run() {
                        delay[0]++;
                        if (delay[0] < 10) {
                            server.execute(this);
                        } else {
                            restoreBotsDelayed(server, botsToRestore, nextIndex);
                        }
                    }
                });
            });
        }
    }
    
    /**
     * Обновление данных всех ботов перед сохранением
     * Сохраняем данные только живых ботов, мёртвые сохраняют последнюю позицию
     */
    public static void updateBotData(MinecraftServer server) {
        for (String name : bots) {
            ServerPlayerEntity bot = server.getPlayerManager().getPlayer(name);
            if (bot != null && bot.isAlive()) {
                // Обновляем данные живого бота
                botDataMap.put(name, new BotData(bot));
            }
            // Мёртвые боты сохраняют последние данные из botDataMap
        }
    }
    
    /**
     * Сохранение списка ботов
     */
    public static void saveBots() {
        if (savePath == null) return;
        
        try (Writer writer = Files.newBufferedWriter(savePath)) {
            GSON.toJson(botDataMap, writer);
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to save bots: " + e.getMessage());
        }
    }
    
    /**
     * Загрузка списка ботов
     */
    private static void loadBots() {
        if (savePath == null || !Files.exists(savePath)) return;
        
        try (Reader reader = Files.newBufferedReader(savePath)) {
            Map<String, BotData> loaded = GSON.fromJson(reader, new TypeToken<Map<String, BotData>>(){}.getType());
            if (loaded != null) {
                botDataMap.putAll(loaded);
                bots.addAll(loaded.keySet());
            }
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to load bots: " + e.getMessage());
        }
    }
    
    /**
     * Сброс при выходе из мира
     */
    public static void reset(MinecraftServer server) {
        updateBotData(server);
        saveBots();
        initialized = false;
    }

    public static boolean spawnBot(MinecraftServer server, String name, ServerCommandSource source) {
        // Проверяем, существует ли уже игрок с таким именем на сервере
        ServerPlayerEntity existingPlayer = server.getPlayerManager().getPlayer(name);
        if (existingPlayer != null && existingPlayer.isAlive()) {
            // Бот уже существует и жив
            if (!bots.contains(name)) {
                bots.add(name); // Добавляем в список если не было
                botDataMap.put(name, new BotData(existingPlayer));
                saveBots();
            }
            return false;
        }

        // Execute Carpet's /player command - spawn in survival mode
        var dispatcher = server.getCommandManager().getDispatcher();
        try {
            // Spawn bot in survival mode using Carpet syntax
            dispatcher.execute("player " + name + " spawn in survival", source);
        } catch (Exception e) {
            // Try alternative method if first fails
            try {
                dispatcher.execute("player " + name + " spawn", source);
                // Force gamemode change after spawn
                dispatcher.execute("gamemode survival " + name, server.getCommandSource());
            } catch (Exception e2) {
                // Даже если команда выбросила исключение, проверим появился ли бот
            }
        }
        
        // Проверяем появился ли бот на сервере (независимо от результата команды)
        // Даём небольшую задержку через execute
        server.execute(() -> {
            ServerPlayerEntity newBot = server.getPlayerManager().getPlayer(name);
            if (newBot != null && !bots.contains(name)) {
                bots.add(name);
                botDataMap.put(name, new BotData(newBot));
                incrementBotsSpawned(); // Увеличиваем счетчик
                saveBots();
                System.out.println("[PVP_BOT] Added bot to list: " + name);
            }
        });
        
        // Добавляем в список сразу (на случай если бот уже появился)
        ServerPlayerEntity newBot = server.getPlayerManager().getPlayer(name);
        if (newBot != null) {
            if (!bots.contains(name)) {
                bots.add(name);
                botDataMap.put(name, new BotData(newBot));
                incrementBotsSpawned(); // Увеличиваем счетчик
                saveBots();
            }
            return true;
        }
        
        // Бот ещё не появился, но добавим имя в список
        // (он появится позже и будет обработан)
        if (!bots.contains(name)) {
            bots.add(name);
            incrementBotsSpawned(); // Увеличиваем счетчик
            saveBots();
        }
        
        return true;
    }

    public static boolean removeBot(MinecraftServer server, String name, ServerCommandSource source) {
        // Удаляем из списка в любом случае
        boolean wasInList = bots.remove(name);
        botDataMap.remove(name); // Удаляем данные бота
        saveBots();
        
        // Очищаем все состояния бота
        BotCombat.removeState(name);
        BotUtils.removeState(name);
        BotNavigation.resetIdle(name);

        String command = "player " + name + " kill";
        var dispatcher = server.getCommandManager().getDispatcher();
        try {
            dispatcher.execute(command, source);
        } catch (Exception e) {
            // Ignore
        }
        
        // Убрали мгновенную отправку - статистика отправится через 30 секунд
        
        return wasInList;
    }

    public static ServerPlayerEntity getBot(MinecraftServer server, String name) {
        return server.getPlayerManager().getPlayer(name);
    }

    public static void removeAllBots(MinecraftServer server, ServerCommandSource source) {
        var dispatcher = server.getCommandManager().getDispatcher();
        for (String name : new HashSet<>(bots)) {
            // Очищаем все состояния бота
            BotCombat.removeState(name);
            BotUtils.removeState(name);
            BotNavigation.resetIdle(name);
            
            String command = "player " + name + " kill";
            try {
                dispatcher.execute(command, source);
            } catch (Exception e) {
                // Ignore
            }
        }
        bots.clear();
        botDataMap.clear(); // Очищаем данные всех ботов
        saveBots();
        
        // Убрали мгновенную отправку - статистика отправится через 30 секунд
    }

    public static int getBotCount() {
        return bots.size();
    }

    public static Set<String> getAllBots() {
        return new HashSet<>(bots);
    }
    
    /**
     * Проверяет жив ли бот - удаляет мёртвых из списка
     */
    public static void cleanupDeadBots(MinecraftServer server) {
        boolean changed = false;
        for (String name : new HashSet<>(bots)) {
            ServerPlayerEntity bot = server.getPlayerManager().getPlayer(name);
            // Бот мёртв если: не существует, не жив, или здоровье <= 0
            boolean isDead = bot == null || !bot.isAlive() || bot.getHealth() <= 0 || bot.isDead();
            if (isDead) {
                // Удаляем мёртвого бота из списка
                bots.remove(name);
                botDataMap.remove(name);
                BotCombat.removeState(name);
                BotUtils.removeState(name);
                BotNavigation.resetIdle(name);
                incrementBotsKilled(); // Увеличиваем счетчик убитых
                changed = true;
                System.out.println("[PVP_BOT] Removed dead bot: " + name);
            }
        }
        if (changed) {
            saveBots();
        }
    }
    
    /**
     * Синхронизирует список ботов с реальными Carpet ботами на сервере
     * Добавляет ботов которые есть на сервере но нет в списке
     */
    public static void syncBots(MinecraftServer server) {
        boolean changed = false;
        // Проверяем всех игроков на сервере
        for (var player : server.getPlayerManager().getPlayerList()) {
            String name = player.getName().getString();
            
            // Пропускаем если уже в списке
            if (bots.contains(name)) continue;
            
            // Carpet боты имеют класс carpet.patches.EntityPlayerMPFake
            String className = player.getClass().getName();
            boolean isFakePlayer = className.contains("EntityPlayerMPFake") || 
                                   className.contains("FakePlayer") ||
                                   className.contains("fake") ||
                                   className.contains("Fake");
            
            if (isFakePlayer) {
                bots.add(name);
                botDataMap.put(name, new BotData(player));
                changed = true;
                System.out.println("[PVP_BOT] Synced Carpet bot: " + name);
            }
        }
        if (changed) {
            saveBots();
        }
    }
    
    /**
     * Увеличивает счетчик заспавненных ботов
     */
    public static void incrementBotsSpawned() {
        botsSpawnedTotal++;
        saveStats();
        // Убрали мгновенную отправку - статистика отправится через 30 секунд
    }
    
    /**
     * Увеличивает счетчик убитых ботов
     */
    public static void incrementBotsKilled() {
        botsKilledTotal++;
        saveStats();
        // Убрали мгновенную отправку - статистика отправится через 30 секунд
    }
    
    /**
     * Возвращает общее количество заспавненных ботов
     */
    public static int getBotsSpawnedTotal() {
        return botsSpawnedTotal;
    }
    
    /**
     * Возвращает общее количество убитых ботов
     */
    public static int getBotsKilledTotal() {
        return botsKilledTotal;
    }
    
    /**
     * Сохранение статистики
     */
    private static void saveStats() {
        if (savePath == null) return;
        
        Path statsPath = savePath.getParent().resolve("stats.json");
        try (Writer writer = Files.newBufferedWriter(statsPath)) {
            StatsData stats = new StatsData();
            stats.botsSpawnedTotal = botsSpawnedTotal;
            stats.botsKilledTotal = botsKilledTotal;
            GSON.toJson(stats, writer);
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to save stats: " + e.getMessage());
        }
    }
    
    /**
     * Загрузка статистики
     */
    private static void loadStats() {
        if (savePath == null) return;
        
        Path statsPath = savePath.getParent().resolve("stats.json");
        if (!Files.exists(statsPath)) return;
        
        try (Reader reader = Files.newBufferedReader(statsPath)) {
            StatsData stats = GSON.fromJson(reader, StatsData.class);
            if (stats != null) {
                botsSpawnedTotal = stats.botsSpawnedTotal;
                botsKilledTotal = stats.botsKilledTotal;
            }
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to load stats: " + e.getMessage());
        }
    }
}
