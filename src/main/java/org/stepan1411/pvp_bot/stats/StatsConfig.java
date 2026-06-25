package org.stepan1411.pvp_bot.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class StatsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static StatsConfig INSTANCE;
    private static Path configPath;

    private boolean enabled = true;
    private String backendHost = "pvpbotstats.survivalworld.win";
    private int backendPort = 32656;
    private String protocol = "UDP";
    private int interval = 30;
    private String serverId = UUID.randomUUID().toString();

    public static StatsConfig load() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve("pvpbot").resolve("stats.json");
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                INSTANCE = GSON.fromJson(reader, StatsConfig.class);
                if (INSTANCE == null) INSTANCE = new StatsConfig();
            } catch (Exception e) {
                INSTANCE = new StatsConfig();
            }
        } else {
            INSTANCE = new StatsConfig();
            save();
        }
        return INSTANCE;
    }

    public static StatsConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void save() {
        if (INSTANCE == null || configPath == null) return;
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isEnabled() { return enabled; }
    public String getBackendHost() { return backendHost; }
    public int getBackendPort() { return backendPort; }
    public String getProtocol() { return protocol; }
    public int getInterval() { return interval; }
    public String getServerId() { return serverId; }
}
