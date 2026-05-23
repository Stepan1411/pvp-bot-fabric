package org.stepan1411.pvp_bot.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.stepan1411.pvp_bot.config.WorldConfigHelper;

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
    private static final Map<String, Integer> nullTickCount = new HashMap<>();
    private static final int MAX_NULL_TICKS = 100;
    private static Path savePath;
    private static boolean initialized = false;
    public static class BotData {
        public String name;
        public double x, y, z;
        public float yaw, pitch;
        public String dimension;
        public String gamemode;
        
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
    
    public static void init(MinecraftServer server) {
        if (initialized) return;
        

        bots.clear();
        botDataMap.clear();
        
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
        try {
            Files.createDirectories(configDir);
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to create config directory: " + e.getMessage());
        }
        
        savePath = org.stepan1411.pvp_bot.config.WorldConfigHelper.getWorldConfigDir().resolve("bots.json");
        loadBots();
        BotSettings settings = BotSettings.get();
        if (settings.isBotsRelogs() && !botDataMap.isEmpty()) {
            System.out.println("[PVP_BOT] Restoring " + botDataMap.size() + " bots...");
            Map<String, BotData> botsToRestore = new HashMap<>(botDataMap);
            bots.clear();
            botDataMap.clear();
            server.execute(() -> restoreBotsDelayed(server, botsToRestore, 0));
        } else if (!settings.isBotsRelogs()) {
            bots.clear();
            botDataMap.clear();
            saveBots();
        }
        
        initialized = true;
    }
    
    private static void restoreBotsDelayed(MinecraftServer server, Map<String, BotData> botsToRestore, int index) {
        restoreBotsDelayedWithRetry(server, botsToRestore, index, 0);
    }
    
    private static void restoreBotsDelayedWithRetry(MinecraftServer server, Map<String, BotData> botsToRestore, int index, int retryCount) {
        final int MAX_RETRIES = 2;
        
        if (index >= botsToRestore.size()) {
            System.out.println("[PVP_BOT] Restored " + bots.size() + " bots");
            return;
        }
        
        String[] names = botsToRestore.keySet().toArray(new String[0]);
        if (index < names.length) {
            String name = names[index];
            BotData data = botsToRestore.get(name);
            ServerPlayerEntity existingPlayer = server.getPlayerManager().getPlayer(name);
            if (existingPlayer != null && !bots.contains(name)) {
                System.out.println("[PVP_BOT] Skipping bot '" + name + "': real player with this name is online");
                final int nextIndex = index + 1;
                server.execute(() -> restoreBotsDelayedWithRetry(server, botsToRestore, nextIndex, 0));
                return;
            }
            var dispatcher = server.getCommandManager().getDispatcher();
            boolean success = false;
            
            try {
                String command = String.format(java.util.Locale.US,
                    "playerspawn %s at %.2f %.2f %.2f facing %.2f %.2f in %s on %s",
                    name, data.x, data.y, data.z, data.yaw, data.pitch,
                    data.gamemode != null ? data.gamemode : "survival",
                    data.dimension
                );
                if (retryCount == 0) {
                    System.out.println("[PVP_BOT] Restoring bot: " + name);
                } else {
                    System.out.println("[PVP_BOT] Retry #" + retryCount + " for bot: " + name);
                }
                dispatcher.execute(command, server.getCommandSource());
                
                bots.add(name);
                botDataMap.put(name, data);
                success = true;
                System.out.println("[PVP_BOT] Successfully restored bot: " + name);
            } catch (Exception e) {

                try {
                    String simpleCommand = String.format(java.util.Locale.US,
                        "playerspawn %s at %.2f %.2f %.2f",
                        name, data.x, data.y, data.z
                    );
                    dispatcher.execute(simpleCommand, server.getCommandSource());
                    bots.add(name);
                    botDataMap.put(name, data);
                    success = true;
                    System.out.println("[PVP_BOT] Restored bot with simple command: " + name);
                } catch (Exception e2) {
                    if (retryCount < MAX_RETRIES) {
                        System.out.println("[PVP_BOT] Failed to restore bot '" + name + "', will retry... (" + (retryCount + 1) + "/" + MAX_RETRIES + ")");
                    } else {
                        System.out.println("[PVP_BOT] Failed to restore bot '" + name + "' after " + (MAX_RETRIES + 1) + " attempts: " + e2.getMessage());
                    }
                }
            }
            

            if (!success && retryCount < MAX_RETRIES) {
                final int currentRetry = retryCount + 1;
                server.execute(() -> {
                    final int[] delay = {0};
                    server.execute(new Runnable() {
                        @Override
                        public void run() {
                            delay[0]++;
                            if (delay[0] < 20) {
                                server.execute(this);
                            } else {
                                restoreBotsDelayedWithRetry(server, botsToRestore, index, currentRetry);
                            }
                        }
                    });
                });
            } else {

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
                                restoreBotsDelayedWithRetry(server, botsToRestore, nextIndex, 0);
                            }
                        }
                    });
                });
            }
        }
    }
    
    
    public static void updateBotData(MinecraftServer server) {
        int updated = 0;
        int skipped = 0;
        int missing = 0;
        for (String name : bots) {
            ServerPlayerEntity bot = server.getPlayerManager().getPlayer(name);
            if (bot != null && bot.isAlive()) {

                botDataMap.put(name, new BotData(bot));
                updated++;
            } else if (!botDataMap.containsKey(name)) {


                missing++;
                System.out.println("[PVP_BOT] WARNING: Bot " + name + " in list but has no data!");
            } else {

                skipped++;
            }
        }

    }
    
    
    public static void saveBots() {
        if (savePath == null) return;
        
        try (Writer writer = Files.newBufferedWriter(savePath)) {
            GSON.toJson(botDataMap, writer);
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to save bots: " + e.getMessage());
        }
    }
    
    
    private static void loadBots() {
        if (savePath == null || !Files.exists(savePath)) {
            return;
        }
        
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
    
    
    public static void reset(MinecraftServer server) {
        updateBotData(server);
        saveBots();
        initialized = false;
    }
    
    public static void switchWorld(MinecraftServer server) {

        updateBotData(server);
        saveBots();

        bots.clear();
        botDataMap.clear();


        savePath = org.stepan1411.pvp_bot.config.WorldConfigHelper.getWorldConfigDir().resolve("bots.json");


        loadBots();


        BotSettings settings = BotSettings.get();
        if (settings.isBotsRelogs() && !botDataMap.isEmpty()) {
            System.out.println("[PVP_BOT] Switching world, restoring " + botDataMap.size() + " bots...");
            Map<String, BotData> botsToRestore = new HashMap<>(botDataMap);
            bots.clear();
            botDataMap.clear();

            server.execute(() -> restoreBotsDelayed(server, botsToRestore, 0));
        }
    }


    public static boolean spawnBot(MinecraftServer server, String name, ServerCommandSource source) {
        boolean isNewBot = !bots.contains(name);

        ServerPlayerEntity existingPlayer = server.getPlayerManager().getPlayer(name);
        if (existingPlayer != null && existingPlayer.isAlive()) {

            if (!bots.contains(name)) {
                bots.add(name);
                botDataMap.put(name, new BotData(existingPlayer));
                saveBots();
                System.out.println("[PVP_BOT] Added existing bot to list: " + name);
            }
            return false;
        }


        var dispatcher = server.getCommandManager().getDispatcher();
        try {

            dispatcher.execute("playerspawn " + name + " at ~ ~ ~ facing 0 0 in survival", source);
        } catch (Exception e) {

            try {
                dispatcher.execute("playerspawn " + name, source);

                dispatcher.execute("gamemode survival " + name, server.getCommandSource());
            } catch (Exception e2) {

            }
        }
        


        server.execute(() -> {
            ServerPlayerEntity newBot = server.getPlayerManager().getPlayer(name);
            if (newBot != null && !bots.contains(name)) {
                bots.add(name);
                botDataMap.put(name, new BotData(newBot));
                saveBots();
                System.out.println("[PVP_BOT] Added bot to list (delayed): " + name);
                

            } else if (newBot != null && bots.contains(name)) {

                botDataMap.put(name, new BotData(newBot));
                saveBots();
                System.out.println("[PVP_BOT] Updated bot data (delayed): " + name);
            }
        });
        

        ServerPlayerEntity newBot = server.getPlayerManager().getPlayer(name);
        if (newBot != null) {
            if (!bots.contains(name)) {
                bots.add(name);
                botDataMap.put(name, new BotData(newBot));
                saveBots();
                System.out.println("[PVP_BOT] Added bot to list (immediate): " + name);

            }
            return true;
        }
        

        if (!bots.contains(name)) {
            bots.add(name);

            BotData defaultData = new BotData();
            defaultData.name = name;
            defaultData.x = source.getPosition().x;
            defaultData.y = source.getPosition().y;
            defaultData.z = source.getPosition().z;
            defaultData.yaw = source.getRotation().y;
            defaultData.pitch = source.getRotation().x;
            defaultData.dimension = source.getWorld().getRegistryKey().getValue().toString();
            defaultData.gamemode = "survival";
            botDataMap.put(name, defaultData);
            saveBots();
            System.out.println("[PVP_BOT] Added bot to list (default data): " + name);
        }
        
        return true;
    }

    public static boolean removeBot(MinecraftServer server, String name, ServerCommandSource source) {

        boolean wasInList = bots.remove(name);
        botDataMap.remove(name);
        saveBots();
        

        BotCombat.removeState(name);
        BotUtils.removeState(name);
        BotNavigation.resetIdle(name);
        

        String command = "player " + name + " kill";
        var dispatcher = server.getCommandManager().getDispatcher();
        try {
            dispatcher.execute(command, source);
        } catch (Exception e) {

        }
        

        
        return wasInList;
    }

    public static ServerPlayerEntity getBot(MinecraftServer server, String name) {
        return server.getPlayerManager().getPlayer(name);
    }

    public static void removeAllBots(MinecraftServer server, ServerCommandSource source) {
        var dispatcher = server.getCommandManager().getDispatcher();
        for (String name : new HashSet<>(bots)) {

            BotCombat.removeState(name);
            BotUtils.removeState(name);
            BotNavigation.resetIdle(name);
            

            String command = "player " + name + " kill";
            try {
                dispatcher.execute(command, source);
            } catch (Exception e) {

            }
        }
        bots.clear();
        botDataMap.clear();
        saveBots();
        

    }

    public static int getBotCount() {
        return bots.size();
    }

    public static Set<String> getAllBots() {
        return new HashSet<>(bots);
    }
    
    public static void reloadBots() {
        bots.clear();
        botDataMap.clear();
        savePath = org.stepan1411.pvp_bot.config.WorldConfigHelper.getWorldConfigDir().resolve("bots.json");
        loadBots();
    }
    
    
    public static void cleanupDeadBots(MinecraftServer server) {
        boolean changed = false;
        for (String name : new HashSet<>(bots)) {
            ServerPlayerEntity bot = server.getPlayerManager().getPlayer(name);

            if (bot == null) {
                int ticks = nullTickCount.getOrDefault(name, 0) + 1;
                nullTickCount.put(name, ticks);
                if (ticks >= MAX_NULL_TICKS) {
                    bots.remove(name);
                    botDataMap.remove(name);
                    BotCombat.removeState(name);
                    BotUtils.removeState(name);
                    BotNavigation.resetIdle(name);
                    nullTickCount.remove(name);
                    changed = true;
                    System.out.println("[PVP_BOT] Removed dead bot (entity gone): " + name);
                }
                continue;
            }

            nullTickCount.remove(name);
            boolean isDead = !bot.isAlive() || bot.getHealth() <= 0 || bot.isDead();
            if (isDead) {
                bots.remove(name);
                botDataMap.remove(name);
                BotCombat.removeState(name);
                BotUtils.removeState(name);
                BotNavigation.resetIdle(name);
                
                // Kick the dead bot from server
                try {
                    var dispatcher = server.getCommandManager().getDispatcher();
                    dispatcher.execute("player " + name + " kill", server.getCommandSource());
                } catch (Exception e) {
                    System.err.println("[PVP_BOT] Error kicking dead bot: " + e.getMessage());
                }
                
                changed = true;
                System.out.println("[PVP_BOT] Removed dead bot: " + name);
            }
        }
        if (changed) {
            saveBots();
        }
    }
}
