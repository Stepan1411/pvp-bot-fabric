package org.stepan1411.pvp_bot.fixes;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;

import java.util.UUID;

public class ProfileLagFix {

    public static void preloadProfileCache(MinecraftServer server, String name) {
        try {
            var cache = server.getApiServices().nameToIdCache();
            if (cache != null) {
                UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                cache.add(new PlayerConfigEntry(offlineUuid, name));
            }
        } catch (Exception e) {
        }
    }
}
