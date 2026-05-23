package org.stepan1411.pvp_bot.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import java.nio.file.Files;
import java.nio.file.Path;


public class WorldConfigHelper {
    
    private static MinecraftServer server;
    private static String currentWorldName = null;
    private static Runnable onWorldChangeCallback = null;
    
    
    public static void init(MinecraftServer minecraftServer) {
        server = minecraftServer;
        currentWorldName = getWorldName();
    }
    
    
    public static void setOnWorldChangeCallback(Runnable callback) {
        onWorldChangeCallback = callback;
    }
    
    
    public static Path getWorldConfigDir() {
        checkWorldChange();
        String worldName = getWorldName();
        return getWorldConfigDir(worldName);
    }
    
    
    private static void checkWorldChange() {
        String newWorldName = getWorldName();
        if (currentWorldName != null && !currentWorldName.equals(newWorldName)) {
            System.out.println("[PVP_BOT] World changed from '" + currentWorldName + "' to '" + newWorldName + "'");
            currentWorldName = newWorldName;
            if (onWorldChangeCallback != null) {
                onWorldChangeCallback.run();
            }
        } else if (currentWorldName == null) {
            currentWorldName = newWorldName;
        }
    }
    
    
    public static Path getWorldConfigDir(String worldName) {
        Path dir = FabricLoader.getInstance().getConfigDir()
            .resolve("pvpbot")
            .resolve("worlds")
            .resolve(worldName);
        
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            System.err.println("[PVP_BOT] Failed to create world config directory: " + e.getMessage());
        }
        
        return dir;
    }
    
    
    public static Path getGlobalConfigDir() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
        
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            System.err.println("[PVP_BOT] Failed to create global config directory: " + e.getMessage());
        }
        
        return dir;
    }
    
    
    private static String getWorldName() {
        if (server != null) {
            String name = server.getSaveProperties().getLevelName();
            return name;
        }
        return "world";
    }
}
