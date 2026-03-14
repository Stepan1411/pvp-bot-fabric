package org.stepan1411.pvp_bot.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotSettings;
import org.stepan1411.pvp_bot.api.event.BotEventManager;
import org.stepan1411.pvp_bot.api.combat.CombatStrategyRegistry;

import java.util.Set;


public class PvpBotAPI {
    
    private static final String API_VERSION = "2.0.0";
    
    
    public static String getApiVersion() {
        return API_VERSION;
    }
    
    
    public static Set<String> getAllBots() {
        return BotManager.getAllBots();
    }
    
    
    public static ServerPlayerEntity getBot(MinecraftServer server, String name) {
        return BotManager.getBot(server, name);
    }
    
    
    public static boolean isBot(String playerName) {
        return BotManager.getAllBots().contains(playerName);
    }
    
    
    public static int getBotCount() {
        return BotManager.getBotCount();
    }
    
    
    public static BotSettings getBotSettings() {
        return BotSettings.get();
    }
    
    
    public static BotEventManager getEventManager() {
        return BotEventManager.getInstance();
    }
    
    
    public static CombatStrategyRegistry getCombatStrategyRegistry() {
        return CombatStrategyRegistry.getInstance();
    }
    
    
    public static int getTotalBotsSpawned() {
        return BotManager.getBotsSpawnedTotal();
    }
    
    
    public static int getTotalBotsKilled() {
        return BotManager.getBotsKilledTotal();
    }
    
    
    public static boolean isBotAlive(MinecraftServer server, String name) {
        ServerPlayerEntity bot = getBot(server, name);
        return bot != null && bot.isAlive();
    }
    
    
    public static float getBotHealth(MinecraftServer server, String name) {
        ServerPlayerEntity bot = getBot(server, name);
        return bot != null ? bot.getHealth() : -1.0f;
    }
    
    
    public static String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("PVP Bot API v").append(API_VERSION).append("\n");
        sb.append("Active bots: ").append(getBotCount()).append("\n");
        sb.append("Total spawned: ").append(getTotalBotsSpawned()).append("\n");
        sb.append("Total killed: ").append(getTotalBotsKilled()).append("\n");
        sb.append("Event handlers: ").append(getEventManager().getSpawnHandlerCount())
          .append(" spawn, ").append(getEventManager().getDeathHandlerCount())
          .append(" death, ").append(getEventManager().getAttackHandlerCount())
          .append(" attack, ").append(getEventManager().getDamageHandlerCount())
          .append(" damage, ").append(getEventManager().getTickHandlerCount())
          .append(" tick\n");
        sb.append("Combat strategies: ").append(getCombatStrategyRegistry().getStrategyCount()).append("\n");
        return sb.toString();
    }
    
    
    public static boolean isInitialized() {
        try {
            return getBotSettings() != null && getEventManager() != null;
        } catch (Exception e) {
            return false;
        }
    }
}
