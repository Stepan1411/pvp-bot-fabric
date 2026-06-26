package org.stepan1411.pvp_bot.stats.local;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class LocalStatsStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final AtomicLong totalBotsSpawned = new AtomicLong(0);
    private final AtomicLong totalBotsKilled = new AtomicLong(0);
    private final AtomicLong totalDamageDealt = new AtomicLong(0);
    private final AtomicLong totalDamageReceived = new AtomicLong(0);
    private final AtomicLong totalBotsActive = new AtomicLong(0);
    private final Map<String, Long> topNames = new HashMap<>();
    private int recordServersOnline = 0;
    private int recordBotsActive = 0;

    private final Path dataFile;
    private int serverBotsActive = 0;
    private final List<Snapshot> history = new ArrayList<>();
    private long lastSnapshotTime = 0;

    public static class Snapshot {
        public final long timestamp;
        public final int serversOnline;
        public final int botsActive;

        public Snapshot(long timestamp, int serversOnline, int botsActive) {
            this.timestamp = timestamp;
            this.serversOnline = serversOnline;
            this.botsActive = botsActive;
        }
    }

    public LocalStatsStore(Path dataFile) {
        this.dataFile = dataFile;
    }

    public synchronized void handleSpawn(String botName) {
        totalBotsSpawned.incrementAndGet();
        if (botName != null && !botName.isEmpty()) {
            topNames.merge(botName, 1L, Long::sum);
        }
        serverBotsActive++;
        totalBotsActive.set(serverBotsActive);
    }

    public synchronized void addDamageDealt(long amount) {
        if (amount > 0) totalDamageDealt.addAndGet(amount);
    }

    public synchronized void addDamageReceived(long amount) {
        if (amount > 0) totalDamageReceived.addAndGet(amount);
    }

    public synchronized void handleKill() {
        totalBotsKilled.incrementAndGet();
        if (serverBotsActive > 0) serverBotsActive--;
        totalBotsActive.set(serverBotsActive);
    }

    public synchronized void handleHeartbeat(int botsActive, long damageDealt, long damageReceived) {
        serverBotsActive = botsActive;
        totalBotsActive.set(serverBotsActive);
        addDamageDealt(damageDealt);
        addDamageReceived(damageReceived);
    }

    public synchronized Map<String, Long> getTopNames() {
        return new LinkedHashMap<>(topNames);
    }

    public synchronized void takeSnapshot() {
        long now = System.currentTimeMillis();
        if (now - lastSnapshotTime < 3000) return;
        lastSnapshotTime = now;
        int curOnline = 1; // local server is always online
        int curActive = (int) totalBotsActive.get();
        if (curOnline > recordServersOnline) recordServersOnline = curOnline;
        if (curActive > recordBotsActive) recordBotsActive = curActive;
        history.add(new Snapshot(now, curOnline, curActive));
    }

    public synchronized int getRecordServersOnline() { return recordServersOnline; }
    public synchronized int getRecordBotsActive() { return recordBotsActive; }

    public synchronized Map<String, Object> getCurrent() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("botsSpawned", totalBotsSpawned.get());
        data.put("botsKilled", totalBotsKilled.get());
        data.put("damageDealt", totalDamageDealt.get());
        data.put("damageReceived", totalDamageReceived.get());
        data.put("serversOnline", 1);
        data.put("botsActive", totalBotsActive.get());
        data.put("recordServersOnline", recordServersOnline);
        data.put("recordBotsActive", recordBotsActive);
        data.put("topNames", getTopNames());
        return data;
    }

    public synchronized List<Snapshot> getHistory(long minutes) {
        long cutoff = System.currentTimeMillis() - minutes * 60 * 1000;
        List<Snapshot> result = new ArrayList<>();
        for (Snapshot s : history) {
            if (s.timestamp >= cutoff) result.add(s);
        }
        return result;
    }

    public synchronized List<Snapshot> getHistoryRange(long start, long end) {
        List<Snapshot> result = new ArrayList<>();
        for (Snapshot s : history) {
            if (s.timestamp >= start && s.timestamp <= end) result.add(s);
        }
        return result;
    }

    public synchronized void save() throws IOException {
        PersistedData data = new PersistedData();
        data.totalBotsSpawned = totalBotsSpawned.get();
        data.totalBotsKilled = totalBotsKilled.get();
        data.totalDamageDealt = totalDamageDealt.get();
        data.totalDamageReceived = totalDamageReceived.get();
        data.topNames = new LinkedHashMap<>(topNames);
        data.recordServersOnline = recordServersOnline;
        data.recordBotsActive = recordBotsActive;
        Files.createDirectories(dataFile.getParent());
        try (Writer writer = Files.newBufferedWriter(dataFile)) {
            GSON.toJson(data, writer);
        }
    }

    public synchronized void load() throws IOException {
        if (!Files.exists(dataFile)) return;
        try (Reader reader = Files.newBufferedReader(dataFile)) {
            PersistedData data = GSON.fromJson(reader, PersistedData.class);
            if (data == null) return;
            totalBotsSpawned.set(data.totalBotsSpawned);
            totalBotsKilled.set(data.totalBotsKilled);
            totalDamageDealt.set(data.totalDamageDealt);
            totalDamageReceived.set(data.totalDamageReceived);
            topNames.clear();
            if (data.topNames != null) topNames.putAll(data.topNames);
            recordServersOnline = data.recordServersOnline;
            recordBotsActive = data.recordBotsActive;
        }
    }

    private static class PersistedData {
        long totalBotsSpawned;
        long totalBotsKilled;
        long totalDamageDealt;
        long totalDamageReceived;
        Map<String, Long> topNames = new LinkedHashMap<>();
        int recordServersOnline;
        int recordBotsActive;
    }
}
