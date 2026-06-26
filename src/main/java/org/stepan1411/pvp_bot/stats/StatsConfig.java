package org.stepan1411.pvp_bot.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class StatsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean enabled = true;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void load() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
        Path file = dir.resolve("stats_config.json");
        try {
            Files.createDirectories(dir);
            if (Files.exists(file)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = GSON.fromJson(Files.readString(file), Map.class);
                enabled = map != null && !"false".equals(String.valueOf(map.getOrDefault("send_anonymous_statistics", "true")));
            } else {
                Map<String, Object> defaults = new HashMap<>();
                defaults.put("send_anonymous_statistics", true);
                Files.writeString(file, GSON.toJson(defaults));
                enabled = true;
            }
        } catch (Exception e) {
            enabled = true;
        }
    }
}
