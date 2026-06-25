package org.stepan1411.pvp_bot.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatsCollector {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final StatsCollector INSTANCE = new StatsCollector();

    private final AtomicLong botsKilled = new AtomicLong(0);
    private final AtomicLong botsSpawned = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicInteger> nameUsage = new ConcurrentHashMap<>();

    private static Path getStatsPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("pvpbot").resolve("stats_data.json");
    }

    public static void load() {
        Path path = getStatsPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                StatsData data = GSON.fromJson(reader, StatsData.class);
                if (data != null) {
                    INSTANCE.botsKilled.set(data.botsKilled);
                    INSTANCE.botsSpawned.set(data.botsSpawned);
                    INSTANCE.nameUsage.clear();
                    if (data.topNames != null) {
                        data.topNames.forEach((name, count) ->
                            INSTANCE.nameUsage.put(name, new AtomicInteger(count)));
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public static void save() {
        Path path = getStatsPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                StatsData data = new StatsData();
                data.botsKilled = INSTANCE.botsKilled.get();
                data.botsSpawned = INSTANCE.botsSpawned.get();
                data.topNames = INSTANCE.getTopNames(10);
                GSON.toJson(data, writer);
            }
        } catch (Exception ignored) {}
    }

    public static StatsCollector get() {
        return INSTANCE;
    }

    public void incrementSpawns(String name) {
        botsSpawned.incrementAndGet();
        nameUsage.computeIfAbsent(name, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public void incrementKills() {
        botsKilled.incrementAndGet();
    }

    public long getBotsKilled() { return botsKilled.get(); }
    public long getBotsSpawned() { return botsSpawned.get(); }

    public Map<String, Integer> getTopNames(int limit) {
        List<Map.Entry<String, AtomicInteger>> sorted = new ArrayList<>(nameUsage.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue().get(), a.getValue().get()));
        Map<String, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(limit, sorted.size()); i++) {
            result.put(sorted.get(i).getKey(), sorted.get(i).getValue().get());
        }
        return result;
    }

    private static class StatsData {
        long botsKilled;
        long botsSpawned;
        Map<String, Integer> topNames;
    }
}
