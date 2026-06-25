package org.stepan1411.pvp_bot.stats;

import com.google.gson.Gson;
import net.minecraft.server.MinecraftServer;
import org.stepan1411.pvp_bot.bot.BotManager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatsSender {

    private static final StatsSender INSTANCE = new StatsSender();
    private static final Gson GSON = new Gson();
    private ScheduledExecutorService scheduler;
    private MinecraftServer server;

    public static StatsSender getInstance() {
        return INSTANCE;
    }

    public void start(MinecraftServer server) {
        this.server = server;
        StatsConfig config = StatsConfig.get();
        if (!config.isEnabled()) return;

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "pvpbot-stats-sender");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::sendStats, config.getInterval(), config.getInterval(), TimeUnit.SECONDS);
    }

    public void stop() {
        sendShutdown();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    public void sendNow() {
        sendStats();
    }

    public void sendShutdown() {
        try {
            StatsConfig config = StatsConfig.get();
            if (!config.isEnabled()) return;

            ShutdownPayload payload = new ShutdownPayload();
            payload.serverId = config.getServerId();
            payload.type = "shutdown";

            byte[] data = GSON.toJson(payload).getBytes("UTF-8");

            if ("TCP".equalsIgnoreCase(config.getProtocol())) {
                sendTcp(data, config.getBackendHost(), config.getBackendPort());
            } else {
                sendUdp(data, config.getBackendHost(), config.getBackendPort());
            }
        } catch (Exception ignored) {}
    }

    private void sendStats() {
        try {
            StatsConfig config = StatsConfig.get();
            if (!config.isEnabled()) return;

            StatsPayload payload = new StatsPayload();
            payload.serverId = config.getServerId();
            payload.type = "stats";
            payload.botsKilled = StatsCollector.get().getBotsKilled();
            payload.botsSpawned = StatsCollector.get().getBotsSpawned();
            payload.botsActive = BotManager.getBotCount();
            payload.topNames = StatsCollector.get().getTopNames(10);

            byte[] data = GSON.toJson(payload).getBytes("UTF-8");

            if ("TCP".equalsIgnoreCase(config.getProtocol())) {
                sendTcp(data, config.getBackendHost(), config.getBackendPort());
            } else {
                sendUdp(data, config.getBackendHost(), config.getBackendPort());
            }
        } catch (Exception e) {
            // silently ignore send errors
        }
    }

    private void sendUdp(byte[] data, String host, int port) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(new DatagramPacket(data, data.length, new InetSocketAddress(host, port)));
        } catch (Exception e) {
        }
    }

    private void sendTcp(byte[] data, String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            socket.getOutputStream().write(data);
            socket.getOutputStream().flush();
        } catch (Exception e) {
        }
    }

    private static class StatsPayload {
        String serverId;
        String type;
        long botsKilled;
        long botsSpawned;
        int botsActive;
        Map<String, Integer> topNames;
    }

    private static class ShutdownPayload {
        String serverId;
        String type;
    }
}
