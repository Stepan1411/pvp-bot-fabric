package org.stepan1411.pvp_bot.api;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;


public class BotAPIIntegration {
    
    
    public static void fireSpawnEvent(ServerPlayerEntity bot) {
        if (bot == null) return;
        
        try {
            PvpBotAPI.getEventManager().fireSpawnEvent(bot);
        } catch (Exception e) {
            System.err.println("[PVP_BOT_API] Error firing spawn event for " + getBotNameSafe(bot) + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    public static void fireDeathEvent(ServerPlayerEntity bot) {
        if (bot == null) return;
        
        try {
            PvpBotAPI.getEventManager().fireDeathEvent(bot);
        } catch (Exception e) {
            System.err.println("[PVP_BOT_API] Error firing death event for " + getBotNameSafe(bot) + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    public static boolean fireAttackEvent(ServerPlayerEntity bot, Entity target) {
        if (bot == null || target == null) return false;
        
        try {
            return PvpBotAPI.getEventManager().fireAttackEvent(bot, target);
        } catch (Exception e) {
            System.err.println("[PVP_BOT_API] Error firing attack event for " + getBotNameSafe(bot) + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    public static boolean fireDamageEvent(ServerPlayerEntity bot, Entity attacker, float damage) {
        if (bot == null) return false;
        
        try {
            return PvpBotAPI.getEventManager().fireDamageEvent(bot, attacker, damage);
        } catch (Exception e) {
            System.err.println("[PVP_BOT_API] Error firing damage event for " + getBotNameSafe(bot) + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    public static void fireTickEvent(ServerPlayerEntity bot) {
        if (bot == null) return;
        
        try {
            PvpBotAPI.getEventManager().fireTickEvent(bot);
        } catch (Exception e) {

            if (bot.age % 200 == 0) {
                System.err.println("[PVP_BOT_API] Error firing tick event for " + getBotNameSafe(bot) + ": " + e.getMessage());
            }
        }
    }
    
    
    public static boolean isValidBotName(String botName) {
        return botName != null && !botName.isEmpty() && botName.length() <= 16;
    }
    
    
    public static String getBotNameSafe(ServerPlayerEntity bot) {
        if (bot == null) return "Unknown";
        try {
            return bot.getName().getString();
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    
    public static void initialize() {
        System.out.println("[PVP_BOT_API] API Integration initialized");
    }
    
    
    public static void cleanup() {
        try {
            PvpBotAPI.getEventManager().clearAllHandlers();
            System.out.println("[PVP_BOT_API] API Integration cleaned up");
        } catch (Exception e) {
            System.err.println("[PVP_BOT_API] Error during cleanup: " + e.getMessage());
        }
    }
}
