package org.stepan1411.pvp_bot.stats.local;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class LocalStatsServer {

    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>(){}.getType();
    private static final int MAX_HISTORY_POINTS = 500;

    private final HttpServer server;
    private final LocalStatsStore store;
    private final SseBroadcaster broadcaster;
    private final Path staticDir;

    public LocalStatsServer(int port, LocalStatsStore store, Path staticDir) throws IOException {
        this.store = store;
        this.staticDir = staticDir;
        this.broadcaster = new SseBroadcaster();

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/ingest", this::handleIngest);
        server.createContext("/api/stats/current", this::handleCurrent);
        server.createContext("/api/stats/history/range", this::handleHistoryRange);
        server.createContext("/api/stats/history", this::handleHistory);
        server.createContext("/api/stats/stream", this::handleStream);
        server.createContext("/", this::serveStatic);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private void serveStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.startsWith("/api/")) return;
        Path file = staticDir.resolve(
            path.equals("/") ? "index.html" : path.substring(1)
        ).normalize();

        if (!file.startsWith(staticDir) || !Files.exists(file) || Files.isDirectory(file)) {
            byte[] msg = "404 Not Found".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, msg.length);
            exchange.getResponseBody().write(msg);
            exchange.getResponseBody().close();
            return;
        }

        String name = file.toString().toLowerCase();
        String mime = "application/octet-stream";
        if (name.endsWith(".html")) mime = "text/html; charset=utf-8";
        else if (name.endsWith(".css")) mime = "text/css; charset=utf-8";
        else if (name.endsWith(".js")) mime = "application/javascript; charset=utf-8";
        else if (name.endsWith(".svg")) mime = "image/svg+xml";
        else if (name.endsWith(".png")) mime = "image/png";
        else if (name.endsWith(".ico")) mime = "image/x-icon";

        byte[] content;
        if (name.endsWith(".html")) {
            String html = Files.readString(file, StandardCharsets.UTF_8);
            String json = GSON.toJson(store.getCurrent());
            html = html.replace("window.__INITIAL_STATS__ = null;", "window.__INITIAL_STATS__ = " + json + ";");
            content = html.getBytes(StandardCharsets.UTF_8);
        } else {
            content = Files.readAllBytes(file);
        }
        exchange.getResponseHeaders().add("Content-Type", mime);
        exchange.getResponseHeaders().add("Cache-Control", "no-cache, must-revalidate");
        sendCorsHeaders(exchange);
        exchange.sendResponseHeaders(200, content.length);
        exchange.getResponseBody().write(content);
        exchange.getResponseBody().close();
    }

    private void handleIngest(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            sendCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> data = GSON.fromJson(body, MAP_TYPE);
            String event = (String) data.get("event");

            if (event == null) {
                sendJson(exchange, 400, "{\"error\":\"Missing event field\"}");
                return;
            }

            switch (event) {
                case "spawn" -> {
                    String botName = (String) data.get("botName");
                    store.handleSpawn(botName);
                }
                case "kill" -> store.handleKill();
                case "heartbeat" -> {
                    int active = ((Number) data.getOrDefault("botsActive", 0)).intValue();
                    long dd = data.containsKey("damageDealt") ? ((Number) data.get("damageDealt")).longValue() : 0;
                    long dr = data.containsKey("damageReceived") ? ((Number) data.get("damageReceived")).longValue() : 0;
                    store.handleHeartbeat(active, dd, dr);
                }
                case "stop" -> {}
                default -> {
                    sendJson(exchange, 400, "{\"error\":\"Unknown event: " + event + "\"}");
                    return;
                }
            }
            store.takeSnapshot();

            Map<String, Object> payload = new LinkedHashMap<>(store.getCurrent());
            broadcaster.broadcast(GSON.toJson(payload));

            sendJson(exchange, 200, "{\"ok\":true}");
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleCurrent(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            sendCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        sendJson(exchange, 200, GSON.toJson(store.getCurrent()));
    }

    private void handleHistory(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            sendCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        Map<String, String> params = queryParams(exchange);
        long minutes = 5;
        if (params.containsKey("minutes")) {
            try { minutes = Long.parseLong(params.get("minutes")); } catch (NumberFormatException ignored) {}
        }
        List<LocalStatsStore.Snapshot> snapshots = store.getHistory(minutes);
        sendJson(exchange, 200, GSON.toJson(downsample(snapshots)));
    }

    private void handleHistoryRange(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            sendCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        Map<String, String> params = queryParams(exchange);
        long start = 0, end = System.currentTimeMillis();
        try {
            if (params.containsKey("start")) start = Long.parseLong(params.get("start"));
            if (params.containsKey("end")) end = Long.parseLong(params.get("end"));
        } catch (NumberFormatException ignored) {}
        List<LocalStatsStore.Snapshot> snapshots = store.getHistoryRange(start, end);
        sendJson(exchange, 200, GSON.toJson(downsample(snapshots)));
    }

    private List<Map<String, Object>> downsample(List<LocalStatsStore.Snapshot> snapshots) {
        if (snapshots.size() <= MAX_HISTORY_POINTS) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (LocalStatsStore.Snapshot snap : snapshots) {
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("timestamp", snap.timestamp);
                point.put("serversOnline", snap.serversOnline);
                point.put("botsActive", snap.botsActive);
                result.add(point);
            }
            return result;
        }
        int bucketSize = snapshots.size() / MAX_HISTORY_POINTS;
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < snapshots.size(); i += bucketSize) {
            int end = Math.min(i + bucketSize, snapshots.size());
            long sumT = 0, sumS = 0, sumB = 0;
            int count = 0;
            for (int j = i; j < end; j++) {
                var s = snapshots.get(j);
                sumT += s.timestamp;
                sumS += s.serversOnline;
                sumB += s.botsActive;
                count++;
            }
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("timestamp", sumT / count);
            point.put("serversOnline", (int) (sumS / count));
            point.put("botsActive", (int) (sumB / count));
            result.add(point);
        }
        return result;
    }

    private void handleStream(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
        exchange.getResponseHeaders().add("Cache-Control", "no-cache");
        exchange.getResponseHeaders().add("Connection", "keep-alive");
        sendCorsHeaders(exchange);
        exchange.sendResponseHeaders(200, 0);

        String initJson = GSON.toJson(store.getCurrent());
        exchange.getResponseBody().write(("data: " + initJson + "\n\n").getBytes());
        exchange.getResponseBody().flush();

        broadcaster.addClient(exchange);
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(5000);
                synchronized (exchange) {
                    exchange.getResponseBody().write(": keepalive\n\n".getBytes());
                    exchange.getResponseBody().flush();
                }
            }
        } catch (InterruptedException ignored) {
        } catch (IOException e) {
            // client disconnected
        } finally {
            broadcaster.removeClient(exchange);
            exchange.close();
        }
    }

    private void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
        sendCorsHeaders(exchange);
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    private void sendCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private Map<String, String> queryParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }
}
