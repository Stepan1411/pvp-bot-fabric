package org.stepan1411.pvp_bot.bot;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class BotManager {

    private static final Set<String> bots = new HashSet<>();

    public static boolean spawnBot(MinecraftServer server, String name, ServerCommandSource source) {
        // Проверяем, существует ли уже игрок с таким именем на сервере
        ServerPlayerEntity existingPlayer = server.getPlayerManager().getPlayer(name);
        if (existingPlayer != null && existingPlayer.isAlive()) {
            // Бот уже существует и жив
            if (!bots.contains(name)) {
                bots.add(name); // Добавляем в список если не было
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
                return false;
            }
        }
        
        bots.add(name);
        return true;
    }

    public static boolean removeBot(MinecraftServer server, String name, ServerCommandSource source) {
        // Удаляем из списка в любом случае
        boolean wasInList = bots.remove(name);
        
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
    }

    public static int getBotCount() {
        return bots.size();
    }

    public static Set<String> getAllBots() {
        return new HashSet<>(bots);
    }
    
    /**
     * Проверяет жив ли бот, если нет - удаляет из списка
     */
    public static void cleanupDeadBots(MinecraftServer server) {
        for (String name : new HashSet<>(bots)) {
            ServerPlayerEntity bot = server.getPlayerManager().getPlayer(name);
            if (bot == null || !bot.isAlive()) {
                bots.remove(name);
                BotCombat.removeState(name);
                BotUtils.removeState(name);
                BotNavigation.resetIdle(name);
            }
        }
    }
}
